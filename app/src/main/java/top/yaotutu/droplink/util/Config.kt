package top.yaotutu.droplink.util

import android.content.Context
import top.yaotutu.droplink.BuildConfig
import top.yaotutu.droplink.data.settings.AppSettings

/**
 * 应用配置类
 * 集中管理所有配置项
 */
object Config {

    private lateinit var appSettings: AppSettings

    /**
     * 初始化配置（在 Application.onCreate 中调用）
     */
    fun init(context: Context) {
        appSettings = AppSettings.getInstance(context)
    }

    /**
     * 获取 API 基础地址
     * 优先使用用户配置的地址，如果没有则使用默认值
     */
    fun getApiBaseUrl(): String {
        return appSettings.getApiBaseUrl()
    }

    /**
     * 获取默认认证服务器地址（编译时固定）
     */
    val DEFAULT_AUTH_SERVER_URL = BuildConfig.AUTH_SERVER_URL

    /**
     * 获取默认 Gotify 服务器地址（编译时固定）
     */
    val DEFAULT_GOTIFY_SERVER_URL = BuildConfig.GOTIFY_SERVER_URL

    /**
     * 获取默认 API 基础地址（保留旧常量，向后兼容）
     */
    val DEFAULT_API_BASE_URL = BuildConfig.AUTH_SERVER_URL

    // API 密钥（编译时固定）
    val API_KEY = BuildConfig.API_KEY

    // 调试模式（debug 版本为 true，release 版本为 false）
    val DEBUG = BuildConfig.DEBUG
}
