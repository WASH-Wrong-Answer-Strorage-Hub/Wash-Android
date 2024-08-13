import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

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
}
