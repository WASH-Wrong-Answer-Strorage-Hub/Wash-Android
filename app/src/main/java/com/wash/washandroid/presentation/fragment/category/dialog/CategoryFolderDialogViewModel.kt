package com.wash.washandroid.presentation.fragment.category.dialog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.model.PostFolderRequest
import com.wash.washandroid.presentation.fragment.category.network.CategoryApiService
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import kotlinx.coroutines.launch

class CategoryFolderDialogViewModel : ViewModel() {

    private val _folder = MutableLiveData<String>()
    val folder: LiveData<String> = _folder

    private val _folderTypeId = MutableLiveData<Int?>()
    val folderTypeId: MutableLiveData<Int?> = _folderTypeId

    private val retrofit = NetworkModule.getClient()

    // API 인터페이스를 생성
    private val apiService: CategoryApiService = retrofit.create(CategoryApiService::class.java)

    fun addCategoryType(folder: String) {
        viewModelScope.launch {
            try {
                val response = apiService.postCategoryFolderType(PostFolderRequest(folder))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val folderId = response.body()?.result?.folderId
                    _folderTypeId.value = folderId
                    Log.d("folderId", "$folderId")
                } else {
                    Log.e("CategoryFolderDialogViewModel", "Failed to add category type: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // 에러 처리
                Log.e("CategoryFolderDialogViewModel", "Error adding category type: ${e.message}")
            }
        }
    }
}