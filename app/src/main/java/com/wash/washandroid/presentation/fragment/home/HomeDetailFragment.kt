package com.wash.washandroid.presentation.fragment.home

import HomeViewModel
import ImageAdapter
import MypageViewModel
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeDetailBinding

class HomeDetailFragment : Fragment() {

    private var _binding: FragmentHomeDetailBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false // 편집 상태를 관리하는 변수

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private val COLUMN_COUNT_EXPANDED = 5
    private val COLUMN_COUNT_CONTRACTED = 3
    private val COLUMN_COUNT_MINIMUM = 1
    private var currentColumnCount = COLUMN_COUNT_EXPANDED // 초기 열의 수 설정

    // 토큰 받아오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeDetail", "프레그먼트 진입")

        // 확대/축소 제스처를 위한 리스너 설정
        scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.d("HomeDetail", "프레그먼트 실행")

        // 폴더 ID 및 폴더명 가져오기
        val folderId = requireArguments().getInt("folderId")
        val folderName = requireArguments().getString("folderName", "폴더명")

        // 토큰 가져오기 (다른 ViewModel 또는 프레그먼트에서 전달된 토큰)
        val refreshToken = mypageViewModel.getRefreshToken()
        val bearerToken = "Bearer $refreshToken"
        Log.d("homeFragment", "$refreshToken")
        Log.d("homeFragment", "$bearerToken")


        // 폴더명 표시
        binding.categoryTag.text = folderName

        // RecyclerView 설정
        setupRecyclerView(currentColumnCount)

        // ViewModel 관찰
        observeViewModel()

        // 이미지 데이터 로드
        if (refreshToken != null) {
            homeViewModel.fetchImagesForFolder(folderId, refreshToken)
        }

        // 뒤로 가기 버튼 클릭 이벤트 설정
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeDetailFragment_to_navigation_home)
        }

        // RecyclerView 터치 이벤트 (확대/축소 제스처 처리)
        binding.recyclerView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

        // "편집" 버튼 클릭 이벤트 설정
        binding.editButton.setOnClickListener {
            isEditing = !isEditing
            updateNotesState()
        }

        // 검색창 동작 설정
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(refreshToken, folderId) // 검색 수행 시 토큰과 폴더 ID 전달
                true
            } else {
                false
            }
        }

        return view
    }

    // 검색 기능 구현
    private fun performSearch(token: String?, folderId: Int) {
        val query = binding.searchEditText.text.toString().trim()

        // 검색 쿼리가 비어 있지 않으면 검색 수행
        if (query.isNotEmpty() && token != null) {
            homeViewModel.searchProblems(query, folderId, token) // 특정 폴더 내 문제 검색
            Log.d("HomeDetailFragment", "Search performed with query: $query in folderId: $folderId")
        } else {
            Log.d("HomeDetailFragment", "Search query is empty or token is null")
        }
    }

    // RecyclerView 설정
    private fun setupRecyclerView(columnCount: Int) {
        val layoutManager = GridLayoutManager(context, columnCount)
        binding.recyclerView.layoutManager = layoutManager

        val screenWidth = resources.displayMetrics.widthPixels
        val itemWidth = screenWidth / columnCount

        adapter = ImageAdapter(
            items = listOf(), // 초기 빈 리스트
            onItemClick = { position -> onImageClick(position) },
            itemWidth = itemWidth,
            isEditing = isEditing,
            onDeleteIconClick = { position -> showDeleteConfirmationDialog(position) }
        )
        binding.recyclerView.adapter = adapter
    }

    // ViewModel 관찰
    private fun observeViewModel() {
        Log.d("HomeDetail", "Observing ViewModel")
        homeViewModel.images.observe(viewLifecycleOwner, { images ->
            // 로그로 이미지 리스트 출력
            Log.d("HomeDetail", "이미지 리스트: ${images.joinToString(separator = ", ") { it.toString() }}")

            // 어댑터에 새로운 데이터 설정
            adapter.items = images
            adapter.notifyDataSetChanged()
        })
    }

    // 이미지 클릭 시 동작
    private fun onImageClick(position: Int) {
        findNavController().navigate(R.id.action_homeDetailFragment_to_homeClickQuizFragment)
    }

    // 이미지 삭제 확인 다이얼로그
    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setMessage("이미지를 삭제하시겠습니까?\n삭제하면 해당 이미지를 복구할 수 없습니다.")
            .setPositiveButton("확인") { dialog, _ ->
                val problemId = adapter.items[position].problemId
                val folderId = requireArguments().getInt("folderId")
                val token = requireArguments().getString("accessToken")
                if (token != null) {
                    homeViewModel.deleteProblem(problemId, folderId, token)
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // 두 손가락 확대 축소 제스처 처리
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector.scaleFactor > 1) { // 축소
                if (currentColumnCount > COLUMN_COUNT_CONTRACTED) { // 현재 열이 3보다 클 때 (5개)
                    currentColumnCount -= 2
                    setupRecyclerView(currentColumnCount)
                } else if (currentColumnCount > COLUMN_COUNT_MINIMUM) { // 현재 열이 1보다 클 때 (3개)
                    currentColumnCount -= 2
                    setupRecyclerView(currentColumnCount)
                }
            } else if (detector.scaleFactor < 1) { // 확대
                if (currentColumnCount < COLUMN_COUNT_EXPANDED) { // 현재 열이 5보다 작을 때 (3일때)
                    currentColumnCount += 2
                    setupRecyclerView(currentColumnCount)
                } else if (currentColumnCount < COLUMN_COUNT_CONTRACTED) { // 현재 열이 3보다 작을 때 (1일때)
                    currentColumnCount += 2
                    setupRecyclerView(currentColumnCount)
                }
            }
            return true
        }
    }

    // 편집 모드 상태 업데이트
    private fun updateNotesState() {
        adapter.setEditing(isEditing)
        binding.editButton.text = if (isEditing) "완료" else "편집"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}