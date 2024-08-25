package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemProblemBinding

// 데이터를 나타내는 클래스
data class Problem(val id: Int, val imageResId: Int)

class ProblemImageAdapter(private val problemList: List<Problem>) :
    RecyclerView.Adapter<ProblemImageAdapter.ProblemViewHolder>() {

    // ViewHolder 클래스
    class ProblemViewHolder(private val binding: ItemProblemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(problem: Problem) {
            // 이미지 바인딩
            binding.problemImageView.setImageResource(problem.imageResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val binding = ItemProblemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProblemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        holder.bind(problemList[position])
    }

    override fun getItemCount(): Int = problemList.size
}
