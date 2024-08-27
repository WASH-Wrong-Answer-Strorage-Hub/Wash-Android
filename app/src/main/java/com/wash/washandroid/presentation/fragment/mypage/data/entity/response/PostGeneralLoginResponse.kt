package com.wash.washandroid.presentation.fragment.mypage.data.entity.response

data class PostGeneralLoginResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
)

data class Result(
    val accesstoken: String,
    val refreshtoken: String
)
