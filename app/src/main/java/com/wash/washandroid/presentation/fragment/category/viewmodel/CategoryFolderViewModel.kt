package com.wash.washandroid.presentation.fragment.category.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
import com.wash.washandroid.model.Folder
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.problem.add.ProblemManager
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemRequest
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    private val problemManager = ProblemManager // 직접 접근

    fun postProblem() {
        viewModelScope.launch {
            val problemDataList = problemManager.getProblems()
            Log.d("problemDataList", problemDataList.toString())

            val responses = problemDataList.map { problemData ->
                async {
                    if (!validateProblemData(problemData)) {
                        Log.e("ValidationError", "Invalid problem data: $problemData")
                        return@async null  // 유효하지 않은 데이터는 건너뜁니다.
                    }

                    val folderId = _folderId.value ?: return@async null
                    Log.d("folderId", folderId.toString())

                    val postRequest = problemData.mainTypeId?.let {
                        problemData.midTypeId?.let { it1 ->
                            PostProblemRequest(
                                folderId = folderId,
                                problemText = problemData.problemText,
                                answer = problemData.answer,
                                memo = problemData.memo,
                                mainTypeId = it,
                                midTypeId = it1,
                                subTypeIds = problemData.subTypeIds,
                                problemImage = listOf(problemData.problemImageUri),
                                solutionImages = problemData.solutionImageUris,
                                passageImages = problemData.passageImageUris,
                                additionalImages = problemData.additionalImageUris
                            )
                        }
                    }

                    Log.d("PostProblemRequest", postRequest.toString())

                    try {
                        val response = postRequest?.let { problemRepository.postProblem(it) }
                        if (response?.isSuccessful == true) {
                            val responseBody = response.body()
                            Log.d("postProblem", "Response Successful for ${problemData.problemText}: $responseBody")
                            responseBody
                        } else {
                            if (response != null) {
                                Log.e("postProblem", "Response Failed for ${problemData.problemText}: ${response.errorBody()?.string()}")
                            }
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("postProblem", "Unexpected Error: ${e.localizedMessage}")
                        null
                    }
                }
            }.awaitAll()

            // 모든 호출이 완료된 후 결과 처리
            responses.forEach { result ->
                if (result != null) {
                    Log.d("postProblem", "Successfully processed problem: $result")
                }
            }
            _postSuccess.value = responses.all { true }
        }
    }

    private val _postSuccess = MutableLiveData<Boolean>()
    val postSuccess: LiveData<Boolean> = _postSuccess

    fun setSuccess() {
        _postSuccess.value = false
    }

    private fun validateProblemData(problemData: ProblemData?): Boolean {
        if (problemData == null) {
            Log.e("ValidationError", "Problem data is null")
            return false
        }

        return true
    }
}