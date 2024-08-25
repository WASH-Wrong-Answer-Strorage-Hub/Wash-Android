
package com.wash.washandroid.presentation.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kakao.sdk.common.KakaoSdk
import com.wash.washandroid.BuildConfig
import com.wash.washandroid.R
import com.wash.washandroid.databinding.ActivityMainBinding
import com.wash.washandroid.presentation.fragment.login.SocialLoginFragment
import com.wash.washandroid.presentation.fragment.splash.SplashFragment
import com.wash.washandroid.presentation.fragment.note.NoteCameraFragment
import com.wash.washandroid.presentation.fragment.note.NoteOptionsBottomSheet

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // BottomNavigationView 설정
        binding.bottomNavi.setupWithNavController(navController)
        binding.bottomNavi.itemIconTintList = null


        // BottomNavigationView 아이템 선택 리스너 설정
        binding.bottomNavi.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_study -> {
                    navController.navigate(R.id.navigation_study)
                    true
                }
                R.id.navigation_note -> {
//                    val bottomSheet = NoteOptionsBottomSheet()
//                    bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                    navController.navigate(R.id.navigation_note)
                    true
                }
                R.id.navigation_graph -> {
                    navController.navigate(R.id.navigation_graph)
                    true
                }
                R.id.navigation_mypage -> {
                    navController.navigate(R.id.navigation_mypage)
                    true
                }
                else -> false
            }
        }

        // SplashFragment를 가장 먼저 보여줌
        if (savedInstanceState == null) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
            navHostFragment.navController.setGraph(R.navigation.mobile_navigation)
        }

    }

    fun hideBottomNavigation(state:Boolean){
        if(state) binding.bottomNavi.visibility = View.GONE else binding.bottomNavi.visibility=View.VISIBLE
    }

    // Set main container padding
    fun setContainerPadding(paddingTop: Int) {
        findViewById<View>(R.id.container).setPadding(0, paddingTop, 0, 0)
    }
}