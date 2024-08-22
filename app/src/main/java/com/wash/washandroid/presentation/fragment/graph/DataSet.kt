package com.wash.washandroid.presentation.fragment.graph

data class MistakeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: SubCategory
)

data class TypeResponse(
    val mainCategory: String,
    val category: String,
    val subCategories: List<SubCategory>
)

data class SubCategory(
    val subCategory: String,
    val totalIncorrect: Int
)
