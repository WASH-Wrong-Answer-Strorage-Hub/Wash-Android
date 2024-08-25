package com.wash.washandroid.presentation.fragment.study.data.model.response

data class StudyFolderResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: List<FolderInfo>
)

data class FolderInfo(
    val folderId: Int,
    val folderName: String,
    val orderValue: Int
)

