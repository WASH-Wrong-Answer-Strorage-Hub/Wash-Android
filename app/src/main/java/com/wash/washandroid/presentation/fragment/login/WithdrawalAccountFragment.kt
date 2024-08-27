package com.wash.washandroid.presentation.fragment.login

import MypageViewModel
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.auth.TokenManagerProvider
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

    private lateinit var mypageViewModel: MypageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWithdrawalAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mypageViewModel = activityViewModels<MypageViewModel>().value

        binding.withdrawalAccountBackBtn.setOnClickListener {
            findNavController().popBackStack() // 이전 프래그먼트로 돌아가기
        }

        binding.withdrawalBtn.setOnClickListener {
            isKakaoLoggedIn { isKakaoLoggedIn ->
                if (isKakaoLoggedIn) {
                    mypageViewModel.deleteUserAccount()
                    kakaoWithdrawal()
                    navigateToLoginFragment()
                } else if (isNaverLoggedIn()) {
                    mypageViewModel.deleteUserAccount()
                    naverWithdrawal()
                    navigateToLoginFragment()
                } else {
                    Toast.makeText(requireContext(), "로그인 상태를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isKakaoLoggedIn(callback: (Boolean) -> Unit) {
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                Log.e("LogoutPopupFragment", "카카오 로그인 상태 확인 실패", error)
                callback(false)
            } else if (tokenInfo != null) {
                Log.i("LogoutPopupFragment", "카카오 로그인 상태 확인 성공: $tokenInfo")
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    private fun isNaverLoggedIn(): Boolean {
        return NaverIdLoginSDK.getAccessToken() != null
    }

    private fun naverWithdrawal() {
        NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
            override fun onSuccess() {
                // Naver 로그아웃 성공
                Log.i(TAG, "네이버 탈퇴 성공. SDK에서 토큰 삭제됨")
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.d(TAG, "네이버 탈퇴 실패: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Log.d(TAG, "네이버 탈퇴 실패: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        })
        Log.e(TAG, "네이버 액세스 토큰을 가져오지 못했습니다.")
    }

    private fun kakaoWithdrawal() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e(TAG, "카카오 탈퇴 실패", error)
            } else {
                Log.i(TAG, "카카오 탈퇴 성공. SDK에서 토큰 삭제됨")
            }
        }
        Log.e(TAG, "카카오 액세스 토큰을 가져오지 못했습니다.")
    }

    private fun navigateToLoginFragment() {
        findNavController().navigate(R.id.action_withdrawalAccountFragment_to_socialLoginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "WithdrawalAccountFragment"
    }
}
