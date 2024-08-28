import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.R
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

    fun fetchFolders(accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                NetworkModule.setAccessToken(accessToken)
                val response = apiService.getFolders("Bearer $accessToken").execute()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 폴더 목록을 성공적으로 받아온 경우
                    val folders = response.body()?.result?.mapIndexed { index, folder ->
                        // 각 폴더의 위치를 추적
                        folderPositionMap[folder.folderId] = index
                        Note(
                            folderId = folder.folderId,
                            title = folder.folderName,
                            imageResId = R.drawable.ic_list_frame
                        )
                    } ?: emptyList()

                    // 폴더 목록을 로그로 출력
                    folders.forEach { note ->
                        Log.d("HomeViewModel", "Fetched Folder: id=${note.folderId}, title=${note.title}, position=${folderPositionMap[note.folderId]}")
                    }

                    // LiveData 업데이트
                    _notes.postValue(folders)
                } else {
                    Log.e("HomeViewModel", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network Error: ${e.message}")
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
                    val problems = response.body()?.result?.problems ?: emptyList()
                    _images.postValue(problems)
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
                        _reorderResult.value = Result.success(response.body()?.result?.message ?: "Reordered successfully")
                        fetchFolders(token) // 폴더 순서 변경 저장
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

}
