package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder

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

        val studyViewModel = ViewModelProvider(requireActivity()).get(StudyViewModel::class.java)

        // 현재 문제 인덱스 리셋
        studyViewModel.resetCurrentProblemIndex()

        // 스와이프, 상태 초기화

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        val folders = listOf(
            StudyFolder("", "국어", listOf("1", "2", "3")),
            StudyFolder("", "수학", listOf("2")),
            StudyFolder("", "영어", listOf("3")),
            StudyFolder("", "Untitled", listOf("4"))
        ) // Todo : 서버로부터 받은 데이터로 교체

        folderAdapter = FolderAdapter(folders) { folder ->
//            Toast.makeText(requireContext(), "${folder.name} 클릭됨", Toast.LENGTH_SHORT).show()
            val bundle = Bundle().apply {
                putString("folderName", folder.folderName)
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