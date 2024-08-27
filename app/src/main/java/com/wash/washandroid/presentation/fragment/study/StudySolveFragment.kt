package com.wash.washandroid.presentation.fragment.study

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var gestureDetector: GestureDetector
    private var isGestureDetected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudySolveBinding.inflate(inflater, container, false)

        folderId = arguments?.getInt("folderId").toString()
        folderName = arguments?.getString("folderName") ?: "folderName"

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        sharedPreferences =
            requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)

        val factory = StudyViewModelFactory(repository, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("fraglog", "solve -- on view created")
        Log.d("fraglog", "**is problem already solved**  :  ${viewModel.getProblemSolvedState()}")

        binding.tvStudySolveTitle.text = folderName
        navController = Navigation.findNavController(view)

        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener(
            context = requireContext(),
            onSwipeRight = { onSwipeRight() },
            onSwipeLeft = { onSwipeLeft() }
        ))

        binding.root.setOnTouchListener { v, event ->
            // DrawerLayout이 열려 있는지 확인
            if (binding.studyDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                // DrawerLayout이 열려 있는 경우 기본 Drawer 동작 유지
                return@setOnTouchListener false
            } else {
                // Drawer가 닫혀 있는 경우에만 제스처 감지
                if (gestureDetector.onTouchEvent(event)) {
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }

        (activity as MainActivity).hideBottomNavigation(true)

        // recycler view 설정
        binding.rvDrawerProgress.layoutManager = LinearLayoutManager(requireContext())
        val problemIds = viewModel.loadProblemIdsFromPreferences(sharedPreferences)
        val progressAdapter = StudyProgressAdapter(problemIds.map { it to "미완료" }, problemIds)
        binding.rvDrawerProgress.adapter = progressAdapter

        // 정답 확인 전송 여부 검사
        if (viewModel.getProblemSolvedState()) {
            Log.d(
                "fraglog",
                "**is problem already solved**  :  ${viewModel.getProblemSolvedState()} -> move to next"
            )
            viewModel.setProblemSolvedState(false)
            viewModel.loadStudyProgress(folderId)
            viewModel.moveToNextProblem(folderId)
        } else {
            // 정답 확인 전송 하지 않은 경우
            viewModel.loadStudyProblem(folderId)
            Log.d(
                "fraglog",
                "**is problem already solved**  :  ${viewModel.getProblemSolvedState()} -> 유지"
            )
        }

        viewModel.studyProblem.observe(viewLifecycleOwner, Observer { studyProblemResponse ->
            studyProblemResponse?.let {
                binding.tvStudySolveTitle.text = it.result.folderName

                // passageImages가 있는지 확인
                val passageUrls = it.result.passageImages
                if (passageUrls.isNullOrEmpty()) {
                    binding.studySolveBtnDes.visibility = View.INVISIBLE
                    Log.d("fraglog", "passageImages : ${it.result.passageImages}.")
                } else {
                    binding.studySolveBtnDes.visibility = View.VISIBLE
                    Log.d("fraglog", "passageImages : ${it.result.passageImages}.")
                }
                updateUI(it)
            } ?: run {
                Log.e("fraglog", "Study problem is null, cannot update UI")
            }
        })

        // studyProgress 로드
        viewModel.loadStudyProgress(folderId)

        // studyProgress 업데이트를 관찰하여 RecyclerView에 반영
        viewModel.studyProgress.observe(viewLifecycleOwner, Observer { progressList ->
            // 서버로부터 가져온 progressList를 어댑터에 업데이트
            progressAdapter.updateProgressList(progressList)

            val isAllCompleted = progressList.all { it.second == "틀린 문제" || it.second == "맞은 문제" }

            if (isAllCompleted) {
//                Log.d("fraglog", "모든 문제 상태가 완료임")
                val bundle = bundleOf("folderId" to folderId)
                navController.navigate(
                    R.id.action_navigation_study_solve_to_navigation_study_complete, bundle
                )
            }
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
            if (!isGestureDetected) {
                val currentProblem = viewModel.getCurrentProblem()
                val imageUrl = currentProblem.result.problemImage.takeIf { it.isNotBlank() }
                    ?: "https://samtoring.com/qstn/NwXVS1yaHZ1xav2YsqAf.png"

                val bundle = bundleOf("image_url" to imageUrl)
                navController.navigate(
                    R.id.action_navigation_study_solve_to_navigation_study_full_screen_image, bundle
                )
            }
            isGestureDetected = false
        }

        binding.studySolveBackBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_study_solve_to_navigation_study)
        }

        // 정답 확인 버튼 클릭 리스너
        binding.studySolveBtnAnswer.setOnClickListener {
            val currentProblem = viewModel.studyProblem.value
            val bundle = bundleOf(
                "folderId" to folderId,
                "problemId" to currentProblem?.result?.problemId,
                "answer" to currentProblem?.result?.answer
            )
//                Toast.makeText(requireContext(), "solve -- id : ${currentProblem.id}, answer : ${currentProblem.answer}, last : ${isLastProblem}", Toast.LENGTH_SHORT).show()

            navController.navigate(
                R.id.action_navigation_study_solve_to_navigation_study_answer, bundle
            )
        }

        binding.ivDrawer.setOnClickListener {
            viewModel.loadStudyProgress(folderId)
            binding.studyDrawerLayout.openDrawer(GravityCompat.END)
        }

        binding.btnRvDrawerFinish.setOnClickListener {

            val bundle = bundleOf("folderId" to folderId)
            navController.navigate(
                R.id.action_navigation_study_solve_to_navigation_study_complete, bundle
            )
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

        // 첫 번째 문제인 경우
        if (viewModel.currentProblemIndex == 0) {
            binding.ivLeftArrow.visibility = View.INVISIBLE
        } else {
            binding.ivLeftArrow.visibility = View.VISIBLE
        }

        // 마지막 문제인 경우
        if (viewModel.currentProblemIndex == (viewModel.getProblemIds().size - 1)) {
            binding.ivRightArrow.visibility = View.INVISIBLE
        } else {
            binding.ivRightArrow.visibility = View.VISIBLE
        }
    }

    private fun openPhotoPager() {
        val currentProblem = viewModel.getCurrentProblem()
        val passageUrls = currentProblem.result.passageImages ?: emptyList()

        Log.d("fraglog", "passageImages: $passageUrls")

        viewModel.setPhotoUris(passageUrls)

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

    private fun onSwipeRight() {
        // 오른쪽 스와이프 처리
        viewModel.moveToNextProblem(folderId)
        setupObservers()
    }

    private fun onSwipeLeft() {
        // 왼쪽 스와이프 처리
        viewModel.moveToPreviousProblem(folderId)
        setupObservers()
    }
}