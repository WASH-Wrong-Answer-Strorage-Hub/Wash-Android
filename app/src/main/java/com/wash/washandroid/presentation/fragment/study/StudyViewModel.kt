package com.wash.washandroid.presentation.fragment.study

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData

class StudyViewModel : ViewModel() {
    private val _problems = MutableLiveData<List<StudyProblem>>()
    val problems: LiveData<List<StudyProblem>> get() = _problems
    var currentProblemIndex: Int = 0
    private var rightSwipeCount: Int = 0
    private var leftSwipeCount: Int = 0
    var isResetRequired: Boolean = true // study fragment 방문 여부 확인

    // 사진 Uri 리스트와 선택된 사진의 위치를 저장
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


    // 문제 데이터를 서버에서 로드하는 함수
    fun loadProblems() {
        if (_problems.value == null || isResetRequired) {
            val fetchedProblems = fetchProblemsFromServer()
            _problems.value = fetchedProblems
            resetProblemsStatus() // StudyFragment로 돌아갔을 때 초기화
            isResetRequired = false

//            Log.d("fraglog", "Problems loaded: ${fetchedProblems.size}")
        }
    }

    // 모든 문제의 상태를 미완료, 0으로 초기화하는 함수 (StudyFragment에서 StudySolveFragment로 진입 시 호출)
    fun resetProblemsStatus() {
        _problems.value = _problems.value?.map { problem ->
            problem.copy(status = "미완료", correctCount = 0, incorrectCount = 0)
        }
        currentProblemIndex = 0
    }

    // 현재 문제 인덱스를 초기화
    fun resetCurrentProblemIndex() {
        currentProblemIndex = 0
    }

    // 서버에서 문제 데이터를 가져오는 함수(임시)
    private fun fetchProblemsFromServer(): List<StudyProblem> {
        // Todo:서버 통신 코드
        return listOf(
            StudyProblem(
                1,
                "https://samtoring.com/qstn/NwXVS1yaHZ1xav2YsqAf.png",
                "수학",
                "②",
                "미완료",
                0,
                0,
                listOf(
                    "https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg",
                    "https://i.pinimg.com/originals/4f/d9/a3/4fd9a3816c3630d2dc7821c0556235ba.jpg",
                    "https://i.pinimg.com/originals/ba/9a/09/ba9a09a7a3fa88c99021bb358ba2f84d.jpg"
                )

            ),
            StudyProblem(
                2,
                "https://t1.daumcdn.net/cafeattach/1G8uF/becf7093270745ac586a4c31741a8fc446e62885",
                "수학",
                "68",
                "미완료",
                0,
                0,
                listOf ("https://i.pinimg.com/originals/4f/d9/a3/4fd9a3816c3630d2dc7821c0556235ba.jpg")

            ),
            StudyProblem(
                3,
                "https://t1.daumcdn.net/cafeattach/1G8uF/272c7efdfc44025870741ada68c861aadee8f701",
                "수학",
                "3",
                "미완료",
                0,
                0,
                listOf ("https://i.pinimg.com/originals/ba/9a/09/ba9a09a7a3fa88c99021bb358ba2f84d.jpg")
            ),
            StudyProblem(
                4,
                "https://t1.daumcdn.net/cafeattach/1G8uF/88b731522f98b390793bdeab1c2157296bf3e115",
                "수학",
                "40",
                "미완료",
                0,
                0,
                listOf ("https://i.pinimg.com/originals/ff/83/ec/ff83ece9abe0cdd120c2cce0687bc1e2.jpg")
            )
            // 추가 문제 데이터...
        )
    }

    fun getTotalProblems(): Int {
        return _problems.value?.size ?: 0
    }

    // 현재 문제 가져오기
    fun getCurrentProblem(): StudyProblem {
        return _problems.value?.get(currentProblemIndex) ?: throw IllegalStateException("문제를 찾을 수 없습니다.")
    }

    // 문제 상태 업데이트
    fun updateProblemStatus(isCorrect: Boolean) {
        _problems.value = _problems.value?.mapIndexed { index, problem ->
            if (index == currentProblemIndex) {
                problem.copy(
                    status = if (isCorrect) "맞은 문제" else "틀린 문제",
                    correctCount = if (isCorrect) problem.correctCount + 1 else problem.correctCount,
                    incorrectCount = if (!isCorrect) problem.incorrectCount + 1 else problem.incorrectCount
                )
            } else {
                problem
            }
        }
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

    fun incrementRightSwipe() {
        rightSwipeCount++
    }

    fun incrementLeftSwipe() {
        leftSwipeCount++
    }

    fun getRightSwipeCount(): Int {
        return rightSwipeCount
    }

    fun getLeftSwipeCount(): Int {
        return leftSwipeCount
    }

    fun getCorrectProblemCount(): Int {
        return _problems.value?.count { it.status == "맞은 문제" } ?: 0
    }


    fun resetSwipeCounts() {
        rightSwipeCount = 0
        leftSwipeCount = 0
    }
}