package com.wash.washandroid.presentation.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeBinding
import com.wash.washandroid.presentation.adapter.Note
import com.wash.washandroid.presentation.adapter.NoteAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false

    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()

        binding.editButton.setOnClickListener {
            isEditing = !isEditing
            adapter.setEditing(isEditing)
            binding.editButton.text = if (isEditing) "완료" else "편집"
        }

        return binding.root
    }

    private fun onCategoryClick(note: Note) {
        val navController = findNavController()
        val currentDestination = navController.currentDestination?.id
        Log.d("HomeFragment", "Navigating to HomeDetailFragment")
        Log.d("HomeFragment", "Current destination: $currentDestination")
        navController.navigate(R.id.action_navigation_home_to_homeDetailFragment)
    }


    private fun setupRecyclerView() {
        val notes = listOf(
            Note("국어", R.drawable.ic_listitem_frame),
            Note("수학", R.drawable.ic_listitem_frame),
            Note("영어", R.drawable.ic_listitem_frame),
            Note("untitled", R.drawable.ic_listitem_frame),
            Note("2024 토플", R.drawable.ic_listitem_frame)
        )

        adapter = NoteAdapter(
            notes = notes,
            onItemClick = { note ->
                Log.d("HomeFragment", "${note.title} 클릭됨")
                onCategoryClick(note)
                Log.d("HomeFragment", "${note.title} 네비게이션")
            },
            onDeleteClick = { note ->
                showDeleteConfirmationDialog()
            },
            isEditing = isEditing
        )

        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("폴더를 삭제하시겠습니까?\n삭제하면 해당 폴더는 복구하기 어렵습니다.")
            .setPositiveButton("확인") { dialog, id ->
                Log.d("HomeFragment", "폴더가 삭제되었습니다.")
            }
            .setNegativeButton("취소") { dialog, id ->
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
