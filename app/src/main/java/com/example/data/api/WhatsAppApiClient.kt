package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface WhatsAppApiService {
    @POST("v20.0/{phone_number_id}/messages")
    suspend fun sendMessage(
        @Path("phone_number_id") phoneNumberId: String,
        @Header("Authorization") token: String,
        @Body request: WhatsAppSendMessageRequest
    ): Response<WhatsAppSendMessageResponse>
}

object WhatsAppApiClient {
    private const val BASE_URL = "https://graph.facebook.com/"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: WhatsAppApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WhatsAppApiService::class.java)
    }
}
