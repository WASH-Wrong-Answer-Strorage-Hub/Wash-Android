package com.wash.washandroid.presentation.fragment.study.data

import com.wash.washandroid.presentation.fragment.study.data.model.response.FolderInfo
import com.wash.washandroid.presentation.fragment.study.data.model.response.ProblemResult
import com.wash.washandroid.presentation.fragment.study.data.model.response.ProblemStatistics
import com.wash.washandroid.presentation.fragment.study.data.model.response.ProblemStatus
import com.wash.washandroid.presentation.fragment.study.data.model.response.ProblemType
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderIdResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyFolderResult
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProgressResponse

// StudyFolderResponse 더미 데이터
val dummyStudyFolderResponse = StudyFolderResponse(
    isSuccess = true,
    code = 2000,
    message = "Success",
    result = listOf(
        FolderInfo(folderId = 1, folderName = "수학", orderValue = 1),
        FolderInfo(folderId = 2, folderName = "영어", orderValue = 2),
        FolderInfo(folderId = 3, folderName = "과학", orderValue = 3),
        FolderInfo(folderId = 4, folderName = "사회", orderValue = 4)
    )
)

// StudyFolderIdResponse 더미 데이터
val dummyStudyFolderIdResponse = StudyFolderIdResponse(
    isSuccess = true,
    code = 2000,
    message = "Success",
    result = StudyFolderResult(
        message = "폴더 선택 완료",
        folderName = "수학",
        problemIds = listOf("101", "102", "103", "104")
    )
)

// StudyProblemResponse 더미 데이터
val dummyStudyProblemResponse = StudyProblemResponse(
    isSuccess = true,
    code = 2000,
    message = "Success",
    result = ProblemResult(
        problemId = "101",
        folderName = "수학",
        answer = "[5]",
        problemImage = "https://samtoring.com/qstn/RqY2E25VxNwI75nUzGzt.png",
        solutionImages = listOf("https://example.com/solution_image1.png"),
        passageImages = listOf("https://img.animalplanet.co.kr/news/2020/05/20/700/al43zzl8j3o72bkbux29.jpg"),
        additionalProblemImages = listOf("https://example.com/additional_image1.png"),
        problemText = "이 문제의 정답은 무엇인가요?",
        gptSessionKey = null,
        problemType = ProblemType(
            대분류 = "수학",
            중분류 = "대수학",
            소분류 = "방정식"
        ),
        statistics = ProblemStatistics(
            wrongCount = 10,
            solvedCount = 50
        )
    )
)

// StudyProgressResponse 더미 데이터
val dummyStudyProgressResponse = StudyProgressResponse(
    isSuccess = true,
    code = 2000,
    message = "Success",
    result = listOf(
        ProblemStatus(problemId = "101", status = "맞은 문제"),
        ProblemStatus(problemId = "102", status = "틀린 문제"),
        ProblemStatus(problemId = "103", status = "미완료"),
        ProblemStatus(problemId = "104", status = "맞은 문제")
    )
)

