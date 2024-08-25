package com.wash.washandroid.presentation.fragment.note

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wash.washandroid.R
import com.wash.washandroid.databinding.BottomSheetDeleteRectangleBinding
import com.wash.washandroid.databinding.NoteBottomSheetOptionsBinding
import com.wash.washandroid.presentation.base.MainActivity

class DeleteRectangleBottomSheet(private val onDelete: () -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDeleteRectangleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.NoDimBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                // 배경을 투명하게 설정
                it.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BottomSheetDeleteRectangleBinding.inflate(inflater, container, false)

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(true)

        // 영역 삭제 버튼 클릭 리스너
        binding.btnDelete.setOnClickListener {
            onDelete()
            dismiss()  // 바텀 시트 닫기
        }

        // 취소 버튼 클릭 리스너
        binding.btnCancel.setOnClickListener {
            dismiss()  // 바텀 시트 닫기
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
