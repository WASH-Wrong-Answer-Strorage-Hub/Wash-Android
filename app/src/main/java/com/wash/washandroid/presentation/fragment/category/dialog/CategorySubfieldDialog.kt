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
import com.wash.washandroid.databinding.DialogCategorySubfieldBinding

class CategorySubfieldDialog : DialogFragment() {

    companion object {
        private const val ARG_PARENT_TYPE_ID = "parentTypeId"

        fun newInstance(parentTypeId: Int): CategorySubfieldDialog {
            val args = Bundle().apply {
                putInt(ARG_PARENT_TYPE_ID, parentTypeId)
            }
            val fragment = CategorySubfieldDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private val categorySubfieldDialogViewModel: CategorySubfieldDialogViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogCategorySubfieldBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_category_subfield, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = categorySubfieldDialogViewModel

        val parentTypeId = arguments?.getInt(ARG_PARENT_TYPE_ID) ?: -1
        setupListeners(binding, parentTypeId)
        observeTypeIds()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            convertDpToPx(320),
            convertDpToPx(160)
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.CategoryAddDialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setupListeners(binding: DialogCategorySubfieldBinding, parentTypeId: Int) {
        binding.categoryAddCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.categoryAddSubfieldCompleteBtn.setOnClickListener {
            val subfield = binding.categoryAddSubfieldEt.text.toString()
            categorySubfieldDialogViewModel.addCategoryType(subfield, parentTypeId, 2)
        }

        binding.categoryAddChapterCompleteBtn.setOnClickListener {
            val chapter = binding.categoryAddChapterEt.text.toString()
            val parentSubfieldTypeId = categorySubfieldDialogViewModel.subfieldTypeId.value ?: return@setOnClickListener
            categorySubfieldDialogViewModel.addCategoryType(chapter, parentSubfieldTypeId, 3)
        }

        binding.categoryAddCompleteBtn.setOnClickListener {
            dismiss()
            // 폴더화면으로 넘어가기
        }
    }

    private fun observeTypeIds() {
        categorySubfieldDialogViewModel.subjectTypeId.observe(viewLifecycleOwner) { typeId ->
            Log.d("subjectTypeId", "$typeId")
        }

        categorySubfieldDialogViewModel.subfieldTypeId.observe(viewLifecycleOwner) { typeId ->
            Log.d("subfieldTypeId", "$typeId")
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}