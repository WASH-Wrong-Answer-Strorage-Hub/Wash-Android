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
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentViewPieChartBinding

data class ChartItem(
    val category: String,
    val percentage: Double
)

class ViewPieChartFragment : Fragment() {

    private var _binding: FragmentViewPieChartBinding? = null
    private val binding get() = _binding!!

    private val initialDataCount = 3

    private val mypageViewModel: MypageViewModel by activityViewModels()
    private val pieChartViewModel: GraphViewModel by activityViewModels()

    private var categoryId: Int? = null
    private var categoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getInt("CATEGORY_ID")
            categoryName = it.getString("CATEGORY_NAME")
            Log.d("ViewPieChartFragment", "categoryId : $categoryId")
            Log.d("ViewPieChartFragment", "categoryName : $categoryName")
        }
    }

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
        binding.categoryTag.text = categoryName ?: "Subject"

        val accessToken = "Bearer ${mypageViewModel.getRefreshToken()}"

        categoryId?.let { id ->
            pieChartViewModel.fetchPieChartData(accessToken, id)
        }

        pieChartViewModel.pieChartData.observe(viewLifecycleOwner) { pieChartData ->
            Log.d("ViewPieChartFragment", "Observed PieChartData: $pieChartData")
            if (pieChartData != null && pieChartData.result != null) {
                val chartItems = pieChartData.result.subCategories.map { portion ->
                    ChartItem(portion.subCategory ?: "Unknown", portion.incorrectPercentage)
                }
                updatePieChart(chartItems)
                updateRecyclerView(chartItems.take(initialDataCount))
                binding.viewMoreBtn.setOnClickListener {
                    updateRecyclerView(chartItems)
                    binding.viewMoreBtn.visibility = View.GONE
                }
            } else {
                Log.e("ViewPieChartFragment", "데이터가 없거나 null입니다. PieChartData: $pieChartData")
            }
        }

        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e is PieEntry) {
                    binding.pieChart.centerText = "${String.format("%.1f", e.value)}%"
                }
                h?.let { binding.pieChart.highlightValue(it) }
            }

            override fun onNothingSelected() {
                binding.pieChart.highlightValues(null)
                binding.pieChart.centerText = ""
            }
        })

        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_viewPieChartFragment_to_navigation_graph)
        }
    }

    private fun updatePieChart(items: List<ChartItem>) {
        val colors = items.indices.map {
            ContextCompat.getColor(requireContext(), when (it % 5) {
                0 -> R.color.main
                1 -> R.color.sub2
                2 -> R.color.sub3
                3 -> R.color.sub4
                else -> R.color.sub5
            })
        }

        val entries = items.map {
            PieEntry(it.percentage.toFloat(), it.category)
        }
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            this.sliceSpace = 3f
            this.setDrawValues(false)
        }

        binding.pieChart.apply {
            this.isRotationEnabled = false
            this.rotationAngle = 0f
            this.setDrawEntryLabels(false)
            this.legend.isEnabled = false
            this.description.isEnabled = false
            this.data = PieData(dataSet)
            this.invalidate()
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
