package com.wash.washandroid.presentation.fragment.study.data.api

import com.wash.washandroid.presentation.fragment.study.data.model.request.AnswerRequest
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyAnswerResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderIdResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProgressResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface StudyApiService {
    // 전체 폴더 조회
    @GET("/folders")
    fun getStudyFolders(
        @Header("Authorization") token: String
    ): Call<StudyFolderResponse>

    // 폴더 조회
    @GET("/studies/folders/{folderId}")
    fun getStudyFolderId(
        @Path("folderId") folderId: String
    ): Call<StudyFolderIdResponse>

    // 문제 조회
    @GET("/studies/folders/{folderId}/problems/{problemId}")
    fun getStudyProblem(
        @Path("folderId") folderId: String,
        @Path("problemId") problemId: String,
    ): Call<StudyProblemResponse>

    // 풀이 진척도 조회
    @GET("/studies/folders/{folderId}/progress")
    fun getStudyProgress(
        @Path("folderId") folderId: String
    ): Call<StudyProgressResponse>

    // 정답 기록
    @POST("/studies/folders/check-answer")
    fun addStudyAnswer(@Body answerRequest: AnswerRequest): Call<StudyAnswerResponse>
}