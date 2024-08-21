package com.wash.washandroid.presentation.fragment.mypage

import MypageViewModel
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentEditNicknameBinding

class EditNicknameFragment : Fragment() {

    private var _binding: FragmentEditNicknameBinding? = null
    private val binding get() = _binding!!

    // ViewModel을 activity 범위에서 공유
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel에서 닉네임을 관찰하고 UI를 업데이트
        mypageViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            if (binding.editNicknameTv.text.toString() != nickname) {
                binding.editNicknameTv.setText(nickname)
            }
        }

        binding.editNicknameBtn.setOnClickListener {
            val newNickname = binding.editNicknameTv.text.toString()
            mypageViewModel.setNickname(newNickname) // ViewModel에 닉네임 설정
            hideKeyboard()
        }

        // Back button 클릭 시 이동
        binding.editNicknameBackBtn.setOnClickListener {
            findNavController().navigate(R.id.navigation_edit_account)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}