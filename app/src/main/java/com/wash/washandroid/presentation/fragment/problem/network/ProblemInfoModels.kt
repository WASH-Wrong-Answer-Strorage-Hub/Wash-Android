package com.wash.washandroid.presentation.fragment.problem.network

import android.net.Uri
import com.google.gson.annotations.SerializedName

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

data class PostProblemResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: PostProblemResult?
)

data class PostProblemResult(
    val problemId: Int
)

data class ProblemData(
    val problemImageUri: Uri?,
    val solutionImageUris: List<Uri>,
    val passageImageUris: List<Uri>,
    val additionalImageUris: List<Uri>,
    val problemText: String,
    val memo: String
)