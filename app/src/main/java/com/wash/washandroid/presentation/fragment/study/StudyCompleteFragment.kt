package com.wash.washandroid.presentation.fragment.study

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.airbnb.lottie.LottieAnimationView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyCompleteBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository

class StudyCompleteFragment : Fragment() {
    private var _binding: FragmentStudyCompleteBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var viewModel: StudyViewModel
    private lateinit var repository: StudyRepository
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var folderId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyCompleteBinding.inflate(inflater, container, false)

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)
        val sharedPreferences = requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)

        val factory = StudyViewModelFactory(repository, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        folderId = arguments?.getString("folderId").toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        viewModel.loadStudyProgress(folderId)
        viewModel.studyProgress.observe(viewLifecycleOwner, Observer { progressList ->
            if (!progressList.isNullOrEmpty()) {
                val totalProblems = progressList.size
                val totalCorrectProblems = viewModel.getCorrectProblemCount()

                binding.tvStudyComplete3.text = "총 ${totalProblems}문제 중\n${totalCorrectProblems}문제를 맞췄습니다."

                Log.d("fraglog", "Total problems: $totalProblems, Correct problems: $totalCorrectProblems")
            } else {
                binding.tvStudyComplete3.text = "문제 데이터를 불러올 수 없습니다."
                Log.e("fraglog", "Progress list is empty")
            }
        })

        // animation 초기화
        lottieAnimationView = binding.studyCompleteAnimation

        binding.ivStudyConfetti.visibility = View.GONE

        lottieAnimationView.setAnimation(R.raw.study_complete_animation)
        lottieAnimationView.repeatCount = 0
        lottieAnimationView.playAnimation()

        // animation 끝났을 때 리스너 설정
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                // 애니메이션 끝났을 때 이미지 표시
                binding.ivStudyConfetti.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })

        binding.btnStudyBackHome.setOnClickListener {
            navController.navigate(
                R.id.action_navigation_study_complete_to_navigation_home, null, NavOptions.Builder().setPopUpTo(R.id.navigation_home, false).build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lottieAnimationView.pauseAnimation()
        _binding = null
    }
}