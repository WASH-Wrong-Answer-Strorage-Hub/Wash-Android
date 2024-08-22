package com.wash.washandroid.data.service

import com.wash.washandroid.presentation.fragment.graph.GraphApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {

    private const val BASE_URL = "https://dev.team-wash.store/"

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

    // 추가적인 서비스가 필요하다면 이렇게 생성할 수 있습니다.
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
