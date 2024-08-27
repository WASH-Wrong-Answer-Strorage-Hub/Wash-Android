package com.wash.washandroid.presentation.fragment.study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder
import com.wash.washandroid.presentation.fragment.study.data.model.response.FolderInfo

class FolderAdapter(
    private var folders: List<FolderInfo>, private val onItemClick: (Int, String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.study_folder_item, parent, false)
        return FolderViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.bind(folder)
    }

    override fun getItemCount() = folders.size

    fun updateFolders(newFolders: List<FolderInfo>) {
        this.folders = newFolders
        notifyDataSetChanged()
    }

    class FolderViewHolder(itemView: View, private val onItemClick: (Int, String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val folderNameTextView: TextView = itemView.findViewById(R.id.tv_study_folder_name)

        fun bind(folder: FolderInfo) {
            // TextView에 텍스트 설정
            folderNameTextView.text = folder.folderName

            // 아이템 클릭 시 folderId, folderName 반환
            itemView.setOnClickListener {
                onItemClick(folder.folderId, folder.folderName)
            }
        }
    }
}
