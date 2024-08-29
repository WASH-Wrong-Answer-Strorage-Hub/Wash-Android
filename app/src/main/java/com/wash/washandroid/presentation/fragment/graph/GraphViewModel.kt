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

    private val _mistakeResponse = MutableLiveData<List<Result>>()
    val mistakeResponse: LiveData<List<Result>> get() = _mistakeResponse

    private val _typeResponse = MutableLiveData<ProblemStatistics?>()  // 타입을 nullable로 변경
    val typeResponse: LiveData<ProblemStatistics?> get() = _typeResponse

    private val _pieChartResponse = MutableLiveData<List<Portion>>()
    val pieChartResponse: LiveData<List<Portion>> get() = _pieChartResponse

    fun fetchMistakeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService
        graphApiService.getMistakes(refreshToken).enqueue(object : Callback<ProblemsResponse> {
            override fun onResponse(call: Call<ProblemsResponse>, response: Response<ProblemsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val mistakes = response.body()?.result?.sortedByDescending { it.incorrectCount } ?: emptyList()
                    _mistakeResponse.postValue(mistakes)
                    Log.d("GraphViewModel", "Mistakes Response: ${response.body()}")
                } else {
                    _mistakeResponse.postValue(emptyList())
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ProblemsResponse>, t: Throwable) {
                _mistakeResponse.postValue(emptyList())
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }

    fun fetchTypeData(refreshToken: String) {
        val graphApiService = RetrofitClient.graphApiService
        graphApiService.getTypes(refreshToken).enqueue(object : Callback<TypeResponse> {
            override fun onResponse(call: Call<TypeResponse>, response: Response<TypeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // `ProblemStatistics` 객체를 가져와서 _typeResponse에 설정
                    val stats = response.body()?.result
                    _typeResponse.postValue(stats)
                    Log.d("GraphViewModel", "Types Response: ${response.body()}")
                } else {
                    // 빈 `ProblemStatistics` 객체를 설정
                    _typeResponse.postValue(null)
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TypeResponse>, t: Throwable) {
                // 빈 `ProblemStatistics` 객체를 설정
                _typeResponse.postValue(null)
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }


    fun fetchPieChartData(accessToken: String) {
        val graphApiService = RetrofitClient.graphApiService
        graphApiService.getRatios(accessToken).enqueue(object : Callback<PieChartResponse> {
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