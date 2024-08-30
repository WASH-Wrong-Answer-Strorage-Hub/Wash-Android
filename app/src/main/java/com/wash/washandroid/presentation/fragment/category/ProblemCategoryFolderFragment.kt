package com.wash.washandroid.presentation.fragment.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentProblemCategoryFolderBinding
import com.wash.washandroid.presentation.fragment.category.adapter.CategoryFolderAdapter
import com.wash.washandroid.presentation.fragment.category.dialog.CategoryFolderDialog
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryViewModel
import com.wash.washandroid.utils.CategoryItemDecoration
import kotlinx.coroutines.launch

class ProblemCategoryFolderFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategoryFolderBinding? = null
    private val binding: FragmentProblemCategoryFolderBinding
        get() = requireNotNull(_binding){"FragmentProblemCategoryFolderBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    private var categoryFolderDialog: CategoryFolderDialog? = null

    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemCategoryFolderBinding.inflate(layoutInflater, container, false)
        binding.viewModel = categoryFolderViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        categoryFolderViewModel.fetchCategoryTypes()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val selectedSubjectTypeId = sharedPref?.getInt("selectedSubjectTypeId", -1) ?: -1
        val selectedSubfieldTypeId = sharedPref?.getInt("selectedSubfieldTypeId", -1) ?: -1
        val selectedChapterTypeId = sharedPref?.getInt("selectedChapterTypeId", -1) ?: -1

        Log.d("subjectType", "Received typeId: $selectedSubjectTypeId")
        Log.d("subfieldType", "Received typeId: $selectedSubfieldTypeId")
        Log.d("chapterType", "Received typeId: $selectedChapterTypeId")

        binding.categoryAdd.setOnClickListener {
            categoryFolderDialog = CategoryFolderDialog()
            categoryFolderDialog?.show(parentFragmentManager, "CustomDialog")
        }

        val verticalSpaceHeight = resources.getDimensionPixelSize(R.dimen.category_item_space)
        binding.categoryFolderRv.addItemDecoration(CategoryItemDecoration(verticalSpaceHeight))

        val adapter = CategoryFolderAdapter(emptyList(), categoryFolderViewModel)
        binding.categoryFolderRv.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryFolderRv.adapter = adapter

        categoryFolderViewModel.categoryTypes.observe(viewLifecycleOwner) { types ->
            adapter.folderTypes = types
            adapter.notifyDataSetChanged() // 모든 아이템을 업데이트하는 대신 notifyItemChanged()로 최적화 가능
        }

        binding.categoryFolderCheckBtn.setOnClickListener {

//            categoryFolderViewModel.selectedButtonId.value?.let { typeId ->
//                Log.d("typeId", "$typeId")
//                categoryFolderViewModel.setFolderId(typeId)
//            }
//
//            val problemData = categoryFolderViewModel.problemData.value
//            Log.d("ProblemData", "ProblemData: $problemData")
//
//            categoryFolderViewModel.selectedButtonId.value?.let { typeId ->
//                Log.d("typeId", "$typeId")
//            }
//
//            // 문제 추가 API 호출
//            viewLifecycleOwner.lifecycleScope.launch {
//                categoryFolderViewModel.postProblem() // 비동기 호출
//
//                // API 응답 관찰
//                categoryFolderViewModel.apiResponse.observe(viewLifecycleOwner) { response ->
//                    if (response != null) {
//                        if (response.isSuccessful) {
//                            Toast.makeText(requireContext(), "문제가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
//                            navController.navigate(R.id.action_navigation_problem_category_folder_to_home_fragment)
//                        } else {
//                            Toast.makeText(requireContext(), "문제 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show()
//                            Log.e("API Error", "Response Failed: ${response.errorBody()?.string()}")
//                            navController.navigate(R.id.action_navigation_problem_category_folder_to_home_fragment)
//                        }
//                    }
//                }
//            }
            navController.navigate(R.id.action_navigation_problem_category_folder_to_home_fragment)

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