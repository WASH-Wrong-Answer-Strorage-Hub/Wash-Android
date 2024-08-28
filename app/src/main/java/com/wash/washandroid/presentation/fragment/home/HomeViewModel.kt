import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.refreshToken
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.home.Problem
import com.wash.washandroid.presentation.fragment.home.ProblemSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val _images = MutableLiveData<List<Problem>>()
    val images: LiveData<List<Problem>> get() = _images

    private val _searchResults = MutableLiveData<List<ProblemSearch>>()
    val searchResults: LiveData<List<ProblemSearch>> get() = _searchResults

    private val _folderProblems = MutableLiveData<List<Problem>>()
    val folderProblems: LiveData<List<Problem>> get() = _folderProblems

    private var _currentFolderId: Int? = null

    private val apiService = NetworkModule.getClient().create(ApiService::class.java)

    fun setCurrentFolderId(folderId: Int) {
        _currentFolderId = folderId
    }

    // 전체 폴더 조회
    fun fetchFolders(accessToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "refreshToken : $accessToken")
            try {
                // NetworkModule에 액세스 토큰 설정
                NetworkModule.setAccessToken(accessToken)

                // API 호출
                val response = apiService.getFolders("Bearer $accessToken").execute()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 폴더 리스트를 변환
                    val folders = response.body()?.result?.map { folder ->
                        Note(
                            folderId = folder.folderId,
                            title = folder.folderName,
                            imageResId = R.drawable.ic_list_frame
                        )
                    } ?: emptyList()

                    // 폴더 리스트 로그 출력
                    folders.forEach { folder ->
                        Log.d("HomeViewModel", "Folder ID: ${folder.folderId}, Name: ${folder.title}")
                    }

                    // LiveData에 폴더 리스트 업데이트
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
                    Log.d("HomeViewModel", "Folder name updated successfully: ${response.body()?.result}")
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
                    Log.d("HomeViewModel", "Folder deleted successfully: ${response.body()?.result}")
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
        Log.d("HomeViewModel","FolderID : ${folderId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getImagesForFolder("Bearer $accessToken", folderId).execute()
                if (response.isSuccessful) {
                    val problems = response.body()?.result?.problems ?: emptyList()
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
                    Log.d("HomeViewModel", "Problem deleted successfully: ${response.body()?.result}")
                    fetchImagesForFolder(folderId, accessToken)
                } else {
                    Log.e("HomeViewModel", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network Error: ${e.message}")
            }
        }
    }

    // 문제 검색 함수 (특정 폴더 또는 전체 문제)
    fun searchProblems(query: String, folderId: Int?, token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.searchProblems(token, folderId, query).execute()
                if (response.isSuccessful) {
                    _searchResults.postValue(response.body()?.result)
                } else {
                    Log.d("HomeViewModel", "검색 오류: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("HomeViewModel", "검색 실패: ${e.message}")
            }
        }
    }
}
