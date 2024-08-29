package com.wash.washandroid.presentation.fragment.category.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.chat.ChatViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemAnswerViewModel
import com.wash.washandroid.presentation.fragment.problem.old.ProblemInfoViewModel

class CategoryFolderViewModelFactory(
    private val repository: ProblemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryFolderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryFolderViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(ProblemInfoViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ProblemInfoViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(ProblemAnswerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProblemAnswerViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
