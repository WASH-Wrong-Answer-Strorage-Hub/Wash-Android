package com.wash.washandroid.presentation.fragment.category

import MypageViewModel
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
import com.wash.washandroid.databinding.FragmentProblemCategorySubjectBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.category.adapter.CategorySubjectAdapter
import com.wash.washandroid.presentation.fragment.category.dialog.CategorySubjectDialog
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryChapterViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubfieldViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubjectViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemAddViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemManager
import com.wash.washandroid.utils.CategoryItemDecoration

class ProblemCategorySubjectFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategorySubjectBinding? = null
    private val binding: FragmentProblemCategorySubjectBinding
        get() = requireNotNull(_binding){"FragmentProblemCategorySubjectBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categorySubjectViewModel: CategorySubjectViewModel by activityViewModels()
    private val categorySubfieldViewModel: CategorySubfieldViewModel by activityViewModels()
    private val categoryChapterViewModel: CategoryChapterViewModel by activityViewModels()
    private val problemAddViewModel: ProblemAddViewModel by activityViewModels()
    private val mypageViewModel: MypageViewModel by activityViewModels()
    private lateinit var token : String

    private lateinit var adapter: CategorySubjectAdapter
    private var categorySubjectDialog: CategorySubjectDialog? = null
    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        token = mypageViewModel.getRefreshToken() ?: ""
        Log.d("ProblemCategorySubjectFragment", "Retrieved token: $token")
    }

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

        Log.d("ProblemCategorySubjectFragment", "Initializing CategorySubjectViewModel with token: $token")
        categorySubjectViewModel.initialize(token)
        categorySubjectViewModel.fetchCategoryTypes()

        setupRecyclerView()  // 리사이클러뷰 설정 함수 호출

        setupListeners()  // 리스너 설정 함수 호출

        observeCategoryTypes()  // 데이터 변경 관찰 함수 호출
    }

    private fun setupRecyclerView() {
        adapter = CategorySubjectAdapter(emptyList(), categorySubjectViewModel)
        binding.categorySubjectRv.layoutManager = LinearLayoutManager(requireContext())
        binding.categorySubjectRv.adapter = adapter

        val verticalSpaceHeight = resources.getDimensionPixelSize(R.dimen.category_item_space)
        binding.categorySubjectRv.addItemDecoration(CategoryItemDecoration(verticalSpaceHeight))
    }

    private fun setupListeners() {
        binding.categoryAddBtn.setOnClickListener {
            categorySubjectDialog = CategorySubjectDialog()
            categorySubjectDialog?.show(parentFragmentManager, "CustomDialog")
        }

        binding.categoryNextBtn.setOnClickListener {
            categorySubjectViewModel.selectedButtonId.value?.let { typeId ->
                Log.d("typeId", "$typeId")
                val bundle = Bundle().apply {
                    putInt("selectedTypeId", typeId)
                }
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@let
                with (sharedPref.edit()) {
                    putInt("selectedSubjectTypeId", typeId)
                    apply()
                }
                val currentIndex = problemAddViewModel.currentIndex.value ?: 0
                ProblemManager.updateMainTypeProblemData(currentIndex, typeId)
                categoryFolderViewModel.setMainTypeId(typeId)
                navController.navigate(R.id.action_navigation_problem_category_subject_to_subfield_fragment, bundle)
            }
        }

        binding.noTypeBtn.setOnClickListener {
            val currentIndex = problemAddViewModel.currentIndex.value ?: 0
            val photoList = problemAddViewModel.photoList.value ?: mutableListOf()

            // 로그로 현재 인덱스와 사진 경로 확인
            Log.d("problemAddViewModel", "Current Index: $currentIndex, Photo: ${photoList[currentIndex]}")

            ProblemManager.updateMainTypeProblemData(currentIndex, null)
            ProblemManager.updateMidTypeProblemData(currentIndex, null)
            ProblemManager.updateSubTypeProblemData(currentIndex, null)


            // 인덱스가 마지막이 아니라면 다음 프로세스를 반복
            if (!problemAddViewModel.isLastIndex()) {
                problemAddViewModel.incrementIndex()
                navController.navigate(R.id.action_navigation_problem_category_subject_to_problem_answer_fragment)
            } else {
                // 모든 사진을 처리했다면 프로세스 종료
                navController.navigate(R.id.action_navigation_problem_category_subject_to_folder_fragment)
                problemAddViewModel.resetIndex() // 인덱스 초기화
            }
        }

        binding.skipBtn.setOnClickListener {

            val currentIndex = problemAddViewModel.currentIndex.value ?: 0
            val photoList = problemAddViewModel.photoList.value ?: mutableListOf()

            // 로그로 현재 인덱스와 사진 경로 확인
            Log.d("problemAddViewModel", "Current Index: $currentIndex, Photo: ${photoList[currentIndex]}")

            val mainTypeId = categorySubjectViewModel.selectedButtonId.value
            val midTypeId = categorySubfieldViewModel.selectedButtonId.value
            val subTypeIds = categoryChapterViewModel.selectedButtonIds.value
            Log.d("mainTypeId", mainTypeId.toString())
            Log.d("midTypeId", midTypeId.toString())
            Log.d("subTypeIds", subTypeIds.toString())

            mainTypeId?.let {
                ProblemManager.updateMainTypeProblemData(currentIndex, it)
            }
            midTypeId?.let {
                ProblemManager.updateMidTypeProblemData(currentIndex, it)
            }
            subTypeIds?.let {
                ProblemManager.updateSubTypeProblemData(currentIndex, it)
            }

            // 인덱스가 마지막이 아니라면 다음 프로세스를 반복
            if (!problemAddViewModel.isLastIndex()) {
                problemAddViewModel.incrementIndex()
                navController.navigate(R.id.action_navigation_problem_category_subject_to_problem_answer_fragment)
            } else {
                // 모든 사진을 처리했다면 프로세스 종료
                navController.navigate(R.id.action_navigation_problem_category_subject_to_folder_fragment)
                problemAddViewModel.resetIndex() // 인덱스 초기화
            }
        }

        binding.categoryBackBtn.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun observeCategoryTypes() {
        categorySubjectViewModel.categoryTypes.observe(viewLifecycleOwner) { types ->
            Log.d("ProblemCategorySubjectFragment", "Category types updated: $types")
            adapter.categoryTypes = types
            adapter.notifyDataSetChanged()
            startProgressBarAnimation()  // ProgressBar 애니메이션 함수 호출
        }
    }

    private fun startProgressBarAnimation() {
        val animation = ObjectAnimator.ofInt(binding.categoryProgressBar, "progress", 0, 33)
        animation.duration = 500
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}