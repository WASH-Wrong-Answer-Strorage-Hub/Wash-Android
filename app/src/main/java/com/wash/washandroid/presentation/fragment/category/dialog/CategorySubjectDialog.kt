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
import com.wash.washandroid.R
import com.wash.washandroid.databinding.DialogCategorySubjectBinding

class CategorySubjectDialog : DialogFragment() {

    private val categorySubjectDialogViewModel: CategorySubjectDialogViewModel by activityViewModels()

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
            dismiss()
            // 폴더화면으로 넘어가기
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