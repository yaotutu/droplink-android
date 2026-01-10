package top.yaotutu.droplink.data.network.api

import retrofit2.http.Body
import retrofit2.http.POST
import top.yaotutu.droplink.data.network.dto.request.SendCodeRequest
import top.yaotutu.droplink.data.network.dto.request.VerifyRequest
import top.yaotutu.droplink.data.network.dto.response.AuthResponse

/**
 * 认证 API 服务接口
 *
 * React 对标：
 * - 类似定义的 API Service 类或 axios 实例的方法
 * - 例如：authApi.sendCode({ email }), authApi.verify({ email, code })
 *
 * 使用 Retrofit 定义 HTTP 请求：
 * - @POST 注解指定 HTTP 方法和路径
 * - @Body 注解将对象序列化为 JSON 请求体
 * - suspend 关键字表示这是一个协程函数（类似 async/await）
 *
 * 注意事项：
 * - 所有网络请求都必须在协程中调用（使用 viewModelScope.launch）
 * - Retrofit 会自动处理 JSON 序列化和反序列化
 * - 异常会被抛出，需要在调用处 try-catch 处理
 *
 * 使用示例：
 * ```kotlin
 * viewModelScope.launch {
 *     try {
 *         val response = authApiService.verify(VerifyRequest(email, code))
 *         // 处理成功响应
 *     } catch (e: Exception) {
 *         // 处理错误
 *     }
 * }
 * ```
 */
interface AuthApiService {

    /**
     * 发送验证码到指定邮箱
     *
     * POST /api/auth/send-code
     * Body: { "email": "user@example.com" }
     *
     * @param request 包含邮箱的请求体
     */
    @POST("/api/auth/send-code")
    suspend fun sendVerificationCode(
        @Body request: SendCodeRequest
    )

    /**
     * 统一身份验证（注册或登录）
     *
     * POST /api/auth/verify
     * Body: { "email": "user@example.com", "code": "0000" }
     *
     * 功能说明：
     * - 后端会自动判断是注册还是登录
     * - 未注册时自动创建账号并返回 Token
     * - 已注册时直接返回 Token
     * - 响应中的 isNewUser 字段标识是否为新注册用户
     *
     * @param request 包含邮箱和验证码的请求体
     * @return 认证响应，包含 Token 和用户信息
     */
    @POST("/api/auth/verify")
    suspend fun verify(
        @Body request: VerifyRequest
    ): AuthResponse

    // ==================== 后期可扩展的接口 ====================
    // 如果需要单独的注册/登录接口，可以在这里添加：
    //
    // @POST("/api/auth/register")
    // suspend fun register(@Body request: VerifyRequest): AuthResponse
    //
    // @POST("/api/auth/login")
    // suspend fun login(@Body request: VerifyRequest): AuthResponse
}
