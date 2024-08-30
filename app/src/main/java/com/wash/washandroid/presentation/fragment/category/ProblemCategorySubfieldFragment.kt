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
import com.wash.washandroid.databinding.FragmentProblemCategorySubfieldBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.category.adapter.CategorySubfieldAdapter
import com.wash.washandroid.presentation.fragment.category.dialog.CategorySubfieldDialog
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategorySubfieldViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemAddViewModel
import com.wash.washandroid.presentation.fragment.problem.add.ProblemManager
import com.wash.washandroid.utils.CategoryItemDecoration

class ProblemCategorySubfieldFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategorySubfieldBinding? = null
    private val binding: FragmentProblemCategorySubfieldBinding
        get() = requireNotNull(_binding){"FragmentProblemCategorySubfieldBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val categorySubfieldViewModel: CategorySubfieldViewModel by activityViewModels()
    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }
    private val problemAddViewModel: ProblemAddViewModel by activityViewModels()

    private var categorySubfieldDialog: CategorySubfieldDialog? = null

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

        // Arguments에서 선택된 typeId 가져오기
        val selectedTypeId = arguments?.getInt("selectedTypeId") ?: -1
        if (selectedTypeId != -1) {
            categorySubfieldViewModel.fetchCategoryTypes(selectedTypeId)
        }

        binding.categoryAdd.setOnClickListener {
            categorySubfieldDialog = CategorySubfieldDialog.newInstance(selectedTypeId)
            categorySubfieldDialog?.show(parentFragmentManager, "CustomDialog")
        }

        val animation = ObjectAnimator.ofInt(binding.categoryProgressBar, "progress", 33, 66)
        animation.duration = 500
        animation.interpolator = AccelerateDecelerateInterpolator()

        val verticalSpaceHeight = resources.getDimensionPixelSize(R.dimen.category_item_space)
        binding.categorySubfieldRv.addItemDecoration(CategoryItemDecoration(verticalSpaceHeight))

        val adapter = CategorySubfieldAdapter(emptyList(), categorySubfieldViewModel)
        binding.categorySubfieldRv.layoutManager = LinearLayoutManager(requireContext())
        binding.categorySubfieldRv.adapter = adapter

        categorySubfieldViewModel.categoryTypes.observe(viewLifecycleOwner) { types ->
            adapter.categoryTypes = types
            adapter.notifyDataSetChanged() // 모든 아이템을 업데이트하는 대신 notifyItemChanged()로 최적화 가능
            animation.start()
        }

        binding.categoryNextBtn.setOnClickListener {
            categorySubfieldViewModel.selectedButtonId.value?.let { typeId ->
                Log.d("typeId", "$typeId")
                val bundle = Bundle().apply {
                    putInt("selectedTypeId", typeId)
                }
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@let
                with (sharedPref.edit()) {
                    putInt("selectedSubfieldTypeId", typeId)
                    apply()
                }
                val currentIndex = problemAddViewModel.currentIndex.value ?: 0
                ProblemManager.updateMidTypeProblemData(currentIndex, typeId)
                categoryFolderViewModel.setMidTypeId(typeId)
                navController.navigate(R.id.action_navigation_problem_category_subfield_to_chapter_fragment, bundle)
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