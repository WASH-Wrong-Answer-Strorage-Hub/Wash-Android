package com.wash.washandroid.presentation.fragment.mypage.data.entity.response

data class UserResponse (
    val isSuccess: Boolean,  // 요청 성공 여부
    val code: Int,           // 응답 코드 (예: 200)
    val message: String,      // 응답 메시지 (예: "로그아웃 성공")
    val usersResult: UsersResult? = null
)