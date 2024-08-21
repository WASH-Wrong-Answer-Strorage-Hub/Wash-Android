package com.wash.washandroid.presentation.fragment.login

import MypageViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.wash.washandroid.BuildConfig
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentSocialLoginBinding
import com.wash.washandroid.presentation.base.MainActivity

class SocialLoginFragment : Fragment() {

    private var _binding: FragmentSocialLoginBinding? = null
    private val binding get() = _binding!!

    // ViewModel을 activityViewModels()를 사용하여 공유 ViewModel 가져오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bottom Navigation 숨기기
        (activity as? MainActivity)?.hideBottomNavigation(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.socialLoginNaverLayout.setOnClickListener {
            naverLogin()
            // Bottom Navigation 보이기
            (activity as? MainActivity)?.hideBottomNavigation(false)
        }
        binding.socialLoginKakaoLayout.setOnClickListener {
            Log.d(TAG, "Kakao login button clicked")
            kakaoLogin()
            // Bottom Navigation 보이기
            (activity as? MainActivity)?.hideBottomNavigation(false)
        }
    }

    private fun kakaoLogin() {
        Log.d(TAG, "Attempting Kakao login")
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오 계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오 계정으로 로그인 성공 ${token.accessToken}")
                mypageViewModel.sendSocialTokenToServer("kakao", token.accessToken)
                navigateToHomeFragment()  // HomeFragment로 이동
            } else {
                Log.w(TAG, "Kakao login callback invoked with no error and no token")
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            Log.d(TAG, "Kakao is available, attempting login with KakaoTalk")
            UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오로 로그인 실패", error)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        Log.w(TAG, "Kakao login was cancelled by the user")
                        return@loginWithKakaoTalk
                    }
                    Log.d(TAG, "Falling back to Kakao Account login")
                    UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오로 로그인 성공 ${token.accessToken}")
                    // 서버로 토큰 전송
                    mypageViewModel.sendSocialTokenToServer("kakao", token.accessToken)
                    navigateToHomeFragment()
                }
            }
        } else {
            Log.d(TAG, "KakaoTalk is not available, attempting login with Kakao Account")
            UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
        }
    }

    private fun naverLogin() {

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.d(TAG, "Naver login successful")
                Log.d(TAG, "AccessToken : " + NaverIdLoginSDK.getAccessToken())
                Log.d(TAG, "ReFreshToken : " + NaverIdLoginSDK.getRefreshToken())
                Log.d(TAG, "Expires : " + NaverIdLoginSDK.getExpiresAt().toString())
                Log.d(TAG, "TokenType : " + NaverIdLoginSDK.getTokenType())
                Log.d(TAG, "State : " + NaverIdLoginSDK.getState().toString())

                val accessToken = NaverIdLoginSDK.getAccessToken()

                // Check if the access token is expired
                val expiresAt = NaverIdLoginSDK.getExpiresAt() * 1000  // 초 단위를 밀리초로 변환
                if (accessToken != null && expiresAt < System.currentTimeMillis()) {
                    Log.d(TAG, "Access token is expired, refreshing token")
                    NaverIdLoginSDK.getRefreshToken()
                }

                val newAccessToken = NaverIdLoginSDK.getAccessToken()
                if (newAccessToken != null) {
                    mypageViewModel.sendSocialTokenToServer("naver", newAccessToken)
                } else {
                    Log.e(TAG, "Failed to refresh Naver token")
                }
                navigateToHomeFragment()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.e(TAG, "Naver login failed: $errorCode $errorDescription")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
    }

    private fun navigateToHomeFragment() {
        findNavController().navigate(R.id.navigation_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SocialLoginFragment"
    }
}