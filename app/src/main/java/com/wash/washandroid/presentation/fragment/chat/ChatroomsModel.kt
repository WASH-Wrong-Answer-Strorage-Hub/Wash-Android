package com.wash.washandroid.presentation.fragment.chat

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
    val roomId: Int,
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

