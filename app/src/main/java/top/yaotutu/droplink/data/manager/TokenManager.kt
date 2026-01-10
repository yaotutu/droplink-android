package top.yaotutu.droplink.data.manager

import android.content.Context
import android.content.SharedPreferences

/**
 * Token 管理器
 *
 * React 对标：
 * - 类似 localStorage 的 token 管理工具函数
 * - getToken(), setToken(), clearToken()
 *
 * 职责：
 * - 专门管理 Gotify 相关的 Token
 * - appToken: 用于发送消息到 Gotify 服务
 * - clientToken: 用于接收 Gotify 消息（可选）
 *
 * 与 SessionManager 的区别：
 * - SessionManager: 管理用户会话信息（User 对象）
 * - TokenManager: 专门管理 Token（appToken, clientToken）
 *
 * 存储方式：
 * - 使用 SharedPreferences 明文存储（按照用户要求）
 * - 生产环境建议使用 EncryptedSharedPreferences 加密存储
 *
 * 使用示例：
 * ```kotlin
 * val tokenManager = TokenManager.getInstance(context)
 *
 * // 保存 Token
 * tokenManager.saveTokens(
 *     appToken = "A1B2C3D4E5F6",
 *     clientToken = "X7Y8Z9A0B1C2"
 * )
 *
 * // 获取 Token
 * val appToken = tokenManager.getAppToken()
 *
 * // 检查是否有有效 Token
 * if (tokenManager.hasValidToken()) {
 *     // 已登录
 * }
 * ```
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "droplink_tokens"
        private const val KEY_APP_TOKEN = "app_token"
        private const val KEY_CLIENT_TOKEN = "client_token"

        @Volatile
        private var instance: TokenManager? = null

        /**
         * 获取单例实例
         *
         * 使用双重检查锁定（Double-Checked Locking）确保线程安全
         *
         * @param context 上下文（会自动使用 ApplicationContext）
         * @return TokenManager 单例实例
         */
        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * 保存 App Token（用于发送消息）
     *
     * @param token Gotify 应用 Token
     */
    fun saveAppToken(token: String) {
        prefs.edit().putString(KEY_APP_TOKEN, token).apply()
    }

    /**
     * 获取 App Token
     *
     * @return Gotify 应用 Token，如果不存在则返回 null
     */
    fun getAppToken(): String? {
        return prefs.getString(KEY_APP_TOKEN, null)
    }

    /**
     * 保存 Client Token（用于接收消息）
     *
     * @param token Gotify 客户端 Token
     */
    fun saveClientToken(token: String) {
        prefs.edit().putString(KEY_CLIENT_TOKEN, token).apply()
    }

    /**
     * 获取 Client Token
     *
     * @return Gotify 客户端 Token，如果不存在则返回 null
     */
    fun getClientToken(): String? {
        return prefs.getString(KEY_CLIENT_TOKEN, null)
    }

    /**
     * 批量保存所有 Token
     *
     * @param appToken Gotify 应用 Token（必需）
     * @param clientToken Gotify 客户端 Token（可选）
     */
    fun saveTokens(appToken: String, clientToken: String? = null) {
        prefs.edit().apply {
            putString(KEY_APP_TOKEN, appToken)
            clientToken?.let { putString(KEY_CLIENT_TOKEN, it) }
            apply()
        }
    }

    /**
     * 清除所有 Token
     *
     * 用于用户退出登录时
     */
    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    /**
     * 检查是否有有效的 Token
     *
     * @return true 如果 appToken 存在且不为空，否则返回 false
     */
    fun hasValidToken(): Boolean {
        return !getAppToken().isNullOrEmpty()
    }
}
