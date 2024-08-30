package com.wash.washandroid.presentation.fragment.problem.add

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProblemAddViewModel : ViewModel() {
    // 사진 목록을 저장하는 LiveData
    private val _photoList = MutableLiveData<MutableList<String>>(mutableListOf())
    val photoList: LiveData<MutableList<String>> get() = _photoList

    // 현재 인덱스를 추적하는 변수
    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> get() = _currentIndex

    // 사진 목록 설정
    fun setPhotos(photoPaths: List<String>) {
        _photoList.value = photoPaths.toMutableList()
        Log.d("ProblemAddViewModel", "setPhotos: photoPaths set with size ${photoPaths.size}")
    }

    // 인덱스를 증가시키는 함수
    fun incrementIndex() {
        val currentIndexBefore = _currentIndex.value ?: 0
        _currentIndex.value = (_currentIndex.value ?: 0) + 1
        Log.d("ProblemAddViewModel", "incrementIndex: from $currentIndexBefore to ${_currentIndex.value}")
    }

    // 인덱스 초기화
    fun resetIndex() {
        Log.d("ProblemAddViewModel", "resetIndex: current index ${_currentIndex.value} reset to 0")
        _currentIndex.value = 0
    }

    // 현재 인덱스가 목록의 마지막인지 확인
    fun isLastIndex(): Boolean {
        val lastIndex = _photoList.value?.size ?: 0
        val realLastIndex = lastIndex - 1
        val isLast = _currentIndex.value == realLastIndex
        Log.d("ProblemAddViewModel", "isLastIndex: currentIndex ${_currentIndex.value}, lastIndex $realLastIndex, isLast $isLast")
        return isLast
    }

    // 현재 인덱스의 사진 경로를 반환하는 함수
    fun getCurrentPhoto(): String? {
        val index = _currentIndex.value ?: 0
        val photo = _photoList.value?.getOrNull(index)
        Log.d("ProblemAddViewModel", "getCurrentPhoto: index $index, photo $photo")
        return photo
    }
}