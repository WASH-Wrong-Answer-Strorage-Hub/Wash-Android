package com.wash.washandroid.presentation.fragment.home

import HomeViewModel
import ImageAdapter
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.accessToken
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeDetailBinding

class HomeDetailFragment : Fragment() {

    private var _binding: FragmentHomeDetailBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false // 편집 상태

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private val COLUMN_COUNT_EXPANDED = 5
    private val COLUMN_COUNT_CONTRACTED = 3
    private val COLUMN_COUNT_MINIMUM = 1
    private var currentColumnCount = COLUMN_COUNT_EXPANDED // 현재 열의 수(초기 5)

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeDetail", "프레그먼트 진입")
        // 확대/축소
        scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.d("HomeDetail", "프레그먼트 실행")

        // Arguments에서 폴더 ID를 가져오고 API 호출
        val folderId = requireArguments().getInt("folderId")
        val token = accessToken // Authorization Token 가져오기 (예: SharedPreferences 등에서)

        setupRecyclerView(currentColumnCount)
        observeViewModel()
        if (token != null) {
            homeViewModel.fetchImagesForFolder(folderId, token)
        }

        // back_btn 클릭 이벤트 설정
        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeDetailFragment_to_navigation_home)
        }

        // 터치 이벤트를 처리할 뷰에 OnTouchListener 설정
        binding.recyclerView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

        // "편집" 버튼 클릭 이벤트 설정
        binding.editButton.setOnClickListener {
            isEditing = !isEditing
            updateNotesState()
        }

        return view
    }

    private fun setupRecyclerView(columnCount: Int) {
        val layoutManager = GridLayoutManager(context, columnCount)
        binding.recyclerView.layoutManager = layoutManager

        val screenWidth = resources.displayMetrics.widthPixels
        val itemWidth = screenWidth / columnCount

        adapter = ImageAdapter(
            items = listOf(), // 초기에는 비어있는 리스트
            onItemClick = { position -> onImageClick(position) },
            itemWidth = itemWidth,
            isEditing = isEditing,
            onDeleteIconClick = { position -> showDeleteConfirmationDialog(position) }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        Log.d("HomeDetail", "Observing ViewModel")
        homeViewModel.images.observe(viewLifecycleOwner, { images ->
            // 로그로 이미지 리스트 출력
            Log.d("HomeDetail", "이미지 리스트: ${images.joinToString(separator = ", ") { it.toString() }}")

            // Update adapter with new data
            adapter.items = images
            adapter.notifyDataSetChanged()
        })
    }


    private fun onImageClick(position: Int) {
        findNavController().navigate(R.id.action_homeDetailFragment_to_homeClickQuizFragment)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setMessage("이미지를 삭제하시겠습니까?\n삭제하면 해당 이미지가 복구할 수 없습니다.")
            .setPositiveButton("확인") { dialog, id ->
                // 확인 버튼 클릭 시 동작
                Log.d("HomeDetail", "이미지가 삭제되었습니다. 아이템 위치: $position")
                // TODO: 실제 삭제 로직 추가
            }
            .setNegativeButton("취소") { dialog, id ->
                // 취소 버튼 클릭 시 동작
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // 두 손가락 확대 축소
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector.scaleFactor > 1) { // 축소
                if (currentColumnCount > COLUMN_COUNT_CONTRACTED) { //3보다 클 때 (5개)
                    currentColumnCount = currentColumnCount - 2
                    setupRecyclerView(currentColumnCount)
                } else if (currentColumnCount > COLUMN_COUNT_MINIMUM) { //1보다 클 때 (3개)
                    currentColumnCount = currentColumnCount - 2
                    setupRecyclerView(currentColumnCount)
                }
            } else if (detector.scaleFactor < 1) { // 확대
                if (currentColumnCount < COLUMN_COUNT_EXPANDED) { //현재 열이 5보다 작을 때 (3일때)
                    currentColumnCount = currentColumnCount + 2
                    setupRecyclerView(currentColumnCount)
                } else if (currentColumnCount < COLUMN_COUNT_CONTRACTED) { //현재 열이 3보다 작을 때 (1일때)
                    currentColumnCount = currentColumnCount + 2
                    setupRecyclerView(currentColumnCount)
                }
            }
            return true
        }
    }

    private fun updateNotesState() {
        // 편집 모드에 따라 adapter에 상태를 업데이트
        adapter.setEditing(isEditing)
        binding.editButton.text = if (isEditing) "완료" else "편집"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
