import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.view.PaymentMethod
import com.wash.washandroid.presentation.fragment.mypage.data.entity.request.NicknameRequest
import com.wash.washandroid.presentation.fragment.mypage.data.entity.request.PostGeneralLoginRequest
import com.wash.washandroid.presentation.fragment.mypage.data.entity.request.RefreshTokenRequest
import com.wash.washandroid.presentation.fragment.mypage.data.service.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File

class MypageViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences 초기화
    private val sharedPreferences = application.getSharedPreferences("mypage_prefs", Context.MODE_PRIVATE)

    // 닉네임 LiveData
    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname

    // 이름 LiveData
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> get() = _name

    // 이메일 LiveData
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    // 프로필 이미지 URL LiveData
    private val _profileImageUrl = MutableLiveData<String>()
    val profileImageUrl: LiveData<String> get() = _profileImageUrl

    private val _isSubscribed = MutableLiveData<Boolean>()
    val isSubscribed: LiveData<Boolean> get() = _isSubscribed

    // 토큰 관리 변수 추가
    private val _refreshToken = MutableLiveData<String?>()
//    private var paymentWidget: PaymentWidget? = null


    init {
        // ViewModel 초기화 시 닉네임, 이름, 이메일 로드
        loadNickname()
        loadName()
        loadEmail()
        loadSubscriptionStatus()
        loadProfileImageUrl()
    }

    // 닉네임 설정
    fun setNickname(newNickname: String) {
        // 기존의 닉네임을 nullable로 처리
        val oldNickname = _nickname.value

        // ViewModel의 LiveData를 즉시 업데이트하지 않음
        viewModelScope.launch {
            try {
                // 서버에 닉네임 변경 요청
                changeNicknameOnServer(newNickname)
            } catch (e: Exception) {
                // 예외가 발생하면 이전 닉네임으로 복원 (nullable 체크)
                oldNickname?.let {
                    _nickname.postValue(it)
                }
                Log.e(TAG, "Failed to change nickname on server", e)
            }
        }
    }

    // 닉네임 로드
    private fun loadNickname() {
        val savedNickname = sharedPreferences.getString("nickname", "default_nickname")
        _nickname.value = savedNickname
    }

    // 이름 로드
    private fun loadName() {
        val savedName = sharedPreferences.getString("name", "default_name")
        _name.value = savedName
    }

    // 이메일 로드
    private fun loadEmail() {
        val savedEmail = sharedPreferences.getString("email", "default_email")
        _email.value = savedEmail
    }

    // 구독 상태 로드
    private fun loadSubscriptionStatus() {
        val savedSubscriptionStatus = sharedPreferences.getBoolean("isSubscribed", false)
        _isSubscribed.value = savedSubscriptionStatus
    }

    // 구독 상태 확인
    fun checkSubscriptionStatus(): Boolean {
        return _isSubscribed.value ?: false
    }

    fun generalLoginUser(email: String, password: String) {
        val loginRequest = PostGeneralLoginRequest(email, password)

        viewModelScope.launch {
            val response = RetrofitClient.apiService.postGeneralLogin(loginRequest)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse?.isSuccess == true) {

                    // 서버로부터 받은 새로운 액세스 토큰을 헤더에서 가져옴
                    val newRefreshToken = loginResponse.result.refreshtoken
                    sharedPreferences.edit().putString("refreshToken", newRefreshToken).apply()

                    if (!newRefreshToken.isNullOrEmpty()) {
                        _refreshToken.value = newRefreshToken
                        Log.i(TAG, "New access token stored: $newRefreshToken")
                        sharedPreferences.edit().putString("refreshToken", newRefreshToken).apply()
                    } else {
                        Log.e(TAG, "Failed to retrieve new access token from server response")
                    }
                } else {
                    Log.e(TAG, "Server returned an error: ${loginResponse?.message}")
                }
            }
        }
    }

    // 서버로 사용자 정보 전송 후 액세스 토큰 저장
    fun sendSocialTokenToServer(socialType: String, token: String) {
        val bearerToken = "Bearer $token"

        viewModelScope.launch {
            try {
                val response = when (socialType) {
                    "kakao" -> RetrofitClient.apiService.postKakaoLogin(bearerToken)
                    "naver" -> RetrofitClient.apiService.postNaverLogin(bearerToken)
                    else -> throw IllegalArgumentException("Unknown social login type")
                }

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.isSuccess == true) {
                        Log.i(TAG, "$socialType token sent to server successfully")

                        // 서버로부터 받은 새로운 액세스 토큰을 헤더에서 가져옴
                        val newRefreshToken = response.headers()["authorization"]?.replace("Bearer ", "")

                        if (!newRefreshToken.isNullOrEmpty()) {
                            _refreshToken.value = newRefreshToken
                            Log.i(TAG, "New access token stored: $newRefreshToken")
                            sharedPreferences.edit().putString("refreshToken", newRefreshToken).apply()
                        } else {
                            Log.e(TAG, "Failed to retrieve new access token from server response")
                        }
                    } else {
                        Log.e(TAG, "Server returned an error: ${loginResponse?.message}")
                    }
                } else {
                    Log.e(TAG, "Failed to send $socialType token to server: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "Error sending $socialType token to server", e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error occurred", e)
            }
        }
    }

    // 로그아웃 요청 시 새로운 액세스 토큰 사용
    fun logoutUser() {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 로그아웃 요청을 할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.logoutUser(bearerToken)
                if (response.isSuccessful) {
                    val logoutResponse = response.body()
                    if (logoutResponse?.isSuccess == true) {
                        Log.i(TAG, "로그아웃 성공: ${logoutResponse.message}")
                        clearToken() // 로그아웃 성공 시 토큰 삭제
                    } else {
                        Log.e(TAG, "로그아웃 실패: ${logoutResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "로그아웃 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "로그아웃 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "로그아웃 중 서버 오류 발생", e)
            } catch (e: Exception) {
                Log.e(TAG, "로그아웃 중 예기치 않은 오류 발생", e)
            }
        }
    }

    fun deleteUserAccount() {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 탈퇴 요청을 할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteUser(bearerToken)
                if (response.isSuccessful) {
                    val deleteResponse = response.body()
                    if (deleteResponse?.isSuccess == true) {
                        clearToken()
                        Log.i(TAG, "회원탈퇴 성공: ${deleteResponse.message}")
                    } else {
                        Log.e(TAG, "회원탈퇴 실패: ${deleteResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "회원탈퇴 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "회원탈퇴 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "회원탈퇴 중 서버 오류 발생", e)
            } catch (e: Exception) {
                Log.e(TAG, "회원탈퇴 중 예기치 않은 오류 발생", e)
            }
        }
    }

    fun getAccountInfo() {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 유저조회를 할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserInfo(bearerToken)
                if (response.isSuccessful) {
                    val getUserInfoResponse = response.body()
                    if (getUserInfoResponse?.isSuccess == true) {
                        val userInfo = getUserInfoResponse.result
                        _nickname.value = userInfo.nickname
                        _name.value = userInfo.name
                        _email.value = userInfo.email

                        val isSubscribed = userInfo.subscribe == 0
                        _isSubscribed.value = isSubscribed

                        Log.i(TAG, "유저조회 성공: ${getUserInfoResponse.message}")
                    } else {
                        Log.e(TAG, "유저조회 실패: ${getUserInfoResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "유저조회 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "유저조회 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "유저조회 중 서버 오류 발생", e)
            } catch (e: Exception) {
                Log.e(TAG, "유저조회 중 예기치 않은 오류 발생", e)
            }
        }
    }

    private fun changeNicknameOnServer(newNickname: String) {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 닉네임 변경 요청을 할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"
        val nicknameRequest = NicknameRequest(newNickname)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.changeNickname(bearerToken, nicknameRequest)
                if (response.isSuccessful) {
                    val changeNicknameResponse = response.body()
                    if (changeNicknameResponse?.isSuccess == true) {
                        _nickname.postValue(newNickname)
                        Log.i(TAG, "닉네임 변경 성공: ${changeNicknameResponse.message}")
                    } else {
                        Log.e(TAG, "닉네임 변경 실패: ${changeNicknameResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "닉네임 변경 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "닉네임 변경 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "닉네임 변경 중 서버 오류 발생", e)
            } catch (e: Exception) {
                Log.e(TAG, "닉네임 변경 중 예기치 않은 오류 발생", e)
            }
        }
    }

    private fun loadProfileImageUrl() {
        val savedUrl = sharedPreferences.getString("profile_image_url", null)
        if (savedUrl != null) {
            _profileImageUrl.value = savedUrl
        }
    }

    fun uploadProfileImage(imagePath: String) {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 프로필 이미지를 업로드할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"
        val imagePart = createImagePart(imagePath)

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.changeProfileImage(bearerToken, imagePart)
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse?.isSuccess == true) {
                        val imageUrl = uploadResponse.result.url
                        _profileImageUrl.postValue(imageUrl)

                        // URL을 SharedPreferences에 저장
                        sharedPreferences.edit().putString("profile_image_url", imageUrl).apply()
                        Log.i(TAG, "프로필 이미지 업로드 성공: ${uploadResponse.message}")
                    } else {
                        Log.e(TAG, "프로필 이미지 업로드 실패: ${uploadResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "프로필 이미지 업로드 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "프로필 이미지 업로드 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 이미지 업로드 중 오류 발생", e)
            }
        }
    }


    private fun createImagePart(filePath: String, partName: String = "file"): MultipartBody.Part {
        // 파일 경로를 사용하여 파일 객체 생성
        val file = File(filePath)

        // 파일과 MIME 타입을 사용하여 요청 바디 생성
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)

        // MultipartBody.Part 객체 생성
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    fun approveSubscription() {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 구독 승인 요청을 할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.approveSubscription(bearerToken)
                if (response.isSuccessful) {
                    val approveResponse = response.body()
                    if (approveResponse?.isSuccess == true) {
                        _isSubscribed.value = true
                        Log.i(TAG, "구독 승인 성공: ${approveResponse.message}")
                    } else {
                        Log.e(TAG, "구독 승인 실패: ${approveResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "구독 승인 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "구독 승인 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "구독 승인 중 서버 오류 발생", e)
            } catch (e: Exception) {
                Log.e(TAG, "구독 승인 중 예기치 않은 오류 발생", e)
            }
        }
    }

    fun cancelSubscription() {
        val token = getRefreshToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "토큰이 존재하지 않습니다. 구독 취소 요청을 할 수 없습니다.")
            return
        }

        val bearerToken = "Bearer $token"

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelSubscription(bearerToken)
                if (response.isSuccessful) {
                    val cancelResponse = response.body()
                    if (cancelResponse?.isSuccess == true) {
                        _isSubscribed.value = false
                        Log.i(TAG, "구독 취소 성공: ${cancelResponse.message}")
                    } else {
                        Log.e(TAG, "구독 취소 실패: ${cancelResponse?.message}")
                    }
                } else if (response.code() == 401) {
                    Log.e(TAG, "구독 취소 요청 실패: Refresh Token 만료")
                    refreshAccessToken() // 토큰 재발급 시도
                } else {
                    Log.e(TAG, "구독 취소 요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "구독 취소 중 서버 오류 발생", e)
            } catch (e: Exception) {
                Log.e(TAG, "구독 취소 중 예기치 않은 오류 발생", e)
            }
        }
    }

    fun refreshAccessToken() {
        val refreshToken = getRefreshToken()

        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "Refresh Token이 존재하지 않습니다. 재발급 요청을 할 수 없습니다.")
            return
        }

        viewModelScope.launch {
            val request = RefreshTokenRequest(refreshToken)
            val response = RetrofitClient.apiService.postRefreshToken("Bearer $refreshToken", request)

            if (response.isSuccessful) {
                val newAccessToken = response.headers()["authorization"]?.replace("Bearer ", "")
                if (!newAccessToken.isNullOrEmpty()) {
                    _refreshToken.value = newAccessToken
                    Log.i(TAG, "새로운 Refresh Token이 성공적으로 재발급되었습니다: $newAccessToken")
                } else {
                    Log.e(TAG, "서버 응답에서 새로운 Refresh Token을 가져오지 못했습니다.")
                }
            } else {
                Log.e(TAG, "Refresh Token 재발급 요청 실패: ${response.errorBody()?.string()}")
            }
        }
    }

    // 토큰 초기화 함수
    fun clearToken() {
        _refreshToken.value = null
        sharedPreferences.edit().remove("accessToken").apply()
        Log.i(TAG, "Access token cleared from ViewModel and SharedPreferences")
    }

    // refresh token (jwt token) 반환 함수
    fun getRefreshToken(): String? {
        val token = _refreshToken.value
        return token ?: sharedPreferences.getString("refreshToken", null)
    }


    companion object {
        private const val TAG = "MyPageViewModel"
    }

}