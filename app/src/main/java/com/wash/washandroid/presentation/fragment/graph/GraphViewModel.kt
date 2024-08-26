package com.wash.washandroid.presentation.fragment.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wash.washandroid.presentation.fragment.mypage.data.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class GraphViewModel : ViewModel() {

    // 많이 틀린 문제 데이터를 저장하는 LiveData
    private val _mistakeResponse = MutableLiveData<List<Result>>()
    val mistakeResponse: LiveData<List<Result>> get() = _mistakeResponse

    // 자주 틀린 과목 및 유형 데이터를 저장하는 LiveData
    private val _typeResponse = MutableLiveData<List<TypeResult>>()
    val typeResponse: LiveData<List<TypeResult>> get() = _typeResponse


    // 파이차트 데이터 저장용 LiveData
    private val _pieChartResponse = MutableLiveData<List<Portion>>()
    val pieChartResponse: LiveData<List<Portion>> get() = _pieChartResponse

    // 많이 틀린 문제
    fun fetchMistakeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService
        graphApiService.getMistakes(refreshToken).enqueue(object : Callback<ProblemsResponse> {
            override fun onResponse(call: Call<ProblemsResponse>, response: Response<ProblemsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // 응답 데이터에서 result 리스트를 가져와서 정렬
                    val mistakes = response.body()?.result?.sortedByDescending { it.incorrectCount } ?: emptyList()
                    _mistakeResponse.postValue(mistakes)

                    // 결과
                    Log.d("GraphViewModel", "Mistakes Response: ${response.body()}")
                } else {
                    _mistakeResponse.postValue(emptyList())

                    // 오류
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ProblemsResponse>, t: Throwable) {
                _mistakeResponse.postValue(emptyList())

                // 실패 로그 찍기
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }

    // 자주 틀린 유형
    fun fetchTypeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService

        graphApiService.getTypes(refreshToken).enqueue(object : Callback<TypeResponse> {
            override fun onResponse(call: Call<TypeResponse>, response: Response<TypeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // 응답 데이터에서 result 리스트를 가져옴
                    val types = response.body()?.result ?: emptyList()
                    _typeResponse.postValue(types)

                    // 결과 로그 찍기
                    Log.d("GraphViewModel", "Types Response: ${response.body()}")
                } else {
                    _typeResponse.postValue(emptyList())

                    // 오류 로그 찍기
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TypeResponse>, t: Throwable) {
                _typeResponse.postValue(emptyList())

                // 실패 로그 찍기
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }

    // 파이차트
    fun fetchPieChartData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService
        Log.d("GraphViewModel","화면 연결")
        graphApiService.getRatios(refreshToken).enqueue(object : Callback<PieChartResponse> {
            override fun onResponse(call: Call<PieChartResponse>, response: Response<PieChartResponse>) {
                if (response.isSuccessful) {
                    val pieChartData = response.body()?.result ?: emptyList()
                    _pieChartResponse.postValue(pieChartData)
                    Log.d("GraphViewModel", "Pie Chart Response: ${response.body()}")
                } else {
                    _pieChartResponse.postValue(emptyList())
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PieChartResponse>, t: Throwable) {
                _pieChartResponse.postValue(emptyList())
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }
}
