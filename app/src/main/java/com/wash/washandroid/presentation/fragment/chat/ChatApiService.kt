package com.wash.washandroid.presentation.fragment.chat

import com.wash.washandroid.model.ChatOCRRequest
import com.wash.washandroid.model.ChatRequest
import com.wash.washandroid.model.ChatResponse
import com.wash.washandroid.model.ChatOCRResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun sendMessageImageUrl(@Body request: ChatOCRRequest): ChatOCRResponse

    @POST("chatrooms")
    suspend fun getChatrooms(@Body request: PostChatRoomRequest): Response<PostChatRoomResponse>

    @POST("chatrooms/chats")
    suspend fun addChat(@Body request: PostChatRequest): Response<PostChatResponse>
}