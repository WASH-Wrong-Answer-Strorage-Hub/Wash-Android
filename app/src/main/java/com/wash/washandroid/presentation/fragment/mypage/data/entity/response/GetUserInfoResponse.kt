package com.wash.washandroid.presentation.fragment.mypage.data.entity.response

data class GetUserInfoResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: UserInfo
) {
    data class UserInfo(
        val id: Int,
        val nickname: String,
        val name: String,
        val provider: String,
        val email: String
    )
}