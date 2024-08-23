package com.wash.washandroid.presentation.fragment.category.network

import com.wash.washandroid.presentation.fragment.problem.network.PostProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class ProblemRepository(private val apiService: ProblemApiService) {

    suspend fun postProblem(
        problemImageFile: File?,
        solutionImageFiles: List<File>,
        passageImageFiles: List<File>,
        additionalImageFiles: List<File>,
        jsonData: String
    ): Response<PostProblemResponse> {

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

        return apiService.postProblem(
            problemImage = problemImagePart,
            solutionImages = solutionImageParts,
            passageImages = passageImageParts,
            additionalImages = additionalImageParts,
            data = dataPart
        )
    }
}
