package com.wash.washandroid.presentation.fragment.study

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder

class FolderAdapter(
    private var folders: List<StudyFolder>,
    private val onClick: (StudyFolder) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.study_folder_item, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.bind(folder, position + 1) // position+1을 넘겨서 번호를 설정
        holder.itemView.setOnClickListener { onClick(folder) }
    }

    override fun getItemCount() = folders.size

    fun updateFolders(newFolders: List<StudyFolder>) {
        this.folders = newFolders
        notifyDataSetChanged()
    }

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(folder: StudyFolder, position: Int) {
            itemView.findViewById<TextView>(R.id.tv_study_folder_name).text = "${folder.folderName}"
        }
    }
}
