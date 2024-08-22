package com.wash.washandroid.presentation.fragment.study.data.repository

import android.util.Log
import com.wash.washandroid.presentation.fragment.study.data.api.StudyApiService
import com.wash.washandroid.presentation.fragment.study.data.dummyStudyFolderIdResponse
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder
import com.wash.washandroid.presentation.fragment.study.data.model.request.AnswerRequest
import com.wash.washandroid.presentation.fragment.study.data.model.response.FolderInfo
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyAnswerResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderIdResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProgressResponse
import com.wash.washandroid.presentation.fragment.study.data.dummyStudyFolderResponse
import com.wash.washandroid.presentation.fragment.study.data.dummyStudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudyRepository(private val studyApiService: StudyApiService) {

    // Get all folders
    fun getStudyFolders(token: String, callback: (List<FolderInfo>?) -> Unit) {
        studyApiService.getStudyFolders("Bearer $token").enqueue(object : Callback<StudyFolderResponse> {
            override fun onResponse(call: Call<StudyFolderResponse>, response: Response<StudyFolderResponse>) {
                if (response.isSuccessful) {
                    val studyFolderResponse = response.body()
                    studyFolderResponse?.let {
                        callback(it.result)
                    } ?: callback(null)
                } else {
                    callback(dummyStudyFolderResponse.result)
                }
            }

            override fun onFailure(call: Call<StudyFolderResponse>, t: Throwable) {
                callback(dummyStudyFolderResponse.result)
                Log.d("fraglog", "get Study Folders onFailure: ${t.message}")
            }
        })
    }

    // Get specific folder by Id
    fun getStudyFolderId(folderId: String, callback: (StudyFolder?) -> Unit) {
        studyApiService.getStudyFolderId(folderId).enqueue(object : Callback<StudyFolderIdResponse> {
            override fun onResponse(call: Call<StudyFolderIdResponse>, response: Response<StudyFolderIdResponse>) {
                if (response.isSuccessful) {
                    val studyFolderIdResponse = response.body()
                    studyFolderIdResponse?.let {
                        val studyFolder = mapToStudyFolder(it)
                        callback(studyFolder)
                    } ?: callback(null)
                } else {
                    val result = response.body()?.result?.let { mapToStudyFolder(it) }
                    callback(result)
                }
            }

            override fun onFailure(call: Call<StudyFolderIdResponse>, t: Throwable) {
                // 실패 시 더미 데이터 사용
                val dummyResult = dummyStudyFolderIdResponse.result.let { mapToStudyFolder(it) }
                callback(dummyResult)
                Log.d("fraglog", "get Study specific Folder by Id onfailure")
            }
        })
    }

    // Get problem
    fun getStudyProblem(folderId: String, problemId: String, callback: (StudyProblemResponse?) -> Unit) {
        val call = studyApiService.getStudyProblem(folderId, problemId)

        // 요청 URL 로그 출력
        Log.d("fraglog", "Request URL: ${call.request().url}")

        studyApiService.getStudyProblem(folderId, problemId).enqueue(object : Callback<StudyProblemResponse> {
            override fun onResponse(call: Call<StudyProblemResponse>, response: Response<StudyProblemResponse>) {
                if (response.isSuccessful) {
                    val studyProblemResponse = response.body()
                    studyProblemResponse?.let {
                        // null 값이 있을 경우 특정 문자열로 대체
                        val modifiedResponse = it.copy(result = it.result.copy(
                            passageImages = it.result.passageImages?.ifEmpty {
                                listOf("https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg")
                            } ?: listOf("https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg"),
                        ))
                        callback(modifiedResponse)
                    } ?: callback(null)
                } else {
                    callback(dummyStudyProblemResponse)
                }
            }

            override fun onFailure(call: Call<StudyProblemResponse>, t: Throwable) {
                callback(null)
                Log.d("fraglog", "get Study Problem onFailure: ${t.message}")
            }
        })
    }


    // Get progress
    fun getStudyProgress(folderId: String, callback: (StudyProgressResponse?) -> Unit) {
        studyApiService.getStudyProgress(folderId).enqueue(object : Callback<StudyProgressResponse> {
            override fun onResponse(call: Call<StudyProgressResponse>, response: Response<StudyProgressResponse>) {
                if (response.isSuccessful) {
                    val studyProgressResponse = response.body()
                    callback(studyProgressResponse)
                } else {
                    callback(null)  // 응답이 실패한 경우 null 전달
                }
            }

            override fun onFailure(call: Call<StudyProgressResponse>, t: Throwable) {
                callback(null)  // 네트워크 요청이 실패한 경우 null 전달
                Log.e("StudyRepository", "getStudyProgress onFailure: ${t.message}")
            }
        })
    }

    // Post answer
    fun sendAnswerRequest(answerRequest: AnswerRequest, callback: (Boolean) -> Unit) {
        val call = studyApiService.addStudyAnswer(answerRequest)

        call.enqueue(object : Callback<StudyAnswerResponse> {
            override fun onResponse(call: Call<StudyAnswerResponse>, response: Response<StudyAnswerResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { studyAnswerResponse ->
                        if (studyAnswerResponse.isSuccess) {
                            Log.d("fraglog", "Answer submission successful")
                            Log.d("fraglog", "Status: ${studyAnswerResponse.result.status}")
                            callback(true)
                        } else {
                            Log.e("fraglog", "Response body indicates failure: ${studyAnswerResponse.message}")
                            callback(false)
                        }
                    } ?: run {
                        Log.e("fraglog", "Response body is null despite successful status")
                        callback(false)
                    }
                } else {
                    Log.e("fraglog", "Request failed with code: ${response.code()}, Error: ${response.errorBody()?.string() ?: "No error body"}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<StudyAnswerResponse>, t: Throwable) {
                Log.e("fraglog", "Network or unexpected error: ${t.message ?: "Unknown error"}")
                callback(false)
            }
        })

    }


    private fun mapToStudyFolder(response: StudyFolderIdResponse): StudyFolder {
        return StudyFolder(
            folderName = response.result.folderName, problemIds = response.result.problemIds
        )
    }

    // StudyFolderResult를 StudyFolder로 변환
    private fun mapToStudyFolder(result: StudyFolderResult): StudyFolder {
        return StudyFolder(
            folderName = result.folderName, problemIds = result.problemIds
        )
    }
}
