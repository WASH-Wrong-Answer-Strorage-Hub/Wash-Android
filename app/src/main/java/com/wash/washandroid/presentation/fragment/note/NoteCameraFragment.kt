package com.wash.washandroid.presentation.fragment.note

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentNoteCameraBinding
import com.wash.washandroid.presentation.base.MainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class NoteCameraFragment : Fragment() {
    private var _binding: FragmentNoteCameraBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var originalPaddingTop: Int = 0
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteCameraBinding.inflate(inflater, container, false)

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        setupViewPager()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 패딩을 저장하고 0으로 설정
        originalPaddingTop = (activity as MainActivity).findViewById<View>(R.id.container).paddingTop
        (activity as MainActivity).setContainerPadding(0)

        // Bottom navigation bar 숨기기
        (activity as MainActivity).hideBottomNavigation(true)

        outputDirectory = getOutputDirectory()

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        binding.btnClose.setOnClickListener {
            navController = Navigation.findNavController(view)
            navController.navigateUp()
        }

        binding.btnSwitchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun setupViewPager() {
        // 어댑터 생성
        val adapter = QuestionModePagerAdapter(requireActivity())
        viewPager.adapter = adapter

        // TabLayoutMediator를 사용하여 TabLayout과 ViewPager2 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabTextView = TextView(requireContext())

            // 커스텀 폰트 로드
            val typeface = ResourcesCompat.getFont(requireContext(), R.font.mangoddobak_r)

            tabTextView.text = adapter.getPageTitle(position)
            tabTextView.typeface = typeface
            tabTextView.textSize = 13f  // 텍스트 크기 설정
            tabTextView.gravity = Gravity.CENTER
            tabTextView.setTextColor(
                if (position == viewPager.currentItem) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.middle_gray)
            ) // 탭의 기본 텍스트 색상 설정

            tabTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            tab.customView = tabTextView
        }.attach()

        // viewpager 모드 변경 리스너 설정
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onTabOrPageSelected(position)
            }
        })

        // 탭 선택 리스너 설정
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                viewPager.currentItem = position
                onTabOrPageSelected(position)
                updateTabColors(tab.position)

                Log.d("fraglog", "탭 $position 클릭됨")
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                val position = tab.position
                viewPager.currentItem = position
                onTabOrPageSelected(position)

                Log.d("fraglog", "탭 $position 클릭됨")
            }
        })
    }

    // 모드 전환 시 로그 출력 및 추가 작업
    private fun onTabOrPageSelected(position: Int) {
        // 탭 변경 시 로그 출력
        val mode = if (position == 0) "한 문제" else "여러 문제"
        Log.d("fraglog", "현재 모드 : $mode")
        updateTabColors(position)
    }

    private fun updateTabColors(selectedPosition: Int) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.customView?.let { tabView ->
                val textView = tabView as TextView
                textView.setTextColor(
                    if (i == selectedPosition) ContextCompat.getColor(requireContext(), R.color.white)
                    else ContextCompat.getColor(requireContext(), R.color.middle_gray)
                )
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Failed to bind camera use cases", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(requireContext(), "Photo capture succeeded: $savedUri", Toast.LENGTH_SHORT).show()

                    // NoteSelectAreaFragment로 이미지 URI 전달
                    Log.d("fraglog", "captured pic uri : $savedUri")
                    val imgUri = bundleOf("imgUri" to savedUri.toString())
                    findNavController().navigate(R.id.action_navigation_note_cam_to_navigation_note_select_area, imgUri)
                }

            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().getExternalFilesDir(null)?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireContext().filesDir
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 원래 패딩을 복원
        (activity as MainActivity).setContainerPadding(originalPaddingTop)

        _binding = null
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}