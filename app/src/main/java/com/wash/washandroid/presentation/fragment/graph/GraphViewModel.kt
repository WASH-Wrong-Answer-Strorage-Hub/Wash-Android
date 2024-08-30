package com.wash.washandroid.presentation.fragment.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wash.washandroid.presentation.fragment.mypage.data.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GraphViewModel : ViewModel() {

    private val _mistakeResponse = MutableLiveData<List<Result>>()
    val mistakeResponse: LiveData<List<Result>> get() = _mistakeResponse

    private val _typeResponse = MutableLiveData<List<ProblemStatistics>?>()
    val typeResponse: LiveData<List<ProblemStatistics>?> get() = _typeResponse

    private val _pieChartData = MutableLiveData<PieChartResponse?>()
    val pieChartData: LiveData<PieChartResponse?> get() = _pieChartData


    fun fetchMistakeData(refreshToken: String) {
        RetrofitClient.graphApiService.getMistakes(refreshToken).enqueue(object : Callback<ProblemsResponse> {
            override fun onResponse(call: Call<ProblemsResponse>, response: Response<ProblemsResponse>) {
                if (response.isSuccessful) {
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
        RetrofitClient.graphApiService.getTypes(refreshToken).enqueue(object : Callback<TypeResponse> {
            override fun onResponse(call: Call<TypeResponse>, response: Response<TypeResponse>) {
                if (response.isSuccessful) {
                    _typeResponse.postValue(response.body()?.result)
                    Log.d("GraphViewModel", "Types Response: ${response.body()}")
                } else {
                    _typeResponse.postValue(null)
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TypeResponse>, t: Throwable) {
                _typeResponse.postValue(null)
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }

    fun fetchPieChartData(accessToken: String, categoryId: Int) {
        RetrofitClient.graphApiService.getRatios(accessToken, categoryId).enqueue(object : Callback<PieChartResponse> {
            override fun onResponse(call: Call<PieChartResponse>, response: Response<PieChartResponse>) {
                if (response.isSuccessful) {
                    _pieChartData.value = response.body()
                    Log.d("GraphViewModel", "PieChart categoryID: $categoryId")
                    Log.d("GraphViewModel", "PieChart Response Body: ${response.body()}")
                } else {
                    _pieChartData.value = null
                    Log.e("GraphViewModel", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PieChartResponse>, t: Throwable) {
                _pieChartData.value = null
                Log.e("GraphViewModel", "Failure: ${t.message}", t)
            }
        })
    }

}
