package com.wash.washandroid.presentation.fragment.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.ChatRequest
import com.wash.washandroid.model.Message
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<MutableList<String>>()
    val messages: LiveData<MutableList<String>> get() = _messages

    val message = MutableLiveData<String>()

    init {
        _messages.value = mutableListOf()
    }

    fun sendMessage() {
        val currentMessage = message.value ?: return
        _messages.value?.add("You: $currentMessage")
        _messages.postValue(_messages.value)

        val userMessage = Message(role = "user", content = currentMessage)
        val systemMessage = Message(role = "system", content = "Say this is a test!")
        val request = ChatRequest(model = "gpt-4o-mini", messages = listOf(systemMessage, userMessage))

        viewModelScope.launch {
            try {
                val response = ChatApi.retrofitService.sendMessage(request)
                val reply = response.choices.firstOrNull()?.message?.content?.trim() ?: "No response"
                _messages.value?.add("ChatGPT: $reply")
                _messages.postValue(_messages.value)
            } catch (e: Exception) {
                _messages.value?.add("ChatGPT: Error occurred: ${e.message}")
                _messages.postValue(_messages.value)
            }
        }

        message.value = ""
    }
}