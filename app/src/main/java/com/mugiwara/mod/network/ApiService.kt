package com.mugiwara.mod.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ===== Data Classes =====

data class ChatRequest(
    val message: String,
    val language: String = "ar"
)

data class ChatResponse(
    val reply: String,
    val language: String
)

data class ImageChatResponse(
    val reply: String,
    val language: String
)

// ===== API Interface =====

interface ApiService {
    @POST("/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

    @Multipart
    @POST("/chat-with-image")
    suspend fun sendMessageWithImage(
        @Part("message") message: okhttp3.RequestBody,
        @Part file: MultipartBody.Part,
        @Part("language") language: okhttp3.RequestBody
    ): Response<ImageChatResponse>

    @GET("/health")
    suspend fun healthCheck(): Response<Map<String, String>>
}

// ===== إعدادات الـ URL =====
/*
    ✅ الحل الكامل لمشكلة "لا يعمل على أجهزة أخرى":

    الخيار 1 (مؤقت - شبكة محلية):
        BASE_URL = "http://192.168.x.x:8000/"
        يعمل فقط على نفس الشبكة WiFi

    الخيار 2 (دائم - الإنترنت):
        انشر الـ backend على Render أو Railway مجاناً
        BASE_URL = "https://mugiwara-mod-ai.onrender.com/"
        يعمل من أي مكان في العالم ✅

    الخيار 3 (مباشر - بدون backend):
        استخدم Groq API مباشرة من التطبيق
        أسرع وأبسط ✅ (انظر DirectApiService أدناه)
*/

// ===== الحل الموصى به: Groq مباشرة بدون backend =====
object GroqDirectClient {
    // ضع مفتاح Groq هنا مباشرة (احصل عليه من console.groq.com مجاناً)
    var GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE // غير لـ BODY عند التشخيص
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }
}

// ===== Groq API Interface =====
interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Body request: GroqRequest): Response<GroqResponse>
}

data class GroqRequest(
    val model: String = "llama3-70b-8192",
    val messages: List<GroqMessage>,
    val max_tokens: Int = 2048,
    val temperature: Double = 0.7
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage
)

// ===== الحل البديل: Backend خارجي =====
object RetrofitClient {
    /*
        ✅ بعد نشر الـ backend على Render/Railway:
        غيّر هذا لـ URL الخاص بك
        مثال: "https://mugiwara-ai.onrender.com/"
    */
    var BASE_URL = "http://10.0.2.2:8000/" // للمحاكي
    // var BASE_URL = "http://192.168.1.100:8000/" // للجهاز الحقيقي على نفس الشبكة
    // var BASE_URL = "https://your-app.onrender.com/" // للإنترنت (الأفضل)

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
