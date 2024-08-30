package com.wash.washandroid.presentation.fragment.graph

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GraphApiService {

    @GET("/problems/statistics/mistakes")
    fun getMistakes(
        @Header("Authorization") accessToken: String
    ): Call<ProblemsResponse>

    @GET("/problems/statistics/incorrects/types")
    fun getTypes(
        @Header("Authorization") accessToken: String
    ): Call<TypeResponse>

    @GET("/problems/statistics/ratios/{categoryId}")
    fun getRatios(
        @Header("Authorization") accessToken: String,
        @Path("categoryId") categoryId: Int
    ): Call<PieChartResponse>

}
