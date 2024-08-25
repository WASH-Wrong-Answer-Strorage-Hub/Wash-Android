package com.wash.washandroid.presentation.fragment.graph

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wash.washandroid.presentation.fragment.mypage.data.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GraphViewModel : ViewModel() {

    // 많이 틀린 문제 데이터를 저장하는 LiveData
    private val _mistakeResponse = MutableLiveData<List<Result>>()
    val mistakeResponse: LiveData<List<Result>> get() = _mistakeResponse

    // 자주 틀린 과목 및 유형 데이터를 저장하는 LiveData
    private val _typeResponse = MutableLiveData<List<Result>>()
    val typeResponse: LiveData<List<Result>> get() = _typeResponse

    // 많이 틀린 문제 데이터를 가져오는 함수
    fun fetchMistakeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService

        graphApiService.getMistakes(refreshToken).enqueue(object : Callback<ProblemsResponse> {
            override fun onResponse(call: Call<ProblemsResponse>, response: Response<ProblemsResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("GraphViewModel", "API Response Successful")
                    if (body != null) {
                        Log.d("GraphViewModel", "Response Body: ${body.result}")
                        val mistakes = body.result.sortedByDescending { it.incorrectCount }
                        _mistakeResponse.postValue(mistakes)
                    } else {
                        Log.d("GraphViewModel", "Response Body is null")
                        _mistakeResponse.postValue(emptyList())
                    }
                } else {
                    Log.e("GraphViewModel", "API Response Error: ${response.code()} - ${response.message()}")
                    _mistakeResponse.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<ProblemsResponse>, t: Throwable) {
                Log.e("GraphViewModel", "API Call Failure", t)
                _mistakeResponse.postValue(emptyList())
            }
        })
    }

    /*
    // 자주 틀린 유형 데이터를 가져오는 함수
    fun fetchTypeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService

        graphApiService.getTypes(refreshToken).enqueue(object : Callback<ProblemsResponse> {
            override fun onResponse(call: Call<ProblemsResponse>, response: Response<ProblemsResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("GraphViewModel", "API Response Successful")
                    if (body != null) {
                        Log.d("GraphViewModel", "Response Body: ${body.result}")
                        val types = body.result
                        _typeResponse.postValue(types)
                    } else {
                        Log.d("GraphViewModel", "Response Body is null")
                        _typeResponse.postValue(emptyList())
                    }
                } else {
                    Log.e("GraphViewModel", "API Response Error: ${response.code()} - ${response.message()}")
                    _typeResponse.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<ProblemsResponse>, t: Throwable) {
                Log.e("GraphViewModel", "API Call Failure", t)
                _typeResponse.postValue(emptyList())
            }
        })
    }
    */
}
