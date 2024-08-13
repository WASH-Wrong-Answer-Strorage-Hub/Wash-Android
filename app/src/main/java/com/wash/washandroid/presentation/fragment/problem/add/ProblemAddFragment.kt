package com.wash.washandroid.presentation.fragment.problem.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentProblemAddBinding
import com.wash.washandroid.presentation.base.MainActivity
import com.wash.washandroid.presentation.fragment.problem.old.ProblemInfoViewModel
import com.wash.washandroid.utils.ProblemInfoDecoration

class ProblemAddFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentProblemAddBinding? = null
    private val binding: FragmentProblemAddBinding
        get() = requireNotNull(_binding){"FragmentProblemAddBinding -> null"}

    private val problemInfoViewModel: ProblemInfoViewModel by activityViewModels()

    private lateinit var photoAdapter: PhotoNewAdapter
    private val photoList = mutableListOf<String>()
    private lateinit var photoPickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProblemAddBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).hideBottomNavigation(true)
        navController = Navigation.findNavController(view)

        photoAdapter = PhotoNewAdapter(requireContext(), photoList, { openGallery() }) { position ->
            openPhotoPager(photoList, position)
            Log.d("position", "$position")
        }

        binding.problemAddRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
        binding.problemAddRv.adapter = photoAdapter

        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = uri.toString()
                    addPhoto(photoList, photoAdapter, photoPath)
                    binding.problemAddBtn.setImageURI(uri)
                    binding.problemAddRv.smoothScrollToPosition(0)
                }
            }
        }

        binding.problemAddNextBtn.setOnClickListener {
            navController.navigate(R.id.action_navigation_problem_add_to_answer_fragment)
        }

        val horizontalSpaceHeight = resources.getDimensionPixelSize(R.dimen.item_space)
        binding.problemAddRv.addItemDecoration(ProblemInfoDecoration(horizontalSpaceHeight))
    }

    private fun openPhotoPager(photoList: List<String>, initialPosition: Int) {
        // 뷰모델에 데이터 설정
        problemInfoViewModel.setPhotoUris(photoList)
        problemInfoViewModel.setSelectedPhotoPosition(initialPosition)

        // 네비게이션으로 뷰페이저로 이동
        navController.navigate(R.id.action_navigation_problem_info_to_photo_slider_fragment)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        photoPickerLauncher.launch(intent)
    }

    private fun addPhoto(photoList: MutableList<String>, adapter: PhotoNewAdapter, photoPath: String) {
        photoList.add(photoPath)
        adapter.notifyItemInserted(photoList.size - 1)
        adapter.notifyItemChanged(photoList.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}