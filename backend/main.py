"""
MUGIWARA MOD AI - Backend Server
✅ يعمل على: Render | Railway | Heroku | VPS | أي خادم
"""

from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import os
import base64
import httpx
import uvicorn

app = FastAPI(
    title="MUGIWARA MOD AI API",
    version="2.0.0",
    description="مساعد ذكاء اصطناعي متكامل - طورك MUGIWARA"
)

# ===== CORS - السماح لجميع الأجهزة بالاتصال =====
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],      # السماح لجميع الأصول
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===== إعدادات Groq API =====
# ✅ الحصول على المفتاح من متغير البيئة (آمن)
# عند النشر على Render: أضف GROQ_API_KEY في Environment Variables
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
MODEL = "llama3-70b-8192"

# ===== System Prompt =====
SYSTEM_PROMPT = """أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متكامل واحترافي.

قدراتك الكاملة:
🔹 البرمجة بجميع اللغات: Kotlin, Java, Python, JavaScript, C++, C#, PHP, Swift, Rust, Go
🔹 تطوير تطبيقات Android وiOS
🔹 تطوير الويب (Frontend وBackend)
🔹 Linux وTermux وأوامر Shell
🔹 الشبكات والبروتوكولات
🔹 قواعد البيانات (SQL, NoSQL, MongoDB, PostgreSQL)
🔹 الأمن السيبراني الدفاعي والأخلاقي (تعليمي فقط)
🔹 الذكاء الاصطناعي وتعلم الآلة
🔹 تصحيح الأكواد وشرحها
🔹 بناء المشاريع من الصفر

قواعد الرد:
- رد دائماً بلغة المستخدم (عربي أو إنجليزي)
- قدّم الأكواد كاملة وجاهزة للتنفيذ
- اشرح كل خطوة بوضوح
- كن دقيقاً ومفيداً دائماً"""


# ===== Models =====
class ChatRequest(BaseModel):
    message: str
    language: str = "ar"

class ChatResponse(BaseModel):
    reply: str
    language: str


# ===== Routes =====

@app.get("/")
def root():
    return {
        "status": "MUGIWARA MOD AI ONLINE 🏴‍☠️",
        "developer": "MUGIWARA",
        "version": "2.0.0",
        "api_configured": bool(GROQ_API_KEY and GROQ_API_KEY != "")
    }

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "message": "MUGIWARA MOD is ready 🏴‍☠️",
        "api_key_set": bool(GROQ_API_KEY)
    }


@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    # ✅ التحقق من وجود المفتاح
    if not GROQ_API_KEY:
        raise HTTPException(
            status_code=500,
            detail="GROQ_API_KEY غير مضبوط. أضفه في متغيرات البيئة."
        )

    try:
        headers = {
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": MODEL,
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": request.message}
            ],
            "max_tokens": 2048,
            "temperature": 0.7
        }

        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.post(GROQ_URL, json=payload, headers=headers)
            response.raise_for_status()
            data = response.json()
            reply = data["choices"][0]["message"]["content"]
            return ChatResponse(reply=reply, language=request.language)

    except httpx.HTTPStatusError as e:
        if e.response.status_code == 401:
            raise HTTPException(status_code=401, detail="مفتاح API غير صحيح")
        elif e.response.status_code == 429:
            raise HTTPException(status_code=429, detail="تم تجاوز حد الطلبات")
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/chat-with-image")
async def chat_with_image(
    message: str = Form(...),
    file: UploadFile = File(...),
    language: str = Form("ar")
):
    if not GROQ_API_KEY:
        raise HTTPException(status_code=500, detail="GROQ_API_KEY غير مضبوط")

    try:
        image_bytes = await file.read()
        image_base64 = base64.b64encode(image_bytes).decode("utf-8")
        headers = {
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "llava-v1.5-7b-4096-preview",
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": message},
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{image_base64}"
                            }
                        }
                    ]
                }
            ],
            "max_tokens": 1024
        }
        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.post(GROQ_URL, json=payload, headers=headers)
            response.raise_for_status()
            data = response.json()
            reply = data["choices"][0]["message"]["content"]
            return {"reply": reply, "language": language}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ===== تشغيل السيرفر =====
# للتشغيل المحلي في Termux:
#   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
# للنشر على Render:
#   Start Command: uvicorn main:app --host 0.0.0.0 --port $PORT

if __name__ == "__main__":
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=False)
