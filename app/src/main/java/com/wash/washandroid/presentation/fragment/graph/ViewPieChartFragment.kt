package com.wash.washandroid.presentation.fragment.graph

import ChartDataAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ViewPieChartFragment : Fragment() {

    private var _binding: FragmentViewPieChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var allEntries: List<PieEntry>
    private lateinit var colors: List<Int>

    // 기본적으로 보여줄 데이터 개수
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

        // 모든 파이 차트 데이터를 설정
        allEntries = listOf(
            PieEntry(45f, "도함수의 활용"),
            PieEntry(25f, "여러 가지 미분법"),
            PieEntry(20f, "정적분의 활용"),
            PieEntry(10f, "미적분의 활용"),
            PieEntry(5f, "기타")
        )

        // 파이 차트의 색상 설정
        colors = listOf(
            requireContext().getColor(R.color.main),
            requireContext().getColor(R.color.sub2),
            requireContext().getColor(R.color.sub3),
            requireContext().getColor(R.color.sub4),
            requireContext().getColor(R.color.sub5)
        )

        // 파이차트는 항상 모든 데이터를 표시
        updatePieChart(allEntries)

        // 기본적으로 상위 3개 데이터만 리사이클러뷰에 표시
        updateRecyclerView(allEntries.take(initialDataCount))

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

        // 더 보기 버튼 클릭 리스너 설정
        binding.viewMoreBtn.setOnClickListener {
            // 전체 데이터를 리사이클러뷰에 표시
            updateRecyclerView(allEntries)
            // 더 보기 버튼 숨기기
            binding.viewMoreBtn.visibility = View.GONE
        }

        // 뒤로가기 버튼 클릭 리스너 설정
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_viewPieChartFragment_to_navigation_graph)
        }
    }

    private fun updatePieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "").apply {
            // 데이터 항목 수에 맞는 색상 리스트 가져오기
            val colorList = colors.take(entries.size).toMutableList()
            Log.d("PieChart", "Color List Used: $colorList") // 색상 리스트 확인

            // 색상 리스트가 데이터 항목 수보다 적으면 반복하여 사용
            while (colorList.size < entries.size) {
                colorList.addAll(colors) // 색상을 반복하여 추가
            }
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




    private fun updateRecyclerView(entries: List<PieEntry>) {
        binding.chartItemsRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = ChartDataAdapter(entries, colors.take(entries.size))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
