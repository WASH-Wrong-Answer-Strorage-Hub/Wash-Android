package com.wash.washandroid.presentation.fragment.study

import MypageViewModel
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder
import com.wash.washandroid.presentation.fragment.study.data.model.request.AnswerRequest
import com.wash.washandroid.presentation.fragment.study.data.model.response.ProblemStatus
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository
import kotlinx.coroutines.*

class StudyViewModel(
    private val repository: StudyRepository,
    private val sharedPreferences: SharedPreferences,
    private val myPageViewModel: MypageViewModel
) : ViewModel() {
    private val _studyProblem = MutableLiveData<StudyProblemResponse>()
    val studyProblem: LiveData<StudyProblemResponse> get() = _studyProblem
    var currentProblemIndex: Int = 0
        private set

    private val _photoUris = MutableLiveData<List<String>>(emptyList())
    val photoUris: LiveData<List<String>> get() = _photoUris

    private val _selectedPhotoPosition = MutableLiveData<Int>()
    val selectedPhotoPosition: LiveData<Int> get() = _selectedPhotoPosition

    private val _studyFolders = MutableLiveData<List<String>>()
    val studyFolders: LiveData<List<String>> get() = _studyFolders

    private val _problemIds = MutableLiveData<List<String>>()
    val problemIds: LiveData<List<String>> get() = _problemIds

    // name으로 id를 찾기 위한 Map
    private val nameToIdMap = mutableMapOf<String, Int>()
    private val _studyProgress = MutableLiveData<List<Pair<String, String>>>()
    val studyProgress: LiveData<List<Pair<String, String>>> get() = _studyProgress

    private val _currentFolderId = MutableLiveData<String>()
    var isProblemAlreadyLoaded = false

    // all folders 불러오기
    fun loadStudyFolders() {
        viewModelScope.launch{
            Log.d("fraglog", "Fetching access token for study folders")

            val token = myPageViewModel.getRefreshToken() ?:
            "default_token"
            Log.d("fraglog", "load study folders -- received token : $token")

            if (token != null) {
                repository.getStudyFolders(token) { folderList ->
                    folderList?.let {
                        Log.d("fraglog", "Folders fetched successfully: $folderList")

                        val sortedFolders = it.sortedBy { folder -> folder.orderValue }
                        val folderNames = sortedFolders.map { folder ->
                            nameToIdMap[folder.folderName] = folder.folderId
                            folder.folderName
                        }
                        _studyFolders.postValue(folderNames)
                    } ?: Log.e("fraglog", "load study folders -- Failed to load folders")
                }
            } else {
                Log.e("fraglog", "load study folders -- Failed to retrieve refresh token")
            }
        }
    }

    // folder 세부 불러오기
    fun loadStudyFolderById(folderId: String) {
        viewModelScope.launch {
            Log.d("fraglog", "Loading folder with ID: $folderId")
            repository.getStudyFolderId(folderId) { folder ->
                folder?.let {
                    _problemIds.value = it.problemIds
                    saveProblemIdsToPreferences(it.problemIds) // SharedPreferences에 저장
                    setCurrentFolderId(folderId)
                    Log.d("fraglog", "***Problem IDs loaded***: ${it.problemIds}")

                    // problemIds가 null이면 재시도
                    if (_problemIds.value.isNullOrEmpty()) {
                        Log.e("fraglog", "Problem IDs are null, retrying...")
                        _problemIds.value = it.problemIds
                    }
                } ?: Log.e("fraglog", "Failed to load folder for id: $folderId")
            }
        }
    }

    // SharedPreferences에 problemIds 저장
    private fun saveProblemIdsToPreferences(problemIds: List<String>) {
        val editor = sharedPreferences.edit()
        editor.putStringSet("problem_ids", problemIds.toSet())
        editor.apply()

        Log.d("fraglog", "SharedPreferences -- problem IDs saved: $problemIds")
    }

    // name으로 id 찾기
    fun getIdByName(name: String): Int? {
        return nameToIdMap[name]
    }

    // SharedPreferences에서 problemIds 불러오기
    private fun loadProblemIdsFromPreferences(): List<String>? {
        val savedProblemIds = sharedPreferences.getStringSet("problem_ids", emptySet())
        return savedProblemIds?.toList()
    }

    fun loadStudyProblem(folderId: String) {
        viewModelScope.launch {
            val problemIds = loadProblemIdsFromPreferences()
            Log.d("fraglog", "loadstudyproblem -- problem IDs: $problemIds, cur prob index: $currentProblemIndex")

            if (!problemIds.isNullOrEmpty() && currentProblemIndex in problemIds.indices) {
                val problemId = problemIds[currentProblemIndex]
                Log.d("fraglog", "Attempting to load problem with ID: $problemId for folder ID: $folderId")

                repository.getStudyProblem(folderId, problemId) { studyProblem ->
                    studyProblem?.let {
                        _studyProblem.postValue(it)
                        Log.d("fraglog", "Problem loaded successfully for problem ID: $problemId")
                    } ?: Log.e("fraglog", "Failed to load problem for folderId: $folderId, problemId: $problemId")
                }
            } else {
                Log.e("fraglog", "Problem IDs are not initialized or index out of bounds. Current index: $currentProblemIndex")
            }
        }
    }


    // progress 불러오기
    fun loadStudyProgress(folderId: String) {
        viewModelScope.launch {
            repository.getStudyProgress(folderId) { response ->
                response?.let {
                    val sortedProgress = it.result.sortedBy { problemStatus -> problemStatus.problemId.toInt() }
                    val progressList = sortedProgress.map { progress ->
                        Pair(progress.problemId, progress.status)
                    }
                    _studyProgress.postValue(progressList)
                    Log.d("fraglog", "folderId: $folderId  progress : $sortedProgress")
                } ?: Log.e("fraglog", "Failed to load study progress")
            }
        }
    }

    // answer 여부 보내기
    fun submitAnswer(folderId: String, problemId: String, swipeDirection: String) {
        viewModelScope.launch {
            val answerRequest = AnswerRequest(folderId, problemId, swipeDirection)

            repository.sendAnswerRequest(answerRequest) { success ->
                if (success) {
                    Log.d("fraglog", "Answer submitted successfully with folderId: ${answerRequest.folderId}, problemId: ${answerRequest.problemId} ")
                } else {
                    Log.e("fraglog", "Failed to submit answer with folderId: ${answerRequest.folderId}, problemId: ${answerRequest.problemId} ")
                }
            }
        }
    }

    fun setCurrentFolderId(folderId: String) {
        _currentFolderId.value = folderId
    }

    fun getCurrentFolderId(): String? {
        return _currentFolderId.value
    }

    fun loadProblemIdsFromPreferences(sharedPreferences: SharedPreferences): List<String> {
        val savedProblemIds = sharedPreferences.getStringSet("problem_ids", emptySet())
        return savedProblemIds?.toList() ?: emptyList()
    }

    fun setPhotoUris(uris: List<String>) {
        Log.d("fraglog", "viewModel -- Saving photo URIs to SharedPreferences: $uris")

        val editor = sharedPreferences.edit()
        val urisString = uris.joinToString(",") // 쉼표로 구분된 문자열로 저장
        editor.putString("photo_uris", urisString)

        val success = editor.commit()

        if (success) {
            Log.d("fraglog", "Successfully saved photo URIs to SharedPreferences")
        } else {
            Log.e("fraglog", "Failed to save photo URIs to SharedPreferences")
        }

        val savedUrisString = sharedPreferences.getString("photo_uris", "")
        Log.d("fraglog", "Immediately after saving, photo URIs in SharedPreferences: $savedUrisString")
    }

    fun loadPhotoUrisFromPreferences(): List<String> {
        val savedUrisString = sharedPreferences.getString("photo_uris", "")

        Log.d("fraglog", "Loaded photo URIs from SharedPreferences before split: $savedUrisString")

        val savedUris = savedUrisString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        Log.d("fraglog", "Loaded photo URIs from SharedPreferences after split: $savedUris")

        return savedUris
    }

    fun setSelectedPhotoPosition(position: Int) {
        _selectedPhotoPosition.value = position
    }

    // 현재 문제 인덱스 초기화
    fun resetCurrentProblemIndex() {
        currentProblemIndex = 0
    }

    // 현재 문제 가져오기
    fun getCurrentProblem(): StudyProblemResponse {
        return _studyProblem.value ?: throw IllegalStateException("문제를 찾을 수 없습니다.")
    }

    // 이전 문제로 이동
    fun moveToPreviousProblem(folderId: String) {
        if (currentProblemIndex > 0) {
            currentProblemIndex--
            loadStudyProblem(folderId) // 이전 문제 로드
        }
    }

    // 다음 문제로 이동
    fun moveToNextProblem(folderId: String) {
        viewModelScope.launch {
            val problemIds = loadProblemIdsFromPreferences()
            if (problemIds != null && currentProblemIndex < problemIds.size - 1) {
                currentProblemIndex++
                loadStudyProblem(folderId) // 다음 문제 로드
            }
        }
    }

    fun getCorrectProblemCount(): Int {
        return _studyProgress.value?.count { it.second == "맞은 문제" } ?: 0
    }
}

class StudyViewModelFactory(
    private val repository: StudyRepository, private val sharedPreferences: SharedPreferences, private val myPageViewModel: MypageViewModel// 추가
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return StudyViewModel(repository, sharedPreferences, myPageViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
