package com.wash.washandroid.presentation.fragment.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.ItemChatBinding
import com.wash.washandroid.model.ChatItemModels

class ChatAdapter(
    private val messages: List<ChatItemModels>,
    private val userProfileImageUrl: String?
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position], userProfileImageUrl)

        val context = holder.itemView.context
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatItemModels, userProfileImageUrl: String?) {
            binding.sender.text = chatMessage.sender
            binding.textViewMessage.text = chatMessage.content

            // 이미지 로딩 처리
            if (chatMessage.sender == "ChatGPT") {
                binding.senderProfile.setImageResource(R.drawable.open_ai) // ChatGPT 기본 이미지 설정
            } else {
                userProfileImageUrl?.let {
                    Glide.with(binding.senderProfile.context)
                        .load(it)
                        .placeholder(R.drawable.open_ai) // 기본 이미지
                        .error(R.drawable.open_ai) // 에러 이미지
                        .into(binding.senderProfile)
                }
            }
        }
    }
}
