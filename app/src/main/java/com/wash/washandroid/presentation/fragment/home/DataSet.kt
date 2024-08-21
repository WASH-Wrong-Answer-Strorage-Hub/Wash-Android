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