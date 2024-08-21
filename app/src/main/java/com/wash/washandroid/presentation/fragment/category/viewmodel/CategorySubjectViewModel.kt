package com.wash.washandroid.presentation.fragment.category.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
import com.wash.washandroid.model.CategorySubjectAddRequest
import com.wash.washandroid.model.CategoryType
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import kotlinx.coroutines.launch

open class CategorySubjectViewModel : ViewModel() {

    private val _selectedButtonId = MutableLiveData<Int?>(null)
    val selectedButtonId: LiveData<Int?> get() = _selectedButtonId

    fun onButtonClicked(buttonId: Int) {
        if (_selectedButtonId.value == buttonId) return
        _selectedButtonId.value = buttonId
    }

    fun getButtonBackground(buttonId: Int): Int {
        return when {
            _selectedButtonId.value == buttonId && buttonId == R.id.category_add -> {
                R.drawable.category_choice_add_clicked_btn
            }
            buttonId == R.id.category_add -> {
                R.drawable.category_choice_add_btn
            }
            _selectedButtonId.value == buttonId -> {
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

    init {
        fetchCategoryTypes()
    }

    private fun fetchCategoryTypes() {
        viewModelScope.launch {
            try {
                val response = apiService.getCategoryTypes()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val types = response.body()?.result?.types ?: emptyList()
                    Log.d("CategorySubjectViewModel", "Fetched category types: $types")
                    _categoryTypes.value = types
                } else {
                    Log.e("CategorySubjectViewModel", "Failed to fetch category types: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CategorySubjectViewModel", "Error fetching category types: ${e.message}")
            }
        }
    }

    // 과목 추가 API 호출 함수
    fun addCategoryType(categoryTypeName: String, typeLevel: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.postCategoryTypes(CategorySubjectAddRequest(categoryTypeName, typeLevel))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 성공적으로 과목이 추가된 경우, 리스트를 다시 가져옴
                    fetchCategoryTypes()
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