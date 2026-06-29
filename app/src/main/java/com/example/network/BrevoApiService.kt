package com.example.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class SendSmtpEmailSender(
    val name: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class SendSmtpEmailTo(
    val email: String,
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class SendSmtpEmail(
    val sender: SendSmtpEmailSender,
    val to: List<SendSmtpEmailTo>,
    val subject: String,
    val htmlContent: String
)

interface BrevoApiService {
    @POST("v3/smtp/email")
    suspend fun sendEmail(
        @Header("api-key") apiKey: String,
        @Body email: SendSmtpEmail
    ): Response<Unit>

    companion object {
        private const val BASE_URL = "https://api.brevo.com/"

        fun create(): BrevoApiService {
            val okHttpClient = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(BrevoApiService::class.java)
        }
    }
}
