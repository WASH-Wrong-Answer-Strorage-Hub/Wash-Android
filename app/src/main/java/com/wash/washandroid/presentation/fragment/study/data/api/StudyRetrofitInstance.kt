package com.wash.washandroid.presentation.fragment.study.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StudyRetrofitInstance {
    private const val BASE_URL = "https://dev.team-wash.store/"

    // Access token 설정
    private var accessToken: String? = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MzcsImVtYWlsIjoiZGhyd29kbXNAbmF2ZXIuY29tIiwiaWF0IjoxNzI0MTQyODExLCJleHAiOjE3MjQxNDY0MTF9.Vemqna7K3F8C7nmgypcEh7ml_9JOtA14VlZvf92pbIc"

    // OkHttpClient에 Access token을 추가하는 Interceptor
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                accessToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // Retrofit 인스턴스 생성
    val api: StudyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudyApiService::class.java)
    }
}