package com.wash.washandroid.presentation.fragment.splash

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wash.washandroid.R
import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.wash.washandroid.BuildConfig
import com.wash.washandroid.presentation.base.MainActivity

class SplashFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NaverIdLoginSDK.initialize(
            requireContext(),
            BuildConfig.NAVER_CLIENT_ID,
            BuildConfig.NAVER_CLIENT_SECRET,
            BuildConfig.NAVER_CLIENT_NAME
        )

        // Bottom Navigation 숨기기
        (activity as? MainActivity)?.hideBottomNavigation(true)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            navigateToNextScreen()
        }

        askNotificationPermission()
    }

    private fun askNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 이상
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                navigateToNextScreen()
            }
        } else {
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        Handler(Looper.getMainLooper()).postDelayed({

            checkLoginStatus { isLoggedIn ->
                if (isLoggedIn) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                    // Bottom Navigation 보이기
                    (activity as? MainActivity)?.hideBottomNavigation(false)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_socialLoginFragment)
                }
            }

        }, 4000)
    }

    private fun checkLoginStatus(callback: (Boolean) -> Unit) {
        var isLoggedIn = false

        // Kakao 로그인 상태 확인
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (tokenInfo != null) {
                callback(isLoggedIn)
                isLoggedIn = true
            } else {
                // Naver 로그인 상태 확인
                isLoggedIn = !NaverIdLoginSDK.getAccessToken().isNullOrEmpty()
                callback(isLoggedIn)
                isLoggedIn = true
            }
        }
    }

}