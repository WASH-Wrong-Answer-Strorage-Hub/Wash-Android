package com.wash.washandroid.model

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: CategoryResult
)

data class CategoryResult(
    @SerializedName("types")
    val types: List<CategoryType>
)

data class CategoryType(
    @SerializedName("type_id")
    val typeId: Int,

    @SerializedName("type_name")
    val typeName: String
)
