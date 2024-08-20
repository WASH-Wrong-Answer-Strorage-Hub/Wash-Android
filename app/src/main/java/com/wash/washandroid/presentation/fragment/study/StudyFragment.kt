package com.wash.washandroid.presentation.fragment.study

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentStudyBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.study.data.api.StudyApiService
import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance
//import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance.retrofit
import com.wash.washandroid.presentation.fragment.study.data.model.StudyFolder
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null

    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var viewModel: StudyViewModel
    private lateinit var repository: StudyRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        recyclerView = binding.studyRv

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        val factory = StudyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        // 현재 문제 인덱스 리셋
        viewModel.resetCurrentProblemIndex()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // RecyclerView 설정
        folderAdapter = FolderAdapter(emptyList()) { folderName ->
            // 아이템 클릭 시 해당 폴더 이름에 대한 ID를 가져오기
            val folderId = viewModel.getIdByName(folderName)
//            Log.d("fraglog", "solve -- Folder ID found for $folderName: $folderId")
            folderId?.let {
                viewModel.loadStudyFolderById(it.toString()) // 폴더 내용 불러오기

                val bundle = Bundle().apply {
                    putInt("folderId", it)
                    putString("folderName", folderName)
                }
                navController.navigate(R.id.action_navigation_study_to_navigation_study_solve, bundle)
            } ?: run {
//                Log.e("fraglog", "Folder ID not found for name: $folderName")
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = folderAdapter

        // LiveData 관찰하여 폴더 데이터가 로드될 때마다 RecyclerView 업데이트
        viewModel.studyFolders.observe(viewLifecycleOwner, Observer { folderNames ->
            folderAdapter.updateFolders(folderNames)
        })

        // 폴더 로드
        viewModel.loadStudyFolders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}