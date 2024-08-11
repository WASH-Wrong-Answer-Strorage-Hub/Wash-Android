package com.wash.washandroid.presentation.fragment.problem.old

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import com.wash.washandroid.databinding.FragmentProblemInfoBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.problem.PhotoAdapter
import com.wash.washandroid.utils.ProblemInfoDecoration

class ProblemInfoFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemInfoBinding? = null
    private val binding: FragmentProblemInfoBinding
        get() = requireNotNull(_binding){"FragmentProblemInfoBinding -> null"}

    private val problemInfoViewModel: ProblemInfoViewModel by activityViewModels()

    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var printAdapter: PhotoAdapter
    private lateinit var addAdapter: PhotoAdapter
    private val solutionPhotoList = mutableListOf<String>()
    private val printPhotoList = mutableListOf<String>()
    private val addPhotoList = mutableListOf<String>()

    private var currentRequestCode: Int = -1

    private lateinit var photoSolutionPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPrintPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoAddPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoProblemPickerLauncher: ActivityResultLauncher<Intent>

    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProblemInfoBinding.inflate(inflater, container, false)

        setupButtons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)
        navController = Navigation.findNavController(view)

        binding.floatingActionButton.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_info_to_chat_fragment)
        }
        startVibrationAnimation(binding.floatingActionButton)


        val categories = listOf("수학", "미적분", "급수")
        val categoryAdapter = ProblemInfoCategoryAdapter(categories)

        binding.categoryRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRv.adapter = categoryAdapter


        binding.photoDeleteLayout.setOnClickListener {
            binding.problemInfoPhoto.setImageURI(null)
            binding.photoDeleteLayout.visibility = View.INVISIBLE // 이미지 삭제하면서 삭제버튼 비활성화
        }

        binding.problemInfoPhotoAdd.setOnClickListener {
            if (isEditing) {
                openProblemGallery()
            } else {
                binding.problemInfoPhotoAdd.isClickable = false
            }
        }

        problemInfoViewModel.problemPhotoUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.problemInfoPhoto.setImageURI(it)
                binding.problemInfoPhoto.clipToOutline = true
                binding.photoDeleteLayout.clipToOutline = true
            }
        }

        photoProblemPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    problemInfoViewModel.setProblemPhotoUri(uri)
                    binding.photoDeleteLayout.visibility = View.VISIBLE // 이미지 설정하면서 삭제버튼 활성화
                }
            }
        }


        photoSolutionPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    addPhoto(solutionPhotoList, photoAdapter, photoPath)
                }
            }
        }

        photoPrintPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    addPhoto(printPhotoList, printAdapter, photoPath)
                }
            }
        }

        photoAddPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    addPhoto(addPhotoList, addAdapter, photoPath)
                }
            }
        }

        setupRecyclerView(binding.problemInfoSolutionRv, solutionPhotoList) { openGallery(1) }
        setupRecyclerView(binding.problemInfoPrintRv, printPhotoList) { openGallery(2) }
        setupRecyclerView(binding.problemInfoAddRv, addPhotoList) { openGallery(3) }

        val horizontalSpaceHeight = resources.getDimensionPixelSize(R.dimen.item_space)
        binding.categoryRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))
        binding.problemInfoSolutionRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))
        binding.problemInfoPrintRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))
        binding.problemInfoAddRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))

        // 화면 빈 공간 클릭시 키보드 숨김 처리
        binding.root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                v.performClick()
            }
            false
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun startVibrationAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, 10f, -10f, 10f, -10f, 10f, 0f)
        animator.duration = 1500
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()
    }

    private fun setupButtons() {
        // 편집 버튼 클릭 시
        binding.problemEditBtnLayout.setOnClickListener {
            toggleEditMode()
            binding.photoDeleteLayout.visibility = View.VISIBLE
        }
        // 완료 버튼 클릭 시
        binding.problemEditBtnLayout.setOnClickListener {
            toggleEditMode()
            binding.photoDeleteLayout.visibility = View.INVISIBLE
        }
    }

    private fun toggleEditMode() {
        isEditing = !isEditing

        // 풀이, 지문, 추가 사진 버튼 표시
        photoAdapter.setAddButtonVisible(isEditing)
        addAdapter.setAddButtonVisible(isEditing)
        printAdapter.setAddButtonVisible(isEditing)

        // 편집 모드일 때 완료 버튼 표시, 그렇지 않으면 편집 버튼 표시
        binding.problemEditBtn.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.problemEditCompleteBtn.visibility = if (isEditing) View.VISIBLE else View.GONE

        // 편집 모드일 때 정답, 메모 EditText 및 문제 사진 추가 버튼 활성화
        binding.problemInfoAnswer.isEnabled = isEditing
        binding.problemInfoMemo.isEnabled = isEditing
        binding.problemInfoPhotoAdd.isClickable = isEditing
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        photoList: MutableList<String>,
        onAddPhotoClick: () -> Unit
    ) {
        val adapter = PhotoAdapter(requireContext(), photoList, onAddPhotoClick) { position ->
            openPhotoPager(photoList, position)
            Log.d("position", "$position")
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        when (recyclerView.id) {
            R.id.problem_info_solution_rv -> photoAdapter = adapter
            R.id.problem_info_print_rv -> printAdapter = adapter
            R.id.problem_info_add_rv -> addAdapter = adapter
        }
    }

    private fun openPhotoPager(photoList: List<String>, initialPosition: Int) {
        // 뷰모델에 데이터 설정
        problemInfoViewModel.setPhotoUris(photoList)
        problemInfoViewModel.setSelectedPhotoPosition(initialPosition)

        // 네비게이션으로 뷰페이저로 이동
        navController.navigate(R.id.action_navigation_problem_info_to_photo_slider_fragment)
    }

    private fun openProblemGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        photoProblemPickerLauncher.launch(intent)
    }

    private fun openGallery(requestCode: Int) {
        currentRequestCode = requestCode
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        when (requestCode) {
            1 -> photoSolutionPickerLauncher.launch(intent)
            2 -> photoPrintPickerLauncher.launch(intent)
            3 -> photoAddPickerLauncher.launch(intent)
        }
    }

    private fun addPhoto(photoList: MutableList<String>, adapter: PhotoAdapter, photoPath: String) {
        photoList.add(photoPath)
        adapter.notifyItemInserted(photoList.size - 1)
        adapter.notifyItemChanged(photoList.size)
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}