package com.wash.washandroid.presentation.fragment.study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder

class FolderAdapter(
    private var folders: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.study_folder_item, parent, false)
        return FolderViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folderName = folders[position]
        holder.bind(folderName)
    }

    override fun getItemCount() = folders.size

    fun updateFolders(newFolders: List<String>) {
        this.folders = newFolders
        notifyDataSetChanged()
    }

    class FolderViewHolder(itemView: View, private val onItemClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val folderNameTextView: TextView = itemView.findViewById(R.id.tv_study_folder_name)

        fun bind(folderName: String) {
            // TextView에 텍스트 설정
            folderNameTextView.text = folderName

            // 아이템 클릭 시 TextView의 텍스트 추출
            itemView.setOnClickListener {
                val clickedFolderName = folderNameTextView.text.toString()
                onItemClick(clickedFolderName)  // 추출한 텍스트를 onItemClick 함수에 전달
            }
        }
    }
}
