package com.mugiwara.mod.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class ChatRequest(val message: String, val language: String = "ar")
data class ChatResponse(val reply: String, val language: String)
data class ImageChatResponse(val reply: String, val language: String)

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
}

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
data class GroqMessage(val role: String, val content: String)
data class GroqResponse(val choices: List<GroqChoice>)
data class GroqChoice(val message: GroqMessage)

object GroqDirectClient {
    var GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE"

    private val httpClient = OkHttpClient.Builder()
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

object RetrofitClient {
    var BASE_URL = "https://your-app.onrender.com/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
