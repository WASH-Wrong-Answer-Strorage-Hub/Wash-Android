package com.wash.washandroid.presentation.fragment.chat

import com.wash.washandroid.model.ChatRequest
import com.wash.washandroid.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}