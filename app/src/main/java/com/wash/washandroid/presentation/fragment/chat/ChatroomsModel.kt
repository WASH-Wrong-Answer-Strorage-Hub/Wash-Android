package com.wash.washandroid.presentation.fragment.chat

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class PostChatRoomRequest(
    var problem_id: Int,
    var session_key: String
)

data class PostChatRoomResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: ChatRoomResult
)

data class ChatRoomResult(
    @SerializedName("roomId")
    val roomId: JsonElement,  // Gson의 JsonElement를 사용하여 유연하게 처리
    val logs: List<ChatLog>
)

data class ChatLog(
    val chatId: Int,
    val message: String,
    val speaker: String,
    val sequence: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class PostChatRequest(
    val room_id: Int,
    val message: String,
    val speaker: String
)

data class PostChatResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: ChatId
)

data class ChatId(
    val chatId: Int
)
