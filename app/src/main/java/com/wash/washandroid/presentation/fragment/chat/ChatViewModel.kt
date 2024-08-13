package com.wash.washandroid.presentation.fragment.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.ChatItemModels
import com.wash.washandroid.model.ChatRequest
import com.wash.washandroid.model.Message
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<MutableList<ChatItemModels>>()
    val messages: LiveData<MutableList<ChatItemModels>> get() = _messages

    val message = MutableLiveData<String>()

    init {
        _messages.value = mutableListOf()
    }

    fun sendMessage() {
        val currentMessage = message.value ?: return
        Log.d("ChatViewModel", "유저가 전송한 메시지: $currentMessage")
        _messages.value?.add(ChatItemModels(sender = "You", content = currentMessage))
        _messages.postValue(_messages.value)

        val userMessage = Message(role = "user", content = currentMessage)
        val systemMessage = Message(role = "system", content = "Say this is a test!")
        val request = ChatRequest(model = "gpt-4o-mini", messages = listOf(systemMessage, userMessage))

        viewModelScope.launch {
            try {
                val response = ChatApi.retrofitService.sendMessage(request)
                val reply = response.choices.firstOrNull()?.message?.content?.trim() ?: "No response"
                Log.d("ChatViewModel", "GPT가 전달한 메시지: $reply")
                _messages.value?.add(ChatItemModels(sender="ChatGPT", content = reply))
                _messages.postValue(_messages.value)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "에러 메시지: ${e.message}")
                _messages.value?.add(ChatItemModels(sender = "ChatGPT", content = "Error occurred: ${e.message}"))
                _messages.postValue(_messages.value)
            }
        }

        message.value = ""
    }
}