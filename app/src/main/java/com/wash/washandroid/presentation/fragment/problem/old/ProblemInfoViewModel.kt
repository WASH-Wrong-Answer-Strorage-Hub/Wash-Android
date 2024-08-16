package com.wash.washandroid.presentation.fragment.problem.old

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProblemInfoViewModel : ViewModel() {
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
}