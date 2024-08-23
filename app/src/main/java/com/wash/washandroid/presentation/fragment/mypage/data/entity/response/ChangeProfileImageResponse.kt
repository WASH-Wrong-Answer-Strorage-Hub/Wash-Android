package com.wash.washandroid.presentation.fragment.mypage.data.entity.response

data class ChangeProfileImageResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: Result
) {
    data class Result(
        val message: String,
        val url: String
    )
}