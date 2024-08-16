package com.wash.washandroid.presentation.fragment.study.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StudyRetrofitInstance {
    private const val BASE_URL = "https://dev.team-wash.store/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: StudyApiService by lazy {
        retrofit.create(StudyApiService::class.java)
    }
}