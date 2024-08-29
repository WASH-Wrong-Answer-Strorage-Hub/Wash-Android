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


        val problemId = requireArguments().getInt("problemId")
        val problemImageUrl = homeViewModel.getProblemImageUrl(problemId)



        // mistakeResponse LiveData 관찰
        viewModel.mistakeResponse.observe(viewLifecycleOwner) { mistakes ->
            Log.d("GraphFragment", "Mistakes Data: $mistakes")
            setupProblemRecyclerView(mistakes)
        }

        // typeResponse LiveData 관찰
        viewModel.typeResponse.observe(viewLifecycleOwner) { stats ->
            Log.d("GraphFragment", "Type Response Data: $stats")
            stats?.let {
                val subjects = it.subCategories.groupBy { subCategory ->
                    // `category`를 기준으로 그룹화
                    it.category
                }.map { (category, subCategories) ->
                    Subject(
                        id = category.hashCode(), // 고유 ID로 `category`의 해시코드 사용
                        name = category, // `category` 이름
                        type = subCategories.sumOf { subCategory -> subCategory.totalIncorrect } // 모든 `totalIncorrect`의 합
                    )
                }

                setupTypeRecyclerView(subjects)
            }
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
        val problems = mistakes.map { mistake ->
            val problemId = mistake.problemId.toInt()
            val imageUrl = homeViewModel.getProblemImageUrl(problemId)
            Log.d("GraphFragment", "Image URL for problemId $problemId: $imageUrl")
            Problem(
                id = problemId,
                imageUrl = imageUrl.toString()
            )
        }

        val problemAdapter = ProblemImageAdapter(problems) { problemId ->
            Log.d("GraphFragment", "Clicked Problem ID: $problemId")
            // 클릭 시 추가 동작 수행 가능
        }
        binding.problemsRecyclerView.adapter = problemAdapter
    }

    // 유형 리스트 RecyclerView 설정
    private fun setupTypeRecyclerView(subjects: List<Subject>) {
        val subjectAdapter = SubjectsAdapter(subjects) { subject ->
            val bundle = Bundle().apply {
                putString("CATEGORY_NAME", subject.name) // 카테고리 이름을 전달
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
