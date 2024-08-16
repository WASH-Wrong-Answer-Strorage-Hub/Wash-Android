package com.wash.washandroid.presentation.fragment.study.data.api

import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyAnswerResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProgressResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StudyApiService {

    // 폴더 조회
    @GET("/studies/folders/{folderId}")
    fun getStudyFolder(
        @Path("folderId") folderId: String
    ): Call<StudyFolderResponse>

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
    ): Call<List<StudyProgressResponse>>

    // 정답 기록
    @POST("/studies/folders/{folderId}/problems/{problemId}/check-answer")
    fun addStudyAnswer(
        @Path("folderId") folderId: String,
        @Path("problemId") problemId: String,
        @Query("swipeDirection") swipeDirection: String
    ): Call<StudyAnswerResponse>
}