package com.wash.washandroid.presentation.fragment.study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

class FolderAdapter(
    private val folders: List<StudyFolder>,
    private val onClick: (StudyFolder) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.study_folder_item, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount() = folders.size

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderNameTextView: TextView = itemView.findViewById(R.id.tv_study_folder_name)

        fun bind(folder: StudyFolder) {
            folderNameTextView.text = folder.folderName
            itemView.setOnClickListener { onClick(folder) }
        }
    }
}
