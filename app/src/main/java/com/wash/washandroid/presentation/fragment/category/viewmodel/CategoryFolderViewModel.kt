package com.wash.washandroid.presentation.fragment.category.viewmodel

import android.net.Uri
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
import com.wash.washandroid.presentation.fragment.problem.network.PostProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File

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

    // API 인터페이스를 생성
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
            val problemData = _problemData.value ?: return@launch
            val mainTypeId = _mainTypeId.value ?: return@launch
            val midTypeId = _midTypeId.value ?: return@launch
            val subTypeIds = _subTypeIds.value ?: return@launch
            val folderId = _folderId.value ?: return@launch

            val token = "Bearer YOUR_TOKEN_HERE"

            val jsonData = """
                {
                    "folderId": $folderId,
                    "problemText": "${problemData.problemText}",
                    "answer": "6",
                    "status": "작성",
                    "memo": "${problemData.memo}",
                    "mainTypeId": $mainTypeId,
                    "midTypeId": $midTypeId,
                    "subTypeIds": $subTypeIds
                }
            """.trimIndent()

            // 문제 데이터의 이미지 URI를 변환하기 위한 파일 객체를 `Activity` 또는 `Fragment`에서 처리하고 전달
            // 예시에서는 파일 객체를 직접 변환하지 않고 파일 객체를 전달한다고 가정
            val problemImageFile = problemData.problemImageUri?.let { uri -> convertUriToFile(uri) }
            val solutionImageFiles = problemData.solutionImageUris.map { uri -> convertUriToFile(uri) }
            val passageImageFiles = problemData.passageImageUris.map { uri -> convertUriToFile(uri) }
            val additionalImageFiles = problemData.additionalImageUris.map { uri -> convertUriToFile(uri) }

            val response = problemRepository.postProblem(
                problemImageFile = problemImageFile,
                solutionImageFiles = solutionImageFiles,
                passageImageFiles = passageImageFiles,
                additionalImageFiles = additionalImageFiles,
                jsonData = jsonData
            )

            Log.d("response", response.toString())

            _apiResponse.value = response
        }
    }

    private fun convertUriToFile(uri: Uri): File {
        // 여기에 실제로 URI를 File로 변환하는 로직을 구현해야 합니다.
        // `Fragment`나 `Activity`에서 이 작업을 수행하고, 변환된 파일을 `ViewModel`로 전달하는 것이 좋습니다.
        throw NotImplementedError("URI to File conversion needs to be implemented")
    }
}