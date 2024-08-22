package com.wash.washandroid.presentation.fragment.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemCategoryBtnBinding
import com.wash.washandroid.model.CategoryType
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryChapterViewModel

class CategoryChapterAdapter(
    var categoryTypes: List<CategoryType>,
    private val viewModel: CategoryChapterViewModel
) : RecyclerView.Adapter<CategoryChapterAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class CategoryViewHolder(private val binding: ItemCategoryBtnBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryType: CategoryType) {
            binding.categoryBtn.text = categoryType.typeName
            binding.categoryBtn.setBackgroundResource(viewModel.getButtonBackground(categoryType.typeId))

            binding.categoryBtn.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition
                viewModel.onButtonClicked(categoryType.typeId)
                notifyItemChanged(previousSelectedPosition)  // 이전에 선택된 항목을 갱신
                notifyItemChanged(selectedPosition)          // 현재 선택된 항목을 갱신
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBtnBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryTypes[position])
    }

    override fun getItemCount(): Int = categoryTypes.size
}
