"""
MUGIWARA MOD AI - Backend Server
Railway Deployment Ready ✅
"""

from fastapi import FastAPI, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import anthropic
import os
from typing import Optional
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="MUGIWARA MOD AI Backend")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# المفتاح يأتي من متغيرات Railway البيئية
ANTHROPIC_API_KEY = os.environ.get("ANTHROPIC_API_KEY")
APP_SECRET = os.environ.get("APP_SECRET", "mugiwara_secret_2025")

if not ANTHROPIC_API_KEY:
    raise RuntimeError("ANTHROPIC_API_KEY غير موجود في متغيرات البيئة")

client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)


class ChatRequest(BaseModel):
    message: str
    system_prompt: Optional[str] = None
    conversation_history: Optional[list] = []
    max_tokens: Optional[int] = 1024


class ChatResponse(BaseModel):
    reply: str
    model: str
    input_tokens: int
    output_tokens: int


DEFAULT_SYSTEM_PROMPT = """أنت مساعد ذكاء اصطناعي متخصص اسمه MUGIWARA AI.

تخصصاتك:
🔧 البرمجة: Python, Kotlin, Java, JavaScript, Bash وجميع اللغات
🐛 تصحيح الأخطاء: تحليل وحل مشاكل الكود
🔒 الأمن السيبراني: تعليم المفاهيم والممارسات الأخلاقية
🐧 Linux & Termux: شرح الأوامر والتوزيعات
📱 تطوير Android: Kotlin, Java, Android Studio
💡 الشرح التقني: بأسلوب واضح وبسيط

قواعد:
- أجب دائماً بالعربية إلا إذا طُلب غير ذلك
- كن دقيقاً ومباشراً
- للأمن السيبراني: التعليم الأخلاقي فقط"""


@app.get("/")
def root():
    return {"status": "✅ MUGIWARA AI يعمل", "version": "2.0"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/chat", response_model=ChatResponse)
async def chat(
    request: ChatRequest,
    x_app_secret: Optional[str] = Header(None)
):
    if x_app_secret and x_app_secret != APP_SECRET:
        raise HTTPException(status_code=401, detail="مفتاح التطبيق غير صحيح")

    messages = []
    if request.conversation_history:
        for msg in request.conversation_history:
            if msg.get("role") in ["user", "assistant"]:
                messages.append({"role": msg["role"], "content": msg["content"]})

    messages.append({"role": "user", "content": request.message})

    try:
        response = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=request.max_tokens,
            system=request.system_prompt or DEFAULT_SYSTEM_PROMPT,
            messages=messages
        )
        return ChatResponse(
            reply=response.content[0].text,
            model=response.model,
            input_tokens=response.usage.input_tokens,
            output_tokens=response.usage.output_tokens
        )
    except anthropic.APIConnectionError:
        raise HTTPException(status_code=503, detail="لا يوجد اتصال بـ Anthropic")
    except anthropic.RateLimitError:
        raise HTTPException(status_code=429, detail="تجاوزت حد الطلبات")
    except Exception as e:
        logger.error(f"خطأ: {e}")
        raise HTTPException(status_code=500, detail="خطأ في السيرفر")


if __name__ == "__main__":
    import uvicorn
    # Railway يضع PORT تلقائياً
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port)
