import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navercorp.nid.oauth.NidOAuthPreferencesManager
import com.wash.washandroid.R
import com.wash.washandroid.presentation.fragment.home.ApiResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NetworkModule {

    private const val BASE_URL = "https://dev.team-wash.store"

    private var retrofit: Retrofit? = null

    private var accessToken: String? = null

    fun setAccessToken(token: String) {
        accessToken = token
    }

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                accessToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }

    fun getClient(): Retrofit {
        return retrofit ?: Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().also { retrofit = it }
    }
}
