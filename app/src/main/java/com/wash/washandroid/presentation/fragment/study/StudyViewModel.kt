package com.wash.washandroid.presentation.fragment.study

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder
import com.wash.washandroid.presentation.fragment.study.data.model.request.AnswerRequest
import com.wash.washandroid.presentation.fragment.study.data.model.response.ProblemStatus
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository
import kotlinx.coroutines.*

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {
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
    val currentFolderId: LiveData<String> get() = _currentFolderId

    // all folders 불러오기
    fun loadStudyFolders() {
        repository.getStudyFolders { folderList ->
            folderList?.let {
                // folderId를 기준으로 오름차순 정렬
                val sortedFolders = it.sortedBy { folder -> folder.folderId }

                // 폴더 이름 리스트를 생성하고 nameToIdMap을 업데이트
                val folderNames = sortedFolders.map { folder ->
                    nameToIdMap[folder.folderName] = folder.folderId
                    folder.folderName
                }

                _studyFolders.postValue(folderNames)
            } ?: Log.e("fraglog", "Failed to load folders")
        }
    }


    // folder 세부 불러오기
    fun loadStudyFolderById(folderId: String) {
        Log.d("fraglog", "Loading folder with ID: $folderId")
        repository.getStudyFolderId(folderId) { folder ->
            folder?.let {
                Thread.sleep(1000)
                _problemIds.postValue(it.problemIds)
                setCurrentFolderId(folderId)
                Log.d("fraglog", "***Problem IDs loaded***: ${it.problemIds}")
                Log.d("fraglog", "viewmodel problemids: ${_problemIds.value}")
            } ?: Log.e("fraglog", "Failed to load folder for id: $folderId")
        }
    }

    // name으로 id 찾기
    fun getIdByName(name: String): Int? {
        return nameToIdMap[name]
    }

    fun setDummyProblemIds() {
        // 값을 바로 설정할 때는 setValue() 사용
        _problemIds.value = listOf("16", "17", "20", "21")
    }

    // problem 불러오기
    fun loadStudyProblem(folderId: String) {
        Log.d("fraglog", "loadstudyproblem -- problem IDs: ${_problemIds.value}, cur prob index: $currentProblemIndex")
        val problemIds = _problemIds.value
        if (problemIds != null && problemIds.isNotEmpty() && currentProblemIndex in problemIds.indices) {
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

    fun loadStudyProblemWithRetryCoroutine(folderId: String) {
        val retries = 5
        val delayTime = 1100L // 0.1초

        GlobalScope.launch(Dispatchers.Main) {
            var attempt = 0
            var success = false

            while (attempt < retries) {
                val problemIds = _problemIds.value
                Log.d("fraglog", "problemIds:${_problemIds.value}")
                if (!problemIds.isNullOrEmpty() && currentProblemIndex in problemIds.indices) {
                    // 문제 ID가 로드되면 문제 로드 실행
                    loadStudyProblem(folderId)
                    success = true
                    break
                } else {
                    // problemIds가 없으면 재시도
                    Log.e("fraglog", "Problem IDs are null or empty. Retrying... ($attempt/$retries)")
                    attempt++
                    delay(delayTime) // 0.1초 대기
                }
            }

            if (!success) {
                Log.e("fraglog", "Failed to load problem IDs after $retries retries.")
                // 예외 처리 로직 추가 (필요 시 UI 메시지 표시 등)
            }
        }
    }


    // progress 불러오기
    fun loadStudyProgress(folderId: String) {
        repository.getStudyProgress(folderId) { response ->
            response?.let {
                val sortedProgress = it.result.sortedBy { problemStatus -> problemStatus.problemId.toInt() }
                val progressList = sortedProgress.map { progress ->
                    Pair(progress.problemId, progress.status)
                }
                _studyProgress.postValue(progressList)
                Log.d("fraglog", "progress : $sortedProgress")
            } ?: Log.e("fraglog", "Failed to load study progress")
        }
    }

    // answer 여부 보내기
    fun submitAnswer(folderId: String, problemId: String, swipeDirection: String) {
        val answerRequest = AnswerRequest(folderId, problemId, swipeDirection)

        repository.sendAnswerRequest(folderId, problemId, answerRequest) { success ->
            if (success) {
                Log.d("fraglog", "Answer submitted successfully {$answerRequest} ")
            } else {
                Log.e("fraglog", "Failed to submit answer {$answerRequest} ")
            }
        }
    }

    // 폴더 ID를 설정하는 함수
    fun setCurrentFolderId(folderId: String) {
        _currentFolderId.value = folderId
    }

    // 폴더 ID를 가져오는 함수
    fun getCurrentFolderId(): String? {
        return _currentFolderId.value
    }


    fun setPhotoUris(uris: List<String>) {
        _photoUris.value = uris
    }

    fun setSelectedPhotoPosition(position: Int) {
        _selectedPhotoPosition.value = position
    }

    // 현재 문제 인덱스를 초기화
    fun resetCurrentProblemIndex() {
        currentProblemIndex = 0
    }

    fun getTotalProblems(): Int {
        return _problemIds.value?.size ?: 0
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
        val problemIds = _problemIds.value
        if (problemIds != null && currentProblemIndex < problemIds.size - 1) {
            currentProblemIndex++
            loadStudyProblem(folderId) // 다음 문제 로드
        }
    }

    fun incrementRightSwipe() {
//        rightSwipeCount++
    }

    fun incrementLeftSwipe() {
//        leftSwipeCount++
    }

    fun getCorrectProblemCount(): Int {
        return _studyProgress.value?.count { it.second == "맞은 문제" } ?: 0
    }
}

class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
