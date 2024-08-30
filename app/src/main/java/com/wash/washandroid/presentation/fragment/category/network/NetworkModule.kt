package com.wash.washandroid.presentation.fragment.category.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val BASE_URL = "https://dev.team-wash.store"

    private var retrofit: Retrofit? = null

    private var accessToken: String? = ""

    fun setAccessToken(token: String) {
        accessToken = token
        retrofit = createRetrofit()
        Log.d("NetworkModule", "Access token set: $accessToken")
    }

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                accessToken?.let {
                    Log.d("NetworkModule", "Adding Authorization header: Bearer $it")
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                val request = requestBuilder.build()
                Log.d("NetworkModule", "Request URL: ${request.url}")
                chain.proceed(request)
            }
            .build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getClient(): Retrofit {
        return retrofit ?: createRetrofit().also { retrofit = it }
    }
}