package com.wash.washandroid.presentation.fragment.graph

import com.wash.washandroid.presentation.fragment.home.ApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface GraphApiService {

    /*
    @GET("/problems/statistics/mistakes")
    fun getMistakes(
        @Header("Authorization") accessToken: String
    ): Call<MistakeResponse>

    @GET("/problems/statistics/types")
    fun getTypes(
        @Header("Authorization") accessToken: String
    ): Call<TypeResponse>
     */

    @GET("/problems/statistics/mistakes")
    fun getMistakes(
        @Header("Authorization") accessToken: String
    ): Call<ProblemsResponse>

    @GET("/problems/statistics/types")
    fun getTypes(
        @Header("Authorization") accessToken: String
    ): Call<ProblemsResponse>  // 만약 동일한 응답 형식을 사용한다면

}