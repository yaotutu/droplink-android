package top.yaotutu.droplink.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 通用 API 响应包装类
 *
 * React 对标：
 * - 类似 Axios 的响应拦截器中的 response.data 包装
 * - 标准化 API 响应格式
 *
 * 服务器返回的所有响应都遵循这个格式：
 * ```json
 * {
 *   "status": "success",
 *   "data": { ... }
 * }
 * ```
 *
 * @param T 数据类型
 * @param status 状态（success/error）
 * @param data 实际数据
 */
@Serializable
data class ApiResponse<T>(
    val status: String,
    val data: T
)
