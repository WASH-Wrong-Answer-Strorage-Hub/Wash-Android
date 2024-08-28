package com.wash.washandroid.presentation.fragment.problem.old

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.ChatOCRMessage
import com.wash.washandroid.model.ChatOCRRequest
import com.wash.washandroid.model.Content
import com.wash.washandroid.model.ImageUrl
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.chat.ChatApi
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import com.wash.washandroid.presentation.fragment.problem.network.ProblemResult
import com.wash.washandroid.presentation.fragment.problem.network.ProblemType
import kotlinx.coroutines.launch
import retrofit2.Retrofit

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

    private val _problemInfo = MutableLiveData<ProblemResult?>()
    val problemInfo: MutableLiveData<ProblemResult?> get() = _problemInfo

    private val _problemType = MutableLiveData<ProblemType>()
    val problemType: LiveData<ProblemType> get() = _problemType

    private val _answer = MutableLiveData<String>()
    val answer: LiveData<String> get() = _answer

    private val _memo = MutableLiveData<String>()
    val memo: LiveData<String> get() = _memo

    private val _problemText = MutableLiveData<String>()
    val problemText: LiveData<String> get() = _problemText

    private val _solutionPhotoList = MutableLiveData<List<String>>()
    val solutionPhotoList: LiveData<List<String>> get() = _solutionPhotoList

    private val _printPhotoList = MutableLiveData<List<String>>()
    val printPhotoList: LiveData<List<String>> get() = _printPhotoList

    private val _addPhotoList = MutableLiveData<List<String>>()
    val addPhotoList: LiveData<List<String>> get() = _addPhotoList


    private var retrofit: Retrofit? = null
    private var apiService: ProblemApiService? = null

    fun initialize(token: String) {
        Log.d("ProblemInfoViewModel", "Initializing with token: $token")
        NetworkModule.setAccessToken(token)
        retrofit = NetworkModule.getClient()
        apiService = retrofit?.create(ProblemApiService::class.java)
        Log.d("ProblemInfoViewModel", "Retrofit and ApiService initialized")
    }

    // 문제 상세 정보를 API를 통해 가져오기
    fun fetchProblemInfo(problemId: String) {
        viewModelScope.launch {
            try {
                val response = apiService?.getProblemInfo(problemId)
                if (response?.isSuccess == true) {
                    _problemInfo.value = response.result
                    Log.d("result", "$response.result")

                    _problemType.value = response.result.problemType  // 유형
                    _problemText.value = response.result.problemText  // 문제 텍스트
                    _answer.value = response.result.answer  // 정답
                    _memo.value = response.result.memo  // 메모
                    processProblemData(response.result)  // 문제 사진
                    processProblemListData(response.result)  // 풀이, 지문, 추가 사진 리스트

                    val problemImageUrl = response.result.problemImage
                    recognizeTextFromImage(problemImageUrl)  // AI로 이미지 인식 후 텍스트 추출
                } else {
                    Log.e("ProblemInfoViewModel", "API Error: ${response?.message}")
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
    }

    private fun processProblemListData(result: ProblemResult) {
        _solutionPhotoList.value = result.solutionImages
        _printPhotoList.value = result.passageImages
        _addPhotoList.value = result.additionalProblemImages
    }

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> get() = _recognizedText

    // 이미지 URL로부터 텍스트를 추출하기 위한 API 요청 함수
    private fun recognizeTextFromImage(imageUrl: String) {
        val textContent = Content(type = "text", text = "문제 사진을 인식해서 문제에 관한 텍스트를 추출해라.")
        val imageContent = Content(type = "image_url", imageUrl = ImageUrl(url = imageUrl))

        val ocrMessage = ChatOCRMessage(role = "user", content = listOf(textContent, imageContent))
        val request = ChatOCRRequest(model = "gpt-4o-mini", messages = listOf(ocrMessage))

        viewModelScope.launch {
            try {
                val response = ChatApi.retrofitService.sendMessageImageUrl(request)
                val recognizedText = response.choices.firstOrNull()?.message?.content?.trim() ?: "No text recognized"
                Log.d("StudyViewModel", "인식된 텍스트: $recognizedText")
                _recognizedText.postValue(recognizedText)
            } catch (e: Exception) {
                Log.e("StudyViewModel", "에러 발생: ${e.message}")
                _recognizedText.postValue("Error occurred: ${e.message}")
            }
        }
    }


}