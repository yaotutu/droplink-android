package top.yaotutu.droplink.data.repository

import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.data.model.ValidationResult

/**
 * 认证仓库接口
 * 负责处理登录、注册等认证相关的业务逻辑
 *
 * React 对标：
 * - 类似于 React 的 Service 层或自定义 Hooks
 * - 提供统一的认证接口，支持多种登录模式
 */
interface AuthRepository {
    // === 官方服务器模式方法 ===

    /**
     * 验证邮箱格式
     */
    fun validateEmail(email: String): ValidationResult

    /**
     * 验证验证码格式
     */
    fun validateVerificationCode(code: String): ValidationResult

    /**
     * 发送验证码到指定邮箱
     * @return Result<Unit> 成功返回 Unit，失败返回异常
     */
    suspend fun sendVerificationCode(email: String): Result<Unit>

    /**
     * 使用验证码统一验证（注册/登录）
     * 后端会自动判断是注册还是登录
     * @return Result<User> 成功返回 User，失败返回异常
     */
    suspend fun verify(email: String, code: String): Result<User>

    // === 自建服务器模式方法 ===

    /**
     * 验证 Gotify 服务器地址格式
     *
     * @param url Gotify 服务器地址（例如：http://192.168.1.100:8080）
     * @return ValidationResult 验证结果
     */
    fun validateGotifyServerUrl(url: String): ValidationResult

    /**
     * 验证 Token 格式（appToken 和 clientToken 通用）
     *
     * @param token Token 字符串
     * @return ValidationResult 验证结果
     */
    fun validateToken(token: String): ValidationResult

    /**
     * 验证自建服务器的 tokens 有效性
     *
     * 调用 Gotify API GET /current/user 验证 clientToken
     * 如果验证成功，保存配置并创建 User 对象
     *
     * @param gotifyServerUrl Gotify 服务器地址
     * @param appToken 应用 Token（用于发送消息）
     * @param clientToken 客户端 Token（用于接收消息）
     * @return Result<User> 成功返回 User，失败返回异常
     *
     * React 对标：
     * - async function verifySelfHostedTokens(url, appToken, clientToken)
     */
    suspend fun verifySelfHostedTokens(
        gotifyServerUrl: String,
        appToken: String,
        clientToken: String
    ): Result<User>
}
