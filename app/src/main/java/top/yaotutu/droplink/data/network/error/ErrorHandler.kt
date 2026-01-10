package top.yaotutu.droplink.data.network.error

import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import top.yaotutu.droplink.data.network.dto.response.ErrorResponse
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 统一错误处理器
 *
 * React 对标：
 * - 类似 axios interceptor 的 error handler
 * - 或者自定义的 handleApiError(error) 函数
 *
 * 核心功能：
 * 1. 将底层异常（IOException、HttpException 等）转换为 NetworkException
 * 2. 解析服务器返回的错误响应体
 * 3. 提供用户友好的中文错误消息
 *
 * 使用场景：
 * ```kotlin
 * try {
 *     val response = apiService.login(email, code)
 * } catch (e: Exception) {
 *     val networkException = ErrorHandler.handleException(e)
 *     val errorMessage = ErrorHandler.getDisplayMessage(networkException)
 *     showError(errorMessage)
 * }
 * ```
 *
 * 异常转换流程：
 * 1. UnknownHostException → NetworkError（无法连接到服务器）
 * 2. SocketTimeoutException → NetworkError（连接超时）
 * 3. IOException → NetworkError（网络异常）
 * 4. HttpException → ClientError/ServerError（根据状态码）
 * 5. SerializationException → ParseError（JSON 解析失败）
 * 6. NetworkException → 直接返回（已经是转换后的异常）
 * 7. 其他 → UnknownError（未知错误）
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    // JSON 解析器（用于解析错误响应体）
    private val json = Json {
        ignoreUnknownKeys = true        // 忽略未知字段（服务器返回额外字段不会报错）
        coerceInputValues = true        // 强制转换输入值（类型不匹配时尝试转换）
        isLenient = true                // 宽松模式（允许非严格 JSON，如单引号）
    }

    /**
     * 处理网络请求异常
     *
     * 将各种底层异常转换为统一的 NetworkException
     *
     * @param throwable 原始异常
     * @return 转换后的 NetworkException
     */
    fun handleException(throwable: Throwable): NetworkException {
        Log.e(TAG, "处理异常: ${throwable.javaClass.simpleName}", throwable)

        return when (throwable) {
            // ==================== 网络连接异常 ====================

            // 无法连接到服务器（DNS 解析失败、服务器不可达）
            is UnknownHostException -> {
                NetworkException.NetworkError(
                    message = "无法连接到服务器，请检查网络连接",
                    cause = throwable
                )
            }

            // 超时异常（连接超时或读取超时）
            is SocketTimeoutException -> {
                NetworkException.NetworkError(
                    message = "连接超时，请检查网络状况",
                    cause = throwable
                )
            }

            // IO 异常（包括连接中断、读写失败等）
            is IOException -> {
                NetworkException.NetworkError(
                    message = "网络异常: ${throwable.message ?: "连接失败"}",
                    cause = throwable
                )
            }

            // ==================== HTTP 错误响应 ====================

            // HTTP 错误响应（4xx, 5xx）
            is HttpException -> {
                handleHttpException(throwable)
            }

            // ==================== JSON 解析异常 ====================

            // JSON 解析异常（响应格式不符合预期）
            is SerializationException -> {
                NetworkException.ParseError(
                    message = "数据格式错误",
                    cause = throwable
                )
            }

            // ==================== 已转换的异常 ====================

            // 已经是 NetworkException，直接返回
            is NetworkException -> throwable

            // ==================== 其他未知异常 ====================

            // 其他未知异常
            else -> {
                NetworkException.UnknownError(
                    message = throwable.message ?: "发生未知错误",
                    cause = throwable
                )
            }
        }
    }

    /**
     * 处理 HTTP 异常（4xx, 5xx）
     *
     * 流程：
     * 1. 获取 HTTP 状态码
     * 2. 尝试解析错误响应体（JSON）
     * 3. 根据状态码范围返回对应的异常类型
     *
     * @param exception HTTP 异常
     * @return 转换后的 NetworkException
     */
    private fun handleHttpException(exception: HttpException): NetworkException {
        val code = exception.code()
        val errorBody = exception.response()?.errorBody()?.string()

        // 尝试解析错误响应体
        val errorMessage = try {
            if (!errorBody.isNullOrEmpty()) {
                // 解析 JSON 错误响应
                val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                errorResponse.getDisplayMessage()
            } else {
                // 响应体为空，使用默认消息
                "服务器返回错误: HTTP $code"
            }
        } catch (e: Exception) {
            // 解析失败，记录警告并使用默认消息
            Log.w(TAG, "无法解析错误响应体", e)
            "服务器返回错误: HTTP $code"
        }

        // 根据状态码范围返回对应的异常类型
        return when (code) {
            // 400-499: 客户端错误
            // - 400 Bad Request: 请求参数错误、验证码错误等
            // - 401 Unauthorized: 未授权、Token 失效
            // - 403 Forbidden: 禁止访问、权限不足
            // - 404 Not Found: 资源不存在
            // - 429 Too Many Requests: 请求过于频繁
            in 400..499 -> {
                NetworkException.ClientError(
                    code = code,
                    message = errorMessage,
                    cause = exception
                )
            }

            // 500-599: 服务器错误
            // - 500 Internal Server Error: 服务器内部错误
            // - 502 Bad Gateway: 网关错误
            // - 503 Service Unavailable: 服务不可用
            // - 504 Gateway Timeout: 网关超时
            in 500..599 -> {
                NetworkException.ServerError(
                    message = errorMessage,
                    cause = exception
                )
            }

            // 其他 HTTP 错误码（不常见）
            else -> {
                NetworkException.UnknownError(
                    message = errorMessage,
                    cause = exception
                )
            }
        }
    }

    /**
     * 将异常转换为用户可读的错误消息
     *
     * 提供用户友好的中文错误提示
     *
     * @param exception 网络异常
     * @return 用户友好的错误消息
     */
    fun getDisplayMessage(exception: Throwable): String {
        return when (val networkException = handleException(exception)) {
            is NetworkException.NetworkError -> networkException.message ?: "网络错误"
            is NetworkException.ServerError -> networkException.message ?: "服务器错误"
            is NetworkException.ClientError -> networkException.message ?: "请求错误"
            is NetworkException.ParseError -> "数据解析失败，请联系技术支持"
            is NetworkException.BusinessError -> networkException.message ?: "操作失败"
            is NetworkException.UnknownError -> networkException.message ?: "未知错误"
        }
    }
}
