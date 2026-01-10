package top.yaotutu.droplink.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 认证成功响应数据
 *
 * React 对标：
 * - 类似 interface AuthResponse { user: UserData; isNewUser: boolean }
 * - axios 响应的 response.data.data
 *
 * 重要说明：
 * - 此结构已根据实际服务器返回调整（2025-11-27）
 * - 服务器返回的是嵌套结构，需要多层解析
 *
 * 服务器实际返回的 JSON 结构：
 * ```json
 * {
 *   "status": "success",
 *   "data": {
 *     "user": {
 *       "email": "user@example.com",
 *       "gotifyUsername": "droplink_xxx",
 *       "tokens": {
 *         "appToken": "A1B2C3D4E5F6",
 *         "clientToken": "X7Y8Z9A0B1C2"
 *       }
 *     },
 *     "isNewUser": false
 *   }
 * }
 * ```
 */

/**
 * Gotify Token 信息
 */
@Serializable
data class Tokens(
    val appToken: String,        // Gotify 应用 Token（必需，用于发送消息）
    val clientToken: String      // Gotify 客户端 Token（必需，用于接收消息）
)

/**
 * 用户信息
 */
@Serializable
data class UserData(
    val email: String,                    // 用户邮箱（必需）
    val gotifyUsername: String? = null,   // Gotify 用户名（可选）
    val tokens: Tokens                    // Gotify Token 信息（必需）
)

/**
 * 认证响应数据（data 部分）
 */
@Serializable
data class AuthData(
    val user: UserData,           // 用户信息（必需）
    val isNewUser: Boolean        // 是否为新注册用户（必需）
)

/**
 * 完整的认证 API 响应
 * 类型别名，方便使用
 */
typealias AuthResponse = ApiResponse<AuthData>
