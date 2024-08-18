package com.wash.washandroid.presentation.fragment.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
import com.wash.washandroid.model.CategoryType
import com.wash.washandroid.presentation.fragment.category.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.NetworkModule
import kotlinx.coroutines.launch

class CategorySubfieldViewModel : ViewModel() {

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

    fun fetchCategoryTypes(parentTypeId: Int) {
        viewModelScope.launch {
            val response = apiService.getCategorySubfieldTypes(parentTypeId)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                _categoryTypes.value = response.body()?.result?.types ?: emptyList()
            }
        }
    }
}