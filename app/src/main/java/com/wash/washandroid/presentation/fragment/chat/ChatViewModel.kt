package com.wash.washandroid.presentation.fragment.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.ChatItemModels
import com.wash.washandroid.model.ChatMessage
import com.wash.washandroid.model.ChatRequest
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val problemRepository: ProblemRepository) : ViewModel() {
    private val _messages = MutableLiveData<MutableList<ChatItemModels>>()
    val messages: LiveData<MutableList<ChatItemModels>> get() = _messages

    val message = MutableLiveData<String>()

    init {
        _messages.value = mutableListOf()
    }

    fun clearChatMessages() {
        _messages.value = mutableListOf()
    }

    fun sendMessage() {
        val currentMessage = message.value ?: return
        Log.d("ChatViewModel", "유저가 전송한 메시지: $currentMessage")
        _messages.value?.add(ChatItemModels(sender = "You", content = currentMessage))
        _messages.postValue(_messages.value)

        val userMessage = ChatMessage(role = "user", content = currentMessage)
        val systemMessage = ChatMessage(role = "system", content = "Say this is a test!")
        val request = ChatRequest(model = "gpt-4o-mini", messages = listOf(systemMessage, userMessage))

        viewModelScope.launch {
            try {
                val response = ChatApi.retrofitService.sendMessage(request)
                val reply = response.choices.firstOrNull()?.message?.content?.trim() ?: "No response"
                Log.d("ChatViewModel", "GPT가 전달한 메시지: $reply")

                _messages.value?.add(ChatItemModels(sender="ChatGPT", content = reply))
                _messages.postValue(_messages.value)

                // 채팅 내역 저장을 위해 gpt 답변을 서버에 전송
                addChatMessage(reply, false)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "에러 메시지: ${e.message}")
                _messages.value?.add(ChatItemModels(sender = "ChatGPT", content = "Error occurred: ${e.message}"))
                _messages.postValue(_messages.value)
            }
        }

        message.value = ""
    }

    private val _chatRoom = MutableLiveData<ChatRoomResult>()
    val chatRoom: LiveData<ChatRoomResult> = _chatRoom

    private val _roomId = MutableLiveData<Int?>()
    val roomId: LiveData<Int?> = _roomId

    private val _problemID = MutableLiveData<Int>()
    val problemId : LiveData<Int> = _problemID

    fun setProblemID(problemId: Int){
        _problemID.value = problemId
    }

    fun findOrCreateChatRoom() {
        viewModelScope.launch {
            try {
                val problemId = _problemID.value ?: return@launch
                val editRequest = PostChatRoomRequest(
                    problem_id = problemId,
                    session_key = "string"
                )
                val response = problemRepository.getChatrooms(editRequest)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val roomIdJsonElement = response.body()?.result?.roomId

                    // roomId 추출 로직
                    val roomId = when {
                        roomIdJsonElement?.isJsonObject == true ->
                            roomIdJsonElement.asJsonObject.get("room_id")?.asInt
                        roomIdJsonElement?.isJsonPrimitive == true ->
                            roomIdJsonElement.asInt
                        else -> null
                    }

                    _roomId.value = roomId
                    Log.d("chatRoom", _chatRoom.value.toString())
                    Log.d("roomId", _roomId.value.toString())

                    // 로그를 LiveData에 설정
                    val initialLogs = response.body()?.result?.logs?.map {
                        ChatItemModels(sender = if (it.speaker == "user") "You" else "ChatGPT", content = it.message)
                    } ?: listOf()

                    // 초기 로그를 리스트에 설정
                    _messages.value = initialLogs.toMutableList()
                } else {
                    Log.e("ChatViewModel", "Failed to find or create chat room: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error: ${e.localizedMessage}")
            }
        }
    }

    fun addChatMessage(message: String, isUser: Boolean) {
        viewModelScope.launch {
            try {
                val roomId = _roomId.value
                Log.d("ChatViewModel", "Room ID: $roomId")  // Room ID 로그 출력

                if (roomId == null) {
                    Log.e("ChatViewModel", "Room ID is null")
                    return@launch
                }

                val request = PostChatRequest(room_id = roomId, message = message, speaker = if (isUser) "user" else "ChatGPT")
                Log.d("ChatViewModel", "Sending chat message request: $request")  // 요청 본문 로그 출력

                val response = problemRepository.addChat(request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("ChatViewModel", "Chat message added successfully: ${response.body()?.message}")
                } else {
                    Log.e("ChatViewModel", "Failed to add chat message: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error adding chat message: ${e.message}")
            }
        }
    }
}