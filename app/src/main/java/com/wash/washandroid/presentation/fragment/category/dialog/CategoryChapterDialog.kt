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
import androidx.navigation.Navigation
import com.wash.washandroid.R
import com.wash.washandroid.databinding.DialogCategoryChapterBinding
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryChapterViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel

class CategoryChapterDialog : DialogFragment() {

    companion object {
        private const val ARG_PARENT_TYPE_ID = "parentTypeId"

        fun newInstance(parentTypeId: Int): CategoryChapterDialog {
            val args = Bundle().apply {
                putInt(ARG_PARENT_TYPE_ID, parentTypeId)
            }
            val fragment = CategoryChapterDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private val categoryChapterDialogViewModel: CategoryChapterDialogViewModel by activityViewModels()
    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels()
    private val categoryChapterViewModel: CategoryChapterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogCategoryChapterBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_category_chapter, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = categoryChapterDialogViewModel

        val parentTypeId = arguments?.getInt(ARG_PARENT_TYPE_ID) ?: -1
        setupListeners(binding, parentTypeId)
        observeTypeIds()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            convertDpToPx(320),
            convertDpToPx(120)
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.CategoryAddDialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setupListeners(binding: DialogCategoryChapterBinding, parentTypeId: Int) {
        binding.categoryAddCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.categoryAddChapterCompleteBtn.setOnClickListener {
            val chapter = binding.categoryAddChapterEt.text.toString()
            categoryChapterDialogViewModel.addCategoryType(chapter, parentTypeId, 3)
            categoryChapterViewModel.fetchCategoryTypes(parentTypeId)
        }

        binding.categoryAddCompleteBtn.setOnClickListener {
            // 폴더화면으로 넘어가기
            val navController = parentFragment?.let { fragment ->
                Navigation.findNavController(fragment.requireView())
            }
            navController?.navigate(R.id.action_navigation_problem_category_chapter_to_folder_fragment)

            // 문제 추가 api에 새롭게 추가된 소분류 한꺼번에 추가하기
            categoryFolderViewModel.setSubTypeIds(listOf(categoryChapterDialogViewModel.chapterTypeId.value ?: 0))
        }
    }

    private fun observeTypeIds() {
        categoryChapterDialogViewModel.subjectTypeId.observe(viewLifecycleOwner) { typeId ->
            Log.d("subjectTypeId", "$typeId")
        }

        categoryChapterDialogViewModel.subfieldTypeId.observe(viewLifecycleOwner) { typeId ->
            Log.d("subfieldTypeId", "$typeId")
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}