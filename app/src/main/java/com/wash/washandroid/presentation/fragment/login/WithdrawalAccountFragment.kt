package com.wash.washandroid.presentation.fragment.login

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentWithdrawalAccountBinding
import com.wash.washandroid.presentation.fragment.mypage.EditAccountFragment
import com.wash.washandroid.presentation.fragment.mypage.MypageFragment

class WithdrawalAccountFragment : Fragment() {

    private var _binding: FragmentWithdrawalAccountBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWithdrawalAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.withdrawalAccountBackBtn.setOnClickListener {
            Toast.makeText(requireContext(), "되돌아가시겠습니까?", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // 이전 프래그먼트로 돌아가기
        }

        binding.withdrawalBtn.setOnClickListener {
            if (isKakaoLoggedIn()) {
                kakaoWithdrawal()
            } else if (isNaverLoggedIn()) {
                naverWithdrawal()
            } else {
                Toast.makeText(requireContext(), "로그인 상태를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            navigateToLoginFragment() // 탈퇴 후 로그인 화면으로 이동
        }
    }

    private fun isKakaoLoggedIn(): Boolean {
        return UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())
    }

    private fun isNaverLoggedIn(): Boolean {
        return NaverIdLoginSDK.getAccessToken() != null
    }

    private fun naverWithdrawal() {
        NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
            override fun onSuccess() {
                // Naver 로그아웃 성공
            }
            override fun onFailure(httpStatus: Int, message: String) {
                Log.d(TAG, "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Log.d(TAG, "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        })
    }

    private fun kakaoWithdrawal() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e(TAG, "연결 끊기 실패", error)
            } else {
                Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
            }
        }
    }

    private fun navigateToLoginFragment() {
        findNavController().navigate(R.id.action_withdrawalAccountFragment_to_socialLoginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
