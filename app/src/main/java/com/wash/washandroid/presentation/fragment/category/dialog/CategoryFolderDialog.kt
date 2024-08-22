package com.wash.washandroid.presentation.fragment.category.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.wash.washandroid.R
import com.wash.washandroid.databinding.DialogCategoryFolderBinding

class CategoryFolderDialog : DialogFragment() {

    private val categoryFolderDialogViewModel: CategoryFolderDialogViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DialogCategoryFolderBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_category_folder, container, false
        )
        binding.lifecycleOwner = this
        binding.viewModel = categoryFolderDialogViewModel

        setupListeners(binding)

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

    private fun setupListeners(binding: DialogCategoryFolderBinding) {
        binding.categoryAddCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.categoryAddFolderCompleteBtn.setOnClickListener {
            val folder = binding.categoryAddFolderEt.text.toString()
            categoryFolderDialogViewModel.addCategoryType(folder)
        }

        binding.categoryAddCompleteBtn.setOnClickListener {
            dismiss()
            // 문제 추가하기 api 최종 전송
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}