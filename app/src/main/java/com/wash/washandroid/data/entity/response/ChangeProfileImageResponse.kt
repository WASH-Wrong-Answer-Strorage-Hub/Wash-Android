package com.wash.washandroid.data.entity.response

data class ChangeProfileImageResponse(
    val isSuccess: Boolean,
    val message: String,
    val result: Result
) {
    data class Result(
        val id: Int,
        val nickname: String,
    )
}