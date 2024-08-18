package com.wash.washandroid.presentation.fragment.login

import MypageViewModel
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentLogoutPopupBinding
import com.wash.washandroid.presentation.fragment.mypage.MypageFragment

class LogoutPopupFragment : DialogFragment() {

    private var _binding: FragmentLogoutPopupBinding? = null
    private val binding get() = _binding!!
    private lateinit var mypageViewModel: MypageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogoutPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mypageViewModel = activityViewModels<MypageViewModel>().value

        binding.logoutCancelView.setOnClickListener {
            dismiss()
        }

        binding.logoutView.setOnClickListener {
            if (isKakaoLoggedIn()) {
                kakaoLogout()
            } else if (isNaverLoggedIn()) {
                naverLogout()
            } else {
                Toast.makeText(requireContext(), "로그인 상태를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            // ViewModel을 통해 로그아웃 요청
            mypageViewModel.logoutUser()
            navigateToLoginFragment()
        }
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그의 배경을 어둡게 설정
        dialog?.window?.setDimAmount(0.7f)
    }

    private fun isKakaoLoggedIn(): Boolean {
        // Kakao 로그인 상태 확인
        return UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())
    }

    private fun kakaoLogout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("LogoutPopupFragment", "카카오 로그아웃 실패.", error)
            } else {
                Log.i("LogoutPopupFragment", "카카오 로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    private fun isNaverLoggedIn(): Boolean {
        // Naver 로그인 상태 확인
        return NaverIdLoginSDK.getAccessToken() != null
    }

    private fun naverLogout() {
        NaverIdLoginSDK.logout() // 네이버 로그아웃
        Log.i("LogoutPopupFragment", "네이버 로그아웃 성공. SDK에서 토큰 삭제됨")
    }

    private fun navigateToLoginFragment() {
        dismiss()
        findNavController().navigate(R.id.navigation_social_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
