package com.wash.washandroid.presentation.fragment.mypage

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentMypageBinding
import com.wash.washandroid.presentation.fragment.login.LogoutPopupFragment
import com.wash.washandroid.presentation.fragment.login.WithdrawalAccountFragment

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null

    private val binding get() = _binding!!

    // ActivityResultLauncher 선언
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 갤러리에서 이미지 선택 후 처리
        // 갤러리에서 이미지 선택 후 처리
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                // 선택된 이미지 처리
                selectedImageUri?.let {
                    // Glide를 사용하여 이미지 로드
                    Glide.with(this)
                        .load(it)
                        .transform(CircleCrop())
                        .into(binding.mypageProfileIv)
                    // mypageEditBtn 숨기기
                    binding.mypageEditBtn.visibility = View.GONE
                    binding.mypageEclipseBtn.visibility = View.GONE
                }
            }
        }

        binding.mypageEditNameLayout.setOnClickListener {
            findNavController().navigate(R.id.navigation_edit_account)
        }

        binding.mypageWithdrawalLayout.setOnClickListener {
            findNavController().navigate(R.id.navigation_withdrawal)
        }

        binding.mypageLogoutLayout.setOnClickListener {
            val logoutPopupFragment = LogoutPopupFragment()
            logoutPopupFragment.show(parentFragmentManager, "LogoutPopupFragment")
        }

        // 프로필 이미지나 eidt 버튼을 누르면 모두 사진 설정이 가능하게끔 함
        // 단 edit 버튼은 프로필 설정 후 사라짐
        binding.mypageEditBtn.setOnClickListener {
            checkGalleryPermission()
        }
        binding.mypageProfileIv.setOnClickListener {
            checkGalleryPermission()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            selectGallery()
        } else {
            // 권한이 거부된 경우 처리
            Toast.makeText(context, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkGalleryPermission() {

        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                selectGallery()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun selectGallery() {
        // 갤러리 호출 인텐트
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}