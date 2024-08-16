package com.wash.washandroid.presentation.fragment.study

data class StudyProblem(
    val problemId: Int,
    val problemText: String,
    val folderName: String,
    val answerText: String, // 정답
    val status: String,
    val correctCount: Int, // 오른쪽 스와이프 횟수
    val incorrectCount: Int, // 왼쪽 스와이프 횟수
    val descriptionUrl: List<String>, // 지문 이미지 Url
)
