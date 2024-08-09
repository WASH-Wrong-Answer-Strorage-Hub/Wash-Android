package com.wash.washandroid.presentation.fragment.study

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData

class StudyViewModel : ViewModel() {
    // 문제 데이터 리스트를 저장하는 MutableLiveData
    private val _problems = MutableLiveData<List<StudyProblem>>()
    val problems: LiveData<List<StudyProblem>> get() = _problems

    // 현재 문제의 인덱스를 저장하는 변수
    var currentProblemIndex: Int = 0
        private set

    // 문제 데이터를 서버에서 로드하는 함수
    fun loadProblems() {
        // 서버에서 데이터를 가져와서 problems에 설정
        val fetchedProblems = fetchProblemsFromServer() // 서버에서 문제 데이터를 가져오는 함수
        _problems.value = fetchedProblems
    }

    // 서버에서 문제 데이터를 가져오는 함수(임시)
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

    fun incrementRightSwipe(index: Int) {
        _problems.value?.let {
            val updatedProblems = it.toMutableList()
            updatedProblems[index] = updatedProblems[index].copy(rightSwipes = updatedProblems[index].rightSwipes + 1)
            _problems.value = updatedProblems
        }
    }

    fun incrementLeftSwipe(index: Int) {
        _problems.value?.let {
            val updatedProblems = it.toMutableList()
            updatedProblems[index] = updatedProblems[index].copy(leftSwipes = updatedProblems[index].leftSwipes + 1)
            _problems.value = updatedProblems
        }
    }

    fun getTotalRightSwipes(): Int {
        return _problems.value?.sumOf { it.rightSwipes } ?: 0
    }

    fun getTotalLeftSwipes(): Int {
        return _problems.value?.sumOf { it.leftSwipes } ?: 0
    }

    fun getTotalProblems(): Int {
        return _problems.value?.size ?: 0
    }

    fun getCurrentProblem(): StudyProblem {
        return _problems.value?.get(currentProblemIndex) ?: throw IllegalStateException("문제를 찾을 수 없습니다.")
    }

    fun isLastProblem(): Boolean {
        return currentProblemIndex == (_problems.value?.size ?: 1) - 1
    }

    // 이전 문제로 이동
    fun moveToPreviousProblem() {
        if (currentProblemIndex > 0) {
            currentProblemIndex--
        }
    }

    // 다음 문제로 이동
    fun moveToNextProblem() {
        _problems.value?.let {
            if (currentProblemIndex < it.size - 1) {
                currentProblemIndex++
            }
        }
    }
}