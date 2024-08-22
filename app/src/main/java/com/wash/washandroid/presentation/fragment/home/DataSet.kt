package com.wash.washandroid.presentation.fragment.home

import com.google.gson.annotations.SerializedName

// 폴더 조회
data class ApiResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Folder>
)

data class Folder(
    @SerializedName("folderId") val folderId: Int,
    @SerializedName("folderName") var folderName: String,
    @SerializedName("orderValue") val orderValue: Int

)


//폴더 이름 수정
data class EditFolder(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: String? // 폴더 이름 수정 시 결과 메시지
)

//문제 미리보기
// 문제 응답
data class ProblemsResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: ProblemsResult
)

// 문제 결과 데이터
data class ProblemsResult(
    @SerializedName("folderName") val folderName: String,
    @SerializedName("problems") val problems: List<Problem>
)

// 문제 데이터
data class Problem(
    @SerializedName("problemId") val problemId: Int,
    @SerializedName("problemText") val problemText: String,
    @SerializedName("problemImage") val problemImage: String, // 이미지 URL
    @SerializedName("orderValue") val orderValue: Int
)
