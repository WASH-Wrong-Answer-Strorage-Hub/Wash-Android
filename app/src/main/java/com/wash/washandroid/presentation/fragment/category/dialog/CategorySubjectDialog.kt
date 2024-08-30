package com.wash.washandroid.presentation.fragment.category.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.wash.washandroid.R
import com.wash.washandroid.databinding.DialogCategorySubjectBinding
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubjectViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemAddViewModel

class CategorySubjectDialog : DialogFragment() {

    private val categorySubjectDialogViewModel: CategorySubjectDialogViewModel by activityViewModels()
    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels()
    private val categorySubjectViewModel: CategorySubjectViewModel by activityViewModels()
    private val problemAddViewModel: ProblemAddViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogCategorySubjectBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_category_subject, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = categorySubjectDialogViewModel

        setupListeners(binding)
        observeTypeIds()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            convertDpToPx(320),
            convertDpToPx(200)
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.CategoryAddDialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setupListeners(binding: DialogCategorySubjectBinding) {
        binding.categoryAddCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.categoryAddSubjectCompleteBtn.setOnClickListener {
            val subject = binding.categoryAddSubjectEt.text.toString()
            categorySubjectDialogViewModel.addCategoryType(subject, 0, 1)
            categorySubjectViewModel.fetchCategoryTypes()
        }

        binding.categoryAddSubfieldCompleteBtn.setOnClickListener {
            val subfield = binding.categoryAddSubfieldEt.text.toString()
            val parentTypeId = categorySubjectDialogViewModel.subjectTypeId.value ?: return@setOnClickListener
            categorySubjectDialogViewModel.addCategoryType(subfield, parentTypeId, 2)
        }

        binding.categoryAddChapterCompleteBtn.setOnClickListener {
            val chapter = binding.categoryAddChapterEt.text.toString()
            val parentTypeId = categorySubjectDialogViewModel.subfieldTypeId.value ?: return@setOnClickListener
            categorySubjectDialogViewModel.addCategoryType(chapter, parentTypeId, 3)
        }

        binding.categoryAddCompleteBtn.setOnClickListener {

            // 문제 추가 api에 새롭게 추가된 대분류, 중분류, 소분류 한꺼번에 추가하기
            categoryFolderViewModel.setMainTypeId(categorySubjectDialogViewModel.subjectTypeId.value ?: 0)
            categoryFolderViewModel.setMidTypeId(categorySubjectDialogViewModel.subfieldTypeId.value ?: 0)
            categoryFolderViewModel.setSubTypeIds(listOf(categorySubjectDialogViewModel.chapterTypeId.value ?: 0))

            val currentIndex = problemAddViewModel.currentIndex.value ?: 0
            val photoList = problemAddViewModel.photoList.value ?: mutableListOf()

            // 로그로 현재 인덱스와 사진 경로 확인
            Log.d("problemAddViewModel", "Current Index: $currentIndex, Photo: ${photoList[currentIndex]}")

            // 폴더화면으로 넘어가기
            val navController = parentFragment?.let { fragment ->
                Navigation.findNavController(fragment.requireView())
            }

            // 인덱스가 마지막이 아니라면 다음 프로세스를 반복
            if (!problemAddViewModel.isLastIndex()) {
                problemAddViewModel.incrementIndex()
                navController?.navigate(R.id.action_navigation_problem_category_subject_to_problem_answer_fragment)
            } else {
                // 모든 사진을 처리했다면 프로세스 종료
                navController?.navigate(R.id.action_navigation_problem_category_subject_to_folder_fragment)
                problemAddViewModel.resetIndex() // 인덱스 초기화
            }
        }
    }

    private fun observeTypeIds() {
        categorySubjectDialogViewModel.subjectTypeId.observe(viewLifecycleOwner) { typeId ->
            Log.d("subjectTypeId", "$typeId")
        }

        categorySubjectDialogViewModel.subfieldTypeId.observe(viewLifecycleOwner) { typeId ->
            Log.d("subfieldTypeId", "$typeId")
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}