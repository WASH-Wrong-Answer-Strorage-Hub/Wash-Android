package com.wash.washandroid.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R

class ImageAdapter(private val itemCount: Int, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)

        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition) //클릭 이벤트 리스너
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_home_detail_recye_img, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // Configure ImageView if needed
    }

    override fun getItemCount(): Int {
        return itemCount
    }
}
