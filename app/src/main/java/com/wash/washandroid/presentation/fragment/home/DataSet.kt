package com.wash.washandroid.presentation.fragment.home

import com.google.gson.annotations.SerializedName
import java.util.Date


data class FolderResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<Folder>?
)

data class Folder(
    @SerializedName("folderId") val folderId: Int,
    @SerializedName("folderName") val folderName: String,
    @SerializedName("orderValue") val orderValue: Int
)