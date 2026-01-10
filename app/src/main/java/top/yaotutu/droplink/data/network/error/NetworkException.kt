package top.yaotutu.droplink.data.network.error

/**
 * 网络层自定义异常类型
 *
 * React 对标：
 * - 类似自定义的 ApiError 类或 Error 子类
 * - 用于统一处理不同类型的网络错误
 *
 * 设计原理：
 * - 使用 sealed class 确保类型安全和穷举性检查
 * - 类似 TypeScript 的 discriminated union types
 * - 所有网络异常都继承自 Exception，可以被标准 try-catch 捕获
 *
 * 异常类型分类：
 * - NetworkError: 网络连接失败（无网络、超时等）
 * - ServerError: 服务器错误（5xx）
 * - ClientError: 客户端错误（4xx）
 * - ParseError: JSON 解析失败
 * - BusinessError: 业务逻辑错误（如验证码错误）
 * - UnknownError: 未知错误
 *
 * 使用示例：
 * ```kotlin
 * try {
 *     val response = apiService.login(email, code)
 * } catch (e: NetworkException.ClientError) {
 *     if (e.code == 400) {
 *         showError("验证码错误")
 *     }
 * } catch (e: NetworkException.NetworkError) {
 *     showError("网络连接失败")
 * }
 * ```
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * 网络连接失败
     *
     * 常见情况：
     * - 无网络连接
     * - 连接超时（ConnectTimeout）
     * - 读取超时（ReadTimeout）
     * - DNS 解析失败（UnknownHostException）
     *
     * React 对标：
     * - axios 的 ECONNABORTED、ETIMEDOUT、ENETUNREACH
     */
    class NetworkError(
        message: String = "网络连接失败，请检查网络设置",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 服务器错误（5xx）
     *
     * 常见情况：
     * - 500 Internal Server Error
     * - 502 Bad Gateway
     * - 503 Service Unavailable
     * - 504 Gateway Timeout
     *
     * React 对标：
     * - HTTP 5xx status codes
     */
    class ServerError(
        message: String = "服务器异常，请稍后重试",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 客户端错误（4xx）
     *
     * 常见情况：
     * - 400 Bad Request（参数错误）
     * - 401 Unauthorized（未授权）
     * - 403 Forbidden（禁止访问）
     * - 404 Not Found（资源不存在）
     * - 429 Too Many Requests（请求过于频繁）
     *
     * React 对标：
     * - HTTP 4xx status codes
     *
     * @param code HTTP 状态码
     */
    class ClientError(
        val code: Int,
        message: String,
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * JSON 解析错误
     *
     * 常见情况：
     * - 响应格式不符合预期
     * - JSON 格式错误
     * - 字段类型不匹配
     *
     * React 对标：
     * - JSON.parse() 抛出的 SyntaxError
     * - Axios 的 response 解析失败
     */
    class ParseError(
        message: String = "数据解析失败",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 业务逻辑错误
     *
     * 常见情况：
     * - 验证码错误
     * - 账号被禁用
     * - 权限不足
     *
     * 与 ClientError 的区别：
     * - ClientError 是 HTTP 层面的错误（状态码）
     * - BusinessError 是业务层面的错误（业务逻辑）
     */
    class BusinessError(
        message: String,
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 未知错误
     *
     * 用于处理无法分类的异常
     */
    class UnknownError(
        message: String = "未知错误",
        cause: Throwable? = null
    ) : NetworkException(message, cause)
}
