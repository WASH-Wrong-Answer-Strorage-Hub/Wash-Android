package com.wash.washandroid.presentation.fragment.problem.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProblemAnswerViewModel : ViewModel() {

    private val _solutionPhotoList = MutableLiveData<List<String>>()
    val solutionPhotoList: LiveData<List<String>> get() = _solutionPhotoList

    private val _printPhotoList = MutableLiveData<List<String>>()
    val printPhotoList: LiveData<List<String>> get() = _printPhotoList

    private val _addPhotoList = MutableLiveData<List<String>>()
    val addPhotoList: LiveData<List<String>> get() = _addPhotoList
}