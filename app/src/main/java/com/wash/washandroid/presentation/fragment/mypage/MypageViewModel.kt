import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.data.entity.request.NicknameRequest
import com.wash.washandroid.data.service.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

    private val _isSubscribed = MutableLiveData<Boolean>()
    val isSubscribed: LiveData<Boolean> get() = _isSubscribed

    // 토큰 관리 변수 추가
    private val _refreshToken = MutableLiveData<String?>()

    init {
        // ViewModel 초기화 시 닉네임, 이름, 이메일 로드
        loadNickname()
        loadName()
        loadEmail()
        loadSubscriptionStatus()
    }

    // 닉네임 설정
    fun setNickname(newNickname: String) {
        _nickname.value = newNickname
        saveNickname(newNickname)
        // 서버로 닉네임 변경 요청
        changeNicknameOnServer(newNickname)
    }

    // 이름 설정
    fun setName(newName: String) {
        _name.value = newName
        saveName(newName)
    }

    // 이메일 설정
    fun setEmail(newEmail: String) {
        _email.value = newEmail
        saveEmail(newEmail)
    }

    fun setSubscribed(isSubscribed: Boolean) {
        _isSubscribed.value = isSubscribed
    }

    // 닉네임 저장
    private fun saveNickname(nickname: String) {
        sharedPreferences.edit().putString("nickname", nickname).apply()
    }

    // 이름 저장
    private fun saveName(name: String) {
        sharedPreferences.edit().putString("name", name).apply()
    }

    // 이메일 저장
    private fun saveEmail(email: String) {
        sharedPreferences.edit().putString("email", email).apply()
    }

    // 구독 상태 저장
    private fun saveSubscriptionStatus(isSubscribed: Boolean) {
        sharedPreferences.edit().putBoolean("isSubscribed", isSubscribed).apply()
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
                        val newAccessToken = response.headers()["authorization"]?.replace("Bearer ", "")

                        if (!newAccessToken.isNullOrEmpty()) {
                            setAccessToken(newAccessToken)  // 새로운 액세스 토큰을 저장
                            Log.i(TAG, "New access token stored: $newAccessToken")
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
        val token = _refreshToken.value
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

    // 탈퇴 요청 시 새로운 액세스 토큰 사용
    fun deleteUserAccount() {
        val token = _refreshToken.value
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
        val token = _refreshToken.value
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
                        Log.i(TAG, "유저조회 성공: ${getUserInfoResponse.message}")
                    } else {
                        Log.e(TAG, "유저조회 실패: ${getUserInfoResponse?.message}")
                    }
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
        val token = _refreshToken.value
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
                        _nickname.value = changeNicknameResponse.result.nickname
                        Log.i(TAG, "닉네임 변경 성공: ${changeNicknameResponse.message}")
                    } else {
                        Log.e(TAG, "닉네임 변경 실패: ${changeNicknameResponse?.message}")
                    }
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

    // 토큰 초기화 함수
    fun clearToken() {
        _refreshToken.value = null
        sharedPreferences.edit().remove("accessToken").apply()
        Log.i(TAG, "Access token cleared from ViewModel and SharedPreferences")
    }

    // 토큰 저장 함수 추가
    fun setAccessToken(token: String?) {
        _refreshToken.value = token
    }

    companion object {
        private const val TAG = "MyPageViewModel"
    }

}