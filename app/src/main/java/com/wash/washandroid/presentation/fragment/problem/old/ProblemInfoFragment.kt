package com.wash.washandroid.presentation.fragment.problem.old

import MypageViewModel
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.wash.washandroid.databinding.FragmentProblemInfoBinding
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.problem.PhotoAdapter
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import com.wash.washandroid.utils.ProblemInfoDecoration
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProblemInfoFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemInfoBinding? = null
    private val binding: FragmentProblemInfoBinding
        get() = requireNotNull(_binding){"FragmentProblemInfoBinding -> null"}

    private val problemInfoViewModel: ProblemInfoViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }
    private val mypageViewModel: MypageViewModel by activityViewModels()

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

        // refresh token 주입
        problemInfoViewModel.initialize(token)

        // problemId로 특정 문제 조회
        problemInfoViewModel.problemId.value?.let { problemId ->
            problemInfoViewModel.fetchProblemInfo(problemId.toString())
            Log.d("ProblemInfoFragment", "연결 $problemId")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        (requireActivity() as MainActivity).hideBottomNavigation(true)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            clearListsAndNotify()
            findNavController().navigateUp()
        }

        binding.floatingActionButton.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_info_to_chat_fragment)
        }
        startVibrationAnimation(binding.floatingActionButton)


        categoryAdapter = ProblemInfoCategoryAdapter(emptyList())
        binding.categoryRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRv.adapter = categoryAdapter

        binding.problemInfoBackBtn.setOnClickListener {
            clearListsAndNotify()

            navController.navigateUp()
        }

        binding.photoDeleteLayout.setOnClickListener {
            Glide.with(this)
                .clear(binding.problemInfoPhoto)
            problemInfoViewModel.setProblemPhotoUri(null)
            binding.photoDeleteLayout.visibility = View.INVISIBLE
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

        binding.problemEditCompleteBtn.setOnClickListener {
            if (isInputValid()) {
                val problemData = collectProblemData()
                Log.d("problemData", problemData.toString())
                problemInfoViewModel.setProblemData(problemData)

                val problemDataValue = problemInfoViewModel.problemData.value
                Log.d("ProblemData", "ProblemData: $problemDataValue")

                Toast.makeText(requireContext(), "문제 정보를 갱신중입니다...", Toast.LENGTH_SHORT).show()

                viewLifecycleOwner.lifecycleScope.launch {
                    problemInfoViewModel.clearProblemDetails() // 기존 데이터 초기화
                    problemInfoViewModel.editProblem() // 비동기 호출

                    problemInfoViewModel.apiResponse.observe(viewLifecycleOwner) { response ->
                        if (response != null) {
                            if (response.isSuccessful) {
                                // 기존 리스트 초기화
                                clearListsAndNotify()

                                problemInfoViewModel.problemId.value?.let { problemId ->
                                    problemInfoViewModel.fetchProblemInfo(problemId.toString())
                                    Log.d("ProblemInfoFragment", "연결 $problemId")
                                }

                                Toast.makeText(requireContext(), "문제가 성공적으로 편집되었습니다.", Toast.LENGTH_SHORT).show()
                                toggleEditMode()
                            } else {
                                Log.e("API Error", "Response Failed: ${response.errorBody()?.string()}")
                                Toast.makeText(requireContext(), "문제 편집에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "사진을 추가하고 정답을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        problemInfoViewModel.problemType.observe(viewLifecycleOwner) { problemType ->
            val categories = listOf(problemType.subject, problemType.subfield, problemType.chapter)
            categoryAdapter.updateCategories(categories)
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

        problemInfoViewModel.problemText.observe(viewLifecycleOwner) { problemText ->
            binding.ocrEt.setText(problemText)
        }

        problemInfoViewModel.answer.observe(viewLifecycleOwner) { answer ->
            answer?.let {
                binding.problemInfoAnswer.setText(answer)
            }
        }

        problemInfoViewModel.memo.observe(viewLifecycleOwner) { memo ->
            memo?.let {
                binding.problemInfoMemo.setText(memo)
            }
        }

        problemInfoViewModel.solutionPhotoList.observe(viewLifecycleOwner) { photoList ->
            solutionPhotoList.addAll(photoList)
            photoAdapter.notifyDataSetChanged()
            Log.d("solutionPhotoList", "$solutionPhotoList")
        }

        problemInfoViewModel.printPhotoList.observe(viewLifecycleOwner) { photoList ->
            printPhotoList.addAll(photoList)
            printAdapter.notifyDataSetChanged()
            Log.d("printPhotoList", "$printPhotoList")
        }

        problemInfoViewModel.addPhotoList.observe(viewLifecycleOwner) { photoList ->
            addPhotoList.addAll(photoList)
            addAdapter.notifyDataSetChanged()
            Log.d("addPhotoList", "$addPhotoList")
        }

        photoProblemPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    Log.d("photoPath", photoPath)
                    problemInfoViewModel.setProblemPhotoUri(uri)
                    binding.photoDeleteLayout.visibility = View.VISIBLE // 이미지 설정하면서 삭제버튼 활성화
                }
            }
        }


        photoSolutionPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    Log.d("photoPath", photoPath)
                    addPhoto(solutionPhotoList, photoAdapter, photoPath)
                    binding.problemInfoSolutionRv.smoothScrollToPosition(solutionPhotoList.size)
                }
            }
        }

        photoPrintPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    Log.d("photoPath", photoPath)
                    addPhoto(printPhotoList, printAdapter, photoPath)
                    binding.problemInfoPrintRv.smoothScrollToPosition(printPhotoList.size)
                }
            }
        }

        photoAddPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    Log.d("photoPath", photoPath)
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

    private fun collectProblemData(): ProblemData {
        val problemImageUri = problemInfoViewModel.problemPhotoUri.value?.let { Uri.parse(it.toString()) }
        val problemImageUrl = problemImageUri?.let { if (!isUrl(it.toString())) uploadImage(convertUriToFile(it)) else it.toString() }

        val solutionImageUrls = solutionPhotoList.map { uri ->
            if (!isUrl(uri)) uploadImage(convertUriToFile(Uri.parse(uri))) else uri
        }

        val passageImageUrls = printPhotoList.map { uri ->
            if (!isUrl(uri)) uploadImage(convertUriToFile(Uri.parse(uri))) else uri
        }

        val additionalImageUrls = addPhotoList.map { uri ->
            if (!isUrl(uri)) uploadImage(convertUriToFile(Uri.parse(uri))) else uri
        }

        val problemText = binding.ocrEt.text.toString()
        val answer = binding.problemInfoAnswer.text.toString()
        val memo = binding.problemInfoMemo.text.toString()

        val problemData = ProblemData(
            problemImageUri = problemImageUrl,
            solutionImageUris = solutionImageUrls,
            passageImageUris = passageImageUrls,
            additionalImageUris = additionalImageUrls,
            problemText = problemText,
            answer = answer,
            memo = memo
        )

        Log.d("ProblemData", "Problem Data collected: $problemData")

        return problemData
    }

    private fun uploadImage(file: File): String? {
        return try {
            // 파일을 MultipartBody.Part로 변환
            val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Retrofit을 이용해 API 호출
            val retrofit = NetworkModule.getClient()
            val apiService: ProblemApiService = retrofit.create(ProblemApiService::class.java)

            runBlocking {
                val response = apiService.postImageUrl(imagePart)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result // URL 반환
                } else {
                    Log.e("uploadImage", "Image upload failed: ${response.message()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("uploadImage", "Failed to upload image: ${e.message}")
            null
        }
    }

    private fun convertUriToFile(uri: Uri): File {
        return try {
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Cannot open input stream from URI")
            val tempFile = File.createTempFile("image", ".png", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            tempFile
        } catch (e: IOException) {
            Log.e("convertUriToFile", "Failed to convert URI to File: ${e.message}")
            throw e
        }
    }

    private fun isUrl(string: String): Boolean {
        return string.startsWith("http://") || string.startsWith("https://")
    }

    private fun startVibrationAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, 12f, -12f, 12f, -12f, 12f, 0f)
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
        binding.ocrEt.isEnabled = isEditing
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

    private fun isInputValid(): Boolean {
        // 이미지가 설정되어 있는지 확인합니다.
        val hasPhoto = problemInfoViewModel.problemPhotoUri.value != null
        val hasText = binding.problemInfoAnswer.text?.isNotBlank() == true
        return hasPhoto && hasText
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun clearListsAndNotify() {
        solutionPhotoList.clear()
        printPhotoList.clear()
        addPhotoList.clear()
        photoAdapter.notifyDataSetChanged()
        printAdapter.notifyDataSetChanged()
        addAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}