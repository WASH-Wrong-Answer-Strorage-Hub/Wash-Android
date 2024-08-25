package com.wash.washandroid.presentation.fragment.graph

import ChartDataAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
    val percentage: String,
    val color: Int
)

class ViewPieChartFragment : Fragment() {

    private var _binding: FragmentViewPieChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var allItems: List<ChartItem>
    private val initialDataCount = 3

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

        // 데이터 초기화 (색상 리소스 사용)
        allItems = listOf(
            ChartItem("도함수의 활용", "45%", ContextCompat.getColor(requireContext(), R.color.main)),
            ChartItem("여러 가지 미분법", "25%", ContextCompat.getColor(requireContext(), R.color.sub2)),
            ChartItem("정적분의 활용", "20%", ContextCompat.getColor(requireContext(), R.color.sub3)),
            ChartItem("미적분의 활용", "10%", ContextCompat.getColor(requireContext(), R.color.sub4)),
            ChartItem("기타", "5%", ContextCompat.getColor(requireContext(), R.color.sub5))
        )

        // 파이 차트의 색상 및 데이터 설정
        updatePieChart(allItems)

        // 기본적으로 상위 3개 데이터만 리사이클러뷰에 표시
        updateRecyclerView(allItems.take(initialDataCount))
        // 더 보기 버튼 클릭 리스너 설정

        binding.viewMoreBtn.setOnClickListener {
            // 전체 데이터를 리사이클러뷰에 표시
            updateRecyclerView(allItems)
            // 더 보기 버튼 숨기기
            binding.viewMoreBtn.visibility = View.GONE
        }

        // 차트 조각 클릭 시 발생하는 이벤트 처리
        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                h?.let {
                    binding.pieChart.highlightValue(it)
                }
                e?.let {
                    val pieEntry = e as PieEntry
                    binding.pieChart.centerText = "${pieEntry.value.toInt()}%" // 퍼센트 표시
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
        val entries = items.map { PieEntry(it.percentage.replace("%", "").toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "").apply {
            // 데이터 항목 수에 맞는 색상 리스트 가져오기
            val colorList = items.map { it.color }
            Log.d("PieChart", "Color List Used: $colorList") // 색상 리스트 확인

            this.colors = colorList
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
