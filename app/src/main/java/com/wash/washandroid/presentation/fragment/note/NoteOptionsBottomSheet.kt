package com.wash.washandroid.presentation.fragment.note

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wash.washandroid.R
import com.wash.washandroid.databinding.NoteBottomSheetOptionsBinding
import com.wash.washandroid.presentation.base.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: NoteBottomSheetOptionsBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_PERMISSIONS = 100
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.NoDimBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                // 하단에 공간을 남겨두기 위해 bottomSheet의 높이를 조정
                it.post {
                    val height = it.height
                    behavior.peekHeight = height + 50.dpToPx()
                }

                // 배경을 투명하게 설정
                it.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteBottomSheetOptionsBinding.inflate(inflater, container, false)

        // Bottom navigation bar 보이게
        (activity as MainActivity).hideBottomNavigation(false)

        checkPermissions { }


        // sdk에 따라 앨범 다르게 보여주기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33 이상인 경우 Photo Picker 초기화
            photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
                handleUriResult(uri)
            }
        } else {
            // API 33 이하인 경우 기존의 gallery launcher 초기화
            galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                handleUriResult(uri)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 카메라로 촬영하기
        binding.buttonCamera.setOnClickListener {
            checkPermissions { startCameraFragment() }
            dismiss()
        }

        // 앨범에서 선택하기
        binding.buttonGallery.setOnClickListener {
            pickImage()
        }

        // 취소 버튼
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun handleUriResult(uri: Uri?) {
        Toast.makeText(requireContext(), "Result received: ${uri}", Toast.LENGTH_SHORT).show()
        Log.d("fraglog", "Result received")
        uri?.let {
            Log.d("fraglog", it.toString())
            val imgUri = bundleOf("imgUri2" to it.toString())
            // select area fragment로 이동
            findNavController().navigate(R.id.action_navigation_note_to_navigation_note_select_area, imgUri)
            dismiss()
        } ?: Log.d("fraglog", "Uri is null")
    }

    fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33 이상인 경우 Photo Picker 사용
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            // API 33 이하인 경우 기존의 gallery launcher 사용
            galleryLauncher.launch("image/*")
        }
    }

    /**
     * 권한 체크
     * 승인이 안 된 경우 권한 요청.
     */
    private fun checkPermissions(onPermissionGranted: () -> Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isEmpty()) {
            // 권한이 이미 승인됨
            // 실제 기능 수행
            onPermissionsGranted(onPermissionGranted)
        } else {
            // 권한 요청
            requestPermissions(permissionsNeeded.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    // start Camera Fragment
    private fun startCameraFragment() {
        val action = R.id.action_navigation_note_to_navigation_note_cam
        findNavController().navigate(action)
        dismiss() // BottomSheet를 닫습니다.
    }


    /**
     * 권한이 모두 승인된 경우 호출됨.
     */
    private fun onPermissionsGranted(onPermissionGranted: () -> Unit) {
//        Toast.makeText(requireContext(), "권한 승인됨", Toast.LENGTH_SHORT).show()
        onPermissionGranted()
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
                onPermissionsGranted { /* 필요 시 아무것도 하지 않도록 빈 람다를 전달 */ }
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
