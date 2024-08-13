package com.wash.washandroid.presentation.fragment.problem

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.databinding.ItemPhotoFullscreenBinding

class PhotoSliderAdapter(
    private val photoUris: List<String>
) : RecyclerView.Adapter<PhotoSliderAdapter.PhotoPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPagerViewHolder {
        val binding = ItemPhotoFullscreenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoPagerViewHolder, position: Int) {
        holder.bind(photoUris[position])
    }

    override fun getItemCount(): Int = photoUris.size

    inner class PhotoPagerViewHolder(private val binding: ItemPhotoFullscreenBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoUri: String) {
            Glide.with(binding.root.context)
                .load(Uri.parse(photoUri))
                .into(binding.fullscreenImage)
        }
    }
}