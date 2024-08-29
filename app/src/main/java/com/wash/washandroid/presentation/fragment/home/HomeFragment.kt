package com.wash.washandroid.presentation.fragment.home

import HomeViewModel
import MypageViewModel
import Note
import NoteAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeBinding
import com.wash.washandroid.presentation.base.MainActivity
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false

    private lateinit var adapter: NoteAdapter
    private val homeViewModel: HomeViewModel by viewModels()
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as MainActivity).hideBottomNavigation(false)

        val refreshToken = mypageViewModel.getRefreshToken()
        Log.d("homeFragment", "$refreshToken")

        setupRecyclerView()
        observeViewModel()

        // 폴더 목록 초기 불러오기
        refreshToken?.let {
            homeViewModel.fetchFolders(it)
        }

        binding.editButton.setOnClickListener {
            isEditing = !isEditing
            adapter.setEditing(isEditing)
            binding.editButton.text = if (isEditing) "완료" else "편집"

            // 편집 모드 완료 시 폴더 목록 새로고침
            if (!isEditing) {
                refreshToken?.let {
                    homeViewModel.fetchFolders(it)
                }
            }
        }

        // 검색 아이콘 클릭 리스너
        binding.searchIcon.setOnClickListener {
            performSearch()
        }

        // 검색창에서 완료 버튼 클릭 리스너
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }

        return binding.root
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString()
        Log.d("HomeFragment", "검색 수행: $query")

        // 검색 요청 보내기
        val refreshToken = mypageViewModel.getRefreshToken().toString()
        homeViewModel.searchProblems(query, null, "Bearer $refreshToken")
    }

    private fun observeViewModel() {
        homeViewModel.notes.observe(viewLifecycleOwner, { notes ->
            adapter.updateNotes(notes)
            updateEmptyViewVisibility(notes.isEmpty())
        })

        homeViewModel.searchResults.observe(viewLifecycleOwner, { results ->
            val notes = results.map { problemSearch ->
                Note(
                    folderId = problemSearch.problemId.toInt(), // problemId를 Int로 변환
                    title = problemSearch.problemText,
                    imageResId = R.drawable.item_search_result // 적절한 이미지 리소스 ID 사용
                )
            }
            adapter.updateNotes(notes)
            updateEmptyViewVisibility(notes.isEmpty())
        })
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun onCategoryClick(note: Note) {
        val bundle = Bundle().apply {
            putInt("folderId", note.folderId)
            putString("folderName", note.title)
        }
        val navController = findNavController()
        Log.d("HomeFragment", "HomeDetailFragment로 이동, folderId: ${note.folderId}")
        navController.navigate(R.id.action_navigation_home_to_homeDetailFragment, bundle)
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            notes = mutableListOf(),
            onItemClick = { note ->
                Log.d("HomeFragment", "아이템 클릭됨: ${note.title}")
                onCategoryClick(note)
            },
            onDeleteClick = { note ->
                showDeleteConfirmationDialog(note)
            },
            onFolderNameChanged = { note ->
                homeViewModel.updateFolderName(note.folderId, note.title, mypageViewModel.getRefreshToken().toString())
            },
            onOrderChanged = { updatedNotes ->
                // 서버에 폴더 순서 업데이트 요청
                val order = updatedNotes.map { it.folderId }
                val token = mypageViewModel.getRefreshToken().toString()
                homeViewModel.reorderFolders(token, order)
            },
            isEditing = isEditing
        )
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter

        // ItemTouchHelper를 사용하여 드래그 앤 드롭 기능 추가
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.moveItem(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 좌우 스와이프에 대한 처리
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setMessage("폴더를 삭제하시겠습니까?\n삭제하면 해당 폴더는 복구하기 어렵습니다.")
            .setPositiveButton("확인") { dialog, _ ->
                homeViewModel.deleteFolder(note.folderId, mypageViewModel.getRefreshToken().toString())
                dialog.dismiss()
                // 삭제 후 폴더 목록 새로고침
                mypageViewModel.getRefreshToken()?.let {
                    homeViewModel.fetchFolders(it)
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
