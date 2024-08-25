package com.wash.washandroid.presentation.fragment.graph

import MypageViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentGraphBinding
import com.wash.washandroid.presentation.adapter.Problem
import com.wash.washandroid.presentation.adapter.ProblemImageAdapter
import com.wash.washandroid.presentation.adapter.Subject
import com.wash.washandroid.presentation.adapter.SubjectsAdapter

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GraphViewModel by activityViewModels()
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        setupRecyclerViews()

        val refreshToken = mypageViewModel.getRefreshToken()
        val bearerToken = "Bearer $refreshToken"
        Log.d("graphFragment", "RefreshToken: $refreshToken")
        Log.d("graphFragment", "BearerToken: $bearerToken")

        viewModel.fetchMistakeData(bearerToken)
        //viewModel.fetchTypeData(bearerToken)

        viewModel.mistakeResponse.observe(viewLifecycleOwner) { mistakes ->
            setupProblemRecyclerView(mistakes)
        }

        viewModel.typeResponse.observe(viewLifecycleOwner) { types ->
            setupTypeRecyclerView(types)
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        val subjects = listOf(
            Subject(1, "수학", "미적분"),
            Subject(2, "수학", "기하"),
            Subject(3, "영어", "토플")
        )
        val problems = listOf(
            Problem(1, "문제 1", R.drawable.temporary_img_test),
            Problem(2, "문제 2", R.drawable.temporary_img_test),
            Problem(3, "문제 3", R.drawable.temporary_img_test),
            Problem(4, "문제 4", R.drawable.temporary_img_test),
            Problem(5, "문제 5", R.drawable.temporary_img_test)
        )

        // SubjectsRecyclerView 설정
        val SJadapter = SubjectsAdapter(subjects) { subject ->
            findNavController().navigate(R.id.action_navigation_graph_to_viewPieChartFragment)
        }
        binding.subjectsRecyclerView.adapter = SJadapter

        // ProblemsRecyclerView 설정
        val PBadapter = ProblemImageAdapter(problems)
        binding.problemsRecyclerView.adapter = PBadapter
    }

    private fun setupProblemRecyclerView(mistakes: List<Result>) {
        val problems = mistakes.map { mistake ->
            // 서버에서 받은 데이터로 Problem 객체를 생성
            Problem(mistake.problemId.toInt(), mistake.problemText, R.drawable.temporary_img_test)
        }

        // ProblemsRecyclerView 설정
        val PBadapter = ProblemImageAdapter(problems)
        binding.problemsRecyclerView.adapter = PBadapter
    }

    private fun setupTypeRecyclerView(types: List<Result>) {
        val subjects = types.map { type ->
            Subject(type.problemId.toInt(), type.problemText, "${type.incorrectCount} mistakes")
        }

        val SJadapter = SubjectsAdapter(subjects) { subject ->
            findNavController().navigate(R.id.action_navigation_graph_to_viewPieChartFragment)
        }
        binding.subjectsRecyclerView.adapter = SJadapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
