package com.wash.washandroid.presentation.fragment.problem.network

import com.google.gson.annotations.SerializedName

// 문제 상세정보 조회 api 응답
data class ProblemInfoResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: ProblemResult
)

data class ProblemResult(
    val problemId: String,
    val answer: String,
    val problemImage: String,
    val solutionImages: List<String>,
    val passageImages: List<String>,
    val additionalProblemImages: List<String>,
    val memo: String,
    val problemText: String,
    val gptSessionKey: String?,
    val problemType: ProblemType,
    val statistics: ProblemStatistics
)

data class ProblemType(
    @SerializedName("대분류")
    val subject: String,

    @SerializedName("중분류")
    val subfield: String,

    @SerializedName("소분류")
    val chapter: String
)

data class ProblemStatistics(
    val wrongCount: Int,
    val solvedCount: Int
)

// 문제 추가 api 요청
data class PostProblemRequest(
    var folderId: Int,
    var problemText: String,
    var answer: String,
    var status: String = "작성",
    var memo: String,
    var mainTypeId: Int,
    var midTypeId: Int,
    var subTypeIds: List<Int?>?,
    var problemImage: List<String?>,
    var solutionImages: List<String?>,
    var passageImages: List<String?>,
    var additionalImages: List<String?>
)


// 문제 추가 api 응답
data class PostProblemResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: PostProblemResult?
)

data class PostProblemResult(
    val problemId: Int
)

// 문제 편집 api 요청
data class EditProblemRequest(
    var problemId: Int,
    var problemText: String,
    var answer: String,
    var status: String = "active",
    var memo: String,
    var problemImage: List<String?>,
    var solutionImages: List<String?>,
    var passageImages: List<String?>,
    var additionalImages: List<String?>
)

// 문제 편집 api 응답
data class EditProblemResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: EditProblemResult?
)

data class EditProblemResult(
    val message: String
)

// 이미지 url 변환 api 응답
data class PostImageUrlResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: String
)

// 문제 추가시 저장용
data class ProblemData(
    val problemImageUri: String?,
    val solutionImageUris: List<String?>,
    val passageImageUris: List<String?>,
    val additionalImageUris: List<String?>,
    val problemText: String,
    val answer: String,
    val memo: String,
    val mainTypeId: Int?,
    val midTypeId: Int?,
    val subTypeIds: List<Int?>?
)

// 문제 편집시 저장용
data class ProblemInfoData(
    val problemImageUri: String?,
    val solutionImageUris: List<String?>,
    val passageImageUris: List<String?>,
    val additionalImageUris: List<String?>,
    val problemText: String,
    val answer: String,
    val memo: String
)