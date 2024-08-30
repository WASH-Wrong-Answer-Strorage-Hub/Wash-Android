package com.wash.washandroid.presentation.fragment.graph

import HomeViewModel
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
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        // RecyclerView 초기화
        setupRecyclerViews()

        // 토큰 가져오기
        val refreshToken = mypageViewModel.getRefreshToken() ?: return binding.root // Token이 없으면 반환
        val bearerToken = "Bearer $refreshToken"

        // API 데이터 가져오기
        viewModel.fetchMistakeData(bearerToken)
        viewModel.fetchTypeData(bearerToken)

        // mistakeResponse LiveData 관찰
        viewModel.mistakeResponse.observe(viewLifecycleOwner) { mistakes ->
            Log.d("GraphFragment", "Mistakes Data: $mistakes")
            setupProblemRecyclerView(mistakes)
        }

        // typeResponse LiveData 관찰
        viewModel.typeResponse.observe(viewLifecycleOwner) { problemStatisticsList ->
            Log.d("GraphFragment", "Type Response Data: $problemStatisticsList")

            // 과목 리스트 생성
            val subjects = problemStatisticsList?.flatMap { stats ->
                stats.categories.map { category ->
                    val totalIncorrect = category.subCategories.sumOf { subCategory -> subCategory.totalIncorrect.toInt() }
                    Log.d("GraphFragment", "Creating Subject: ${category.category}, Total Incorrect: $totalIncorrect")
                    Subject(
                        name = category.category, // `category` 이름
                        type = totalIncorrect, // `totalIncorrect`의 합
                        categoryId = category.categoryId // `categoryId` 추가
                    )
                }
            } ?: emptyList()

            // `totalIncorrect`의 합이 큰 순서대로 정렬
            val sortedSubjects = subjects.sortedByDescending { it.type }

            // 정렬된 리스트로 RecyclerView 설정
            setupTypeRecyclerView(sortedSubjects)
        }

        return binding.root
    }

    // RecyclerView 초기화
    private fun setupRecyclerViews() {
        binding.problemsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.subjectsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    // 문제 리스트 RecyclerView 설정
    private fun setupProblemRecyclerView(mistakes: List<Result>) {
        val problemList = mistakes.map { mistake ->
            val problemId = mistake.problemId.toInt()
            val imageUrl = mistake.problemImage // API에서 받은 이미지 URL 사용
            Log.d("problemImg", "Image URL for problemId $problemId: $imageUrl")
            Problem(
                id = problemId,
                imageUrl = imageUrl.toString() // URL을 Problem 객체에 전달
            )
        }

        val problemAdapter = ProblemImageAdapter(problemList) { problemId ->
            Log.d("GraphFragment", "Clicked Problem ID: $problemId")
            // 클릭 시 추가 동작 수행 가능
        }
        binding.problemsRecyclerView.adapter = problemAdapter
    }

    // 유형 리스트 RecyclerView 설정
    private fun setupTypeRecyclerView(subjects: List<Subject>) {
        val subjectAdapter = SubjectsAdapter(subjects) { subject ->
            // 카테고리 클릭 시 로그 추가
            Log.d("GraphFragment", "Clicked Subject: Name=${subject.name}, CategoryId=${subject.categoryId}")

            // PieChart 데이터 요청
            val refreshToken = mypageViewModel.getRefreshToken() ?: return@SubjectsAdapter
            val bearerToken = "Bearer $refreshToken"
            viewModel.fetchPieChartData(bearerToken, subject.categoryId)

            // Fragment로 데이터 전달
            val bundle = Bundle().apply {
                putInt("CATEGORY_ID", subject.categoryId)
                putString("CATEGORY_NAME", subject.name)
            }
            findNavController().navigate(R.id.action_navigation_graph_to_viewPieChartFragment, bundle)
        }
        binding.subjectsRecyclerView.adapter = subjectAdapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
