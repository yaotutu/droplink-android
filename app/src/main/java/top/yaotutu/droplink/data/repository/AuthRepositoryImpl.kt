package top.yaotutu.droplink.data.repository

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import top.yaotutu.droplink.data.manager.SessionManager
import top.yaotutu.droplink.data.manager.TokenManager
import top.yaotutu.droplink.data.model.QrCodeValidationResult
import top.yaotutu.droplink.data.model.QrLoginData
import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.data.model.ValidationResult
import top.yaotutu.droplink.data.network.RetrofitClient
import top.yaotutu.droplink.data.network.dto.request.SendCodeRequest
import top.yaotutu.droplink.data.network.dto.request.VerifyRequest
import top.yaotutu.droplink.data.network.error.ErrorHandler
import top.yaotutu.droplink.data.settings.AppSettings

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
            code.length != 6 -> ValidationResult(false, "验证码必须是6位")
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

    // === 自建服务器模式方法实现 ===

    override fun validateGotifyServerUrl(url: String): ValidationResult {
        val trimmedUrl = url.trim()

        return when {
            trimmedUrl.isEmpty() -> ValidationResult(false, "服务器地址不能为空")
            !isValidUrlFormat(trimmedUrl) -> ValidationResult(false, "服务器地址格式不正确，需以 http:// 或 https:// 开头")
            else -> ValidationResult(true)
        }
    }

    /**
     * 验证 URL 格式（支持 http 和 https）
     *
     * React 对标：
     * - const isValidUrl = (url) => /^https?:\/\/[\w\-.]+(?::\d+)?(?:\/.*)?$/.test(url)
     */
    private fun isValidUrlFormat(url: String): Boolean {
        val urlRegex = Regex("^https?://[\\w\\-.]+(?::\\d+)?(?:/.*)?$")
        return urlRegex.matches(url)
    }

    override fun validateToken(token: String): ValidationResult {
        return when {
            token.isEmpty() -> ValidationResult(false, "Token 不能为空")
            token.length < 8 -> ValidationResult(false, "Token 格式不正确（至少8位）")
            else -> ValidationResult(true)
        }
    }

    override suspend fun verifySelfHostedTokens(
        gotifyServerUrl: String,
        appToken: String,
        clientToken: String
    ): Result<User> {
        return try {
            // 1. 获取 AppSettings 实例并保存原服务器地址（用于回滚）
            val appSettings = AppSettings.getInstance(context)
            val originalGotifyUrl = appSettings.getGotifyServerUrl()

            Log.d(TAG, "开始验证自建服务器: $gotifyServerUrl")

            // 2. 临时设置 Gotify 服务器地址（不持久化，验证通过后再保存）
            appSettings.setGotifyServerUrl(gotifyServerUrl)
            RetrofitClient.resetInstance()

            // 3. 创建 Gotify API 服务
            val gotifyApiService = RetrofitClient.getInstance(context).getGotifyApiService()

            try {
                // 4. 调用 GET /current/user?token={clientToken} 验证 clientToken
                val userResponse = gotifyApiService.getCurrentUser(clientToken)

                // 5. 验证成功，创建 User 对象
                val user = User(
                    id = userResponse.name ?: "self-hosted-user",
                    email = "self-hosted@local",  // 默认邮箱（自建服务器模式）
                    username = userResponse.name ?: "Self-Hosted User",
                    token = appToken  // 保存 appToken（用于发送消息）
                )

                // 6. 持久化保存配置
                appSettings.setGotifyServerUrl(gotifyServerUrl)
                tokenManager.saveTokens(
                    appToken = appToken,
                    clientToken = clientToken
                )
                sessionManager.saveUserSession(user)

                Log.d(TAG, "自建服务器验证成功: ${user.username}")
                Result.success(user)

            } catch (e: Exception) {
                // 验证失败，回滚服务器地址
                appSettings.setGotifyServerUrl(originalGotifyUrl)
                RetrofitClient.resetInstance()
                throw e
            }

        } catch (e: Exception) {
            val networkException = ErrorHandler.handleException(e)

            // 根据异常类型提供友好的错误信息
            val errorMessage = when {
                networkException is java.net.UnknownHostException -> "无法连接到服务器，请检查地址是否正确"
                networkException is javax.net.ssl.SSLException -> "SSL 证书验证失败"
                networkException is java.net.SocketTimeoutException -> "连接超时，请检查网络或服务器状态"
                networkException.message?.contains("401") == true -> "Token 验证失败，请检查 clientToken 是否正确"
                networkException.message?.contains("404") == true -> "服务器接口不存在，请确认 Gotify 服务器版本"
                else -> "验证失败: ${ErrorHandler.getDisplayMessage(networkException)}"
            }

            Log.e(TAG, "自建服务器验证失败: $errorMessage", networkException)
            Result.failure(Exception(errorMessage, networkException))
        }
    }

    // === 二维码登录模式方法实现 ===

    override fun validateQrCodeData(qrCodeContent: String): QrCodeValidationResult {
        return try {
            // 1. 解析 JSON
            val json = Json { ignoreUnknownKeys = true }
            val qrData = json.decodeFromString<QrLoginData>(qrCodeContent)

            // 2. 验证类型
            if (qrData.type != "droplink_qr_login") {
                return QrCodeValidationResult.Error("无效的二维码类型")
            }

            // 3. 验证时间戳（5 分钟有效期）
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - qrData.timestamp
            if (timeDiff > 5 * 60 * 1000L) {
                return QrCodeValidationResult.Error("二维码已过期，请重新生成")
            }
            if (timeDiff < 0) {
                return QrCodeValidationResult.Error("二维码时间戳无效")
            }

            // 4. 验证必需字段
            if (qrData.data.gotifyServerUrl.isEmpty() ||
                qrData.data.appToken.isEmpty() ||
                qrData.data.clientToken.isEmpty()) {
                return QrCodeValidationResult.Error("二维码数据不完整")
            }

            Log.d(TAG, "二维码验证成功: ${qrData.data.gotifyServerUrl}")
            QrCodeValidationResult.Success(qrData)

        } catch (e: SerializationException) {
            Log.e(TAG, "二维码格式错误", e)
            QrCodeValidationResult.Error("二维码格式错误")
        } catch (e: Exception) {
            Log.e(TAG, "解析二维码失败", e)
            QrCodeValidationResult.Error("解析二维码失败: ${e.message}")
        }
    }

    override suspend fun loginWithQrCode(qrLoginData: QrLoginData): Result<User> {
        Log.d(TAG, "开始二维码登录: ${qrLoginData.data.gotifyServerUrl}")

        // 直接复用 verifySelfHostedTokens() 的逻辑
        return verifySelfHostedTokens(
            gotifyServerUrl = qrLoginData.data.gotifyServerUrl,
            appToken = qrLoginData.data.appToken,
            clientToken = qrLoginData.data.clientToken
        )
    }
}
