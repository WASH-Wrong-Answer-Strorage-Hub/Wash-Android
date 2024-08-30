package com.wash.washandroid.presentation.fragment.category.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.wash.washandroid.R
import com.wash.washandroid.model.Folder
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemRequest
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import kotlinx.coroutines.launch
import retrofit2.Response

class CategoryFolderViewModel(private val problemRepository: ProblemRepository) : ViewModel() {

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

    private val _categoryTypes = MutableLiveData<List<Folder>>()
    val categoryTypes: LiveData<List<Folder>> get() = _categoryTypes

    private val retrofit = NetworkModule.getClient()
    private val apiService: CategoryApiService = retrofit.create(CategoryApiService::class.java)

    init {
        fetchCategoryTypes()
    }

    fun fetchCategoryTypes() {
        viewModelScope.launch {
            val response = apiService.getCategoryFolderTypes()
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                _categoryTypes.value = response.body()?.result ?: emptyList()
            }
        }
    }

    private val _problemData = MutableLiveData<ProblemData>()
    val problemData: LiveData<ProblemData> get() = _problemData

    private val _mainTypeId = MutableLiveData<Int>()
    val mainTypeId: LiveData<Int> get() = _mainTypeId

    private val _midTypeId = MutableLiveData<Int>()
    val midTypeId: LiveData<Int> get() = _midTypeId

    private val _subTypeIds = MutableLiveData<List<Int>>()
    val subTypeIds: LiveData<List<Int>> get() = _subTypeIds

    private val _folderId = MutableLiveData<Int>()
    val folderId: LiveData<Int> get() = _folderId

    private val _apiResponse = MutableLiveData<Response<PostProblemResponse>>()
    val apiResponse: LiveData<Response<PostProblemResponse>> get() = _apiResponse

    fun setProblemData(problemData: ProblemData) {
        _problemData.value = problemData
    }

    fun setMainTypeId(mainTypeId: Int) {
        _mainTypeId.value = mainTypeId
    }

    fun setMidTypeId(midTypeId: Int) {
        _midTypeId.value = midTypeId
    }

    fun setSubTypeIds(subTypeIds: List<Int>) {
        _subTypeIds.value = subTypeIds
    }

    fun setFolderId(folderId: Int) {
        _folderId.value = folderId
    }

    fun postProblem() {
        viewModelScope.launch {
            try {
                val problemData = _problemData.value ?: run {
                    Log.e("ValidationError", "Problem data is null")
                    return@launch
                }
                if (!validateProblemData(problemData)) {
                    Log.e("ValidationError", "Invalid problem data")
                    return@launch
                }

                val folderId = _folderId.value ?: run {
                    Log.e("ValidationError", "Folder ID is null")
                    return@launch
                }
                val mainTypeId = _mainTypeId.value ?: run {
                    Log.e("ValidationError", "Main Type ID is null")
                    return@launch
                }
                val midTypeId = _midTypeId.value ?: run {
                    Log.e("ValidationError", "Mid Type ID is null")
                    return@launch
                }
                val subTypeIds = _subTypeIds.value ?: run {
                    Log.e("ValidationError", "Sub Type IDs are null")
                    return@launch
                }

                val postRequest = PostProblemRequest(
                    folderId = folderId,
                    problemText = problemData.problemText,
                    answer = problemData.answer,
                    memo = problemData.memo,
                    mainTypeId = mainTypeId,
                    midTypeId = midTypeId,
                    subTypeIds = subTypeIds,
                    problemImage = listOf(problemData.problemImageUri),
                    solutionImages = problemData.solutionImageUris,
                    passageImages = problemData.passageImageUris,
                    additionalImages = problemData.additionalImageUris
                )

                val response = problemRepository.postProblem(postRequest)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("postProblem", "Response Successful: $responseBody")
                } else {
                    Log.e("postProblem", "Response Failed: ${response.errorBody()?.string()}")
                }

                _apiResponse.value = response
            } catch (e: JsonSyntaxException) {
                Log.e("postProblem", "JSON Parsing Error: ${e.localizedMessage}")
            } catch (e: Exception) {
                Log.e("postProblem", "Unexpected Error: ${e.localizedMessage}")
            }
        }
    }
    private fun validateProblemData(problemData: ProblemData?): Boolean {
        if (problemData == null) {
            Log.e("ValidationError", "Problem data is null")
            return false
        }

        return true
    }
}