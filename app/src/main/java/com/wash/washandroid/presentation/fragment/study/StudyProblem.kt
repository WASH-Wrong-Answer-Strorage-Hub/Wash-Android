package com.wash.washandroid.presentation.fragment.study

data class StudyProblem(
    val id: Int,
    val imageUrl: String, // 문제 이미지 Url
    val descriptionUrl: String, // 지문 이미지 Url
    val answer: String, // 정답
    var rightSwipes: Int = 0,  // 오른쪽 스와이프 횟수
    var leftSwipes: Int = 0    // 왼쪽 스와이프 횟수
)
