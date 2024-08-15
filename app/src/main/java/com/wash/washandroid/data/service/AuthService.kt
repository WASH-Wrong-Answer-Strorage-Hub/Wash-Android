package com.wash.washandroid.data.service

import com.wash.washandroid.data.entity.request.LoginRequest
import com.wash.washandroid.data.entity.response.LoginResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthService {

    @POST("auth/naver")
    suspend fun postNaverLogin(
        @Header("Authorization") token: String
    ): Response<LoginResponse>

    @POST("auth/kakao")
    suspend fun postKakaoLogin(
        @Header("Authorization") token: String
    ): Response<LoginResponse>
}