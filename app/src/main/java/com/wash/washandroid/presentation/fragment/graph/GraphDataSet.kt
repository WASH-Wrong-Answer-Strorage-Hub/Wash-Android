package com.wash.washandroid.presentation.fragment.graph

import com.google.gson.annotations.SerializedName

data class ProblemsResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<Result> // result를 리스트로 변경
)
data class Result(
    @SerializedName("problem_id")    val problemId: Long,
    @SerializedName("folder_id")    val folderId: Long,
    @SerializedName("user_id")    val userId: Long,
    @SerializedName("problem_text")    val problemText: String?,
    @SerializedName("answer")    val answer: String,
    @SerializedName("status")    val status: String,
    @SerializedName("correct_count")    val correctCount: Int,
    @SerializedName("incorrect_count")    val incorrectCount: Int,
    @SerializedName("order_value")    val orderValue: Int,
    @SerializedName("created_at")    val createdAt: String,  // ISO 8601 형식의 날짜/시간 문자열
    @SerializedName("updated_at")    val updatedAt: String,
    @SerializedName("subscription_plan")    val subscriptionPlan: String?,
    @SerializedName("memo")    val memo: String,
    @SerializedName("gpt_session_key")    val gptSessionKey: String?,
    @SerializedName("progress")    val progress: String,
    @SerializedName("main_category")    val mainCategory: String?,
    @SerializedName("sub_category")    val subCategory: String?
)
data class TypeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<TypeResult> // result를 리스트로 변경
)

data class TypeResult(
    val sub_category: String,
    val total_incorrect: String // 총 틀린 문제 수를 문자열로 받음
)

// 파이차트
data class PieChartResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Portion>
)

data class Portion(
    @SerializedName("sub_category") val sub_category: String,
    @SerializedName("incorrect_percentage") val incorrect_percentage: String
)
