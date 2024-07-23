package com.wash.washandroid.presentation.base

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.wash.washandroid.R
import com.wash.washandroid.databinding.ActivityMainBinding
import com.wash.washandroid.presentation.fragment.note.NoteCameraFragment
import com.wash.washandroid.presentation.fragment.note.NoteOptionsBottomSheet

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navi)
        bottomNavigationView.setupWithNavController(navController)

        // BottomNavigationView 아이템 선택 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                // add icon 클릭 시 NoteOptionsBottomSheet 로 이동
                R.id.navigation_note -> {
                    val bottomSheet = NoteOptionsBottomSheet()
                    bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                    true
                }

                else -> {
                    // Note 아이템이 아닌 경우 navController로 처리
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }


    // Set main container padding
    fun setContainerPadding(paddingTop: Int) {
        findViewById<View>(R.id.container).setPadding(0, paddingTop, 0, 0)
    }

    // Set Bottom navigation Visibility
    fun setBottomNavigationVisibility(visibility: Int) {
        findViewById<View>(R.id.bottom_navi).visibility = visibility
    }

    // Start camera fragment
    fun startCameraFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, NoteCameraFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }
}