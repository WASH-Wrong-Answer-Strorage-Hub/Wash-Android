package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyAnswerBinding

class StudyAnswerFragment : Fragment() {
    private var _binding: FragmentStudyAnswerBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var swipeView: SwipeFlingAdapterView
    private lateinit var adapter: CardAdapter
    private lateinit var viewModel: StudyViewModel

    private val data: MutableList<String> = mutableListOf()

    private var currentProblemIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudyAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(requireActivity()).get(StudyViewModel::class.java)

        // 데이터 수신
        val problemId = arguments?.getInt("problemId")
        val answer = arguments?.getString("answer") ?: "정답 불러오기 실패"
        val isLastProblem = arguments?.getBoolean("isLastProblem") ?: false

//        Toast.makeText(requireContext(), "id : ${problemId}, answer : ${answer}, last : ${isLastProblem}", Toast.LENGTH_SHORT).show()

        // data 리스트에 정답을 추가
        data.add(answer)

        swipeView = binding.studyCardFrame
        adapter = CardAdapter(requireContext(), data)

        swipeView.adapter = adapter
        swipeView.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
                data.removeAt(0)
                adapter.notifyDataSetChanged()
            }

            override fun onLeftCardExit(p0: Any?) {
                // 왼쪽으로 스와이프될 때 동작
                viewModel.incrementLeftSwipe(currentProblemIndex)
                currentProblemIndex++
            }

            override fun onRightCardExit(p0: Any?) {
                viewModel.incrementRightSwipe(currentProblemIndex)
                currentProblemIndex++
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                // 어댑터가 비어가고 있을 때 동작
                if (isLastProblem && itemsInAdapter == 0) { // 문제가 마지막 문제일 때, 완료 화면으로 이동
                    navController.navigate(R.id.action_navigation_study_answer_to_navigation_study_complete)
                } else if (itemsInAdapter == 0) {
                    navController.popBackStack()
                }
            }

            override fun onScroll(scrollProgressPercent: Float) {
                // 스크롤 진행 상황
            }
        })

        swipeView.setOnItemClickListener { itemPosition, dataObject ->
//            Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.studySolveBackBtn.setOnClickListener {
            StudyExitDialog.showDialog(childFragmentManager)
        }

        binding.ivDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        binding.btnStudyO.setOnClickListener {
            // 오른쪽으로 스와이프
            swipeView.topCardListener.selectRight()
            // 맞은 문제 카운트
            viewModel.incrementRightSwipe(currentProblemIndex)
            currentProblemIndex++

        }

        binding.btnStudyX.setOnClickListener {
            // 왼쪽으로 스와이프
            swipeView.topCardListener.selectLeft()
            // 틀린 문제 카운트
            viewModel.incrementLeftSwipe(currentProblemIndex)
            currentProblemIndex++
        }

        // 뒤로가기 클릭 시 다이얼로그 띄우기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                StudyExitDialog.showDialog(childFragmentManager)
            }
        })

    }
}