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
    val result: List<CategoryAddType>
)

data class CategoryAddType(
    @SerializedName("typeId")
    val typeId: Int,

    @SerializedName("typeName")
    val typeName: String
)

data class CategorySubjectAddRequest(
    var typeName: String, // 국어
    var typeLevel: Int // 1
)

data class CategorySubfieldAddRequest(
    var typeName: String, // 국어
    var parentTypeId: Int,
    var typeLevel: Int // 2
)

data class CategoryChapterAddRequest(
    var typeName: String, // 국어
    var parentTypeId: Int,
    var typeLevel: Int // 3
)
