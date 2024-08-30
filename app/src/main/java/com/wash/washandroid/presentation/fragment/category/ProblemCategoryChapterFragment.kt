package com.wash.washandroid.presentation.fragment.category

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentProblemCategoryChapterBinding
import com.wash.washandroid.presentation.fragment.category.adapter.CategoryChapterAdapter
import com.wash.washandroid.presentation.fragment.category.dialog.CategoryChapterDialog
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryChapterViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemAddViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemManager
import com.wash.washandroid.utils.CategoryItemDecoration

class ProblemCategoryChapterFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategoryChapterBinding? = null
    private val binding: FragmentProblemCategoryChapterBinding
        get() = requireNotNull(_binding){"FragmentProblemCategoryChapterBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categoryChapterViewModel: CategoryChapterViewModel by activityViewModels()
    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }
    private val problemAddViewModel: ProblemAddViewModel by activityViewModels()

    private var categoryChapterDialog: CategoryChapterDialog? = null


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

        // Arguments에서 선택된 typeId 가져오기
        val selectedTypeId = arguments?.getInt("selectedTypeId") ?: -1
        if (selectedTypeId != -1) {
            categoryChapterViewModel.fetchCategoryTypes(selectedTypeId)
        }

        binding.categoryAdd.setOnClickListener {
            categoryChapterDialog = CategoryChapterDialog.newInstance(selectedTypeId)
            categoryChapterDialog?.show(parentFragmentManager, "CustomDialog")
        }

        val animation = ObjectAnimator.ofInt(binding.categoryProgressBar, "progress", 66, 100)
        animation.duration = 500
        animation.interpolator = AccelerateDecelerateInterpolator()

        val verticalSpaceHeight = resources.getDimensionPixelSize(R.dimen.category_item_space)
        binding.categoryChapterRv.addItemDecoration(CategoryItemDecoration(verticalSpaceHeight))

        val adapter = CategoryChapterAdapter(emptyList(), categoryChapterViewModel)
        binding.categoryChapterRv.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryChapterRv.adapter = adapter

        categoryChapterViewModel.categoryTypes.observe(viewLifecycleOwner) { types ->
            adapter.categoryTypes = types
            adapter.notifyDataSetChanged() // 모든 아이템을 업데이트하는 대신 notifyItemChanged()로 최적화 가능
            animation.start()
        }

        binding.categoryNextBtn.setOnClickListener {

            val currentIndex = problemAddViewModel.currentIndex.value ?: 0
            val photoList = problemAddViewModel.photoList.value ?: mutableListOf()

            // 로그로 현재 인덱스와 사진 경로 확인
            Log.d("problemAddViewModel", "Current Index: $currentIndex, Photo: ${photoList[currentIndex]}")

            // 인덱스가 마지막이 아니라면 다음 프로세스를 반복
            if (!problemAddViewModel.isLastIndex()) {
                problemAddViewModel.incrementIndex()
                val selectedTypeIds = categoryChapterViewModel.selectedButtonIds.value
                selectedTypeIds?.let { ids ->
                    Log.d("selectedTypeIds", "$ids")
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@let
                    with(sharedPref.edit()) {
                        putStringSet("selectedChapterTypeIds", ids.map { it.toString() }.toSet())
                        apply()
                    }
                    ProblemManager.updateSubTypeProblemData(currentIndex, ids)
                    categoryFolderViewModel.setSubTypeIds(ids)
                }
                navController.navigate(R.id.action_navigation_problem_category_chapter_to_problem_answer_fragment)
            } else {
                // 모든 사진을 처리했다면 프로세스 종료
                navController.navigate(R.id.action_navigation_problem_category_chapter_to_folder_fragment)
                problemAddViewModel.resetIndex() // 인덱스 초기화
            }
        }

        binding.categoryBackBtn.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}