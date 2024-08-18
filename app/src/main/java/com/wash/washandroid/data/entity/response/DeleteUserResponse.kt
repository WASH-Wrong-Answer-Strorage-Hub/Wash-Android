package com.wash.washandroid.data.entity.response

data class DeleteUserResponse(
    val isSuccess: Boolean,  // 요청 성공 여부
    val code : Int,           // 응답 코드 (예: 200)
    val message: String,      // 응답 메시지 (예: "회원탈퇴 성공")
    val usersResult: UsersResult? = null
)