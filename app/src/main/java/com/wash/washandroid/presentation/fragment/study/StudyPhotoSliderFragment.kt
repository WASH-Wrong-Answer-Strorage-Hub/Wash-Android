package com.wash.washandroid.presentation.fragment.study

import MypageViewModel
import android.content.Context
import android.content.SharedPreferences
import com.wash.washandroid.presentation.fragment.problem.PhotoSliderAdapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.wash.washandroid.databinding.FragmentPhotoSliderBinding
import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository

class StudyPhotoSliderFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentPhotoSliderBinding? = null
    private val binding: FragmentPhotoSliderBinding
        get() = requireNotNull(_binding) { "FragmentPhotoSliderBinding -> null" }

    private lateinit var viewModel: StudyViewModel
    private lateinit var myPageViewModel: MypageViewModel
    private lateinit var repository: StudyRepository
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoSliderBinding.inflate(inflater, container, false)

        // SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)

        // Repository 초기화
        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        // ViewModel 초기화
        myPageViewModel = ViewModelProvider(requireActivity()).get(MypageViewModel::class.java)
        val factory = StudyViewModelFactory(repository, sharedPreferences, myPageViewModel)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        val savedUris = viewModel.loadPhotoUrisFromPreferences()

        Log.d("fraglog", "Loaded photo URIs from SharedPreferences in PhotoSliderFragment: $savedUris")

        val initialPosition = viewModel.selectedPhotoPosition.value ?: 0

        if (savedUris.isNotEmpty()) {
            val adapter = PhotoSliderAdapter(savedUris)
            binding.viewPager.adapter = adapter
            binding.dotsIndicator.attachTo(binding.viewPager)

            binding.viewPager.post {
                binding.viewPager.currentItem = initialPosition
                Log.d("initialPosition", "$initialPosition")
            }
        } else {
            Log.e("fraglog", "No photo URIs found in SharedPreferences")
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