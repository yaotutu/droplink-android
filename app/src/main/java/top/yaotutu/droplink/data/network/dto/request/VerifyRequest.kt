package top.yaotutu.droplink.data.network.dto.request

import kotlinx.serialization.Serializable

/**
 * 统一验证请求体（注册或登录）
 *
 * React 对标：
 * - 类似 interface VerifyPayload { email: string; code: string }
 *
 * 用途：
 * - 调用 /api/auth/verify 接口
 * - 后端会自动判断是注册还是登录
 * - 未注册时自动创建账号，已注册时直接返回 Token
 *
 * @param email 用户邮箱地址
 * @param code 验证码（测试模式下为 0000，生产环境为 6 位随机数）
 */
@Serializable
data class VerifyRequest(
    val email: String,
    val code: String
)
