package com.wash.washandroid.presentation.fragment.category.network

import com.wash.washandroid.model.CategorySubjectAddRequest
import com.wash.washandroid.model.CategoryAddResponse
import com.wash.washandroid.model.CategoryResponse
import com.wash.washandroid.model.FolderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CategoryApiService {
    @GET("/problems/types/1")
    suspend fun getCategoryTypes(): Response<CategoryResponse>

    @GET("/problems/types/2")
    suspend fun getCategorySubfieldTypes(@Query("parentTypeId") parentTypeId: Int): Response<CategoryResponse>

    @GET("/problems/types/3")
    suspend fun getCategoryChapterTypes(@Query("parentTypeId") parentTypeId: Int): Response<CategoryResponse>

    @GET("/folders")
    suspend fun getCategoryFolderTypes(): Response<FolderResponse>

    @POST("/problems/types")
    suspend fun postCategoryTypes(@Body request: CategorySubjectAddRequest): Response<CategoryAddResponse>
}