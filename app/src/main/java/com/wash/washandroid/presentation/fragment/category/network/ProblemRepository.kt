package com.wash.washandroid.presentation.fragment.category.network

import com.wash.washandroid.presentation.fragment.chat.ChatApiService
import com.wash.washandroid.presentation.fragment.chat.PostChatRequest
import com.wash.washandroid.presentation.fragment.chat.PostChatResponse
import com.wash.washandroid.presentation.fragment.chat.PostChatRoomRequest
import com.wash.washandroid.presentation.fragment.chat.PostChatRoomResponse
import com.wash.washandroid.presentation.fragment.problem.network.EditProblemRequest
import com.wash.washandroid.presentation.fragment.problem.network.EditProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemRequest
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import retrofit2.Response

class ProblemRepository {

    private val retrofit = NetworkModule.getClient()
    private val apiService: ProblemApiService = retrofit.create(ProblemApiService::class.java)
    private val chatService: ChatApiService = retrofit.create(ChatApiService::class.java)


    suspend fun postProblem(request: PostProblemRequest): Response<PostProblemResponse> {
        return apiService.postProblem(request)
    }

    suspend fun editProblem(request: EditProblemRequest): Response<EditProblemResponse> {
        return apiService.editProblem(request)
    }

    suspend fun getChatrooms(request: PostChatRoomRequest): Response<PostChatRoomResponse> {
        return chatService.getChatrooms(request)
    }

    suspend fun addChat(request: PostChatRequest): Response<PostChatResponse> {
        return chatService.addChat(request)
    }
}
