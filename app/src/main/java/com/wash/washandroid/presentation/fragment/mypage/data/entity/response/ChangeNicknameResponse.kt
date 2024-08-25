package com.wash.washandroid.presentation.fragment.mypage.data.entity.response

data class ChangeNicknameResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: Result
) {
    data class Result(
        val id: Int,
        val nickname: String,
        val provider: String,
        val email: String
    )
}

