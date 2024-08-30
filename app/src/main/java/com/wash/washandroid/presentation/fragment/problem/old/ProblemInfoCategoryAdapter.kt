package com.wash.washandroid.presentation.fragment.problem.old

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

class ProblemInfoCategoryAdapter(private var categories: List<String>) :
    RecyclerView.Adapter<ProblemInfoCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_problem_info_category, parent, false)
        return CategoryViewHolder(view as TextView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.textView.text = categories[position]

        when (position) {
            0 -> {
                holder.textView.setBackgroundResource(R.drawable.problem_info_category_subject_background) // 대분류: 녹색
            }
            1 -> {
                holder.textView.setBackgroundResource(R.drawable.problem_info_category_subfield_background) // 중분류: 청록색
            }
            else -> {
                holder.textView.setBackgroundResource(R.drawable.problem_info_category_chapter_background) // 소분류: 연두색
            }
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<String>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    class CategoryViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}