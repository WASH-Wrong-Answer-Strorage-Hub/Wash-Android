package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.wash.washandroid.databinding.FragmentStudyFullScreenImageBinding

public class StudyFullScreenImageFragment : Fragment() {
    private lateinit var binding: FragmentStudyFullScreenImageBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudyFullScreenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // 이미지 URI 받기
        val imageUrl = arguments?.getString("image_url")

        // Glide로 이미지 로드
        Glide.with(this).load(imageUrl).into(binding.ivFullscreenImage)

        // 닫기 버튼 클릭 시 현재 프래그먼트 닫기
        binding.btnCloseFullscreen.setOnClickListener {
            navController.popBackStack()
        }
    }
}
