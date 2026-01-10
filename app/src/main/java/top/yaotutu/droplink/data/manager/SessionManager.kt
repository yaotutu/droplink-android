package top.yaotutu.droplink.data.manager

import android.content.Context
import android.content.SharedPreferences
import top.yaotutu.droplink.data.model.User

/**
 * 会话管理器
 * 负责管理用户登录状态和会话数据
 *
 * React 概念对标：
 * - 类似于 Context API + localStorage
 * - 提供全局的用户状态管理
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "droplink_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_TOKEN = "user_token"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * 保存用户登录信息
     */
    fun saveUserSession(user: User) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_TOKEN, user.token)
            apply()
        }
    }

    /**
     * 获取当前登录用户
     */
    fun getUser(): User? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        return User(
            id = prefs.getString(KEY_USER_ID, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            username = prefs.getString(KEY_USER_NAME, "") ?: "",
            token = prefs.getString(KEY_USER_TOKEN, "") ?: ""
        ).takeIf { it.id.isNotEmpty() }
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * 清除用户会话（退出登录）
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
