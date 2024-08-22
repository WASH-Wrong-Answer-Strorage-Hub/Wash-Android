package com.wash.washandroid.presentation.fragment.problem.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ProblemApiService {

    @GET("problems/{problemId}")
    suspend fun getProblemInfo(@Path("problemId") problemId: String): ProblemInfoResponse
}