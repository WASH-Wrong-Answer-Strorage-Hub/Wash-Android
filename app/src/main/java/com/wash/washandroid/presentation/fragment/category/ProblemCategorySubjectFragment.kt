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
import com.wash.washandroid.databinding.FragmentProblemCategorySubjectBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubjectViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel

class ProblemCategorySubjectFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategorySubjectBinding? = null
    private val binding: FragmentProblemCategorySubjectBinding
        get() = requireNotNull(_binding){"FragmentProblemCategorySubjectBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categorySubjectViewModel: CategorySubjectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemCategorySubjectBinding.inflate(layoutInflater, container, false)
        binding.categorySubjectViewModel = categorySubjectViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        (requireActivity() as MainActivity).hideBottomNavigation(true)

        val animation = ObjectAnimator.ofInt(binding.categoryProgressBar, "progress", 0, 33)
        animation.duration = 500
        animation.interpolator = AccelerateDecelerateInterpolator()

        val buttonList = listOf(
            binding.categoryLanguage,
            binding.categoryMath,
            binding.categoryEnglish,
            binding.categorySociety,
            binding.categoryScience,
            binding.categoryAdd
        )

        buttonList.forEach { button ->
            button.setOnClickListener {
                categorySubjectViewModel.onButtonClicked(button.id)
                updateButtonBackgrounds()
                categoryViewModel.submitCategorySubject(button.text.toString())
                animation.start()
            }
        }

        categorySubjectViewModel.selectedButtonId.observe(viewLifecycleOwner) {
            updateButtonBackgrounds()
        }

        categorySubjectViewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // TODO: 다음 버튼 활성화 로직
        }

        binding.categoryNextBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_subject_to_subfield_fragment)
        }

        binding.skipBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_subject_to_folder_fragment)
        }
    }

    private fun updateButtonBackgrounds() {
        val buttonList = listOf(
            binding.categoryLanguage,
            binding.categoryMath,
            binding.categoryEnglish,
            binding.categorySociety,
            binding.categoryScience,
            binding.categoryAdd
        )
        buttonList.forEach { button ->
            button.setBackgroundResource(categorySubjectViewModel.getButtonBackground(button.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}