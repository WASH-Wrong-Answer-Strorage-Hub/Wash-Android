package com.wash.washandroid.presentation.fragment.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentViewPieChartBinding

class ViewPieChartFragment : Fragment() {

    private var _binding: FragmentViewPieChartBinding? = null
    private val binding get() = _binding!!

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

        // 백 버튼 클릭 이벤트 처리
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_viewPieChartFragment_to_navigation_graph)
        }

        // 여기에 PieChart 설정 등을 추가할 수 있습니다.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
