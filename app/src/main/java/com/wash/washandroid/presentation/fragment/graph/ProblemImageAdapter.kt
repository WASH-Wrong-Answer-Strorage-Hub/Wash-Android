package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.databinding.ItemProblemBinding

data class Problem(val id: Int, val imageUrl: String)

class ProblemImageAdapter(
    private val problemList: List<Problem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ProblemImageAdapter.ProblemViewHolder>() {

    class ProblemViewHolder(private val binding: ItemProblemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(problem: Problem, onItemClick: (Int) -> Unit) {
            Glide.with(binding.problemImageView.context)
                .load(problem.imageUrl)
                .into(binding.problemImageView)
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
