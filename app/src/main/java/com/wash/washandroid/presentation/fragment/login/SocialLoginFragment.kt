package com.wash.washandroid.presentation.fragment.login

import MypageViewModel
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

        // 네이티브 앱 키를 사용하여 카카오 SDK 초기화
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_API_KEY)
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
            kakaoLogin()
            // Bottom Navigation 보이기
            (activity as? MainActivity)?.hideBottomNavigation(false)
        }
    }

    private fun kakaoLogin() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                getKakaoUserInfo() // 로그인 성공 후 사용자 정보 가져오기
                navigateToHomeFragment()
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    getKakaoUserInfo()
                    navigateToHomeFragment()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
        }
    }

    private fun getKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                // 카카오는 닉네임이 이름
                val nickname = user.kakaoAccount?.profile?.nickname ?: "Unknown"
//                val name = user.kakaoAccount?.profile?.name ?: "No Name" // 카카오 계정에 이름이 있을 경우 사용
                val email = user.kakaoAccount?.email ?: "No Email"

                // ViewModel에 사용자 정보 저장
                mypageViewModel.setName(nickname)
                mypageViewModel.setEmail(email)

                Log.i(TAG, "사용자 정보 요청 성공: $nickname,$email")
            }
        }
    }


    private fun naverLogin() {
        NaverIdLoginSDK.initialize(
            requireContext(),
            BuildConfig.NAVER_CLIENT_ID,
            BuildConfig.NAVER_CLIENT_SECRET,
            BuildConfig.NAVER_CLIENT_NAME
        )

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.d(TAG, "AccessToken : " + NaverIdLoginSDK.getAccessToken())
                Log.d(TAG, "ReFreshToken : " + NaverIdLoginSDK.getRefreshToken())
                Log.d(TAG, "Expires : " + NaverIdLoginSDK.getExpiresAt().toString())
                Log.d(TAG, "TokenType : " + NaverIdLoginSDK.getTokenType())
                Log.d(TAG, "State : " + NaverIdLoginSDK.getState().toString())

                getNaverUserInfo() // 로그인 성공 후 사용자 정보 가져오기
                navigateToHomeFragment()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.e(TAG, "$errorCode $errorDescription")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
    }

    private fun getNaverUserInfo() {
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val nickname = response.profile?.nickname ?: "Unknown"
                val name = response.profile?.name ?: "No Name"
                val email = response.profile?.email ?: "No Email"

                // ViewModel에 사용자 정보 저장
                mypageViewModel.setNickname(nickname)  // 닉네임 설정
                mypageViewModel.setName(name)  // 이름 설정
                mypageViewModel.setEmail(email)  // 이메일 설정

                Log.i(TAG, "네이버 사용자 정보 요청 성공: $nickname, $name, $email")
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e(TAG, "네이버 사용자 정보 요청 실패: $message")
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e(TAG, "네이버 사용자 정보 요청 중 오류 발생: $message")
            }
        })
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
