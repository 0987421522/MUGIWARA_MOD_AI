package com.mugiwara.mod.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

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

object RetrofitClient {
    // غيّر هذا الـ IP لـ IP هاتفك على الشبكة
    private const val BASE_URL = "http://YOUR_PHONE_IP:8000/"

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
