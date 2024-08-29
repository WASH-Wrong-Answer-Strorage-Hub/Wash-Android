package com.wash.washandroid.presentation.fragment.graph

import MypageViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentViewPieChartBinding

data class ChartItem(
    val category: String,
    val percentage: String
)

class ViewPieChartFragment : Fragment() {

    private var _binding: FragmentViewPieChartBinding? = null
    private val binding get() = _binding!!

    private val initialDataCount = 3

    private val mypageViewModel: MypageViewModel by activityViewModels()
    private val pieChartViewModel: GraphViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 토큰
        val refreshToken = mypageViewModel.getRefreshToken()
        val bearerToken = "Bearer $refreshToken"

        // 전달받은 카테고리 이름을 업데이트
        val categoryName = arguments?.getString("CATEGORY_NAME") ?: "Subject"
        binding.categoryTag.text = categoryName

        // 파이차트 데이터 관찰
        pieChartViewModel.pieChartResponse.observe(viewLifecycleOwner) { pieChartData ->
            val chartItems = pieChartData.map {
                val percentage = String.format("%.1f", it.incorrect_percentage.toFloat())
                ChartItem(it.sub_category ?: "Unknown", "${percentage}%") //파이차트 전달받기
            }
            updatePieChart(chartItems)
            updateRecyclerView(chartItems.take(initialDataCount))
            binding.viewMoreBtn.setOnClickListener {
                updateRecyclerView(chartItems)
                binding.viewMoreBtn.visibility = View.GONE
            }
        }

        // 데이터 요청
        pieChartViewModel.fetchPieChartData(bearerToken)

        // 차트 조각 클릭 시 이벤트 처리
        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                h?.let {
                    binding.pieChart.highlightValue(it)
                }
                e?.let {
                    val pieEntry = e as PieEntry
                    binding.pieChart.centerText = "${String.format("%.1f", pieEntry.value)}%" // 소수점 한 자리 표시
                }
            }

            override fun onNothingSelected() {
                binding.pieChart.highlightValues(null)
                binding.pieChart.centerText = ""
            }
        })

        // 뒤로가기 버튼 클릭 리스너 설정
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_viewPieChartFragment_to_navigation_graph)
        }
    }

    private fun updatePieChart(items: List<ChartItem>) {
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.main),
            ContextCompat.getColor(requireContext(), R.color.sub2),
            ContextCompat.getColor(requireContext(), R.color.sub3),
            ContextCompat.getColor(requireContext(), R.color.sub4),
            ContextCompat.getColor(requireContext(), R.color.sub5)
        )

        val entries = items.map { PieEntry(it.percentage.replace("%", "").toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors.take(items.size) // 데이터 항목 수에 맞는 색상 리스트 적용
            this.sliceSpace = 3f
            this.setDrawValues(false) // 퍼센트 표시 제거
        }

        binding.pieChart.apply {
            this.isRotationEnabled = false
            this.rotationAngle = 0f // 초기 회전 각도 설정

            this.setDrawEntryLabels(false) // 레이블 숨기기
            this.legend.isEnabled = false // 범례 숨기기
            this.description.isEnabled = false // 설명 숨기기
            this.data = PieData(dataSet)
            this.invalidate() // 차트 갱신
        }
    }

    private fun updateRecyclerView(items: List<ChartItem>) {
        binding.chartItemsRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = ChartDataAdapter(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}