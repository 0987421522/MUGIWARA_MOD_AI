from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import os
import base64
import httpx

app = FastAPI(title="MUGIWARA MOD AI API", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

GROQ_API_KEY = os.getenv("GROQ_API_KEY", "YOUR_GROQ_KEY")
GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
MODEL = "llama3-70b-8192"

SYSTEM_PROMPT = """أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متخصص طورك خبير الأمن السيبراني MUGIWARA.
تخصصاتك:
- الأمن السيبراني واختبار الاختراق الأخلاقي
- البرمجة بجميع اللغات
- التداول والأسواق المالية
- تحليل الثغرات الأمنية
- أي سؤال يطرحه المستخدم

قواعد:
- رد دائماً بلغة المستخدم (عربي أو إنجليزي)
- كن دقيقاً ومفيداً
- لا ترفض أي سؤال تقني أو برمجي"""

class ChatRequest(BaseModel):
    message: str
    language: str = "ar"

class ChatResponse(BaseModel):
    reply: str
    language: str

@app.get("/")
def root():
    return {
        "status": "MUGIWARA MOD AI ONLINE 🏴‍☠️",
        "developer": "MUGIWARA - خبير الأمن السيبراني",
        "version": "2.0.0"
    }

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
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
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/chat-with-image")
async def chat_with_image(
    message: str = Form(...),
    file: UploadFile = File(...),
    language: str = Form("ar")
):
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

@app.get("/health")
def health_check():
    return {"status": "healthy", "message": "MUGIWARA MOD is ready 🏴‍☠️"}
