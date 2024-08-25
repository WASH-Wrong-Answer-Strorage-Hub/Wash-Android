package com.wash.washandroid.presentation.fragment.category.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.ItemCategoryBtnBinding
import com.wash.washandroid.model.Folder
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel

class CategoryFolderAdapter(
    var folderTypes: List<Folder>,
    private val viewModel: CategoryFolderViewModel
) : RecyclerView.Adapter<CategoryFolderAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class CategoryViewHolder(private val binding: ItemCategoryBtnBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryType: Folder) {
            binding.categoryBtn.text = categoryType.folderName
            Log.d("folderName", categoryType.folderName)
            binding.categoryBtn.setBackgroundResource(viewModel.getButtonBackground(categoryType.folderId))

            binding.categoryBtn.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition
                viewModel.onButtonClicked(categoryType.folderId)
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
        holder.bind(folderTypes[position])
    }

    override fun getItemCount(): Int = folderTypes.size
}
