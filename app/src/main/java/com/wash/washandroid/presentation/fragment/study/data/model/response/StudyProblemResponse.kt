package com.wash.washandroid.presentation.fragment.study.data.model.response

data class StudyProblemResponse(
    val problemId: String,
    val problemText: String,
    val folderName: String,
    val answerText: String,
    val status: String,
    val correctCount: Int,
    val incorrectCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val message: String
)
