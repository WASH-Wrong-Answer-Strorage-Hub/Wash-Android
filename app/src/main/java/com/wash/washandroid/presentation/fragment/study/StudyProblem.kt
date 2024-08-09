package com.wash.washandroid.presentation.fragment.study

data class StudyProblem(
    val id: Int,
    val imageUrl: String, // 문제 이미지 Url
    val descriptionUrl: String, // 지문 이미지 Url
    val answer: String // 정답
)
