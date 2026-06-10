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
    val language: String = "ar",
    val conversation_history: List<Map<String, String>> = emptyList()
)

data class ChatResponse(
    val reply: String,
    val model: String = "",
    val input_tokens: Int = 0,
    val output_tokens: Int = 0
)

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

    @GET("/health")
    suspend fun healthCheck(): Response<Map<String, String>>
}

object RetrofitClient {
    var BASE_URL = "https://web-production-ca8c2.up.railway.app/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
