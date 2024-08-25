package com.wash.washandroid.presentation.fragment.category

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import com.wash.washandroid.utils.CategoryItemDecoration
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProblemCategoryFolderFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemCategoryFolderBinding? = null
    private val binding: FragmentProblemCategoryFolderBinding
        get() = requireNotNull(_binding){"FragmentProblemCategoryFolderBinding -> null"}
    private val categoryViewModel: CategoryViewModel by activityViewModels()

    private var categoryFolderDialog: CategoryFolderDialog? = null

    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels {
        CategoryFolderViewModelFactory(
            ProblemRepository(
                NetworkModule
                    .getClient()
                    .create(ProblemApiService::class.java)
            )
        )
    }


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

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val selectedSubjectTypeId = sharedPref?.getInt("selectedSubjectTypeId", -1)
        val selectedSubfieldTypeId = sharedPref?.getInt("selectedSubfieldTypeId", -1)
        val selectedChapterTypeId = sharedPref?.getInt("selectedChapterTypeId", -1)

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
            categoryFolderViewModel.selectedButtonId.value?.let { typeId ->
                Log.d("typeId", "$typeId")
            }

            // 문제 추가 api 최종 전송
            categoryFolderViewModel.postProblem()

            categoryFolderViewModel.apiResponse.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "문제가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "문제 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_problem_category_folder, true)
                .build()
            navController.navigate(R.id.action_navigation_problem_category_folder_to_home_fragment, null, navOptions)
        }

        binding.categoryBackBtn.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream for URI: $uri")
        val file = File(context.cacheDir, "temp_image_file_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }

    private fun processAndSetProblemData() {
//        val problemData = ProblemData // 문제 데이터를 수집하는 메서드
//
//        val problemImageFile = problemData.problemImageUri?.let { uri ->
//            uriToFile(requireContext(), uri)
//        }
//        val solutionImageFiles = problemData.solutionImageUris.map { uri ->
//            uriToFile(requireContext(), uri)
//        }
//        val passageImageFiles = problemData.passageImageUris.map { uri ->
//            uriToFile(requireContext(), uri)
//        }
//        val additionalImageFiles = problemData.additionalImageUris.map { uri ->
//            uriToFile(requireContext(), uri)
//        }
//
//        // ViewModel에 데이터 설정
//        categoryFolderViewModel.setProblemData(problemData)
//
//        // 문제를 API에 제출
//        categoryFolderViewModel.postProblem()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}