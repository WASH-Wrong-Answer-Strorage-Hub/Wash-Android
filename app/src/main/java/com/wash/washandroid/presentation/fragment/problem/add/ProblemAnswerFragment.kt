package com.wash.washandroid.presentation.fragment.problem.add

import MypageViewModel
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import com.wash.washandroid.databinding.FragmentProblemAnswerBinding
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import com.wash.washandroid.presentation.fragment.category.network.NetworkModule
import com.wash.washandroid.presentation.fragment.category.network.ProblemRepository
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModel
import com.wash.washandroid.presentation.fragment.category.viewmodel.CategoryFolderViewModelFactory
import com.wash.washandroid.presentation.fragment.problem.PhotoAdapter
import com.wash.washandroid.presentation.fragment.problem.network.ProblemApiService
import com.wash.washandroid.presentation.fragment.problem.network.ProblemData
import com.wash.washandroid.presentation.fragment.problem.old.ProblemInfoViewModel
import com.wash.washandroid.utils.ProblemInfoDecoration
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProblemAnswerFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemAnswerBinding? = null
    private val binding: FragmentProblemAnswerBinding
        get() = requireNotNull(_binding){"FragmentProblemAnswerBinding -> null"}

    private val problemInfoViewModel: ProblemInfoViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }

    private val problemAnswerViewModel: ProblemAnswerViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }

    private val categoryFolderViewModel: CategoryFolderViewModel by activityViewModels {
        val problemRepository = ProblemRepository()
        CategoryFolderViewModelFactory(problemRepository)
    }

    private val problemAddViewModel: ProblemAddViewModel by activityViewModels()

    private val mypageViewModel: MypageViewModel by activityViewModels()
    private lateinit var token : String

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

    private var isEditing = true

    private var mainProblemImageUrl = ""

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

        _binding = FragmentProblemAnswerBinding.inflate(inflater, container, false)

        // 현재 인덱스의 사진을 설정하는 로직
        problemAddViewModel.currentIndex.observe(viewLifecycleOwner) {
            val currentPhoto = problemAddViewModel.getCurrentPhoto()
            currentPhoto?.let {
                binding.problemInfoPhoto.setImageURI(Uri.parse(it))
                binding.problemInfoPhoto.clipToOutline = true
                Log.d("currentPhoto", "$currentPhoto")
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)
        navController = Navigation.findNavController(view)

        problemAnswerViewModel.initialize(token)

        // 페이지 상단 문제 인덱스 표시
        val currentIndex = problemAddViewModel.currentIndex.value ?: 0
        val photoList = problemAddViewModel.photoList.value ?: mutableListOf()
        val totalProblemCount = photoList.size
        val problemText = "문제  ${currentIndex + 1}/$totalProblemCount"
        val spannableString = SpannableString(problemText)
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0CCF67"))
        val startIndex = problemText.indexOf("${currentIndex + 1}")
        val endIndex = startIndex + "${currentIndex + 1}".length

        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.problemInfoTv.text = spannableString

//        binding.photoDeleteLayout.setOnClickListener {
//            binding.problemInfoPhoto.setImageURI(null)
//            binding.photoDeleteLayout.visibility = View.INVISIBLE // 이미지 삭제하면서 삭제버튼 비활성화
//        }

        binding.problemInfoBackBtn.setOnClickListener {
            navController.navigateUp()
        }

        binding.problemInfoPhotoAdd.setOnClickListener {
            if (isEditing) {
                openProblemGallery()
            } else {
                binding.problemInfoPhotoAdd.isClickable = false
            }
        }

        // 현재 인덱스의 사진을 가져와서 url로 변환 후 OCR 기능 실행
        problemAddViewModel.currentIndex.observe(viewLifecycleOwner) {
            val currentPhoto = problemAddViewModel.getCurrentPhoto()
            currentPhoto?.let {
                val problemImageUri = currentPhoto.let { Uri.parse(it) }
                val problemImageUrl = problemImageUri?.let { uploadImage(convertUriToFile(it)) }
                Log.d("currentPhoto", "$currentPhoto")
                Log.d("problemImageUrl", "$problemImageUrl")

                if (problemImageUrl != null) {
                    problemAnswerViewModel.recognizeTextFromImage(problemImageUrl)
                    mainProblemImageUrl = problemImageUrl
                }
            }
        }

        problemAnswerViewModel.recognizedText.observe(viewLifecycleOwner) { recognizedText ->
            binding.ocrEt.setText(recognizedText)
        }

        binding.problemInfoNextBtn.setOnClickListener {
            if (isInputValid()) {
                val problemData = collectProblemData()
                Log.d("problemData", problemData.toString())
                categoryFolderViewModel.setProblemData(problemData)
                ProblemManager.addProblemData(problemData)
                problemAnswerViewModel.setRecognizedText("")

                navController.navigate(R.id.action_navigation_problem_answer_to_category_subject_fragment)
            } else {
                Toast.makeText(requireContext(), "사진을 추가하고 정답을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        problemAnswerViewModel.problemPhotoUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.problemInfoPhoto.setImageURI(it)
                binding.problemInfoPhoto.clipToOutline = true
                binding.photoDeleteLayout.clipToOutline = true
            } ?: Log.d("ProblemPhotoUri", "problemPhotoUri is null")
        }

        photoProblemPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    problemAnswerViewModel.setProblemPhotoUri(uri)
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
        binding.problemInfoSolutionRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))
        binding.problemInfoPrintRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))
        binding.problemInfoAddRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))

        // 풀이, 지문, 추가 사진 버튼 표시
        photoAdapter.setAddButtonVisible(isEditing)
        addAdapter.setAddButtonVisible(isEditing)
        printAdapter.setAddButtonVisible(isEditing)

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
        val problemImageUrl = mainProblemImageUrl

        val solutionImageUrls = solutionPhotoList.map { uri ->
            uploadImage(convertUriToFile(Uri.parse(uri)))
        }

        val passageImageUrls = printPhotoList.map { uri ->
            uploadImage(convertUriToFile(Uri.parse(uri)))
        }

        val additionalImageUrls = addPhotoList.map { uri ->
            uploadImage(convertUriToFile(Uri.parse(uri)))
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
            memo = memo,
            mainTypeId = 1, // 더미 데이터
            midTypeId = 2, // 더미 데이터
            subTypeIds = listOf(3, 4) // 더미 데이터
        )

        Log.d("ProblemData", "Problem Data collected: $problemData")

        return problemData
    }

    private fun uploadImage(file: File): String? {
        return try {
            // 파일을 MultipartBody.Part로 변환
            val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val retrofit = NetworkModule.getClient()
            val apiService: ProblemApiService = retrofit.create(ProblemApiService::class.java)

            runBlocking {
                // Retrofit을 이용해 API 호출
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
        problemAnswerViewModel.setPhotoUris(photoList)
        problemAnswerViewModel.setSelectedPhotoPosition(initialPosition)

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

    private fun isInputValid(): Boolean {
        // 현재 인덱스의 사진을 가져옵니다.
        val currentPhoto = problemAddViewModel.getCurrentPhoto()

        // 이미지가 설정되어 있는지 확인합니다.
        val hasPhoto = currentPhoto != null && binding.problemInfoPhoto.drawable != null
        val hasText = binding.problemInfoAnswer.text?.isNotBlank() == true
        return hasPhoto && hasText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}