package com.wash.washandroid.presentation.fragment.category.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.wash.washandroid.R
import com.wash.washandroid.databinding.DialogCategoryFolderBinding
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel

class CategoryFolderDialog : DialogFragment() {

    private val categoryFolderDialogViewModel: CategoryFolderDialogViewModel by activityViewModels()
    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels()

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
            categoryFolderViewModel.fetchCategoryTypes()
        }

        binding.categoryAddCompleteBtn.setOnClickListener {

            categoryFolderViewModel.setFolderId(categoryFolderDialogViewModel.folderTypeId.value ?: 0)

            // 문제 추가 api 최종 전송
            categoryFolderViewModel.postProblem()

            // 홈화면으로 넘어가기
            val navController = parentFragment?.let { fragment ->
                Navigation.findNavController(fragment.requireView())
            }
            navController?.navigate(R.id.action_navigation_problem_category_folder_to_home_fragment)
        }

        categoryFolderViewModel.apiResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                Toast.makeText(requireContext(), "문제가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "문제 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}