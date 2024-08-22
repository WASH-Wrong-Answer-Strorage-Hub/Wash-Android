package com.wash.washandroid.presentation.fragment.category.dialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.CategorySubjectAddRequest
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import kotlinx.coroutines.launch

class CategorySubjectDialogViewModel : ViewModel() {

    private val _subject = MutableLiveData<String>()
    val subject: LiveData<String> = _subject

    private val _subfield = MutableLiveData<String>()
    val subfield: LiveData<String> = _subfield

    private val _chapter = MutableLiveData<String>()
    val chapter: LiveData<String> = _chapter

    private val _subjectTypeId = MutableLiveData<Int>()
    val subjectTypeId: LiveData<Int> = _subjectTypeId

    private val _subfieldTypeId = MutableLiveData<Int>()
    val subfieldTypeId: LiveData<Int> = _subfieldTypeId

    fun updateSubject(value: String) {
        _subject.value = value
    }

    fun updateSubfield(value: String) {
        _subfield.value = value
    }

    fun updateChapter(value: String) {
        _chapter.value = value
    }

    private val retrofit = NetworkModule.getClient()

    // API 인터페이스를 생성
    private val apiService: CategoryApiService = retrofit.create(CategoryApiService::class.java)

    fun addCategoryType(categoryTypeName: String, parentTypeId: Int, typeLevel: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.postCategoryTypes(CategorySubjectAddRequest(categoryTypeName, parentTypeId, typeLevel))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val typeId = response.body()?.result?.firstOrNull()?.typeId
                    typeId?.let {
                        when (typeLevel) {
                            1 -> _subjectTypeId.value = it
                            2 -> _subfieldTypeId.value = it
                        }
                        Log.d("typeId", "$it")
                    }
                } else {
                    Log.e("CategorySubjectViewModel", "Failed to add category type: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 에러 처리
                Log.e("CategorySubjectViewModel", "Error adding category type: ${e.message}")
            }
        }
    }
}