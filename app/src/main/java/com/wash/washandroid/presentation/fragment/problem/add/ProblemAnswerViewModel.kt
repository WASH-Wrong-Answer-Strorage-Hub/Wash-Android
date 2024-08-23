package com.wash.washandroid.presentation.fragment.problem.add

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.ChatOCRMessage
import com.wash.washandroid.model.ChatOCRRequest
import com.wash.washandroid.model.Content
import com.wash.washandroid.model.ImageUrl
import com.wash.washandroid.presentation.fragment.chat.ChatApi
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import kotlinx.coroutines.launch

class ProblemAnswerViewModel : ViewModel() {
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> get() = _recognizedText

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

    private val _problemData = MutableLiveData<ProblemData>()
    val problemData: LiveData<ProblemData> = _problemData

    fun setProblemData(data: ProblemData) {
        _problemData.value = data
    }
}