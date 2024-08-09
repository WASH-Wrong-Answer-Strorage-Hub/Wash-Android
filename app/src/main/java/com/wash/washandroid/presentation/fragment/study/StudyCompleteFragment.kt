package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyCompleteBinding
import com.wash.washandroid.presentation.base.MainActivity

class StudyCompleteFragment : Fragment(){
    private var _binding: FragmentStudyCompleteBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyCompleteBinding.inflate(inflater, container, false)

        // id, 폴더명 수신
//        val folderId = arguments?.getInt("folderId")
//        val folderName = arguments?.getString("folderName")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        // 홈으로 이동하기
        binding.btnStudyBackHome.setOnClickListener {
            navController.navigate(
                R.id.action_navigation_study_complete_to_navigation_home,
                null,
                NavOptions.Builder().setPopUpTo(R.id.navigation_home, false).build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}