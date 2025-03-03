package com.wash.washandroid.model

import com.google.gson.annotations.SerializedName

data class FolderResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: List<Folder>
)

data class Folder(
    @SerializedName("folderId")
    val folderId: Int,

    @SerializedName("folderName")
    val folderName: String,

    @SerializedName("orderValue")
    val orderValue: Int
)

data class PostFolderResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: PostFolder
)

data class PostFolder(
    @SerializedName("message")
    val message: String,

    @SerializedName("folderId")
    val folderId: Int
)

data class PostFolderRequest(
    var folderName: String
)