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
import androidx.recyclerview.widget.LinearLayoutManager
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

        // RecyclerView 초기화
        setupRecyclerViews()

        // 토큰
        val refreshToken = mypageViewModel.getRefreshToken() ?: return binding.root // Token이 없으면 반환
        val bearerToken = "Bearer $refreshToken"


        // API 데이터
        viewModel.fetchMistakeData(bearerToken)
        viewModel.fetchTypeData(bearerToken)

        // mistakeResponse LiveData
        viewModel.mistakeResponse.observe(viewLifecycleOwner) { mistakes ->
            Log.d("GraphFragment", "Mistakes Data: $mistakes")
            setupProblemRecyclerView(mistakes)
        }

        // typeResponse LiveData
        viewModel.typeResponse.observe(viewLifecycleOwner) { types ->
            Log.d("GraphFragment", "Types Data: $types")
            setupTypeRecyclerView(types)
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        // 레이아웃 매니저 설정
        binding.problemsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.subjectsRecyclerView.layoutManager = LinearLayoutManager(context)

    }

    private fun setupProblemRecyclerView(mistakes: List<Result>) {

        val problems = mistakes.map { mistake ->
            // Result를 Problem으로 변환
            Problem(
                id = mistake.problemId.toInt(),
                imageResId = R.drawable.temporary_img_test, // 자리 표시자 이미지
            )
        }

        val PBadapter = ProblemImageAdapter(problems) { problemId ->
            // 아이템 클릭 시 로그 찍기
            Log.d("GraphFragment", "Clicked Problem ID: $problemId")
        }
        binding.problemsRecyclerView.adapter = PBadapter
    }

    private fun setupTypeRecyclerView(types: List<TypeResult>) {
        val subjects = types.map { type ->
            // TypeResult를 Subject로 변환
            Subject(
                id = type.sub_category.hashCode(), // 카테고리에 기반한 고유 ID
                name = type.sub_category ?: "제목 없음", // 카테고리 null 처리
                type = "${type.total_incorrect}개의 실수" // 오류 수
            )
        }

        val SJadapter = SubjectsAdapter(subjects) { subject ->
            // 과목 클릭 시 PieChartFragment로 이동 및 카테고리 이름 전달
            val bundle = Bundle().apply {
                putString("CATEGORY_NAME", subject.name) // 카테고리 이름을 전달
            }
            findNavController().navigate(R.id.action_navigation_graph_to_viewPieChartFragment, bundle)
        }
        binding.subjectsRecyclerView.adapter = SJadapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
