package com.wash.washandroid.presentation.fragment.study

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StudyViewModel : ViewModel() {
    // 문제 데이터 리스트를 저장하는 MutableLiveData
    val problems: MutableLiveData<List<StudyProblem>> = MutableLiveData()

    // 문제 데이터를 서버에서 로드하는 함수
    fun loadProblems() {
        // 서버에서 데이터를 가져와서 problems에 설정
        val fetchedProblems = fetchProblemsFromServer() // 서버에서 문제 데이터를 가져오는 함수
        problems.value = fetchedProblems
    }

    // 서버에서 문제 데이터를 가져오는 함수 (예시로 작성)
    private fun fetchProblemsFromServer(): List<StudyProblem> {
        // 서버 통신 코드 또는 테스트 데이터를 생성하는 코드
        return listOf(
            StudyProblem(
                1,
                "https://samtoring.com/qstn/NwXVS1yaHZ1xav2YsqAf.png",
                "https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg",
                "②"
            ),
            StudyProblem(
                2,
                "https://t1.daumcdn.net/cafeattach/1G8uF/becf7093270745ac586a4c31741a8fc446e62885",
                "https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg",
                "68"
            ),
            StudyProblem(
                3,
                "https://t1.daumcdn.net/cafeattach/1G8uF/272c7efdfc44025870741ada68c861aadee8f701",
                "https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg",
                "3"
            ),
            StudyProblem(
                4,
                "https://t1.daumcdn.net/cafeattach/1G8uF/88b731522f98b390793bdeab1c2157296bf3e115",
                "https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg",
                "40"
            )
            // 추가 문제 데이터...
        )
    }
}