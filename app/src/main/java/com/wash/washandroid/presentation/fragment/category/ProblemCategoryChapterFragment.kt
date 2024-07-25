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
import com.wash.washandroid.databinding.FragmentProblemCategoryChapterBinding
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryChapterViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel

class ProblemCategoryChapterFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategoryChapterBinding? = null
    private val binding: FragmentProblemCategoryChapterBinding
        get() = requireNotNull(_binding){"FragmentProblemCategoryChapterBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categoryChapterViewModel: CategoryChapterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemCategoryChapterBinding.inflate(layoutInflater, container, false)
        binding.categoryChapterViewModel = categoryChapterViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val animation = ObjectAnimator.ofInt(binding.categoryProgressBar, "progress", 66, 100)
        animation.duration = 500
        animation.interpolator = AccelerateDecelerateInterpolator()

        val buttonList = listOf(
            binding.categoryChapter1,
            binding.categoryChapter2,
            binding.categoryChapter3,
            binding.categoryChapter4,
            binding.categoryChapter5,
            binding.categoryChapter6,
            binding.categoryChapter7
        )

        buttonList.forEach { button ->
            button.setOnClickListener {
                categoryChapterViewModel.onButtonClicked(button.id)
                updateButtonBackgrounds()
                categoryViewModel.submitCategoryChapter(button.text.toString())
                animation.start()
            }
        }

        categoryChapterViewModel.selectedButtonId.observe(viewLifecycleOwner) {
            updateButtonBackgrounds()
        }

        categoryChapterViewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            // TODO: 다음 버튼 활성화 로직
        }

        binding.categoryAdd.setOnClickListener {
            // TODO: 추가하기 버튼 로직
            categoryViewModel.submitAddOption()
        }

        binding.categoryNextBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_chapter_to_folder_fragment)
        }

        binding.categoryBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_chapter_to_subfield_fragment)
        }
    }

    private fun updateButtonBackgrounds() {
        val buttonList = listOf(
            binding.categoryChapter1,
            binding.categoryChapter2,
            binding.categoryChapter3,
            binding.categoryChapter4,
            binding.categoryChapter5,
            binding.categoryChapter6,
            binding.categoryChapter7
        )
        buttonList.forEach { button ->
            button.setBackgroundResource(categoryChapterViewModel.getButtonBackground(button.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}