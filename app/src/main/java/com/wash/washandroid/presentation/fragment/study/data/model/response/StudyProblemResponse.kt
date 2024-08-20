package com.wash.washandroid.presentation.fragment.study.data.model.response

data class StudyProblemResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: ProblemResult
)

data class ProblemResult(
    val problemId: String,
    val folderName: String,
    val answer: String,
    val problemImage: List<String>?, // 문제
    val solutionImage: List<String>?, // 풀이 이미지
    val passageImage: List<String>?, // 지문 이미지
    val additionalProblemImage: List<String>?, // 문제 추가 이미지
    val problemText: String,
    val gptSessionKey: String?, // nullable
    val problemType: ProblemType,
    val statistics: ProblemStatistics
)

data class ProblemType(
    val 대분류: String,
    val 중분류: String,
    val 소분류: String
)

data class ProblemStatistics(
    val wrongCount: Int,
    val solvedCount: Int
)
