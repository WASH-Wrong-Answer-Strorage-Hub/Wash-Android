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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.accessToken
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
                val query = binding.searchEditText.text.toString()
                homeViewModel.searchProblems(query, null, refreshToken.toString()) // 전체 문제 검색
                true
            } else {
                false
            }
        }

        return binding.root
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()

        // 쿼리가 비어 있지 않을 때만 검색을 수행
        if (query.isNotEmpty()) {
            val token = mypageViewModel.getRefreshToken().toString() // 토큰 가져오기
            homeViewModel.searchProblems(query, null, token) // 전체 문제 검색
            Log.d("HomeFragment", "Search performed with query: $query")
        } else {
            Log.d("HomeFragment", "Search query is empty")
        }
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

        // 로그에 폴더 ID 출력
        Log.d("HomeFragment", "폴더 이동 : ${note.folderId}")

        // 폴더 상세 페이지로 이동
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
                Log.d("HomeFragment", "아이템 클릭됨: ${note.title}")
                onCategoryClick(note)
            },
            onDeleteClick = { note ->
                showDeleteConfirmationDialog(note)
            },
            onFolderNameChanged = { note ->
                homeViewModel.updateFolderName(note.folderId, note.title, mypageViewModel.getRefreshToken().toString())
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
