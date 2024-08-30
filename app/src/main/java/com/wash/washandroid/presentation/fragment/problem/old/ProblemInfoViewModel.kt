package com.wash.washandroid.presentation.fragment.problem.old

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.problem.network.EditProblemRequest
import com.wash.washandroid.presentation.fragment.problem.network.EditProblemResponse
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import com.wash.washandroid.presentation.fragment.problem.network.ProblemInfoData
import com.wash.washandroid.presentation.fragment.problem.network.ProblemResult
import com.wash.washandroid.presentation.fragment.problem.network.ProblemType
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit

class ProblemInfoViewModel(private val problemRepository: ProblemRepository) : ViewModel() {
    private val _problemPhotoUri = MutableLiveData<Uri?>()
    val problemPhotoUri: LiveData<Uri?> get() = _problemPhotoUri

    fun setProblemPhotoUri(uri: Uri?) {
        _problemPhotoUri.value = uri
    }

    private val _photoUris = MutableLiveData<List<String>>(emptyList())
    val photoUris: LiveData<List<String>> get() = _photoUris

    private val _selectedPhotoPosition = MutableLiveData<Int>()
    val selectedPhotoPosition: LiveData<Int> get() = _selectedPhotoPosition

    fun setPhotoUris(uris: List<String>) {
        _photoUris.value = uris
    }

    fun setSelectedPhotoPosition(position: Int) {
        _selectedPhotoPosition.value = position
    }

    private val _firstPhotoUri = MutableLiveData<String?>()
    val firstPhotoUri: LiveData<String?> get() = _firstPhotoUri

    private val _remainingPhotoUris = MutableLiveData<List<String>>()
    val remainingPhotoUris: LiveData<List<String>> get() = _remainingPhotoUris

    fun setFirstPhoto(uri: String?) {
        _firstPhotoUri.value = uri
    }

    fun setRemainingPhotos(uris: List<String>) {
        _remainingPhotoUris.value = uris
    }

    private val _problemInfo = MutableLiveData<ProblemResult?>()
    val problemInfo: MutableLiveData<ProblemResult?> get() = _problemInfo

    private val _problemType = MutableLiveData<ProblemType>()
    val problemType: LiveData<ProblemType> get() = _problemType

    private val _answer = MutableLiveData<String>()
    val answer: LiveData<String> get() = _answer

    private val _memo = MutableLiveData<String>()
    val memo: LiveData<String> get() = _memo

    private val _problemText = MutableLiveData<String>()
    val problemText: LiveData<String> get() = _problemText

    private val _solutionPhotoList = MutableLiveData<List<String>>()
    val solutionPhotoList: LiveData<List<String>> get() = _solutionPhotoList

    private val _printPhotoList = MutableLiveData<List<String>>()
    val printPhotoList: LiveData<List<String>> get() = _printPhotoList

    private val _addPhotoList = MutableLiveData<List<String>>()
    val addPhotoList: LiveData<List<String>> get() = _addPhotoList

    // 빈 리스트 할당
    fun clearProblemDetails() {
        _solutionPhotoList.value = mutableListOf()
        _printPhotoList.value = mutableListOf()
        _addPhotoList.value = mutableListOf()
    }


    private var retrofit: Retrofit? = null
    private var apiService: ProblemApiService? = null

    fun initialize(token: String) {
        Log.d("ProblemInfoViewModel", "Initializing with token: $token")
        NetworkModule.setAccessToken(token)
        retrofit = NetworkModule.getClient()
        apiService = retrofit?.create(ProblemApiService::class.java)
        Log.d("ProblemInfoViewModel", "Retrofit and ApiService initialized")
    }

    private val _problemID = MutableLiveData<Int>()
    val problemId : LiveData<Int> = _problemID

    fun setProblemID(problemId: Int){
        _problemID.value = problemId
    }

    // 문제 상세 정보 API 호출
    fun fetchProblemInfo(problemId: String) {
        viewModelScope.launch {
            try {
                val response = apiService?.getProblemInfo(problemId)
                Log.d("ProblemInfoViewModel","연결 $problemId")
                if (response?.isSuccess == true) { // 성공 여부를 직접 확인
                    _problemInfo.value = response.result
                    Log.d("result", "$response.result")

                    _problemType.value = response.result.problemType  // 유형
                    _problemText.value = response.result.problemText  // 문제 텍스트
                    _answer.value = response.result.answer  // 정답
                    _memo.value = response.result.memo  // 메모
                    processProblemData(response.result)  // 문제 사진
                    processProblemListData(response.result)  // 풀이, 지문, 추가 사진 리스트
                } else {
                    Log.e("ProblemInfoViewModel", "API Error: ${response?.message}")
                }
            } catch (e: Exception) {
                Log.e("ProblemInfoViewModel", "Exception occurred: ${e.message}", e)
            }
        }
    }

    private fun processProblemData(result: ProblemResult) {
        // 문제 이미지 URI 설정
        val problemImageUrl = result.problemImage
        if (problemImageUrl.isNotEmpty()) {
            _problemPhotoUri.value = Uri.parse(problemImageUrl)
            Log.d("problemPhotoUri", "Set problem photo URI: $problemImageUrl")
        } else {
            _problemPhotoUri.value = null
            Log.d("problemPhotoUri", "No problem photo URL provided, URI set to null")
        }
    }

    private fun processProblemListData(result: ProblemResult) {
        // 하단 이미지 리스트 URI 설정
        _solutionPhotoList.value = result.solutionImages
        _printPhotoList.value = result.passageImages
        _addPhotoList.value = result.additionalProblemImages
    }


    private val _problemInfoData = MutableLiveData<ProblemInfoData>()
    val problemInfoData: LiveData<ProblemInfoData> get() = _problemInfoData

    fun setProblemData(problemInfoData: ProblemInfoData) {
        _problemInfoData.value = problemInfoData
    }

    private val _apiResponse = MutableLiveData<Response<EditProblemResponse>>()
    val apiResponse: LiveData<Response<EditProblemResponse>> get() = _apiResponse

    fun editProblem() {
        viewModelScope.launch {
            try {
                val problemInfoData = _problemInfoData.value ?: run {
                    Log.e("ValidationError", "ProblemInfoData is null")
                    return@launch
                }
                if (!validateProblemData(problemInfoData)) {
                    Log.e("ValidationError", "Invalid problemInfoData")
                    return@launch
                }

                val problemIdValue = _problemID.value ?: return@launch

                val editRequest = EditProblemRequest(
                    problemId = problemIdValue,
                    problemText = problemInfoData.problemText,
                    answer = problemInfoData.answer,
                    memo = problemInfoData.memo,
                    problemImage = listOf(problemInfoData.problemImageUri),
                    solutionImages = problemInfoData.solutionImageUris,
                    passageImages = problemInfoData.passageImageUris,
                    additionalImages = problemInfoData.additionalImageUris
                )

                val response = problemRepository.editProblem(editRequest)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("editProblem", "Response Successful: $responseBody")
                    _apiResponse.postValue(response)
                } else {
                    Log.e("editProblem", "Response Failed: ${response.errorBody()?.string()}")
                    _apiResponse.postValue(response)
                }
            } catch (e: Exception) {
                Log.e("editProblem", "Unexpected Error: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }
    private fun validateProblemData(problemInfoData: ProblemInfoData?): Boolean {
        if (problemInfoData == null) {
            Log.e("ValidationError", "ProblemInfoData is null")
            return false
        }

        return true
    }

}