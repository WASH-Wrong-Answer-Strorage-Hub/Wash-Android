package com.wash.washandroid.presentation.fragment.study.data.model.response

data class StudyProgressResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: List<ProblemStatus>
)

data class ProblemStatus(
    val problemId: String,
    val status: String
)

