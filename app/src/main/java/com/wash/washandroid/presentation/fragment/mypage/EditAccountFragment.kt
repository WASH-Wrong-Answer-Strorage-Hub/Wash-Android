package com.wash.washandroid.presentation.fragment.mypage

import MypageViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentEditAccountBinding

class EditAccountFragment : Fragment() {

    private var _binding: FragmentEditAccountBinding? = null
    private val binding get() = _binding!!

    // ViewModel을 activityViewModels()를 사용하여 공유 ViewModel 가져오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel에서 닉네임을 가져와서 TextView에 설정
        mypageViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.editAccountNicknameTv.text = nickname
        }

        // 이름과 이메일 불러오기 (이 정보를 SocialLoginFragment에서 가져오거나 ViewModel을 통해 관리해야 함)
        // 예시: 만약 ViewModel에 이름과 이메일이 저장되어 있다면
        mypageViewModel.name.observe(viewLifecycleOwner) { name ->
            binding.editAccountNameTv.text = name
        }

        mypageViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.editLinkedAccountTv.text = email
        }

        binding.editAccountBackBtn.setOnClickListener {
            findNavController().navigate(R.id.navigation_mypage)
        }

        binding.editAccountNicknameBtn.setOnClickListener {
            findNavController().navigate(R.id.navigation_edit_nickname)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
