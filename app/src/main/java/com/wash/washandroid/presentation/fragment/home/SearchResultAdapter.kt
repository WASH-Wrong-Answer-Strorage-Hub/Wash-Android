package com.wash.washandroid.presentation.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R


data class SearchResultItem(
    var folderId: Int,
    var title: String,
    val imageResId: Int
)

class SearchResultAdapter(
    private var items: List<SearchResultItem>,
    private val onItemClick: (Int) -> Unit,
    private val onDeleteIconClick: (Int) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchText: TextView = itemView.findViewById(R.id.search_text)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIconImageView)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
            deleteIcon.setOnClickListener {
                onDeleteIconClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.searchText.text = item.title // 여기서 title을 사용
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<SearchResultItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    // 새로 추가된 메서드
    fun getItem(position: Int): SearchResultItem {
        return items[position]
    }
}
