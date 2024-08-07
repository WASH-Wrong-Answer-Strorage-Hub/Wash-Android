package com.wash.washandroid.presentation.fragment.problem

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

class ProblemInfoCategoryAdapter(private val categories: List<String>) :
    RecyclerView.Adapter<ProblemInfoCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_problem_info_category, parent, false)
        return CategoryViewHolder(view as TextView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.textView.text = categories[position]
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}