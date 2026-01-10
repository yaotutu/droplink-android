package top.yaotutu.droplink.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 发送验证码请求体
 *
 * React 对标：
 * - 类似 TypeScript 中的 interface SendCodePayload { email: string }
 * - 用于 axios.post('/api/auth/send-code', payload) 的 payload
 *
 * 用途：
 * - 向指定邮箱发送验证码
 * - 测试模式下验证码固定为 0000
 *
 * @param email 用户邮箱地址
 */
@Serializable
data class SendCodeRequest(
    val email: String
)
