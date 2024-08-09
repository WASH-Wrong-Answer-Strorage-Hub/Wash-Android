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
import com.wash.washandroid.presentation.base.MainActivity

class SplashFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SplashFragment", "onCreateView called")
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bottom Navigation 숨기기
        (activity as? MainActivity)?.hideBottomNavigation(true)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("SplashFragment", "Notification permission granted")
            } else {
                Log.d("SplashFragment", "Notification permission denied")
            }
            navigateToNextScreen()
        }

        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        Log.d("SplashFragment", "askNotificationPermission called")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 이상
            Log.d("SplashFragment", "Android 13 or higher detected")
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("SplashFragment", "Notification permission not granted, requesting permission")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("SplashFragment", "Notification permission already granted")
                navigateToNextScreen()
            }
        } else {
            Log.d("SplashFragment", "Android version below 13, no need to request notification permission")
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        Log.d("SplashFragment", "Navigating to next screen")
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_socialLoginFragment)
        }, 4000)
    }
}