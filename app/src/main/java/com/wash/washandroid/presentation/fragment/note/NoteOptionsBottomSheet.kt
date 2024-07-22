package com.wash.washandroid.presentation.fragment.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wash.washandroid.R
import com.wash.washandroid.databinding.NoteBottomSheetOptionsBinding

class NoteOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: NoteBottomSheetOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.NoDimBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteBottomSheetOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 카메라로 촬영하기
        binding.buttonCamera.setOnClickListener {
            dismiss()
        }

        // 앨범에서 선택하기
        binding.buttonGallery.setOnClickListener {
            dismiss()
        }

        // 취소 버튼
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
