import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.refreshToken
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.home.ApiResponse
import com.wash.washandroid.presentation.fragment.home.DeleteProblemResponse
import com.wash.washandroid.presentation.fragment.home.EditFolder
import com.wash.washandroid.presentation.fragment.home.HomeFragment
import com.wash.washandroid.presentation.fragment.home.Problem
import com.wash.washandroid.presentation.fragment.home.ProblemsResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeViewModel : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val apiService = NetworkModule.getClient().create(ApiService::class.java)

    private val _images = MutableLiveData<List<Problem>>()
    val images: LiveData<List<Problem>> get() = _images

    private var _currentFolderId: Int? = null
    fun setCurrentFolderId(folderId: Int) {
        _currentFolderId = folderId
    }

    // 전체 폴더 조회
    fun fetchFolders(accessToken: String) {
        Log.d("homeviewmodel-folder", "$accessToken")
        viewModelScope.launch {
            NetworkModule.setAccessToken(accessToken)
            apiService.getFolders("Bearer $accessToken").enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
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
                        Log.e("HomeViewModel", "Response Error: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("HomeViewModel", "Network Error: ${t.message}")
                }
            })
        }
    }

    fun updateFolderName(folderId: Int, newFolderName: String, accessToken: String) {
        viewModelScope.launch {
            val updateRequest = mapOf("folderName" to newFolderName)
            apiService.updateFolderName("Bearer $accessToken", folderId, updateRequest)
                .enqueue(object : Callback<EditFolder> {
                    override fun onResponse(call: Call<EditFolder>, response: Response<EditFolder>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.isSuccess == true) {
                                Log.d("HomeViewModel", "Folder name updated successfully: ${apiResponse.result}")
                            } else {
                                Log.e("HomeViewModel", "API Error: ${apiResponse?.message}")
                            }
                        } else {
                            Log.e("HomeViewModel", "Response Error: ${response.code()} ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<EditFolder>, t: Throwable) {
                        Log.e("HomeViewModel", "Network Error: ${t.message}")
                    }
                })
        }
    }

    fun deleteFolder(folderId: Int, accessToken: String) {
        viewModelScope.launch {
            apiService.deleteFolder(accessToken, folderId)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.isSuccess == true) {
                                Log.d("HomeViewModel", "Folder deleted successfully: ${apiResponse.result}")
                                fetchFolders(accessToken) // 폴더 삭제 후 목록 다시 가져옴
                            } else {
                                Log.e("HomeViewModel", "API Error: ${apiResponse?.message}")
                            }
                        } else {
                            Log.e("HomeViewModel", "Response Error: ${response.code()} ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("HomeViewModel", "Network Error: ${t.message}")
                    }
                })
        }
    }

    fun fetchImagesForFolder(folderId: Int, accessToken: String) {
        Log.d("HomeViewModel", "Fetching images for folderId: $folderId with token: $accessToken")
        apiService.getImagesForFolder("Bearer $accessToken", folderId).enqueue(object : Callback<ProblemsResponse> {
            override fun onResponse(call: Call<ProblemsResponse>, response: Response<ProblemsResponse>) {
                if (response.isSuccessful) {
                    val problems = response.body()?.result?.problems ?: emptyList()
                    _images.value = problems
                    Log.d("HomeViewModel", "Fetched images: $problems")
                } else {
                    Log.e("HomeViewModel", "Response Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ProblemsResponse>, t: Throwable) {
                Log.e("HomeViewModel", "Network Error: ${t.message}")
            }
        })
    }

    fun deleteProblem(problemId: Int, folderId: Int, accessToken: String) {
        viewModelScope.launch {
            apiService.deleteProblem("Bearer $accessToken", problemId)
                .enqueue(object : Callback<DeleteProblemResponse> {
                    override fun onResponse(call: Call<DeleteProblemResponse>, response: Response<DeleteProblemResponse>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.isSuccess == true) {
                                Log.d("HomeViewModel", "Problem deleted successfully: ${apiResponse.result}")
                                fetchImagesForFolder(folderId, accessToken) // 문제 삭제 후 목록 다시 가져옴
                            } else {
                                Log.e("HomeViewModel", "API Error: ${apiResponse?.message}")
                            }
                        } else {
                            Log.e("HomeViewModel", "Response Error: ${response.code()} ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<DeleteProblemResponse>, t: Throwable) {
                        Log.e("HomeViewModel", "Network Error: ${t.message}")
                    }
                })
        }
    }
}
