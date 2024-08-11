package com.wash.washandroid.presentation.fragment.problem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.ItemPhotoAddBtnBinding
import com.wash.washandroid.databinding.ItemPhotoBinding

class PhotoAdapter(
    private val context: Context,
    private val photoList: MutableList<String>,
    private val onAddPhotoClick: () -> Unit,
    private val onPhotoClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PHOTO = 1
        private const val VIEW_TYPE_ADD_BUTTON = 2
    }

    // Adapter에서 버튼의 표시 여부를 제어하기 위한 변수
    private var isAddButtonVisible = false

    override fun getItemViewType(position: Int): Int {
        return if (position == photoList.size) VIEW_TYPE_ADD_BUTTON else VIEW_TYPE_PHOTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_PHOTO) {
            val binding = ItemPhotoBinding.inflate(LayoutInflater.from(context), parent, false)
            PhotoViewHolder(binding)
        } else {
            val binding = ItemPhotoAddBtnBinding.inflate(LayoutInflater.from(context), parent, false)
            AddPhotoViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoViewHolder) {
            holder.bind(photoList[position])
        } else if (holder is AddPhotoViewHolder) {
            holder.bind(photoList.size)
            holder.itemView.visibility = if (isAddButtonVisible) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener { if (isAddButtonVisible) onAddPhotoClick() }
        }
    }

    override fun getItemCount(): Int {
        return photoList.size + 1
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < photoList.size) {
            photoList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, photoList.size) // 아이템을 제거한 이후의 아이템들에 대해 포지션 업데이트
            notifyItemChanged(photoList.size)
        }
    }

    fun setAddButtonVisible(visible: Boolean) {
        isAddButtonVisible = visible
        notifyItemChanged(photoList.size)
        for (i in 0 until photoList.size) {
            notifyItemChanged(i)
        }
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photoUri: String) {
            Glide.with(context)
                .load(photoUri)
                .into(binding.photoIv)

            binding.photoIv.clipToOutline = true

            if (!isAddButtonVisible) {
                binding.photoIv.setOnClickListener {
                    onPhotoClick(adapterPosition)
                }
            }

            binding.photoDeleteLayout.visibility = if (isAddButtonVisible) View.VISIBLE else View.GONE
            binding.photoDeleteLayout.setOnClickListener {
                removeItem(this.layoutPosition)
            }
        }
    }

    inner class AddPhotoViewHolder(private val binding: ItemPhotoAddBtnBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photoCount: Int) {
            binding.photoCount.text = photoCount.toString()

            // 글자색 설정
            if (photoCount > 0) {
                binding.photoCount.setTextColor(context.getColor(R.color.main))
            } else {
                binding.photoCount.setTextColor(context.getColor(R.color.dark_gray))
            }
        }
    }
}