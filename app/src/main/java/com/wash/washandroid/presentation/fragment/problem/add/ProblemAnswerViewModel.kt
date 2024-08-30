package com.wash.washandroid.presentation.fragment.problem.add

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
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.chat.ChatApi
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class ProblemAnswerViewModel(private val problemRepository: ProblemRepository) : ViewModel() {

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

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> get() = _recognizedText

    private var retrofit: Retrofit? = null
    private var apiService: ProblemApiService? = null

    fun initialize(token: String) {
        Log.d("ProblemAnswerViewModel", "Initializing with token: $token")
        NetworkModule.setAccessToken(token)
        retrofit = NetworkModule.getClient()
        apiService = retrofit?.create(ProblemApiService::class.java)
        Log.d("ProblemAnswerViewModel", "Retrofit and ApiService initialized")
    }

    // 이미지 URL로부터 텍스트를 추출하기 위한 API 요청 함수
    fun recognizeTextFromImage(imageUrl: String) {
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