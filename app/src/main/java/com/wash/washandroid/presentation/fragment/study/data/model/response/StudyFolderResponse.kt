package com.wash.washandroid.presentation.fragment.study.data.model.response

data class StudyFolderResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: StudyFolderResult
)

data class StudyFolderResult(
    val message: String,
    val folderName: String,
    val problemIds: List<String>
)

