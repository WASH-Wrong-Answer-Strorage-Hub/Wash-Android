package com.wash.washandroid.model

import com.google.gson.annotations.SerializedName

data class CategoryAddResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: List<CategoryType>
)

data class CategoryAddType(
    @SerializedName("typeId")
    val typeId: Int,

    @SerializedName("typeName")
    val typeName: String
)

data class CategoryAddRequest(
    var typeName: String, // 국어
    var parentTypeId: Int, // 1
    var typeLevel: Int // 2
)
