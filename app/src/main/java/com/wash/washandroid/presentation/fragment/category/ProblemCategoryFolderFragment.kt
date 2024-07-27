package com.wash.washandroid.presentation.fragment.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentProblemCategoryFolderBinding
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel

class ProblemCategoryFolderFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategoryFolderBinding? = null
    private val binding: FragmentProblemCategoryFolderBinding
        get() = requireNotNull(_binding){"FragmentProblemCategoryFolderBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categoryFolderViewModel: CategoryFolderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemCategoryFolderBinding.inflate(layoutInflater, container, false)
        binding.viewModel = categoryFolderViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val buttonList = listOf(
            binding.categoryFolder1,
            binding.categoryFolder2,
            binding.categoryFolder3,
            binding.categoryFolder4,
            binding.categoryFolder5
        )

        buttonList.forEach { button ->
            button.setOnClickListener {
                categoryFolderViewModel.onButtonClicked(button.id)
                updateButtonBackgrounds()
                categoryViewModel.submitCategoryChapter(button.text.toString())
            }
        }

        categoryFolderViewModel.selectedButtonId.observe(viewLifecycleOwner) {
            updateButtonBackgrounds()
        }

        categoryFolderViewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // TODO: 다음 버튼 활성화 로직
        }

        binding.categoryAdd.setOnClickListener {
            // TODO: 추가하기 버튼 로직
            categoryViewModel.submitAddOption()
        }

        binding.categoryFolderCheckBtn.setOnClickListener {
            // TODO: 문제 저장 후 이동 로직
        }

        binding.categoryBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_folder_to_chapter_fragment)
        }
    }

    private fun updateButtonBackgrounds() {
        val buttonList = listOf(
            binding.categoryFolder1,
            binding.categoryFolder2,
            binding.categoryFolder3,
            binding.categoryFolder4,
            binding.categoryFolder5
        )
        buttonList.forEach { button ->
            button.setBackgroundResource(categoryFolderViewModel.getButtonBackground(button.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}