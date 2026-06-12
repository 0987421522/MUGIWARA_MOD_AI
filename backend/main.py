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

class ChatResponse(BaseModel):
    reply: str
    model: str = ""
    provider: str = ""
    input_tokens: int = 0
    output_tokens: int = 0
    latency_ms: int = 0

SYSTEM_PROMPT = """أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متكامل.
تخصصات: برمجة، Linux/Termux، أمن سيبراني، Android، ويب، قواعد بيانات، AI/ML، تداول."""

def detect_best_provider(message: str) -> str:
    msg_lower = message.lower()
    available = [p for p in ["deepseek","gemini","claude"] if os.environ.get(f"{p.upper()}_API_KEY")]
    if not available: return "none"
    if any(kw in msg_lower for kw in ["صورة","image","photo","فيديو","video"]):
        if "gemini" in available: return "gemini"
    if any(kw in msg_lower for kw in ["اكتب كود","code","python","java","kotlin","اختراق","ثغرة","exploit","payload","sql injection","nmap"]):
        if "deepseek" in available: return "deepseek"
    if any(kw in msg_lower for kw in ["حلل","ناقش","قارن","اكتب مقال","لخص","استراتيجية"]):
        if "claude" in available: return "claude"
    return random.choice(available)

async def call_deepseek(messages: list, system: str) -> ChatResponse:
    start = time.time()
    api_key = os.environ.get("DEEPSEEK_API_KEY")
    async with httpx.AsyncClient(timeout=120) as client:
        resp = await client.post("https://api.deepseek.com/chat/completions",
            headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
            json={"model": "deepseek-chat", "messages": [{"role":"system","content":system}, *messages], "max_tokens": 8192})
        data = resp.json()
        return ChatResponse(reply=data["choices"][0]["message"]["content"], model=f"deepseek/{data['model']}", provider="deepseek",
            input_tokens=data["usage"]["prompt_tokens"], output_tokens=data["usage"]["completion_tokens"],
            latency_ms=int((time.time()-start)*1000))

async def call_gemini(messages: list, system: str) -> ChatResponse:
    start = time.time()
    from google import genai
    from google.genai import types
    client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))
    text = system + "\n\n" + "\n".join(f"{'المستخدم' if m['role']=='user' else 'المساعد'}: {m['content']}" for m in messages)
    response = client.models.generate_content(model="gemini-2.5-flash", contents=text,
        config=types.GenerateContentConfig(max_output_tokens=4096))
    return ChatResponse(reply=response.text, model="gemini/gemini-2.5-flash", provider="gemini",
        latency_ms=int((time.time()-start)*1000))

async def call_claude(messages: list, system: str) -> ChatResponse:
    start = time.time()
    cm = [{"role": m["role"], "content": m["content"]} for m in messages]
    async with httpx.AsyncClient(timeout=120) as client:
        resp = await client.post("https://api.anthropic.com/v1/messages",
            headers={"x-api-key": os.environ.get("ANTHROPIC_API_KEY"), "anthropic-version": "2023-06-01", "Content-Type": "application/json"},
            json={"model": "claude-haiku-3-5-20241022", "max_tokens": 4096, "system": system, "messages": cm})
        data = resp.json()
        return ChatResponse(reply=data["content"][0]["text"], model=f"claude/{data['model']}", provider="claude",
            input_tokens=data["usage"]["input_tokens"], output_tokens=data["usage"]["output_tokens"],
            latency_ms=int((time.time()-start)*1000))

@app.get("/")
def root():
    p = []
    for name in ["deepseek","gemini","claude"]:
        key = os.environ.get(f"{name.upper()}_API_KEY")
        p.append({"name": name, "status": "✅" if key else "❌", "key_set": bool(key)})
    return {"status": "MUGIWARA MOD AI v5.3", "providers": p}

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    try:
        messages = []
        if request.conversation_history:
            for msg in request.conversation_history[-20:]:
                if isinstance(msg, dict) and msg.get("role") in ["user","assistant"]:
                    messages.append({"role": msg["role"], "content": msg.get("content","")})
        messages.append({"role": "user", "content": request.message})
        provider = request.provider if request.provider != "auto" else detect_best_provider(request.message)
        for p, f in [("deepseek",call_deepseek), ("gemini",call_gemini), ("claude",call_claude)]:
            if provider == p:
                return await f(messages, SYSTEM_PROMPT)
        for p, f in [("deepseek",call_deepseek), ("gemini",call_gemini), ("claude",call_claude)]:
            if os.environ.get(f"{p.upper()}_API_KEY"):
                return await f(messages, SYSTEM_PROMPT)
        raise HTTPException(500, "لا توجد أي وكالة مفعلة!")
    except HTTPException: raise
    except Exception as e: raise HTTPException(500, str(e))

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port)
