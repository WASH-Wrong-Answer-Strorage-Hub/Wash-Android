package com.wash.washandroid.presentation.fragment.mypage

import MypageViewModel
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
import androidx.fragment.app.activityViewModels
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

    // ViewModel을 activityViewModels()를 사용하여 공유 ViewModel 가져오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

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
        mypageViewModel.getAccountInfo()

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

        // ViewModel에서 닉네임을 가져와서 TextView에 설정
        mypageViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.mypageNameTv.text = nickname
        }

//        mypageViewModel.setSubscribed(true)  // 구독 상태를 true로 설정하여 테스트

        // 구독 유무에 따른 UI 업데이트
        mypageViewModel.isSubscribed.observe(viewLifecycleOwner) { isSubscribed ->
            if (isSubscribed) {
                binding.normalVerTv.text = "Pro 버전"
                binding.normalVerExplainTv.text = "문제 풀기, 문제 유형 정리, 문제 텍스트 인식,\n" +
                        "문제 검색, gpt 문제 풀이 \n"
                binding.upgradeBtn.visibility = View.GONE
                binding.upgradeTv.visibility = View.GONE
            } else {
                binding.normalVerTv.text = "일반 버전"
                binding.normalVerExplainTv.text = "문제 풀기, 문제 유형 정리"
                binding.upgradeBtn.visibility = View.VISIBLE
                binding.upgradeTv.visibility = View.VISIBLE
            }
        }

        binding.mypageSubscribeLayout.setOnClickListener {

            if (mypageViewModel.checkSubscriptionStatus()) {
                findNavController().navigate(R.id.navigation_subscribe)
            } else {
                findNavController().navigate(R.id.navigation_subscribe_menu)
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