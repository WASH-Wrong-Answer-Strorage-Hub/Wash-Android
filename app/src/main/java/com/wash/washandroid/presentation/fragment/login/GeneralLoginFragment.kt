package com.wash.washandroid.presentation.fragment.login

import MypageViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentGeneralLoginBinding
import com.wash.washandroid.databinding.FragmentSocialLoginBinding
import com.wash.washandroid.presentation.base.MainActivity

class GeneralLoginFragment: Fragment() {

    private var _binding: FragmentGeneralLoginBinding? = null
    private val binding get() = _binding!!

    // ViewModel을 activityViewModels()를 사용하여 공유 ViewModel 가져오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bottom Navigation 숨기기
        (activity as? MainActivity)?.hideBottomNavigation(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generalLoginFinish.setOnClickListener {
            val email = binding.generalLoginTv.text.toString()
            val password = binding.generalLoginPasswordTv.text.toString()
            mypageViewModel.generalLoginUser(email, password)
            (activity as? MainActivity)?.hideBottomNavigation(false)
            findNavController().navigate(R.id.navigation_home)
        }
    }
}