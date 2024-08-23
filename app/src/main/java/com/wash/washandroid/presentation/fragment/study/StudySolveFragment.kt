package com.wash.washandroid.presentation.fragment.study

import MypageViewModel
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudySolveBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository

class StudySolveFragment : Fragment() {
    private var _binding: FragmentStudySolveBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var viewModel: StudyViewModel
    private lateinit var repository: StudyRepository
    private lateinit var folderId: String
    private lateinit var folderName: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudySolveBinding.inflate(inflater, container, false)

        folderId = arguments?.getInt("folderId").toString()
        folderName = arguments?.getString("folderName") ?: "folderName"

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        sharedPreferences = requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)

        val factory = StudyViewModelFactory(repository, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStudySolveTitle.text = folderName
        navController = Navigation.findNavController(view)

        (activity as MainActivity).hideBottomNavigation(true)

        setupRecyclerView()
        observeViewModel()

        // 문제 해결 여부에 따른 문제 이동
        if (viewModel.isProblemAlreadySolved) {
            Log.d("fraglog", "Problem already solved, moving to next problem")
            viewModel.isProblemAlreadySolved = false // 플래그를 초기화
            viewModel.moveToNextProblem(folderId)
        } else {
            // 문제가 해결되지 않은 경우 현재 문제 로드
            viewModel.loadStudyProblem(folderId)
        }

        binding.ivLeftArrow.setOnClickListener {
            viewModel.moveToPreviousProblem(folderId)
        }

        binding.ivRightArrow.setOnClickListener {
            viewModel.moveToNextProblem(folderId)
        }

        binding.studySolveBtnAnswer.setOnClickListener {
            navigateToAnswerFragment()
        }

        binding.ivDrawer.setOnClickListener {
            binding.studyDrawerLayout.openDrawer(GravityCompat.END)
        }

        binding.btnRvDrawerFinish.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_complete)
        }

        binding.studySolveBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study)
        }
    }

    private fun setupRecyclerView() {
        binding.rvDrawerProgress.layoutManager = LinearLayoutManager(requireContext())
        val problemIds = viewModel.loadProblemIdsFromPreferences(sharedPreferences)
        val progressAdapter = StudyProgressAdapter(problemIds.map { it to "미완료" }, problemIds)
        binding.rvDrawerProgress.adapter = progressAdapter

        viewModel.studyProgress.observe(viewLifecycleOwner, Observer { progressList ->
            progressAdapter.updateProgressList(progressList)
        })
    }

    private fun observeViewModel() {
        viewModel.studyProblem.observe(viewLifecycleOwner, Observer { studyProblemResponse ->
            studyProblemResponse?.let {
                updateUI(it)
            } ?: Log.e("fraglog", "Study problem is null, cannot update UI")
        })
    }

    private fun updateUI(problem: StudyProblemResponse) {
        binding.tvStudySolveProblemId.text = "문제 " + (viewModel.currentProblemIndex + 1)
        val imageUrl = problem.result.problemImage.takeIf { it.isNotBlank() }
            ?: "https://samtoring.com/qstn/NwXVS1yaHZ1xav2YsqAf.png"
        Glide.with(this).load(imageUrl).into(binding.ivSolveCard)
    }

    private fun navigateToAnswerFragment() {
        val currentProblem = viewModel.studyProblem.value
        val bundle = bundleOf(
            "folderId" to folderId,
            "problemId" to currentProblem?.result?.problemId,
            "answer" to currentProblem?.result?.answer
        )
        navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_answer, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}