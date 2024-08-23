package com.wash.washandroid.presentation.fragment.problem.old

import MypageViewModel
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
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
import com.bumptech.glide.Glide
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
    private lateinit var categoryAdapter: ProblemInfoCategoryAdapter
    private val solutionPhotoList = mutableListOf<String>()
    private val printPhotoList = mutableListOf<String>()
    private val addPhotoList = mutableListOf<String>()

    private var currentRequestCode: Int = -1

    private lateinit var photoSolutionPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPrintPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoAddPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoProblemPickerLauncher: ActivityResultLauncher<Intent>

    private var isEditing = false

    private val mypageViewModel: MypageViewModel by activityViewModels()
    private lateinit var token : String

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

        _binding = FragmentProblemInfoBinding.inflate(inflater, container, false)

        setupButtons()

        Log.d("ProblemCategorySubjectFragment", "Initializing CategorySubjectViewModel with token: $token")
        problemInfoViewModel.initialize(token)

        // problemId로 특정 문제 조회
        problemInfoViewModel.fetchProblemInfo("118")

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

        categoryAdapter = ProblemInfoCategoryAdapter(emptyList())
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
                // 사진이 존재하는지 확인
                val currentPhotoUri = problemInfoViewModel.problemPhotoUri.value
                val currentPhotoDrawable = binding.problemInfoPhoto.drawable

                if (currentPhotoDrawable != null) {
                    // 사진이 존재하면 뷰페이저로 이동
                    openPhotoPager(listOf(currentPhotoUri.toString()), 0)  // 사진은 1개이므로 position은 0
                }
            }
        }

        problemInfoViewModel.problemPhotoUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                Glide.with(this)
                    .load(uri)
                    .into(binding.problemInfoPhoto)
                binding.problemInfoPhoto.clipToOutline = true
                binding.photoDeleteLayout.clipToOutline = true
            }
        }

        problemInfoViewModel.solutionPhotoList.observe(viewLifecycleOwner) { photoList ->
            solutionPhotoList.addAll(photoList)
            photoAdapter.notifyDataSetChanged()
        }

        problemInfoViewModel.printPhotoList.observe(viewLifecycleOwner) { photoList ->
            printPhotoList.addAll(photoList)
            printAdapter.notifyDataSetChanged()
        }

        problemInfoViewModel.addPhotoList.observe(viewLifecycleOwner) { photoList ->
            addPhotoList.addAll(photoList)
            addAdapter.notifyDataSetChanged()
        }

        problemInfoViewModel.problemType.observe(viewLifecycleOwner) { problemType ->
            val categories = listOf(problemType.subject, problemType.subfield, problemType.chapter)
            categoryAdapter.updateCategories(categories)
        }

        problemInfoViewModel.answer.observe(viewLifecycleOwner) { answer ->
            answer?.let {
                binding.problemInfoAnswer.setText(answer)
            }
        }

        problemInfoViewModel.problemText.observe(viewLifecycleOwner) { problemText ->
            problemText?.let {
                binding.ocrEt.setText(problemText)
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
                    binding.problemInfoSolutionRv.smoothScrollToPosition(solutionPhotoList.size)
                }
            }
        }

        photoPrintPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    addPhoto(printPhotoList, printAdapter, photoPath)
                    binding.problemInfoPrintRv.smoothScrollToPosition(printPhotoList.size)
                }
            }
        }

        photoAddPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    addPhoto(addPhotoList, addAdapter, photoPath)
                    binding.problemInfoAddRv.smoothScrollToPosition(addPhotoList.size)
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
        binding.photoDeleteLayout.visibility = if (isEditing) View.VISIBLE else View.GONE
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