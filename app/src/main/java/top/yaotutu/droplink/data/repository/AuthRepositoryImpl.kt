package top.yaotutu.droplink.data.repository

import android.content.Context
import android.util.Log
import top.yaotutu.droplink.data.manager.SessionManager
import top.yaotutu.droplink.data.manager.TokenManager
import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.data.model.ValidationResult
import top.yaotutu.droplink.data.network.RetrofitClient
import top.yaotutu.droplink.data.network.dto.request.SendCodeRequest
import top.yaotutu.droplink.data.network.dto.request.VerifyRequest
import top.yaotutu.droplink.data.network.error.ErrorHandler

/**
 * 认证仓库实现类
 *
 * React 对标：
 * - 类似 Custom Hook 或 API Service 的实现
 * - 内部调用 axios 实例发送请求
 */
class AuthRepositoryImpl(
    private val context: Context
) : AuthRepository {

    private val sessionManager = SessionManager.getInstance(context)
    private val tokenManager = TokenManager.getInstance(context)
    private val retrofitClient = RetrofitClient.getInstance(context)
    private val authApiService = retrofitClient.getAuthApiService()

    companion object {
        private const val TAG = "AuthRepository"
    }

    override fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()

        return when {
            trimmedEmail.isEmpty() -> ValidationResult(false, "邮箱不能为空")
            !isValidEmailFormat(trimmedEmail) -> ValidationResult(false, "邮箱格式不正确")
            else -> ValidationResult(true)
        }
    }

    private fun isValidEmailFormat(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    override fun validateVerificationCode(code: String): ValidationResult {
        return when {
            code.isEmpty() -> ValidationResult(false, "验证码不能为空")
            code.length != 4 -> ValidationResult(false, "验证码必须是4位")
            !code.all { it.isDigit() } -> ValidationResult(false, "验证码必须全部为数字")
            else -> ValidationResult(true)
        }
    }

    override suspend fun sendVerificationCode(email: String): Result<Unit> {
        return try {
            authApiService.sendVerificationCode(SendCodeRequest(email))
            Log.d(TAG, "验证码发送成功: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            val networkException = ErrorHandler.handleException(e)
            val errorMessage = ErrorHandler.getDisplayMessage(networkException)
            Log.e(TAG, "发送验证码失败: $errorMessage", networkException)
            Result.failure(Exception(errorMessage, networkException))
        }
    }

    override suspend fun verify(email: String, code: String): Result<User> {
        return try {
            val response = authApiService.verify(VerifyRequest(email, code))

            // 从嵌套的响应结构中提取数据
            val userData = response.data.user
            val tokens = userData.tokens

            // 转换为 User 模型
            val user = User(
                id = email,  // 使用 email 作为 userId
                email = userData.email,
                username = userData.gotifyUsername ?: email.split("@")[0],
                token = tokens.appToken
            )

            // 保存 Token
            tokenManager.saveTokens(
                appToken = tokens.appToken,
                clientToken = tokens.clientToken
            )

            // 保存用户会话
            sessionManager.saveUserSession(user)

            Log.d(TAG, "验证成功: ${user.email}, isNewUser: ${response.data.isNewUser}")
            Result.success(user)

        } catch (e: Exception) {
            val networkException = ErrorHandler.handleException(e)
            val errorMessage = ErrorHandler.getDisplayMessage(networkException)
            Log.e(TAG, "验证失败: $errorMessage", networkException)
            Result.failure(Exception(errorMessage, networkException))
        }
    }
}
