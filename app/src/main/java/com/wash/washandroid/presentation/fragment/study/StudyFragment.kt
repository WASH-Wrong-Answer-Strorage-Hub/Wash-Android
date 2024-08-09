package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyBinding
import com.wash.washandroid.presentation.base.MainActivity

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null

    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        recyclerView = binding.studyRv
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        val folders = listOf(
            StudyFolder(1, "국어"),
            StudyFolder(2, "수학"),
            StudyFolder(3, "영어"),
            StudyFolder(4, "Untitled")
        ) // Todo : 서버로부터 받은 데이터로 교체

        folderAdapter = FolderAdapter(folders) { folder ->
//            Toast.makeText(requireContext(), "${folder.name} 클릭됨", Toast.LENGTH_SHORT).show()
            val bundle = Bundle().apply {
                putInt("folderId", folder.id)
                putString("folderName", folder.name)
            }
            navController.navigate(R.id.action_navigation_study_to_navigation_study_solve, bundle)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = folderAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}