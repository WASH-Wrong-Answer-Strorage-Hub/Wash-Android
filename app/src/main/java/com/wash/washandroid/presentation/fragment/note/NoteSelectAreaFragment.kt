package com.wash.washandroid.presentation.fragment.note

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentNoteSelectAreaBinding
import com.wash.washandroid.presentation.base.MainActivity

class NoteSelectAreaFragment : Fragment() {

    private var _binding: FragmentNoteSelectAreaBinding? = null
    private val binding get() = _binding!!
    private var originalPaddingTop: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteSelectAreaBinding.inflate(inflater, container, false)
        Toast.makeText(requireContext(), "select area fragment", Toast.LENGTH_SHORT).show()
        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 패딩을 저장하고 0으로 설정
        originalPaddingTop = (activity as MainActivity).findViewById<View>(R.id.container).paddingTop
        (activity as MainActivity).setContainerPadding(0)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        // bundle로 이미지 uri 수신
        val imageUri = arguments?.getString("imgUri")
        if (imageUri == "0") {
            Toast.makeText(requireContext(), "이미지 불러오기 실패", Toast.LENGTH_SHORT).show()
        } else {
            loadCapturedImage(Uri.parse(imageUri))
        }

        binding.btnCrop.setOnClickListener {
            // Crop and save the image here
        }
    }

    private fun loadCapturedImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(binding.ivCaptured)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 원래 패딩을 복원
        (activity as MainActivity).setContainerPadding(originalPaddingTop)

        // bottom navigation 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        _binding = null
    }
}
