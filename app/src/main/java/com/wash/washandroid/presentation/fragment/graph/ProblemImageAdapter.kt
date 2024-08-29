package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.ItemProblemBinding

data class Problem(val id: Int, val imageUrl: String)

class ProblemImageAdapter(
    private val problemList: List<Problem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ProblemImageAdapter.ProblemViewHolder>() {

    class ProblemViewHolder(private val binding: ItemProblemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(problem: Problem, onItemClick: (Int) -> Unit) {
            if (problem.imageUrl.isNotEmpty()) {
                // URL이 유효한 경우 Glide로 이미지 로드
                Glide.with(binding.problemImageView.context)
                    .load(problem.imageUrl)
                    .into(binding.problemImageView)
            } else {
                // URL이 비어 있거나 유효하지 않은 경우 임시 이미지 로드
                binding.problemImageView.setImageResource(R.drawable.temporary_img_test) //임시 이미지
            }

            binding.root.setOnClickListener {
                onItemClick(problem.id)
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
