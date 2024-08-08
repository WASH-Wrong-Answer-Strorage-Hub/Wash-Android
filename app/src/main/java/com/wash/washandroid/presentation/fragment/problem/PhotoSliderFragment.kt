package com.wash.washandroid.presentation.fragment.problem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.wash.washandroid.databinding.FragmentPhotoSliderBinding

class PhotoSliderFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentPhotoSliderBinding? = null
    private val binding: FragmentPhotoSliderBinding
        get() = requireNotNull(_binding) { "FragmentPhotoSliderBinding -> null" }

    private val problemInfoViewModel: ProblemInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoSliderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        val photoUris = problemInfoViewModel.photoUris.value ?: emptyList()
        val initialPosition = problemInfoViewModel.selectedPhotoPosition.value ?: 0

        val adapter = PhotoSliderAdapter(photoUris)
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        // 뷰페이저 어댑터 설정 후 초기 위치 설정
        binding.viewPager.post {
            binding.viewPager.currentItem = initialPosition

            Log.d("initialPosition", "$initialPosition")
        }

        binding.closeButton.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}