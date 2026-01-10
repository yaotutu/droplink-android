package top.yaotutu.droplink.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 错误响应数据
 *
 * React 对标：
 * - 类似 axios 的 error.response.data
 * - 在 catch 块中解析的错误信息
 *
 * 用途：
 * - 解析服务器返回的错误信息（4xx, 5xx）
 * - 提供用户友好的错误提示
 *
 * 可能的错误响应格式：
 * - 格式 1: { "error": "验证码错误", "code": 400 }
 * - 格式 2: { "message": "邮箱不存在", "code": 404 }
 * - 格式 3: { "details": "服务器异常" }
 *
 * 注意事项：
 * - 所有字段都是可选的，以兼容不同的错误响应格式
 * - 使用 getDisplayMessage() 方法获取最终的错误提示
 *
 * @param error 错误描述（字段名可能为 error）
 * @param message 错误消息（字段名可能为 message）
 * @param code 错误码（HTTP 状态码或业务错误码）
 * @param details 详细错误信息
 */
@Serializable
data class ErrorResponse(
    val error: String? = null,              // 错误描述
    val message: String? = null,            // 错误消息（兼容不同字段名）
    val code: Int? = null,                  // 错误码
    val details: String? = null             // 详细错误信息
) {
    /**
     * 获取可显示的错误消息
     *
     * 优先级：error > message > details > 默认提示
     *
     * @return 用户友好的错误消息
     */
    fun getDisplayMessage(): String {
        return error ?: message ?: details ?: "未知错误"
    }
}
