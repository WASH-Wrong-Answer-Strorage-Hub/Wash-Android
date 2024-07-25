package com.wash.washandroid.presentation.fragment.category

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentProblemCategorySubfieldBinding
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubfieldViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel

class ProblemCategorySubfieldFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategorySubfieldBinding? = null
    private val binding: FragmentProblemCategorySubfieldBinding
        get() = requireNotNull(_binding){"FragmentProblemCategorySubfieldBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categorySubfieldViewModel: CategorySubfieldViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemCategorySubfieldBinding.inflate(layoutInflater, container, false)
        binding.categorySubfieldViewModel = categorySubfieldViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val animation = ObjectAnimator.ofInt(binding.categoryProgressBar, "progress", 33, 66)
        animation.duration = 500
        animation.interpolator = AccelerateDecelerateInterpolator()

        val buttonList = listOf(
            binding.categorySubfield1,
            binding.categorySubfield2,
            binding.categorySubfield3,
            binding.categorySubfield4
        )

        buttonList.forEach { button ->
            button.setOnClickListener {
                categorySubfieldViewModel.onButtonClicked(button.id)
                updateButtonBackgrounds()
                categoryViewModel.submitCategorySubfield(button.text.toString())
                animation.start()
            }
        }

        categorySubfieldViewModel.selectedButtonId.observe(viewLifecycleOwner) {
            updateButtonBackgrounds()
        }

        categorySubfieldViewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // TODO: 다음 버튼 활성화 로직
        }

        binding.categoryAdd.setOnClickListener {
            // TODO: 추가하기 버튼 로직
            categoryViewModel.submitAddOption()
        }

        binding.categoryNextBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_subfield_to_chapter_fragment)
        }

        binding.categoryBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_subfield_to_subject_fragment)
        }
    }

    private fun updateButtonBackgrounds() {
        val buttonList = listOf(
            binding.categorySubfield1,
            binding.categorySubfield2,
            binding.categorySubfield3,
            binding.categorySubfield4
        )
        buttonList.forEach { button ->
            button.setBackgroundResource(categorySubfieldViewModel.getButtonBackground(button.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}