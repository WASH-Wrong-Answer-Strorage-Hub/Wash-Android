package com.wash.washandroid.presentation.fragment.problem.add

import android.util.Log
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData

object ProblemManager {
    private val problemDatas = mutableListOf<ProblemData>()

    fun addProblemData(problemData: ProblemData) {
        problemDatas.add(problemData)
        Log.d("problemData", problemData.toString())
        Log.d("problemDatas", problemDatas.toString())
    }

    fun updateMainTypeProblemData(index: Int, mainTypeId: Int?) {
        if (index < problemDatas.size) {
            val existingData = problemDatas[index]
            val updatedData = existingData.copy(mainTypeId = mainTypeId)
            problemDatas[index] = updatedData
            Log.d("problemData", "Updated at index $index: $updatedData")
        } else {
            Log.e("problemData", "Index out of bounds")
        }
    }

    fun updateMidTypeProblemData(index: Int, midTypeId: Int?) {
        if (index < problemDatas.size) {
            val existingData = problemDatas[index]
            val updatedData = existingData.copy(midTypeId = midTypeId)
            problemDatas[index] = updatedData
            Log.d("problemData", "Updated at index $index: $updatedData")
        } else {
            Log.e("problemData", "Index out of bounds")
        }
    }

    fun updateSubTypeProblemData(index: Int, subTypeIds: List<Int?>?) {
        if (index < problemDatas.size) {
            val existingData = problemDatas[index]
            val updatedData = existingData.copy(subTypeIds = subTypeIds)
            problemDatas[index] = updatedData
            Log.d("problemData", "Updated at index $index: $updatedData")
        } else {
            Log.e("problemData", "Index out of bounds")
        }
    }

    fun getProblems(): List<ProblemData> {
        Log.d("ProblemManager", problemDatas.toString())
        return problemDatas
    }

    fun clearProblems() {
        problemDatas.clear()
    }
}