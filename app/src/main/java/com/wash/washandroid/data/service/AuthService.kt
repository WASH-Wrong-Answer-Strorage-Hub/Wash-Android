package com.wash.washandroid.data.service

import com.wash.washandroid.data.entity.request.NicknameRequest
import com.wash.washandroid.data.entity.response.ChangeNicknameResponse
import com.wash.washandroid.data.entity.response.ChangeProfileImageResponse
import com.wash.washandroid.data.entity.response.DeleteUserResponse
import com.wash.washandroid.data.entity.response.GetUserInfoResponse
import com.wash.washandroid.data.entity.response.LoginResponse
import com.wash.washandroid.data.entity.response.LogoutResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

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

    @GET("users/info")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<GetUserInfoResponse>

    @PATCH("users/info/nickname")
    suspend fun changeNickname(
        @Header("Authorization") token: String,
        @Body nicknameRequest: NicknameRequest
    ): Response<ChangeNicknameResponse>

    @Multipart
    @PATCH("users/info/profileImage")
    suspend fun changeProfileImage(
        @Header("Authorization") token: String,
        @Part imageFile: MultipartBody.Part
    ): Response<ChangeProfileImageResponse>
}