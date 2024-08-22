package com.wash.washandroid.presentation.fragment.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wash.washandroid.data.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GraphViewModel : ViewModel() {

    // 많이 틀린 문제 데이터를 저장하는 LiveData
    private val _mistakeResponse = MutableLiveData<List<MistakeResponse>>()
    val mistakeResponse: LiveData<List<MistakeResponse>> get() = _mistakeResponse

    // API를 호출하여 많이 틀린 문제 데이터를 가져오는 함수
    fun fetchMistakeData(refreshToken: String) {
            val graphApiService = RetrofitClient.graphApiService

            // Retrofit을 사용한 비동기 API 호출
            graphApiService.getMistakes(refreshToken).enqueue(object : Callback<MistakeResponse> {
                // API 호출 성공 시 호출되는 콜백
                override fun onResponse(call: Call<MistakeResponse>, response: Response<MistakeResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        // 응답이 성공적이고 데이터가 있을 경우 LiveData를 업데이트
                        val mistakes = response.body()?.let { listOf(it) } ?: emptyList()
                        _mistakeResponse.postValue(mistakes)
                    } else {
                        // 응답이 성공적이지 않거나 데이터가 없을 경우 빈 리스트로 설정
                        _mistakeResponse.postValue(emptyList())
                    }
                }

                // API 호출 실패 시 호출되는 콜백
                override fun onFailure(call: Call<MistakeResponse>, t: Throwable) {
                    // API 호출 실패 시 오류 처리 (여기서는 빈 리스트로 설정)
                    _mistakeResponse.postValue(emptyList())
                }
            })
    }
}
