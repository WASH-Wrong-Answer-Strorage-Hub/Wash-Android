package com.wash.washandroid.presentation.fragment.category.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository

class CategoryFolderViewModelFactory(
    private val repository: ProblemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryFolderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryFolderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
