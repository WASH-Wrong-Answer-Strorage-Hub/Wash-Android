package com.wash.washandroid.presentation.fragment.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentChatBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.utils.ChatItemDecoration

class ChatFragment : Fragment() {

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        setupKeyboardListener()

        chatAdapter = ChatAdapter(emptyList())
        binding.chatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        val itemSpace = resources.getDimensionPixelSize(R.dimen.item_space)
        binding.chatRv.addItemDecoration(ChatItemDecoration(itemSpace))

        chatViewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            Log.d("ChatFragment", "글자 업데이트 완료: $messages")
            chatAdapter = ChatAdapter(messages)
            binding.chatRv.adapter = chatAdapter
            binding.chatRv.scrollToPosition(messages.size - 1)
        })

        binding.chatMessageSendBtn.setOnClickListener {
            Log.d("ChatFragment", "전송 버튼 클릭됨")
            chatViewModel.sendMessage()
            binding.chatMessageInput.setText("")
            binding.chatMessageSendBtn.setImageResource(R.drawable.chat_message_unsend)
        }

        binding.chatMessageInput.doAfterTextChanged {
            chatViewModel.message.value = it.toString()
            binding.chatMessageSendBtn.setImageResource(R.drawable.chat_message_send)
            Log.d("ChatFragment", "글자 변화 감지: ${it.toString()}")
        }
    }

    private fun setupKeyboardListener() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = binding.root.rootView.height - binding.root.height
            if (heightDiff > 300) { // 키보드가 올라온 경우
                binding.chatRv.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }
}
