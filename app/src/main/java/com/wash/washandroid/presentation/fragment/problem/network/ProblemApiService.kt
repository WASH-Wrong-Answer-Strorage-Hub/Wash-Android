package com.wash.washandroid.presentation.fragment.problem.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProblemApiService {

    @GET("problems/{problemId}")
    suspend fun getProblemInfo(@Path("problemId") problemId: String): ProblemInfoResponse

    @Multipart
    @POST("problems")
    suspend fun postProblem(
        @Part problemImage: MultipartBody.Part?,
        @Part solutionImages: List<MultipartBody.Part>,
        @Part passageImages: List<MultipartBody.Part>,
        @Part additionalImages: List<MultipartBody.Part>,
        @Part("data") data: RequestBody
    ): Response<PostProblemResponse>
}