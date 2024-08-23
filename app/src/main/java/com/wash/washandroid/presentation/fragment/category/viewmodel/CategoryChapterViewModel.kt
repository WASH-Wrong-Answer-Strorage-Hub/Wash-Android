package com.wash.washandroid.presentation.fragment.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
import com.wash.washandroid.model.CategoryType
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import kotlinx.coroutines.launch

class CategoryChapterViewModel : ViewModel() {

    // 기존의 단일 선택 ID에서 리스트로 변경
    private val _selectedButtonIds = MutableLiveData<List<Int>>(emptyList())
    val selectedButtonIds: LiveData<List<Int>> get() = _selectedButtonIds

    // 버튼 클릭 시 ID를 리스트에 추가하거나 제거하는 메서드
    fun onButtonClicked(buttonId: Int) {
        val currentList = _selectedButtonIds.value ?: emptyList()
        _selectedButtonIds.value = if (currentList.contains(buttonId)) {
            currentList - buttonId  // 이미 선택된 경우 리스트에서 제거
        } else {
            currentList + buttonId  // 선택되지 않은 경우 리스트에 추가
        }
    }

    fun getButtonBackground(buttonId: Int): Int {
        return when {
            _selectedButtonIds.value?.contains(buttonId) == true && buttonId == R.id.category_add -> {
                R.drawable.category_choice_add_clicked_btn
            }
            buttonId == R.id.category_add -> {
                R.drawable.category_choice_add_btn
            }
            _selectedButtonIds.value?.contains(buttonId) == true -> {
                R.drawable.category_choice_clicked_btn
            }
            else -> {
                R.drawable.category_choice_btn
            }
        }
    }

    private val _categoryTypes = MutableLiveData<List<CategoryType>>()
    val categoryTypes: LiveData<List<CategoryType>> get() = _categoryTypes

    private val retrofit = NetworkModule.getClient()

    // API 인터페이스를 생성
    private val apiService: CategoryApiService = retrofit.create(CategoryApiService::class.java)

    fun fetchCategoryTypes(parentTypeId: Int) {
        viewModelScope.launch {
            val response = apiService.getCategoryChapterTypes(parentTypeId)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                _categoryTypes.value = response.body()?.result?.types ?: emptyList()
            }
        }
    }
}