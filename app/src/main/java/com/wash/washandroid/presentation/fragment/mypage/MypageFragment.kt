package com.wash.washandroid.presentation.fragment.mypage

import MypageViewModel
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
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
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentMypageBinding
import com.wash.washandroid.presentation.fragment.login.LogoutPopupFragment
import com.yalantis.ucrop.UCrop
import java.io.File

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>

    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mypageViewModel.getAccountInfo()

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    startCrop(uri)
                }
            }
        }

        // 프로필 이미지 URL이 업데이트될 때마다 이미지 로드
        mypageViewModel.profileImageUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url)
                .transform(CircleCrop())
                .into(binding.mypageProfileIv)
        }

        cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri: Uri? = UCrop.getOutput(result.data!!)
                resultUri?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .transform(CircleCrop())
                        .into(binding.mypageProfileIv)

                    val filePath = requireContext().getRealPathFromURI(uri)
                    filePath?.let { path ->
                        mypageViewModel.uploadProfileImage(path)
                    }
                }
            }
        }

        mypageViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.mypageNameTv.text = nickname
        }

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

        binding.upgradeBtn.setOnClickListener {
            if (mypageViewModel.checkSubscriptionStatus()) {
                findNavController().navigate(R.id.navigation_subscribe)
            } else {
                findNavController().navigate(R.id.navigation_subscribe_menu)
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

        binding.mypageEditBtn.setOnClickListener {
            checkGalleryPermission()
        }
        binding.mypageProfileIv.setOnClickListener {
            checkGalleryPermission()
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            selectGallery()
        } else {
            Toast.makeText(context, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "croppedImage_${System.currentTimeMillis()}.jpg"))

        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(false)
        }

        val uCrop = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(200, 200)
            .withOptions(options)

        cropLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun Context.getRealPathFromURI(uri: Uri): String? {
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }

    private fun saveProfileImagePath(path: String) {
        val sharedPreferences = requireContext().getSharedPreferences("mypage_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profile_image_path", path).apply()
    }

    private fun loadProfileImage() {
        val sharedPreferences = requireContext().getSharedPreferences("mypage_prefs", Context.MODE_PRIVATE)
        val profileImagePath = sharedPreferences.getString("profile_image_path", null)

        if (!profileImagePath.isNullOrEmpty()) {
            val file = File(profileImagePath)
            if (file.exists()) {
                // 로그 추가: 경로 및 파일 확인
                Log.d("MypageFragment", "Loading profile image from path: $profileImagePath")

                binding.mypageProfileIv.setImageDrawable(null)
                Glide.with(this)
                    .load(file)
                    .transform(CircleCrop())
                    .into(binding.mypageProfileIv)
            } else {
                Log.e("MypageFragment", "File does not exist at path: $profileImagePath")
            }
        } else {
            Log.e("MypageFragment", "Profile image path is null or empty")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}