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
import com.wash.washandroid.presentation.fragment.home.ReorderResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val _images = MutableLiveData<List<Problem>>()
    val images: LiveData<List<Problem>> get() = _images

    private val _searchResults = MutableLiveData<List<ProblemSearch>>()
    val searchResults: LiveData<List<ProblemSearch>> get() = _searchResults

    private val _folderProblems = MutableLiveData<List<Problem>>()
    val folderProblems: LiveData<List<Problem>> get() = _folderProblems

    // 폴더 이동
    private val _reorderResult = MutableLiveData<Result<String>>()
    val reorderResult: LiveData<Result<String>> get() = _reorderResult



    private val apiService = NetworkModule.getClient().create(ApiService::class.java)

    // 전체 폴더 조회
    fun fetchFolders(accessToken: String) {
        Log.d("homeviewmodel-folder", "$accessToken")
        viewModelScope.launch {
            NetworkModule.setAccessToken(accessToken)
            apiService.getFolders("Bearer $accessToken").enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    Log.d("homeviewmodel-folder", "$accessToken")
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
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("HomeViewModel", "Network Error: ${t.message}")
                }
            })
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
                    val problems = response.body()?.result?.problems ?: emptyList()
                    _images.postValue(problems)
                    Log.d("HomeViewModel", "Fetched images: $problems")

                    // _images 업데이트 후 로그
                    Log.d("HomeViewModel", "Images value after fetch: ${_images.value}")
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
                val response = apiService.searchProblems(token, folderId, query).execute()

                if (response.isSuccessful) {
                    _searchResults.postValue(response.body()?.result)
                } else {
                    Log.e("HomeViewModel", "Search Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Search Error: ${e.message}")
            }
        }
    }

    //폴더 위치 이동

    // 폴더를 서버에서 호출
    fun reorderFolders(token: String, order: List<Int>) {
        val requestBody = ReorderRequest(order)
        Log.d("HomeViewModel", "Reordering folders with order: $order")
        viewModelScope.launch {
            apiService.reorderFolders("Bearer $token", requestBody).enqueue(object : Callback<ReorderResponse> {
                override fun onResponse(call: Call<ReorderResponse>, response: Response<ReorderResponse>) {
                    if (response.isSuccessful) {
                        Log.d("HomeViewModel", "Reorder response: ${response.body()}")
                        val resultMessage = response.body()?.result?.message ?: "Reordered successfully"
                        _reorderResult.value = Result.success(resultMessage)

                        // 폴더 목록을 직접 업데이트
                        fetchFolders(token) // 필요에 따라 이 호출을 제거할 수 있습니다.
                    } else {
                        Log.e("HomeViewModel", "Reorder API Error: ${response.message()}")
                        _reorderResult.value = Result.failure(Exception("Failed to reorder folders"))
                    }
                }

                override fun onFailure(call: Call<ReorderResponse>, t: Throwable) {
                    _reorderResult.value = Result.failure(t)
                }
            })
        }
    }


    // 폴더 위치를 저장할 Map
    private val folderPositionMap = mutableMapOf<Int, Int>()
    private fun updateFolderPositionMap(folders: List<Note>) {
        folderPositionMap.clear()
        folders.forEachIndexed { index, note ->
            folderPositionMap[note.folderId] = index
        }
    }

    //id에 따른 이미지 전달
    fun getProblemImageUrl(problemId: Int): String? {
        Log.d("HomeViewModel", "Current _images value: ${_images.value}")
        val problem = _images.value?.find { it.problemId == problemId }
        Log.d("HomeViewModel", "Found problem: $problem for problemId: $problemId")
        return problem?.problemImage
    }

}
