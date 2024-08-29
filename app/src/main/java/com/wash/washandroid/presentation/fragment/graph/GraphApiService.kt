package com.wash.washandroid.presentation.fragment.graph

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface GraphApiService {

    @GET("/problems/statistics/mistakes")
    fun getMistakes(
        @Header("Authorization") accessToken: String
    ): Call<ProblemsResponse>


    @GET("/problems/statistics/types")
    fun getTypes(
        @Header("Authorization") accessToken: String
    ): Call<TypeResponse> // 수정된 TypeResponse 사용
    @GET("/problems/statistics/ratios")
    fun getRatios(
        @Header("Authorization") accessToken: String
    ): Call<PieChartResponse>
}