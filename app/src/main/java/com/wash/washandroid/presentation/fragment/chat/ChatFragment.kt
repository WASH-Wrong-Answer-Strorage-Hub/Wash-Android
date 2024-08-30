package com.wash.washandroid.presentation.fragment.chat

import MypageViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentChatBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.problem.old.ProblemInfoViewModel
import com.wash.washandroid.utils.ChatItemDecoration

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var navController: NavController
    private val problemInfoModel: ProblemInfoViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }
    private val myPageViewModel: MypageViewModel by activityViewModels()


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
        navController = Navigation.findNavController(view)

        binding.categoryBackBtn.setOnClickListener {
            chatViewModel.clearChatMessages()
            navController.navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            chatViewModel.clearChatMessages()
            findNavController().navigateUp()
        }

        chatViewModel.findOrCreateChatRoom()

        setupKeyboardListener()

        val userProfileImageUrl = myPageViewModel.profileImageUrl.value

        chatAdapter = ChatAdapter(emptyList(), userProfileImageUrl)
        binding.chatRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        val itemSpace = resources.getDimensionPixelSize(R.dimen.item_space)
        binding.chatRv.addItemDecoration(ChatItemDecoration(itemSpace))

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            Log.d("ChatFragment", "글자 업데이트 완료: $messages")
            chatAdapter = ChatAdapter(messages, userProfileImageUrl)
            binding.chatRv.adapter = chatAdapter
            binding.chatRv.scrollToPosition(messages.size - 1)
        }

        binding.aiCommand.setOnClickListener {
            problemInfoModel.problemText.observe(viewLifecycleOwner) { problemText ->
                problemText?.let {
                    val aiCommand = "다음 문제의 자세한 풀이방법을 알려줘: $it"
                    chatViewModel.message.value = aiCommand
                    chatViewModel.sendMessage()
                }
            }
        }

        binding.chatMessageSendBtn.setOnClickListener {
            Log.d("ChatFragment", "전송 버튼 클릭됨")

            // 채팅 내역 저장을 위한 사용자의 질문을 서버에 저장
            chatViewModel.addChatMessage(chatViewModel.message.value.toString(), true)
            Log.d("chatViewModel", chatViewModel.message.value.toString())

            // GPT 답변을 얻기 위한 openAI 서버에 질문 전송
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
