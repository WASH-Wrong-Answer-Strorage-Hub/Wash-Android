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
import com.wash.washandroid.databinding.FragmentSubscribeBinding

class SubscribeFragment: Fragment() {

    private var _binding: FragmentSubscribeBinding? = null

    private val binding get() = _binding!!

    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubscribeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.terminateSubscribeBtn.setOnClickListener {
            mypageViewModel.setSubscribed(false)
            findNavController().navigate(R.id.navigation_subscribe_menu)
        }

        binding.subscribeBackBtn.setOnClickListener {
            findNavController().navigate(R.id.navigation_subscribe_menu)
        }

    }
}