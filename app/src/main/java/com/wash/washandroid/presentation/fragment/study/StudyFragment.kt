package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.databinding.FragmentStudyBinding
import com.wash.washandroid.presentation.base.MainActivity

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null

    private val binding get() = _binding!!

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

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        val folders = listOf("국어", "수학", "영어", "Untitled") // Todo : 서버로부터 받은 데이터로 교체
        folderAdapter = FolderAdapter(folders) { folderName ->
            Toast.makeText(requireContext(), "$folderName 클릭됨", Toast.LENGTH_SHORT).show()
            // Todo : 폴더 내용을 보여주는 화면으로 이동하는 코드 추가
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = folderAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}