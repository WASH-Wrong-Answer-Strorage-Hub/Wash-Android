package com.wash.washandroid.presentation.fragment.mypage.data.service

import com.wash.washandroid.presentation.fragment.mypage.data.entity.request.NicknameRequest
import com.wash.washandroid.presentation.fragment.mypage.data.entity.request.PostGeneralLoginRequest
import com.wash.washandroid.presentation.fragment.mypage.data.entity.request.RefreshTokenRequest
import com.wash.washandroid.presentation.fragment.mypage.data.entity.response.ChangeNicknameResponse
import com.wash.washandroid.presentation.fragment.mypage.data.entity.response.ChangeProfileImageResponse
import com.wash.washandroid.presentation.fragment.mypage.data.entity.response.GetUserInfoResponse
import com.wash.washandroid.presentation.fragment.mypage.data.entity.response.PostGeneralLoginResponse
import com.wash.washandroid.presentation.fragment.mypage.data.entity.response.SubscriptionResponse
import com.wash.washandroid.presentation.fragment.mypage.data.entity.response.UserResponse
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

    @POST("auth/login/general")
    suspend fun postGeneralLogin(
        @Body postGeneralLoginRequest: PostGeneralLoginRequest
    ): Response<PostGeneralLoginResponse>

    @POST("auth/naver")
    suspend fun postNaverLogin(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @POST("auth/kakao")
    suspend fun postKakaoLogin(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @POST("auth/refresh")
    suspend fun postRefreshToken(
        @Header("Authorization") token: String,
        @Body refreshToken: RefreshTokenRequest
    ): Response<UserResponse>

    @PATCH("users/delete")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @PATCH("users/logout")
    suspend fun logoutUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>

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
        @Part file: MultipartBody.Part
    ): Response<ChangeProfileImageResponse>

    @PATCH("subscription/approve")
    suspend fun approveSubscription (
        @Header("Authorization") token: String
    ): Response<SubscriptionResponse>

    @PATCH("subscription/cancel")
    suspend fun cancelSubscription (
        @Header("Authorization") token: String
    ): Response<SubscriptionResponse>
}