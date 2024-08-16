package com.wash.washandroid.presentation.fragment.study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

class StudyProgressAdapter(private var progressList: List<StudyProblem>) :
    RecyclerView.Adapter<StudyProgressAdapter.ProgressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.study_item_drawer, parent, false)
        return ProgressViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val currentItem = progressList[position]
        holder.problemNumberTextView.text = currentItem.problemId.toString()

        // 문제 상태에 따른 배경 설정
        when (currentItem.status) {
            "맞은 문제" -> {
                holder.problemStatusImageView.setImageResource(R.drawable.study_sm_correct)
            }

            "틀린 문제" -> {
                holder.problemStatusImageView.setImageResource(R.drawable.study_sm_incorrect)
            }

            else -> { // "미완료"
                holder.problemStatusImageView.setImageResource(R.drawable.study_sm_incomplete)
            }
        }
    }

    override fun getItemCount(): Int = progressList.size

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val problemNumberTextView: TextView = itemView.findViewById(R.id.tv_problem_number)
        val problemStatusImageView: ImageView = itemView.findViewById(R.id.iv_problem_status)
    }

    fun updateProgressList(newList: List<StudyProblem>) {
        progressList = newList
        notifyDataSetChanged()
    }
}
