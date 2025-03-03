package com.wash.washandroid.presentation.fragment.category.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
import com.wash.washandroid.model.CategoryType
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import kotlinx.coroutines.launch
import retrofit2.Retrofit

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

    private var retrofit: Retrofit? = null
    private var apiService: CategoryApiService? = null

    fun initialize(token: String) {
        Log.d("CategorySubjectViewModel", "Initializing with token: $token")
        NetworkModule.setAccessToken(token)
        retrofit = NetworkModule.getClient()
        apiService = retrofit?.create(CategoryApiService::class.java)
        Log.d("CategorySubjectViewModel", "Retrofit and ApiService initialized")
    }

    fun fetchCategoryTypes() {
        Log.d("CategorySubjectViewModel", "Fetching category types...")
        viewModelScope.launch {
            try {
                val response = apiService?.getCategoryTypes()
                if (response?.isSuccessful == true && response.body()?.isSuccess == true) {
                    val types = response.body()?.result?.types ?: emptyList()
                    Log.d("CategorySubjectViewModel", "Fetched category types: $types")
                    _categoryTypes.value = types
                } else {
                    Log.e("CategorySubjectViewModel", "Failed to fetch category types: ${response?.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("CategorySubjectViewModel", "Error fetching category types: ${e.message}")
            }
        }
    }
}