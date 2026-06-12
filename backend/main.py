from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import os, httpx, random, time
from typing import Optional, List

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    message: str
    language: str = "ar"
    conversation_history: Optional[List[dict]] = []
    provider: Optional[str] = "auto"

class ProviderStatus(BaseModel):
    name: str
    status: str
    key_set: bool
    models: List[str] = []

class ChatResponse(BaseModel):
    reply: str
    model: str = ""
    provider: str = ""
    input_tokens: int = 0
    output_tokens: int = 0
    latency_ms: int = 0

SYSTEM_PROMPT = """أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متكامل واحترافي.
تخصصاتك: البرمجة بجميع اللغات (Python, Kotlin, Java, C++, JavaScript),
Linux وTermux، الشبكات والأمن السيبراني الأخلاقي،
تطوير تطبيقات Android، تطوير ويب Frontend/Backend،
قواعد البيانات SQL/NoSQL، تصحيح الأكواد وشرحها،
الذكاء الاصطناعي وML، التداول والأسواق المالية.
رد دائماً بلغة المستخدم، قدم أكواداً كاملة، كن دقيقاً ومفيداً."""

def detect_best_provider(message: str) -> str:
    msg_lower = message.lower()
    available = []
    if os.environ.get("DEEPSEEK_API_KEY"): available.append("deepseek")
    if os.environ.get("GEMINI_API_KEY"): available.append("gemini")
    if os.environ.get("ANTHROPIC_API_KEY"): available.append("claude")
    if not available: return "none"

    if any(kw in msg_lower for kw in ["صورة","image","photo","رسم","تصميم","شعار","logo","فيديو","video"]):
        if "gemini" in available: return "gemini"
    if any(kw in msg_lower for kw in ["اكتب كود","code","python","java","kotlin","c++","javascript","sql","اختراق","ثغرة","exploit","payload","reverse shell","sql injection","xss","nmap"]):
        if "deepseek" in available: return "deepseek"
    if any(kw in msg_lower for kw in ["حلل","ناقش","قارن","اكتب مقال","ترجمة","translate","لخص","استراتيجية","اقتراح"]):
        if "claude" in available: return "claude"
    if len(message.split()) > 200:
        if "gemini" in available: return "gemini"
    return random.choice(available)

async def call_deepseek(messages: list, system: str) -> ChatResponse:
    start = time.time()
    api_key = os.environ.get("DEEPSEEK_API_KEY")
    combined = str(messages)
    use_reasoner = any(kw in combined.lower() for kw in ["اختراق","ثغرة","exploit","payload","reverse shell","privilege escalation","cve","reason","فكر","حلل"])
    model = "deepseek-reasoner" if use_reasoner else "deepseek-chat"
    async with httpx.AsyncClient(timeout=120) as client:
        resp = await client.post(
            "https://api.deepseek.com/chat/completions",
            headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
            json={"model": model, "messages": [{"role": "system", "content": system}, *messages], "max_tokens": 8192, "temperature": 0.7}
        )
        if resp.status_code != 200:
            raise HTTPException(status_code=resp.status_code, detail=f"DeepSeek: {resp.text}")
        data = resp.json()
        elapsed = int((time.time() - start) * 1000)
        reply = data["choices"][0]["message"]["content"]
        if "reasoning_content" in data["choices"][0]["message"]:
            reasoning = data["choices"][0]["message"]["reasoning_content"]
            reply = f"🧠 **التحليل:**\n{reasoning}\n\n---\n\n{reply}"
        return ChatResponse(reply=reply, model=f"deepseek/{data['model']}", provider="deepseek",
            input_tokens=data["usage"]["prompt_tokens"], output_tokens=data["usage"]["completion_tokens"], latency_ms=elapsed)

async def call_gemini(messages: list, system: str) -> ChatResponse:
    start = time.time()
    api_key = os.environ.get("GEMINI_API_KEY")
    from google import genai
    from google.genai import types
    client = genai.Client(api_key=api_key)
    conversation_text = f"{system}\n\n"
    for msg in messages:
        role = "المستخدم" if msg["role"] == "user" else "المساعد"
        conversation_text += f"{role}: {msg['content']}\n"
    model = "gemini-2.5-pro" if len(str(messages)) > 10000 else "gemini-2.5-flash"
    try:
        response = client.models.generate_content(
            model=model, contents=conversation_text,
            config=types.GenerateContentConfig(max_output_tokens=4096, temperature=0.7)
        )
        elapsed = int((time.time() - start) * 1000)
        return ChatResponse(reply=response.text, model=f"gemini/{model}", provider="gemini",
            input_tokens=response.usage_metadata.prompt_token_count if response.usage_metadata else 0,
            output_tokens=response.usage_metadata.candidates_token_count if response.usage_metadata else 0, latency_ms=elapsed)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Gemini: {str(e)}")

async def call_claude(messages: list, system: str) -> ChatResponse:
    start = time.time()
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    claude_messages = [{"role": msg["role"], "content": msg["content"]} for msg in messages]
    combined = str(messages)
    model = "claude-sonnet-4-20250514" if len(combined) > 5000 else "claude-haiku-3-5-20241022"
    async with httpx.AsyncClient(timeout=120) as client:
        resp = await client.post(
            "https://api.anthropic.com/v1/messages",
            headers={"x-api-key": api_key, "anthropic-version": "2023-06-01", "Content-Type": "application/json"},
            json={"model": model, "max_tokens": 4096, "system": system, "messages": claude_messages}
        )
        if resp.status_code != 200:
            raise HTTPException(status_code=resp.status_code, detail=f"Claude: {resp.text}")
        data = resp.json()
        elapsed = int((time.time() - start) * 1000)
        return ChatResponse(reply=data["content"][0]["text"], model=f"claude/{data['model']}", provider="claude",
            input_tokens=data["usage"]["input_tokens"], output_tokens=data["usage"]["output_tokens"], latency_ms=elapsed)

@app.get("/")
def root():
    providers = []
    providers.append(ProviderStatus(name="deepseek", status="✅ جاهز" if os.environ.get("DEEPSEEK_API_KEY") else "❌ غير مضبوط", key_set=bool(os.environ.get("DEEPSEEK_API_KEY")), models=["deepseek-chat","deepseek-reasoner"]))
    providers.append(ProviderStatus(name="gemini", status="✅ جاهز" if os.environ.get("GEMINI_API_KEY") else "❌ غير مضبوط", key_set=bool(os.environ.get("GEMINI_API_KEY")), models=["gemini-2.5-flash","gemini-2.5-pro"]))
    providers.append(ProviderStatus(name="claude", status="✅ جاهز" if os.environ.get("ANTHROPIC_API_KEY") else "❌ غير مضبوط", key_set=bool(os.environ.get("ANTHROPIC_API_KEY")), models=["claude-haiku-3-5","claude-sonnet-4"]))
    return {"status": "MUGIWARA MOD AI ONLINE v5.2 - Multi-Provider", "smart_routing": True, "providers": providers}

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    try:
        messages = []
        if request.conversation_history:
            for msg in request.conversation_history[-20:]:
                if isinstance(msg, dict) and msg.get("role") in ["user", "assistant"]:
                    messages.append({"role": msg["role"], "content": msg.get("content","")})
        messages.append({"role": "user", "content": request.message})
        provider = request.provider
        if provider == "auto":
            provider = detect_best_provider(request.message)
        last_error = None
        try:
            if provider == "deepseek":
                if not os.environ.get("DEEPSEEK_API_KEY"): raise Exception("DEEPSEEK_API_KEY غير مضبوط")
                return await call_deepseek(messages, SYSTEM_PROMPT)
            elif provider == "gemini":
                if not os.environ.get("GEMINI_API_KEY"): raise Exception("GEMINI_API_KEY غير مضبوط")
                return await call_gemini(messages, SYSTEM_PROMPT)
            elif provider == "claude":
                if not os.environ.get("ANTHROPIC_API_KEY"): raise Exception("ANTHROPIC_API_KEY غير مضبوط")
                return await call_claude(messages, SYSTEM_PROMPT)
            else:
                raise Exception(f"وكالة غير معروفة: {provider}")
        except Exception as e:
            last_error = e
            fallbacks = [p for p in ["deepseek","gemini","claude"] if p != provider and os.environ.get(f"{p.upper()}_API_KEY")]
            for fb in fallbacks:
                try:
                    if fb == "deepseek": return await call_deepseek(messages, SYSTEM_PROMPT)
                    elif fb == "gemini": return await call_gemini(messages, SYSTEM_PROMPT)
                    elif fb == "claude": return await call_claude(messages, SYSTEM_PROMPT)
                except: continue
        raise HTTPException(status_code=500, detail=f"🔴 جميع الوكالات غير متوفرة. آخر خطأ: {str(last_error)}")
    except HTTPException: raise
    except Exception as e: raise HTTPException(status_code=500, detail=f"خطأ: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port)
