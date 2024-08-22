package com.wash.washandroid.presentation.fragment.mypage

import MypageViewModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentSubscribeMenuBinding

class SubscribeMenuFragment: Fragment() {

    private var _binding: FragmentSubscribeMenuBinding? = null

    private val binding get() = _binding!!

    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubscribeMenuBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.subscribeBtn.setOnClickListener {
            mypageViewModel.approveSubscription()
            findNavController().navigate(R.id.navigation_subscribe)
        }

        binding.subscribeMenuBackBtn.setOnClickListener {
            findNavController().navigate(R.id.navigation_mypage)
        }

    }
}