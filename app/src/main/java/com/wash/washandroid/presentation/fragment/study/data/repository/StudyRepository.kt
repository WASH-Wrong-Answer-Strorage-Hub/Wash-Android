package com.wash.washandroid.presentation.fragment.study.data.repository

import android.util.Log
import com.wash.washandroid.presentation.fragment.study.data.api.StudyApiService
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudyRepository(private val studyApiService: StudyApiService) {

    // Get folder
    fun getStudyFolder(folderId: String, callback: (StudyFolder?) -> Unit) {
        studyApiService.getStudyFolder(folderId).enqueue(object : Callback<StudyFolderResponse> {
            override fun onResponse(call: Call<StudyFolderResponse>, response: Response<StudyFolderResponse>) {
                if (response.isSuccessful) {
                    val studyFolderResponse = response.body()
                    studyFolderResponse?.let {
                        val studyFolder = mapToStudyFolder(it)
                        callback(studyFolder)
                    } ?: callback(null)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<StudyFolderResponse>, t: Throwable) {
                callback(null)
                Log.d("fraglog", "get Study Folder onfailure")
            }
        })
    }

    // get problem

    // Get progress

    // post answer

    private fun mapToStudyFolder(response: StudyFolderResponse): StudyFolder {
        return StudyFolder(
            folderName = response.result.folderName,
            problemIds = response.result.problemIds
        )
    }
}
