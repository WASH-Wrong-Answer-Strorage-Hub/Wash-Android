import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wash.washandroid.data.entity.request.LoginRequest
import com.wash.washandroid.data.service.RetrofitClient
import com.wash.washandroid.presentation.fragment.study.StudyExitDialog.Companion.TAG
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

    // 서버로 사용자 정보 전송
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
                        val refreshToken = loginResponse.result?.refreshToken
                        // refreshToken을 사용하여 필요한 추가 작업 수행
                        Log.i(TAG, "Received refresh token: $refreshToken")
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

    companion object {
        private const val TAG = "MyPageViewModel"
    }

}