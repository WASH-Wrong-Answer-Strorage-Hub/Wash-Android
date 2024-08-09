package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudySolveBinding
import com.wash.washandroid.presentation.base.MainActivity

class StudySolveFragment : Fragment() {
    private var _binding: FragmentStudySolveBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var viewModel: StudyViewModel
    private var currentProblemIndex = 0  // 현재 문제의 인덱스

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudySolveBinding.inflate(inflater, container, false)

        // id, 폴더명 수신
        val folderId = arguments?.getInt("folderId")
        val folderName = arguments?.getString("folderName")

        binding.tvStudySolveTitle.text = folderName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        // ViewModel 인스턴스 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(StudyViewModel::class.java)

        // ViewModel의 problems LiveData 보기
        viewModel.problems.observe(viewLifecycleOwner, Observer { problemList ->
            // 문제가 비어있지 않으면 첫 번째 문제를 표시
            if (problemList.isNotEmpty()) {
                updateUI(problemList)
            }
        })

        // 문제 데이터를 로드
        viewModel.loadProblems()

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
            viewModel.problems.value?.let { problemList ->
                if (currentProblemIndex > 0) {
                    currentProblemIndex--
                    updateUI(problemList)
                } else {
                    Toast.makeText(requireContext(), "첫 번째 문제입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 오른쪽 화살표 클릭 시 다음 문제로 이동
        binding.ivRightArrow.setOnClickListener {
            viewModel.problems.value?.let { problemList ->
                if (currentProblemIndex < problemList.size - 1) {
                    currentProblemIndex++
                    updateUI(problemList)
                } else {
                    Toast.makeText(requireContext(), "마지막 문제입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.studySolveBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study)
        }

        binding.studySolveBtnAnswer.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_answer)
        }

        binding.ivDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun updateUI(problemList: List<StudyProblem>) {
        val currentProblem = problemList[currentProblemIndex]
        binding.tvStudySolveProblemId.text = "문제 " + currentProblem.id.toString()

        // 문제 이미지 로드
        Glide.with(this)
            .load(currentProblem.imageUrl)
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