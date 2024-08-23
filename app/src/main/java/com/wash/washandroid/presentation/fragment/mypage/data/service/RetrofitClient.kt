package com.wash.washandroid.presentation.fragment.mypage.data.service

import com.wash.washandroid.presentation.fragment.graph.GraphApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {

    private const val BASE_URL = "http://172.20.10.4:3000"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: AuthService = retrofit.create(AuthService::class.java)

    // 새로운 GraphApiService 추가
    val graphApiService: GraphApiService = retrofit.create(GraphApiService::class.java)
}
