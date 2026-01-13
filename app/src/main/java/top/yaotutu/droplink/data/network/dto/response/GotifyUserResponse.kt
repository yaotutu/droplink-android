package top.yaotutu.droplink.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * Gotify 用户信息响应
 * 对应 GET /current/user 接口返回
 *
 * React 对标：
 * - 类似于 TypeScript 的 interface GotifyUserResponse {...}
 * - 用于验证 clientToken 的有效性
 *
 * API 端点：GET /current/user?token={clientToken}
 *
 * 成功响应示例：
 * ```json
 * {
 *   "id": 1,
 *   "name": "admin",
 *   "admin": true
 * }
 * ```
 *
 * 失败响应示例 (401):
 * ```json
 * {
 *   "error": "Unauthorized",
 *   "errorCode": 401,
 *   "errorDescription": "invalid token"
 * }
 * ```
 *
 * 使用场景：
 * - AuthRepositoryImpl.verifySelfHostedTokens() 调用此接口验证 tokens
 * - 验证成功后，使用 name 字段作为用户名
 */
@Serializable
data class GotifyUserResponse(
    /**
     * Gotify 用户 ID
     */
    val id: Int? = null,

    /**
     * Gotify 用户名
     * 用于创建 User 对象的 username 字段
     */
    val name: String? = null,

    /**
     * 是否为管理员
     */
    val admin: Boolean? = false
)
