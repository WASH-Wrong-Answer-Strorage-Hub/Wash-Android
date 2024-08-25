package com.wash.washandroid.presentation.fragment.graph

data class ProblemsResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<Result> // result를 리스트로 변경
)

data class Result(
    val problemId: Long,
    val folderId: Long,
    val userId: Long,
    val problemText: String,
    val answer: String,
    val status: String,
    val correctCount: Int,
    val incorrectCount: Int,
    val orderValue: Int,
    val createdAt: String,  // ISO 8601 형식의 날짜/시간 문자열로 변경
    val updatedAt: String,
    val subscriptionPlan: String?,
    val memo: String,
    val gptSessionKey: String?,
    val progress: String,
    val mainCategory: String?,
    val subCategory: String?
)
