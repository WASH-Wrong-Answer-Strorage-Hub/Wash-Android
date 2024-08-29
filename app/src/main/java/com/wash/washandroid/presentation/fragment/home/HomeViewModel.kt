import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.home.ApiResponse
import com.wash.washandroid.presentation.fragment.home.Problem
import com.wash.washandroid.presentation.fragment.home.ProblemSearch
import com.wash.washandroid.presentation.fragment.home.ReorderRequest
import com.wash.washandroid.presentation.fragment.mypage.data.service.RetrofitClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class HomeViewModel : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val _images = MutableLiveData<List<Problem>>()
    val images: LiveData<List<Problem>> get() = _images

    private val _searchResults = MutableLiveData<List<ProblemSearch>>()
    val searchResults: LiveData<List<ProblemSearch>> get() = _searchResults

    private val _folderProblems = MutableLiveData<List<Problem>>()
    val folderProblems: LiveData<List<Problem>> get() = _folderProblems

    private val _reorderResult = MutableLiveData<Result<String>>()
    val reorderResult: LiveData<Result<String>> get() = _reorderResult

    private val apiService = NetworkModule.getClient().create(ApiService::class.java)

    // 전체 폴더 조회
    fun fetchFolders(accessToken: String) {
        Log.d("homeviewmodel-folder", "$accessToken")

        viewModelScope.launch {
            try {
                NetworkModule.setAccessToken(accessToken)
                val response = apiService.getFolders("Bearer $accessToken").awaitResponse()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true) {
                        val folders = apiResponse.result.map { folder ->
                            Note(
                                folderId = folder.folderId,
                                title = folder.folderName,
                                imageResId = R.drawable.ic_list_frame
                            )
                        }
                        _notes.value = folders
                        Log.d("HomeViewModel", "Fetched folders: $folders")
                    } else {
                        Log.e("HomeViewModel", "API Error: ${apiResponse?.message}")
                    }
                } else {
                    Log.e("HomeViewModel-folder", "Response Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Fetch Folders Error: ${e.message}")
            }
        }
    }


    fun updateFolderName(folderId: Int, newFolderName: String, accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updateRequest = mapOf("folderName" to newFolderName)
                val response = apiService.updateFolderName("Bearer $accessToken", folderId, updateRequest).execute()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchFolders(accessToken)
                } else {
                    Log.e("HomeViewModel", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network Error: ${e.message}")
            }
        }
    }

    fun deleteFolder(folderId: Int, accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.deleteFolder("Bearer $accessToken", folderId).execute()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchFolders(accessToken)
                } else {
                    Log.e("HomeViewModel", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network Error: ${e.message}")
            }
        }
    }

    fun fetchImagesForFolder(folderId: Int, accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getImagesForFolder("Bearer $accessToken", folderId).execute()
                if (response.isSuccessful) {

                    // Log the raw response and problems list
                    Log.d("HomeViewModel", "API Response Code: ${response.code()}")
                    Log.d("HomeViewModel", "API Response Message: ${response.message()}")
                    Log.d("HomeViewModel", "API Response Body: ${response.body()}")

                    // Update LiveData with the fetched problems

                    val problems = response.body()?.result?.problems ?: emptyList()
                    Log.d("HomeViewModel", "Fetched problems: $problems")
                    _images.postValue(problems)
                    Log.d("HomeViewModel", "Fetched images: $problems")

                } else {
                    Log.e("HomeViewModel", "Response Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network Error: ${e.message}")
            }
        }
    }



    fun deleteProblem(problemId: Int, folderId: Int, accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.deleteProblem("Bearer $accessToken", problemId).execute()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("HomeViewModel-delete", "Problem deleted successfully")
                    fetchImagesForFolder(folderId, accessToken)
                } else {
                    Log.e("HomeViewModel", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network Error: ${e.message}")
            }
        }
    }


    fun searchProblems(query: String, folderId: Int?, token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.searchProblems("Bearer $token", folderId, query).execute()

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse?.isSuccess == true) {
                        _searchResults.postValue(searchResponse.result)
                    } else {
                        Log.e("HomeViewModel", "Search Error: ${searchResponse?.message}")
                    }
                } else {
                    Log.e("HomeViewModel", "Search Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Search Error: ${e.message}")
            }
        }
    }

    // 폴더 위치 이동
    fun reorderFolders(token: String, order: List<Int>) {
        val requestBody = ReorderRequest(order)
        Log.d("HomeViewModel", "Reordering folders with order: $order")
        viewModelScope.launch {
            try {
                val response = apiService.reorderFolders("Bearer $token", requestBody).awaitResponse()
                if (response.isSuccessful) {
                    val resultMessage = response.body()?.result?.message ?: "Reordered successfully"
                    _reorderResult.value = Result.success(resultMessage)
                    Log.d("HomeViewModel", "Reordering folders succeeded: $resultMessage")

                    // 폴더 순서가 서버에 저장된 후 다시 폴더를 가져와서 로컬 데이터 갱신
                    //fetchFolders(token)
                } else {
                    Log.e("HomeViewModel", "Reorder API Error: ${response.message()}")
                    _reorderResult.value = Result.failure(Exception("Failed to reorder folders"))
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Reorder Error: ${e.message}")
                _reorderResult.value = Result.failure(e)
            }
        }
    }


    fun getProblemImageUrl(problemId: Int): String? {
        Log.d("problemImg", "Current _images value: ${_images.value}")
        val problem = _images.value?.find { it.problemId == problemId }
        Log.d("problemImg", "Found problem: $problem for problemId: $problemId")
        return problem?.problemImage
    }
}