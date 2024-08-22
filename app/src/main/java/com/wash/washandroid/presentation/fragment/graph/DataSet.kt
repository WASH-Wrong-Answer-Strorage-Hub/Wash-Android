package com.wash.washandroid.presentation.fragment.graph

data class MistakeResponse(
    val problemId: String,
    val problemImage: String,
    val mistakeCount: Int
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
