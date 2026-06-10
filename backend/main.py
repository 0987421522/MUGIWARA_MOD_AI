from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import anthropic
import os
from typing import Optional

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
    conversation_history: Optional[list] = []

class ChatResponse(BaseModel):
    reply: str
    model: str = ""
    input_tokens: int = 0
    output_tokens: int = 0

SYSTEM_PROMPT = """أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متكامل واحترافي.
تخصصاتك: البرمجة بجميع اللغات، Linux، Termux، الشبكات، Android، الويب، الأمن السيبراني الأخلاقي.
قواعد: رد دائماً بلغة المستخدم، قدم أكواداً كاملة، كن دقيقاً ومفيداً."""

@app.get("/")
def root():
    key = os.environ.get("ANTHROPIC_API_KEY", "")
    return {
        "status": "MUGIWARA MOD AI ONLINE v3",
        "key_set": bool(key),
        "key_prefix": key[:12] + "..." if key else "NOT SET"
    }

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="ANTHROPIC_API_KEY غير موجود")

    try:
        client = anthropic.Anthropic(api_key=api_key)

        messages = []
        if request.conversation_history:
            for msg in request.conversation_history:
                if isinstance(msg, dict) and msg.get("role") in ["user", "assistant"]:
                    messages.append({
                        "role": msg["role"],
                        "content": msg["content"]
                    })

        messages.append({"role": "user", "content": request.message})

        response = client.messages.create(
            model="claude-haiku-4-5-20251001",
            max_tokens=1024,
            system=SYSTEM_PROMPT,
            messages=messages
        )

        return ChatResponse(
            reply=response.content[0].text,
            model=response.model,
            input_tokens=response.usage.input_tokens,
            output_tokens=response.usage.output_tokens
        )

    except anthropic.AuthenticationError:
        raise HTTPException(status_code=401, detail="مفتاح API غير صحيح")
    except anthropic.RateLimitError:
        raise HTTPException(status_code=429, detail="تجاوزت حد الطلبات")
    except anthropic.APIConnectionError:
        raise HTTPException(status_code=503, detail="لا يوجد اتصال بـ Anthropic")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"خطأ: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port)
