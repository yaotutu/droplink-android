package top.yaotutu.droplink.data.model

import kotlinx.serialization.Serializable

/**
 * 二维码登录数据模型
 *
 * React 对标：
 * - interface QrLoginData { ... }
 *
 * 用途：
 * - 解析二维码 JSON 数据
 * - 验证数据完整性
 * - 传递给 AuthRepository 进行验证
 *
 * 数据格式示例：
 * ```json
 * {
 *   "version": "1.0",
 *   "type": "droplink_qr_login",
 *   "timestamp": 1737532800000,
 *   "data": {
 *     "gotifyServerUrl": "http://192.168.1.100:8080",
 *     "appToken": "A1B2C3D4E5F6G7H8",
 *     "clientToken": "X9Y8Z7W6V5U4T3S2",
 *     "serverName": "我的 Droplink 服务器"
 *   }
 * }
 * ```
 */
@Serializable
data class QrLoginData(
    /**
     * 数据格式版本（当前 "1.0"）
     * 用于向后兼容，未来可能支持 "2.0" 等新版本
     */
    val version: String,

    /**
     * 二维码类型标识（固定值 "droplink_qr_login"）
     * 用于识别是否为 Droplink 二维码，防止扫描到其他应用的二维码
     */
    val type: String,

    /**
     * 二维码生成时间戳（毫秒）
     * 用于防止重放攻击，二维码有效期为 5 分钟
     */
    val timestamp: Long,

    /**
     * 二维码数据负载
     * 包含服务器地址和 Token 信息
     */
    val data: QrLoginPayload
)

/**
 * 二维码数据负载
 *
 * 包含登录所需的核心数据：
 * - Gotify 服务器地址
 * - appToken（发送消息）
 * - clientToken（接收消息）
 * - serverName（可选，用于日志记录）
 */
@Serializable
data class QrLoginPayload(
    /**
     * Gotify 服务器地址（http:// 或 https://）
     * 示例："http://192.168.1.100:8080"
     */
    val gotifyServerUrl: String,

    /**
     * 应用 Token（用于发送消息到 Gotify）
     * 示例："A1B2C3D4E5F6G7H8"
     */
    val appToken: String,

    /**
     * 客户端 Token（用于接收 Gotify 消息）
     * 示例："X9Y8Z7W6V5U4T3S2"
     */
    val clientToken: String,

    /**
     * 服务器友好名称（可选）
     * 用于日志记录，方便用户识别不同的服务器
     * 示例："我的 Droplink 服务器"
     */
    val serverName: String? = null
)

/**
 * 二维码验证结果
 *
 * React 对标：
 * - type QrCodeValidationResult = { success: true, data: QrLoginData } | { success: false, message: string }
 *
 * 使用密封类（Sealed Class）实现类型安全的结果处理
 */
sealed class QrCodeValidationResult {
    /**
     * 验证成功
     * @param data 已验证的二维码数据
     */
    data class Success(val data: QrLoginData) : QrCodeValidationResult()

    /**
     * 验证失败
     * @param message 错误信息（用户友好的中文提示）
     */
    data class Error(val message: String) : QrCodeValidationResult()
}
