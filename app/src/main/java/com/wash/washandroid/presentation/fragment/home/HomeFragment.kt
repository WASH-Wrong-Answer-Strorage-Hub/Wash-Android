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
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeBinding
import com.wash.washandroid.presentation.base.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false

    private lateinit var adapter: NoteAdapter
    private val homeViewModel: HomeViewModel by viewModels()

    // 토큰 받아오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

    // 더미 데이터 추가
    private var dummyImages = listOf(
        Problem(101, "이미지 1", "https://dummyimage.com/600x400/000/fff&text=Image+1", 1),
        Problem(102, "이미지 2", "https://dummyimage.com/600x400/ff6347/fff&text=Image+2", 1),
        Problem(103, "이미지 3", "https://dummyimage.com/600x400/4682b4/fff&text=Image+3", 2),
        Problem(104, "이미지 4", "https://dummyimage.com/600x400/008000/fff&text=Image+4", 2)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as MainActivity).hideBottomNavigation(false)

        val refreshToken = mypageViewModel.getRefreshToken()
        val bearerToken = "Bearer $refreshToken"
        Log.d("homeFragment", "$refreshToken")
        Log.d("homeFragment", "$bearerToken")

        setupRecyclerView()
        observeViewModel()

        // fetchFolders에 token을 전달
        if (refreshToken != null) {
            homeViewModel.fetchFolders(refreshToken)
        }

        binding.editButton.setOnClickListener {
            isEditing = !isEditing
            adapter.setEditing(isEditing)
            binding.editButton.text = if (isEditing) "완료" else "편집"
        }

        // Search icon click listener
        binding.searchIcon.setOnClickListener {
            performSearch()
        }

        // 검색창
        binding.searchEditText.setOnEditorActionListener { v, actionId, _ ->
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
        Log.d("HomeFragment", "Search performed with query: $query")
    }

    private fun observeViewModel() {
        homeViewModel.notes.observe(viewLifecycleOwner, { notes ->
            adapter.updateNotes(notes)
            updateEmptyViewVisibility(notes.isEmpty())
        })
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun onCategoryClick(note: Note) {
        val bundle = Bundle().apply {
            putInt("folderId", note.folderId)
            putString("folderName", note.title)
        }
        val navController = findNavController()
        Log.d("HomeFragment", "Navigating to HomeDetailFragment with folderId: ${note.folderId}")
        navController.navigate(R.id.action_navigation_home_to_homeDetailFragment, bundle)
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            notes = mutableListOf(),
            onItemClick = { note ->
                Log.d("HomeFragment", "${note.title} 클릭됨")
                onCategoryClick(note)
            },
            onDeleteClick = { note ->
                showDeleteConfirmationDialog(note)
            },
            onFolderNameChanged = { note ->
                homeViewModel.updateFolderName(note.folderId, note.title,
                    mypageViewModel.getRefreshToken().toString()
                )
            },
            isEditing = isEditing
        )

        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setMessage("폴더를 삭제하시겠습니까?\n삭제하면 해당 폴더는 복구하기 어렵습니다.")
            .setPositiveButton("확인") { dialog, _ ->
                homeViewModel.deleteFolder(note.folderId,
                    mypageViewModel.getRefreshToken().toString()
                )
                dialog.dismiss()
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
