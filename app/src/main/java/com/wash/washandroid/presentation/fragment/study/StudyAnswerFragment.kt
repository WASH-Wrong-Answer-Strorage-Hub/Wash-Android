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

//        Toast.makeText(requireContext(), "answer -- id : ${problemId}, answer : ${answer}, last : ${isLastProblem}", Toast.LENGTH_SHORT).show()

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
                viewModel.incrementLeftSwipe()
                viewModel.updateProblemStatus(isCorrect = false)
                viewModel.moveToNextProblem()
            }

            override fun onRightCardExit(p0: Any?) {
                viewModel.incrementRightSwipe()
                viewModel.updateProblemStatus(isCorrect = true)
                viewModel.moveToNextProblem()
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                // 어댑터가 비어가고 있을 때 동작
                if (itemsInAdapter == 0) {
                    navController.popBackStack()
                }
            }

            override fun onScroll(scrollProgressPercent: Float) {
                val cardBg = view.findViewById<View>(R.id.study_answer_card_bg)
                // 스크롤 진행 상황
                if (scrollProgressPercent > 0) {
                    // 오른쪽으로 스와이프 중
                    cardBg.setBackgroundResource(R.drawable.study_card_background_green)
                    binding.ivStudySolve.setBackgroundResource(R.drawable.study_correct)
                } else if (scrollProgressPercent < 0) {
                    // 왼쪽으로 스와이프 중
                    cardBg.setBackgroundResource(R.drawable.study_card_background_red)
                    binding.ivStudySolve.setBackgroundResource(R.drawable.study_incorrect)
                } else {
                    // 스크롤 중이 아닌 상태로 되돌아올 때
                    cardBg.setBackgroundResource(R.drawable.study_card_background)
                    binding.ivStudySolve.setBackgroundResource(R.drawable.study_incomplete)
                }
            }
        })

        swipeView.setOnItemClickListener { itemPosition, dataObject ->
        }

        binding.studySolveBackBtn.setOnClickListener {
            StudyExitDialog.showDialog(childFragmentManager)
        }

        binding.btnStudyO.setOnClickListener {
            // 오른쪽으로 스와이프
            swipeView.topCardListener.selectRight()
            val cardBg = view.findViewById<View>(R.id.study_answer_card_bg)
            cardBg.setBackgroundResource(R.drawable.study_card_background_green)
            binding.ivStudySolve.setBackgroundResource(R.drawable.study_correct)
            viewModel.updateProblemStatus(isCorrect = true)
        }

        binding.btnStudyX.setOnClickListener {
            // 왼쪽으로 스와이프
            swipeView.topCardListener.selectLeft()
            val cardBg = view.findViewById<View>(R.id.study_answer_card_bg)
            cardBg.setBackgroundResource(R.drawable.study_card_background_red)
            binding.ivStudySolve.setBackgroundResource(R.drawable.study_incorrect)
            viewModel.updateProblemStatus(isCorrect = false)
        }

        // 뒤로가기 클릭 시 다이얼로그 띄우기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                StudyExitDialog.showDialog(childFragmentManager)
            }
        })

    }
}