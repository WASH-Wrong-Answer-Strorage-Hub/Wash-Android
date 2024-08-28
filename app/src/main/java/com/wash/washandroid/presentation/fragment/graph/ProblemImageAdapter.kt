package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.databinding.ItemProblemBinding

// 데이터를 나타내는 클래스
data class Problem(val id: Int, val imageResId: Int)

class ProblemImageAdapter(
    private val problemList: List<Problem>,
    private val onItemClick: (Int) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<ProblemImageAdapter.ProblemViewHolder>() {

    class ProblemViewHolder(private val binding: ItemProblemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(problem: Problem, onItemClick: (Int) -> Unit) {
            binding.problemImageView.setImageResource(problem.imageResId)
            /*Glide.with(binding.problemImageView.context)
                .load(problem.imageResId)
                .into(binding.problemImageView)

             */
            binding.root.setOnClickListener {
                onItemClick(problem.id) // 클릭 시 아이디 전달
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val binding = ItemProblemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProblemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        holder.bind(problemList[position], onItemClick)
    }

    override fun getItemCount(): Int = problemList.size
}
