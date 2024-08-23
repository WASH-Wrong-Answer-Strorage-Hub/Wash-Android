package com.wash.washandroid.presentation.fragment.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wash.washandroid.presentation.fragment.mypage.data.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GraphViewModel : ViewModel() {

    // 많이 틀린 문제 데이터를 저장하는 LiveData
    private val _mistakeResponse = MutableLiveData<List<MistakeResponse>>()
    val mistakeResponse: LiveData<List<MistakeResponse>> get() = _mistakeResponse

    // 자주 틀린 과목 및 유형 데이터를 저장하는 LiveData
    private val _typeResponse = MutableLiveData<List<TypeResponse>>()
    val typeResponse: LiveData<List<TypeResponse>> get() = _typeResponse


    // 많이 틀린 문제 데이터를 가져오는 함수
    fun fetchMistakeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService

        graphApiService.getMistakes(refreshToken).enqueue(object : Callback<MistakeResponse> {
            // API 호출 성공 시 호출되는 콜백
            override fun onResponse(call: Call<MistakeResponse>, response: Response<MistakeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // 응답 데이터가 있을 경우 많이 틀린 문제 순으로 정렬
                    val mistakes = response.body()?.let {
                        listOf(it)
                    }?.sortedByDescending { it.result.totalIncorrect } ?: emptyList()

                    _mistakeResponse.postValue(mistakes)
                } else {
                    _mistakeResponse.postValue(emptyList())
                }
            }
            // API 호출 실패 시 호출되는 콜백
            override fun onFailure(call: Call<MistakeResponse>, t: Throwable) {
                // 응답이 성공적이지 않거나 데이터가 없을 경우 빈 리스트로 설정
                _mistakeResponse.postValue(emptyList())
            }
        })
    }

    fun fetchTypeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService

        graphApiService.getTypes(refreshToken).enqueue(object : Callback<TypeResponse> {
            override fun onResponse(call: Call<TypeResponse>, response: Response<TypeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val types = response.body()?.let {
                        listOf(it)
                    } ?: emptyList()

                    _typeResponse.postValue(types)
                } else {
                    _typeResponse.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<TypeResponse>, t: Throwable) {
                _typeResponse.postValue(emptyList())
            }
        })
    }

}
