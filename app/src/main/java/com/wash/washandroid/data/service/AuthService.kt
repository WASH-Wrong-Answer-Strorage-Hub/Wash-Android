package com.wash.washandroid.data.service

import com.wash.washandroid.data.entity.response.DeleteUserResponse
import com.wash.washandroid.data.entity.response.LoginResponse
import com.wash.washandroid.data.entity.response.LogoutResponse
import retrofit2.Response
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

    @PATCH("users/delete")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<DeleteUserResponse>

    @PATCH("users/logout")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>
}