package com.wash.washandroid.presentation.fragment.mypage.data.entity.response

data class SubscriptionResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Result
) {
    data class Result (
        val result: String
    )
}