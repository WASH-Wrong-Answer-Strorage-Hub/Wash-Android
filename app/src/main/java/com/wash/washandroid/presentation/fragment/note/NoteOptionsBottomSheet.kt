package com.wash.washandroid.presentation.fragment.note

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
            checkPermissions()
        }

        // 앨범에서 선택하기
        binding.buttonGallery.setOnClickListener {
            checkPermissions()
        }

        // 취소 버튼
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * 권한 체크
     * 승인이 안 된 경우 권한 요청.
     */
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isEmpty()) {
            // 권한이 이미 승인됨
            // 실제 기능 수행
            onPermissionsGranted()
        } else {
            // 권한 요청
            requestPermissions(permissionsNeeded.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    /**
     * 권한이 모두 승인된 경우 호출됨.
     */
    private fun onPermissionsGranted() {
        Toast.makeText(requireContext(), "권한 승인됨", Toast.LENGTH_SHORT).show()
        // 카메라 촬영 기능 또는 앨범 선택 기능 추가
    }

    /**
     * Handles the result of the permission request.
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int).
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // 모든 권한이 승인됨
                onPermissionsGranted()
            } else {
                Toast.makeText(requireContext(), "권한을 승인하지 않으면 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 100
    }
}
