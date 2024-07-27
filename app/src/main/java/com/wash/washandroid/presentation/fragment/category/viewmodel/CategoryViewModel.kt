package com.wash.washandroid.presentation.fragment.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel : ViewModel() {

    // viewmodel 저장용
    private val _categorySubject = MutableLiveData<String>()

    // 해당 viewmodel을 참고하는 모든 fragment에서 사용하는 변수
    val categorySubject: LiveData<String> get() = _categorySubject

    private val _categorySubfield = MutableLiveData<String>()
    val categorySubfield: LiveData<String> get() = _categorySubfield

    private val _categoryChapter = MutableLiveData<String>()
    val categoryChapter: LiveData<String> get() = _categoryChapter

    private val _categoryFolder = MutableLiveData<String>()
    val categoryFolder: LiveData<String> get() = _categoryFolder

    fun submitCategorySubject(subject: String) {
        _categorySubject.value = subject
    }

    fun submitCategorySubfield(subfield: String) {
        _categorySubfield.value = subfield
    }

    fun submitCategoryChapter(chapter: String) {
        _categoryChapter.value = chapter
    }

    fun submitCategoryFolder(folder: String) {
        _categoryFolder.value = folder
    }

    fun submitAddOption() {
        // 추가하기 옵션 로직
    }
}