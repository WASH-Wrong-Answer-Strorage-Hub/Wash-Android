package com.wash.washandroid.presentation.fragment.problem.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProblemApiService {

    // 문제 상세 정보 조회
    @GET("problems/{problemId}")
    suspend fun getProblemInfo(@Path("problemId") problemId: String): ProblemInfoResponse

    // 이미지 url 변환
    @Multipart
    @POST("problems/image")
    suspend fun postImageUrl(@Part file: MultipartBody.Part?): Response<PostImageUrlResponse>

    // 문제 추가
    @POST("problems")
    suspend fun postProblem(@Body request: PostProblemRequest): Response<PostProblemResponse>

    // 문제 수정
    @PATCH("problems/edit")
    suspend fun editProblem(@Body request: EditProblemRequest): Response<EditProblemResponse>
}