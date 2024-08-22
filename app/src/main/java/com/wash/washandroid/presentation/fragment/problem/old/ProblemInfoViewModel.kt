package com.wash.washandroid.presentation.fragment.problem.old

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import com.wash.washandroid.presentation.fragment.problem.network.ProblemResult
import com.wash.washandroid.presentation.fragment.problem.network.ProblemType
import kotlinx.coroutines.launch

class ProblemInfoViewModel : ViewModel() {
    private val _problemPhotoUri = MutableLiveData<Uri?>()
    val problemPhotoUri: LiveData<Uri?> get() = _problemPhotoUri

    fun setProblemPhotoUri(uri: Uri?) {
        _problemPhotoUri.value = uri
    }

    private val _photoUris = MutableLiveData<List<String>>(emptyList())
    val photoUris: LiveData<List<String>> get() = _photoUris

    private val _selectedPhotoPosition = MutableLiveData<Int>()
    val selectedPhotoPosition: LiveData<Int> get() = _selectedPhotoPosition

    fun setPhotoUris(uris: List<String>) {
        _photoUris.value = uris
    }

    fun setSelectedPhotoPosition(position: Int) {
        _selectedPhotoPosition.value = position
    }

    private val _firstPhotoUri = MutableLiveData<String?>()
    val firstPhotoUri: LiveData<String?> get() = _firstPhotoUri

    private val _remainingPhotoUris = MutableLiveData<List<String>>()
    val remainingPhotoUris: LiveData<List<String>> get() = _remainingPhotoUris

    fun setFirstPhoto(uri: String?) {
        _firstPhotoUri.value = uri
    }

    fun setRemainingPhotos(uris: List<String>) {
        _remainingPhotoUris.value = uris
    }

    private val retrofit = NetworkModule.getClient()

    // API 인터페이스를 생성
    private val apiService: ProblemApiService = retrofit.create(ProblemApiService::class.java)

    private val _problemInfo = MutableLiveData<ProblemResult?>()
    val problemInfo: MutableLiveData<ProblemResult?> get() = _problemInfo

    private val _problemType = MutableLiveData<ProblemType>()
    val problemType: LiveData<ProblemType> get() = _problemType

    private val _answer = MutableLiveData<String>()
    val answer: LiveData<String> get() = _answer

    private val _problemText = MutableLiveData<String>()
    val problemText: LiveData<String> get() = _problemText

    // 문제 정보를 API를 통해 가져오기
    fun fetchProblemInfo(problemId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getProblemInfo(problemId)
                if (response.isSuccess) { // 성공 여부를 직접 확인
                    _problemInfo.value = response.result
                    _answer.value = response.result.answer
                    _problemText.value = response.result.problemText
                    _problemType.value = response.result.problemType
                    processProblemData(response.result)
                    Log.d("result", "$response.result")
                } else {
                    Log.e("ProblemInfoViewModel", "API Error: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("ProblemInfoViewModel", "Exception occurred: ${e.message}", e)
            }
        }
    }

    private fun processProblemData(result: ProblemResult) {
        // 이미지 URI 설정
        _problemPhotoUri.value = Uri.parse(result.problemImage)

        // 첫 번째 사진과 나머지 사진 설정
        if (result.solutionImages.isNotEmpty()) {
            setFirstPhoto(result.solutionImages.first())
            setRemainingPhotos(result.solutionImages.drop(1))
        }

        // 필요한 경우 다른 데이터 처리 로직 추가 가능
    }
}