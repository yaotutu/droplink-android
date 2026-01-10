package top.yaotutu.droplink.data.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import top.yaotutu.droplink.BuildConfig
import top.yaotutu.droplink.data.manager.TokenManager
import top.yaotutu.droplink.data.network.api.AuthApiService
import top.yaotutu.droplink.data.network.api.GotifyApiService
import top.yaotutu.droplink.data.network.interceptor.LoggingInterceptor
import top.yaotutu.droplink.data.network.interceptor.TokenInterceptor
import top.yaotutu.droplink.data.settings.AppSettings
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端管理类
 *
 * React 对标：
 * - 类似创建 axios 实例的工厂函数
 * - const axiosInstance = axios.create({ baseURL, timeout, interceptors })
 *
 * 核心功能：
 * 1. 创建和管理 Retrofit 实例（单例模式）
 * 2. 配置 OkHttp（拦截器、超时）
 * 3. 支持双服务器配置（认证服务器 + Gotify 服务器）
 * 4. 提供 API 服务实例
 */
class RetrofitClient private constructor(context: Context) {

    private val appSettings = AppSettings.getInstance(context)
    private val tokenManager = TokenManager.getInstance(context)

    // JSON 序列化配置
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true        // 忽略未知字段
        coerceInputValues = true        // 强制转换输入值
        isLenient = true                // 宽松模式
        explicitNulls = false           // null 值不序列化
    }

    /**
     * 创建 OkHttpClient
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(TokenInterceptor(tokenManager))
            .addInterceptor(LoggingInterceptor(BuildConfig.DEBUG))
            .build()
    }

    /**
     * 创建 Retrofit 实例
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * 认证服务器 Retrofit 实例（懒加载）
     */
    private val authRetrofit: Retrofit by lazy {
        val baseUrl = appSettings.getAuthServerUrl()
        createRetrofit(baseUrl)
    }

    /**
     * Gotify 服务器 Retrofit 实例（懒加载，后期使用）
     */
    private val gotifyRetrofit: Retrofit by lazy {
        val baseUrl = appSettings.getGotifyServerUrl()
        createRetrofit(baseUrl)
    }

    /**
     * 获取认证 API 服务
     */
    fun getAuthApiService(): AuthApiService {
        return authRetrofit.create(AuthApiService::class.java)
    }

    /**
     * 获取 Gotify API 服务
     */
    fun getGotifyApiService(): GotifyApiService {
        return gotifyRetrofit.create(GotifyApiService::class.java)
    }

    companion object {
        @Volatile
        private var instance: RetrofitClient? = null

        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): RetrofitClient {
            return instance ?: synchronized(this) {
                instance ?: RetrofitClient(context.applicationContext).also { instance = it }
            }
        }

        /**
         * 重置单例实例
         * 用于服务器地址修改后重新创建 Retrofit 实例
         */
        fun resetInstance() {
            synchronized(this) {
                instance = null
            }
        }
    }
}
