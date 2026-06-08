from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import openai
import os
from typing import Optional
import base64

app = FastAPI(title="MUGIWARA MOD AI API", version="1.0.0")

# CORS for Android
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# OpenAI API Key - استبدل بـ KEY الخاص بك أو استخدم متغير بيئة
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "YOUR-OPENAI-KEY-HERE")
openai.api_key = OPENAI_API_KEY

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
        "developer": "MUGIWARA - Monkey D. Luffy",
        "version": "1.0.0"
    }

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    try:
        system_prompt = (
            "أنت MUGIWARA MOD، مساعد ذكاء اصطناعي متخصص في الأمن السيبراني، "
            "البرمجة، اختبار الاختراق الأخلاقي، والتداول. تم تطويرك بواسطة الخبير "
            "في الأمن السيبراني MUGIWARA. رد دائمًا بلغة المستخدم (عربية أو إنجليزية). "
            "قدم إجابات دقيقة ومفيدة حول: الأمن السيبراني، اختبار الاختراق، الثغرات، "
            "البرمجة، التداول، وأي سؤال يطرحه المستخدم."
            if request.language == "ar"
            else "You are MUGIWARA MOD, an AI assistant specialized in cybersecurity, "
                 "programming, ethical hacking, and trading. Developed by cybersecurity "
                 "expert MUGIWARA. Always respond in the user's language. "
                 "Provide accurate answers about: cybersecurity, penetration testing, "
                 "vulnerabilities, programming, trading, and any user questions."
        )
        
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": request.message}
            ],
            max_tokens=1000
        )
        
        reply = response.choices[0].message.content
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
        
        system_prompt = (
            "أنت MUGIWARA MOD. حلل الصورة وأجب على سؤال المستخدم."
            if language == "ar"
            else "You are MUGIWARA MOD. Analyze the image and answer the user's question."
        )
        
        response = openai.ChatCompletion.create(
            model="gpt-4-vision-preview",
            messages=[
                {"role": "system", "content": system_prompt},
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
            max_tokens=1000
        )
        
        reply = response.choices[0].message.content
        return {"reply": reply, "language": language}
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health_check():
    return {"status": "healthy", "message": "MUGIWARA MOD is ready 🏴‍☠️"}
