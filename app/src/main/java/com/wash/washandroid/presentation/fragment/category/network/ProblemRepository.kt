package com.wash.washandroid.presentation.fragment.category.network

import android.util.Log
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class ProblemRepository {

    private val retrofit = NetworkModule.getClient()
    private val apiService: ProblemApiService = retrofit.create(ProblemApiService::class.java)

    suspend fun postProblem(
        problemImageFile: File?,
        solutionImageFiles: List<File>,
        passageImageFiles: List<File>,
        additionalImageFiles: List<File>,
        jsonData: String
    ): Response<PostProblemResponse> {

        Log.d("postProblem", "Problem Image File: ${problemImageFile?.name}")
        Log.d("postProblem", "Solution Image Files: ${solutionImageFiles.map { it.name }}")
        Log.d("postProblem", "Passage Image Files: ${passageImageFiles.map { it.name }}")
        Log.d("postProblem", "Additional Image Files: ${additionalImageFiles.map { it.name }}")
        Log.d("postProblem", "JSON Data: $jsonData")

        val problemImagePart = problemImageFile?.let {
            MultipartBody.Part.createFormData(
                name = "problemImage",
                filename = it.name,
                body = it.asRequestBody("image/png".toMediaTypeOrNull())
            )
        }

        val solutionImageParts = solutionImageFiles.map {
            MultipartBody.Part.createFormData(
                name = "solutionImages",
                filename = it.name,
                body = it.asRequestBody("image/png".toMediaTypeOrNull())
            )
        }

        val passageImageParts = passageImageFiles.map {
            MultipartBody.Part.createFormData(
                name = "passageImages",
                filename = it.name,
                body = it.asRequestBody("image/png".toMediaTypeOrNull())
            )
        }

        val additionalImageParts = additionalImageFiles.map {
            MultipartBody.Part.createFormData(
                name = "additionalImages",
                filename = it.name,
                body = it.asRequestBody("image/png".toMediaTypeOrNull())
            )
        }

        val dataPart = jsonData.toRequestBody("application/json".toMediaTypeOrNull())

        Log.d("postProblem", "Request Data Prepared")

        return apiService.postProblem(
            problemImage = problemImagePart,
            solutionImages = solutionImageParts,
            passageImages = passageImageParts,
            additionalImages = additionalImageParts,
            data = dataPart
        )
    }
}
