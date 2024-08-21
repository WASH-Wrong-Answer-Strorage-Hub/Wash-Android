import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.accessToken
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.home.ApiResponse
import com.wash.washandroid.presentation.fragment.home.EditFolder
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeViewModel : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    // Retrofit 인스턴스 생성
    private val apiService = NetworkModule.getClient().create(ApiService::class.java)
    val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MzcsImVtYWlsIjoiZGhyd29kbXNAbmF2ZXIuY29tIiwiaWF0IjoxNzI0MjU0OTY5LCJleHAiOjE3MjQyNTg1Njl9.4vERuBmA_c9D7KdB-ixbv9vv9yQwljd9NjgDnwN2PN0"
    fun fetchFolders() {
        viewModelScope.launch {
            // 액세스 토큰 가져오기 (NaverIdLoginSDK 사용)
            if (!accessToken.isNullOrEmpty()) {
                NetworkModule.setAccessToken(accessToken) // NetworkModule에 토큰 설정
                apiService.getFolders("Bearer $accessToken").enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.isSuccess == true) {
                                val folders = apiResponse.result.map { folder ->
                                    Note(
                                        folderId = folder.folderId,
                                        title = folder.folderName,
                                        imageResId = R.drawable.ic_listitem_frame
                                    )
                                }
                                _notes.value = folders
                                Log.d("HomeViewModel", "Fetched folders: $folders")
                            } else {
                                Log.e("HomeViewModel", "API Error: ${apiResponse?.message}")
                            }
                        } else {
                            Log.e("HomeViewModel", "Response Error: ${response.code()} ${response.message()}")
                            Log.e("HomeViewModel", "Response Error Body: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("HomeViewModel", "Network Error: ${t.message}")
                    }
                })
            } else {
                Log.e("HomeViewModel", "Failed to fetch folders: Access token is null or empty")
            }
        }
    }

    // 폴더 이름 업데이트 함수 추가
    fun updateFolderName(folderId: Int, newFolderName: String) {
        viewModelScope.launch {
            if (!accessToken.isNullOrEmpty()) {
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
            } else {
                Log.e("HomeViewModel", "Failed to update folder name: Access token is null or empty")
            }
        }
    }
    // 폴더 삭제 함수 추가
    fun deleteFolder(folderId: Int) {
        viewModelScope.launch {
            if (!accessToken.isNullOrEmpty()) {
                apiService.deleteFolder(accessToken, folderId)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                val apiResponse = response.body()
                                if (apiResponse?.isSuccess == true) {
                                    Log.d("HomeViewModel", "Folder deleted successfully: ${apiResponse.result}")
                                    // 폴더 삭제 후 목록을 다시 가져옴
                                    fetchFolders()
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
            } else {
                Log.e("HomeViewModel", "Failed to delete folder: Access token is null or empty")
            }
        }
    }

}