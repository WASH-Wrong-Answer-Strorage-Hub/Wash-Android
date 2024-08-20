package com.wash.washandroid.presentation.fragment.study.data.model.response

data class StudyAnswerResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: AnswerResult
)

data class AnswerResult(
    val status: String,
    val correctAnswer: String
)
