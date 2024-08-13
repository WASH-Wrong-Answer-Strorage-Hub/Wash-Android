package com.wash.washandroid.presentation.fragment.problem.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProblemAnswerViewModel : ViewModel() {
    val isPhotoAdded = MutableLiveData<Boolean>(false)
    val isAnswerEntered = MutableLiveData<Boolean>(false)
}