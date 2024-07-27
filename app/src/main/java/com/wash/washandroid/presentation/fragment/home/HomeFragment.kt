package com.wash.washandroid.presentation.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // "국어" TextView 클릭 이벤트 설정
        binding.cateKorean.setOnClickListener {
            Log.d("HomeFragment","클릭 리스너")
            onCategoryClick()
            Log.d("HomeFragment","국어 클릭 완료 페이지 이동")
        }

        return binding.root
    }
    private fun onCategoryClick() {
        val navController = findNavController()
        Log.d("HomeFragment", "Navigating to HomeDetailFragment 전")
        navController.navigate(R.id.homeDetailFragment)
        Log.d("HomeFragment", "Navigating to HomeDetailFragment")
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
