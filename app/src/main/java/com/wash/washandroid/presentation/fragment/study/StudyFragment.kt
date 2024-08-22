package com.wash.washandroid.presentation.fragment.study

import MypageViewModel
import android.content.Context
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
import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance
//import com.wash.washandroid.presentation.fragment.study.data.api.StudyRetrofitInstance.retrofit
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null

    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var viewModel: StudyViewModel
    private lateinit var myPageViewModel: MypageViewModel
    private lateinit var repository: StudyRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        recyclerView = binding.studyRv

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        val sharedPreferences = requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)

        myPageViewModel = ViewModelProvider(requireActivity()).get(MypageViewModel::class.java)
        val factory = StudyViewModelFactory(repository, sharedPreferences, myPageViewModel)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        // 현재 문제 인덱스 리셋
        viewModel.resetCurrentProblemIndex()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        // recyclerview adapter 클릭 이벤트
        folderAdapter = FolderAdapter(emptyList()) { folderName ->
            val folderId = viewModel.getIdByName(folderName)
            folderId?.let {
                viewModel.loadStudyFolderById(it.toString())

                // problemIds 가 로드된 후에만 이동하도록 보장
                viewModel.problemIds.observe(viewLifecycleOwner, Observer { problemIds ->
                    if (!problemIds.isNullOrEmpty()) {
                        val bundle = Bundle().apply {
                            putInt("folderId", it)
                            putString("folderName", folderName)
                        }
                        navController.navigate(R.id.action_navigation_study_to_navigation_study_solve, bundle)
                    } else {
                        Log.e("fraglog", "Problem IDs are not yet loaded for folderId: $folderId")
                    }
                })
            } ?: run {
                Log.e("fraglog", "Folder ID not found for name: $folderName")
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = folderAdapter

        // study folders 로드될 때마다 RecyclerView 업데이트
        viewModel.studyFolders.observe(viewLifecycleOwner, Observer { folderNames ->
            folderAdapter.updateFolders(folderNames)
        })

        // study folders 로드
        viewModel.loadStudyFolders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}