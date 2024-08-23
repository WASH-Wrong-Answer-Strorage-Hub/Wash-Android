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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentProblemCategorySubjectBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.category.adapter.CategorySubjectAdapter
import com.wash.washandroid.presentation.fragment.category.dialog.CategorySubjectDialog
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubjectViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel
import com.wash.washandroid.utils.CategoryItemDecoration

class ProblemCategorySubjectFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategorySubjectBinding? = null
    private val binding: FragmentProblemCategorySubjectBinding
        get() = requireNotNull(_binding){"FragmentProblemCategorySubjectBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categorySubjectViewModel: CategorySubjectViewModel by viewModels()

    private val mypageViewModel: MypageViewModel by activityViewModels()
    private lateinit var token : String

    private lateinit var adapter: CategorySubjectAdapter
    private var categorySubjectDialog: CategorySubjectDialog? = null

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
                navController.navigate(R.id.action_navigation_problem_category_subject_to_subfield_fragment, bundle)
            }
        }

        binding.skipBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_category_subject_to_folder_fragment)
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