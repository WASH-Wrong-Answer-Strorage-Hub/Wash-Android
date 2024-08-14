package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyExitDialogBinding

class StudyExitDialog : DialogFragment() {

    private var _binding: FragmentStudyExitDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudyExitDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = parentFragment?.findNavController() ?: requireActivity().findNavController(R.id.navigation_study_answer)


        // 확인 버튼 클릭 리스너
        binding.btnDialogConfirm.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_answer_to_navigation_study_solve)
//            NavOptions.Builder().setPopUpTo(R.id.navigation_study_answer, true).build()
            dismiss()
        }

        // 취소 버튼 클릭 리스너
        binding.btnDialogCancel.setOnClickListener {
            // 취소 버튼 클릭 시 실행할 작업
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StudyExitDialog"

        fun showDialog(fragmentManager: FragmentManager) {
            val dialog = StudyExitDialog()
            dialog.show(fragmentManager, TAG)
        }
    }
}
