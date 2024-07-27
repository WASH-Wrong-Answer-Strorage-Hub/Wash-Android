package com.wash.washandroid.presentation.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeDetailBinding
import com.wash.washandroid.presentation.adapter.ImageAdapter

class HomeDetailFragment : Fragment() {

    private var _binding: FragmentHomeDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private val COLUMN_COUNT_EXPANDED = 5
    private val COLUMN_COUNT_CONTRACTED = 3
    private var currentColumnCount = COLUMN_COUNT_EXPANDED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())

        Log.d("HomeDetail", "프레그먼트 진입")

        // 초기 RecyclerView 설정
        setupRecyclerView(currentColumnCount)

        view.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

        // back_btn 클릭 이벤트 설정
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeDetailFragment_to_navigation_home)
        }

        return view
    }

    private fun setupRecyclerView(columnCount: Int) {
        val layoutManager = GridLayoutManager(context, columnCount)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ImageAdapter(columnCount * columnCount) { position ->
            onImageClick(position)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun onImageClick(position: Int) {
        findNavController().navigate(R.id.action_homeDetailFragment_to_homeClickQuizFragment)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newColumnCount = if (scaleFactor > 1) {
                COLUMN_COUNT_EXPANDED
            } else {
                COLUMN_COUNT_CONTRACTED
            }

            // Only update if the column count has changed
            if (currentColumnCount != newColumnCount) {
                currentColumnCount = newColumnCount
                setupRecyclerView(newColumnCount)
            }

            return true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
