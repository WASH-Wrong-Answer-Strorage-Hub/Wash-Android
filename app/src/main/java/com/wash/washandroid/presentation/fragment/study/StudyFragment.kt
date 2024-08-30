package com.wash.washandroid.presentation.fragment.study

import MypageViewModel
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.wash.washandroid.presentation.fragment.study.data.repository.StudyRepository
import com.navercorp.nid.NaverIdLoginSDK.getRefreshToken

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null

    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var viewModel: StudyViewModel
    private lateinit var repository: StudyRepository
    private lateinit var token: String
    private lateinit var myPageSharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)

        recyclerView = binding.studyRv

        val studyApiService = StudyRetrofitInstance.api
        repository = StudyRepository(studyApiService)

        val sharedPreferences =
            requireContext().getSharedPreferences("study_prefs", Context.MODE_PRIVATE)
        myPageSharedPreferences =
            requireContext().getSharedPreferences("mypage_prefs", Context.MODE_PRIVATE)

        val factory = StudyViewModelFactory(repository, sharedPreferences)
        viewModel = ViewModelProvider(this, factory).get(StudyViewModel::class.java)

        // 현재 문제 인덱스 리셋
        viewModel.resetCurrentProblemIndex()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get refresh token
//        token = getRefreshToken() ?: ""
        token = myPageSharedPreferences.getString("refreshToken", null) ?: " "
//        Log.d("fraglog","refresh token 불러오기 : ${getRefreshToken()}")
//        Log.d("fraglog","refresh token 불러오기 : ${token}")

        navController = Navigation.findNavController(view)

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        // recyclerview adapter 클릭 이벤트
        folderAdapter = FolderAdapter(emptyList()) { folderId, folderName ->
            Log.d("fraglog", "Folder clicked: folderId = $folderId, folderName = $folderName")

            viewModel.loadStudyFolderById(folderId.toString())

            // problemIds 가 로드된 후에만 이동하도록 보장
            viewModel.problemIds.observe(viewLifecycleOwner, Observer { problemIds ->
                if (!problemIds.isNullOrEmpty()) {
                    Log.d("fraglog", "Problem IDs loaded: $problemIds")
                    val bundle = Bundle().apply {
                        putInt("folderId", folderId)
                        putString("folderName", folderName)
                    }
                    navController.navigate(
                        R.id.action_navigation_study_to_navigation_study_solve, bundle
                    )
                } else {
                    Log.e("fraglog", "Problem IDs are not yet loaded for folderId: $folderId")
                }
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = folderAdapter

        // study folders 로드될 때마다 RecyclerView 업데이트
        viewModel.folders.observe(viewLifecycleOwner, Observer { folders ->
            Log.d("StudyFragment", "Study folders loaded: $folders")
            folderAdapter.updateFolders(folders)
        })

        // study folders 로드
        Log.d("StudyFragment", "Attempting to load study folders") // 로그 추가
        viewModel.loadStudyFolders(token)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}