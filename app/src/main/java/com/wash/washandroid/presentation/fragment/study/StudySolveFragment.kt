package com.wash.washandroid.presentation.fragment.study

import MypageViewModel
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudySolveBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance
import com.wash.washandroid.presentation.fragment.study.data.model.response.StudyProblemResponse
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository

class StudySolveFragment : Fragment() {
    private var _binding: FragmentStudySolveBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var viewModel: StudyViewModel
    private lateinit var repository: StudyRepository
    private lateinit var folderId: String
    private lateinit var folderName: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var problemIds: List<String>
    private lateinit var progressAdapter: StudyProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudySolveBinding.inflate(inflater, container, false)

        folderId = arguments?.getInt("folderId").toString()
        folderName = arguments?.getString("folderName") ?: "folderName"

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        sharedPreferences = requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)

        val factory = StudyViewModelFactory(repository, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStudySolveTitle.text = folderName
        navController = Navigation.findNavController(view)

        (activity as MainActivity).hideBottomNavigation(true)

        // recycler view 설정
        binding.rvDrawerProgress.layoutManager = LinearLayoutManager(requireContext())
        val problemIds = viewModel.loadProblemIdsFromPreferences(sharedPreferences)
        val progressAdapter = StudyProgressAdapter(problemIds.map { it to "미완료" }, problemIds)
        binding.rvDrawerProgress.adapter = progressAdapter

        // 문제 해결 여부를 확인하여 다음 문제로 이동
        if (viewModel.isProblemAlreadySolved) {
            Log.d("fraglog", "problem already solved, moving to next problem")
            viewModel.moveToNextProblem(folderId)
            viewModel.isProblemAlreadySolved = false // 플래그를 초기화
        } else {
            // 문제가 이미 해결되지 않은 경우에만 문제를 로드
            viewModel.loadStudyProblem(folderId)
        }

        viewModel.studyProblem.observe(viewLifecycleOwner, Observer { studyProblemResponse ->
            studyProblemResponse?.let {
                binding.tvStudySolveTitle.text = it.result.folderName

                // passageImages가 있는지 확인
                val passageUrls = it.result.passageImages
                if (passageUrls.isNullOrEmpty()) {
                    binding.studySolveBtnDes.visibility = View.INVISIBLE
                    Log.d("fraglog", "passageImages is null or empty, hiding button.")
                } else {
                    binding.studySolveBtnDes.visibility = View.VISIBLE
                    Log.d("fraglog", "passageImages available, showing button.")
                }

                updateUI(it)
            } ?: run {
                Log.e("fraglog", "Study problem is null, cannot update UI")
            }
        })

        // studyProgress 로드
        viewModel.loadStudyProgress(folderId)

        if (viewModel.isProblemAlreadySolved) {
            Log.d("fraglog", "problem already solved move to next problem")
            viewModel.moveToNextProblem(folderId)
        }

        // studyProgress 업데이트를 관찰하여 RecyclerView에 반영
        viewModel.studyProgress.observe(viewLifecycleOwner, Observer { progressList ->
            // 서버로부터 가져온 progressList를 어댑터에 업데이트
            progressAdapter.updateProgressList(progressList)
        })

        // 지문 보기
        binding.studySolveBtnDes.setOnClickListener {
            openPhotoPager()
        }

        // 왼쪽 화살표 클릭 시 이전 문제로 이동
        binding.ivLeftArrow.setOnClickListener {
            viewModel.moveToPreviousProblem(folderId)
            setupObservers()
        }

        // 오른쪽 화살표 클릭 시 다음 문제로 이동
        binding.ivRightArrow.setOnClickListener {
            viewModel.moveToNextProblem(folderId)
            setupObservers()
        }

        // 문제 이미지 클릭 리스너
        binding.ivSolveCard.setOnClickListener {
            val currentProblem = viewModel.getCurrentProblem()
            val imageUrl = currentProblem.result.problemImage.takeIf { it.isNotBlank() } ?: "https://samtoring.com/qstn/NwXVS1yaHZ1xav2YsqAf.png"

            val bundle = bundleOf("image_url" to imageUrl)
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_full_screen_image, bundle)
        }

        binding.studySolveBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study)
        }

        // 정답 확인 버튼 클릭 리스너
        binding.studySolveBtnAnswer.setOnClickListener {
            val currentProblem = viewModel.studyProblem.value
            val bundle = bundleOf(
                "folderId" to folderId, "problemId" to currentProblem?.result?.problemId, "answer" to currentProblem?.result?.answer
            )
//                Toast.makeText(requireContext(), "solve -- id : ${currentProblem.id}, answer : ${currentProblem.answer}, last : ${isLastProblem}", Toast.LENGTH_SHORT).show()

            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_answer, bundle)
        }

        binding.ivDrawer.setOnClickListener {
            binding.studyDrawerLayout.openDrawer(GravityCompat.END)
        }

        binding.btnRvDrawerFinish.setOnClickListener {

            val bundle = bundleOf("folderId" to folderId)
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_complete, bundle)
        }
    }

    private fun setupObservers() {
        viewModel.studyProblem.observe(viewLifecycleOwner, Observer { studyProblemResponse ->
            updateUI(studyProblemResponse)
        })
    }

    private fun updateUI(problem: StudyProblemResponse) {
        binding.tvStudySolveProblemId.text = "문제 " + (viewModel.currentProblemIndex + 1)
        val imageUrl = problem.result.problemImage.takeIf { it.isNotBlank() }
            ?: "https://samtoring.com/qstn/NwXVS1yaHZ1xav2YsqAf.png"
        Glide.with(this).load(imageUrl).into(binding.ivSolveCard)
    }

    private fun openPhotoPager() {
        val currentProblem = viewModel.getCurrentProblem()
        val passageUrls = currentProblem.result.passageImages ?: emptyList()

        Log.d("fraglog", "passageImages: $passageUrls")

        viewModel.setPhotoUris(passageUrls)

        val sharedPreferences = requireContext().getSharedPreferences("your_shared_prefs", Context.MODE_PRIVATE)
        val savedUrisString = sharedPreferences.getString("photo_uris", "")

        Log.d("fraglog", "Loaded photo URIs from SharedPreferences before split: $savedUrisString")

        val savedUris = savedUrisString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        Log.d("fraglog", "Loaded photo URIs from SharedPreferences after split: $savedUris")

        viewModel.setSelectedPhotoPosition(0)

        navController.navigate(R.id.action_navigation_study_solve_to_navigation_study_photo_slider)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}