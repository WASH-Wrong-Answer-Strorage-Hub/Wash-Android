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

        val imageUrl = arguments?.getString("image_url")

        Glide.with(this).load(imageUrl).into(binding.ivFullscreenImage)

        binding.btnCloseFullscreen.setOnClickListener {
            navController.popBackStack()
        }
    }
}
