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
data class SendResendEmail(
    val from: String,
    val to: List<String>,
    val subject: String,
    val html: String
)

interface ResendApiService {
    @POST("emails")
    suspend fun sendEmail(
        @Header("Authorization") authorizationHeader: String,
        @Body email: SendResendEmail
    ): Response<Unit>

    companion object {
        private const val BASE_URL = "https://api.resend.com/"

        fun create(): ResendApiService {
            val okHttpClient = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(ResendApiService::class.java)
        }
    }
}
