package top.yaotutu.droplink.data.settings

import android.content.Context
import top.yaotutu.droplink.BuildConfig

/**
 * 应用设置管理
 * 负责存储和管理用户可配置的设置项
 * 使用 SharedPreferences 持久化存储
 */
class AppSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_API_BASE_URL = "api_base_url"
        private const val KEY_GOTIFY_SERVER_URL = "gotify_server_url"  // 新增：Gotify 服务器地址

        @Volatile
        private var instance: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return instance ?: synchronized(this) {
                instance ?: AppSettings(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * 获取 API 基础地址（保留旧方法，向后兼容）
     * 优先使用用户配置的地址，如果没有则使用默认值
     */
    fun getApiBaseUrl(): String {
        return prefs.getString(KEY_API_BASE_URL, null) ?: BuildConfig.AUTH_SERVER_URL
    }

    /**
     * 设置 API 基础地址
     */
    fun setApiBaseUrl(url: String) {
        prefs.edit().putString(KEY_API_BASE_URL, url).apply()
    }

    /**
     * 获取认证服务器地址
     * 优先使用用户配置的地址，如果没有则使用默认值
     */
    fun getAuthServerUrl(): String {
        return prefs.getString(KEY_API_BASE_URL, null) ?: BuildConfig.AUTH_SERVER_URL
    }

    /**
     * 设置认证服务器地址
     */
    fun setAuthServerUrl(url: String) {
        prefs.edit().putString(KEY_API_BASE_URL, url).apply()
    }

    /**
     * 获取 Gotify 服务器地址
     * 优先使用用户配置的地址，如果没有则使用默认值
     */
    fun getGotifyServerUrl(): String {
        return prefs.getString(KEY_GOTIFY_SERVER_URL, null) ?: BuildConfig.GOTIFY_SERVER_URL
    }

    /**
     * 设置 Gotify 服务器地址
     */
    fun setGotifyServerUrl(url: String) {
        prefs.edit().putString(KEY_GOTIFY_SERVER_URL, url).apply()
    }

    /**
     * 重置为默认配置
     */
    fun resetToDefault() {
        prefs.edit()
            .remove(KEY_API_BASE_URL)
            .remove(KEY_GOTIFY_SERVER_URL)
            .apply()
    }
}
