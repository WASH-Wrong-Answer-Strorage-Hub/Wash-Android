package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudySolveBinding
import com.wash.washandroid.presentation.base.MainActivity

class StudySolveFragment : Fragment() {
    private var _binding: FragmentStudySolveBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var viewModel: StudyViewModel
    private lateinit var folderName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudySolveBinding.inflate(inflater, container, false)

        // 폴더명 수신
        folderName = arguments?.getString("folderName") ?: "folderName"

        // ViewModel 인스턴스 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(StudyViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStudySolveTitle.text = folderName

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        // recycler view 설정
        binding.rvDrawerProgress.layoutManager = LinearLayoutManager(requireContext())
        val progressAdapter = StudyProgressAdapter(emptyList())
        binding.rvDrawerProgress.adapter = progressAdapter


        viewModel.loadProblems()

        // 문제 데이터 업데이트 시 UI 업데이트
        viewModel.problems.observe(viewLifecycleOwner, Observer { problemList ->

            // 문제가 비어있지 않으면 현재 문제를 표시
            if (problemList.isNotEmpty()) {
                updateUI(problemList)

                // recycler view에 문제 리스트 갱신
                progressAdapter.updateProgressList(problemList)
//                Log.d("fraglog", "Progress list updated with size: ${problemList.size}")
            } else {
//                Log.d("fraglog", "Problem list is empty")
            }
        })

        // 초기화가 필요하지 않을 경우 문제 상태 유지
        if (viewModel.isResetRequired) {
            viewModel.resetProblemsStatus() // 문제 상태 초기화
        }

        // 지문 보기
        binding.studySolveBtnDes.setOnClickListener {
            if (binding.ivSolveCardDes.visibility == View.VISIBLE) {
                binding.ivSolveCardDes.visibility = View.GONE
            } else {
                binding.ivSolveCardDes.visibility = View.VISIBLE
            }
        }

        // 왼쪽 화살표 클릭 시 이전 문제로 이동
        binding.ivLeftArrow.setOnClickListener {
            viewModel.moveToPreviousProblem()
            updateUI(viewModel.problems.value ?: emptyList())
        }

        // 오른쪽 화살표 클릭 시 다음 문제로 이동
        binding.ivRightArrow.setOnClickListener {
            viewModel.moveToNextProblem()
            updateUI(viewModel.problems.value ?: emptyList())
        }

        binding.studySolveBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study)
        }

        // 정답 확인 버튼 클릭
        binding.studySolveBtnAnswer.setOnClickListener {
            val currentProblem = viewModel.getCurrentProblem()
            val bundle = bundleOf(
                "problemId" to currentProblem.problemId,
                "answer" to currentProblem.answerText
            )
//                Toast.makeText(requireContext(), "solve -- id : ${currentProblem.id}, answer : ${currentProblem.answer}, last : ${isLastProblem}", Toast.LENGTH_SHORT).show()

            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_answer, bundle)
        }

        binding.ivDrawer.setOnClickListener {
            binding.studyDrawerLayout.openDrawer(GravityCompat.END)
        }

        binding.btnRvDrawerFinish.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_complete)
        }
    }

    private fun updateUI(problemList: List<StudyProblem>) {
        val currentProblem = problemList[viewModel.currentProblemIndex]
        binding.tvStudySolveProblemId.text = "문제 " + currentProblem.problemId.toString()

        // 문제 이미지 로드
        Glide.with(this)
            .load(currentProblem.problemText)
            .into(binding.ivSolveCard)

        // 지문 이미지 로드
        Glide.with(this)
            .load(currentProblem.descriptionUrl)
            .into(binding.ivSolveCardDes)

        // 지문 이미지를 초기에는 숨겨둠
        binding.ivSolveCardDes.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}