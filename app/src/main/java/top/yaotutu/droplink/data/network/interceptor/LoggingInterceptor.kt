package top.yaotutu.droplink.data.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.StandardCharsets

/**
 * 增强型日志拦截器
 *
 * React 对标：
 * - 类似 axios interceptor 中的 console.log
 * - 或者 fetch 的 request/response logging
 *
 * 功能：
 * - 在 Debug 模式下打印详细的请求和响应日志
 * - 自动格式化 JSON 和其他内容
 * - 显示请求耗时
 *
 * 日志示例：
 * ```
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 请求: POST http://111.228.1.24:3600/api/auth/verify
 * Headers: Content-Type: application/json
 * Body: {"email":"test@example.com","code":"0000"}
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * ✅ 响应: 200 (耗时: 1234ms)
 * URL: http://111.228.1.24:3600/api/auth/verify
 * Response Body: {"email":"test@example.com","appToken":"xxx"}
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * ```
 */
class LoggingInterceptor(
    private val isDebug: Boolean = true
) : Interceptor {

    companion object {
        private const val TAG = "HTTP"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 非 Debug 模式不打印日志
        if (!isDebug) {
            return chain.proceed(request)
        }

        // ==================== 打印请求信息 ====================
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🚀 请求: ${request.method} ${request.url}")
        Log.d(TAG, "Headers: ${request.headers}")

        // 打印请求体（仅支持文本类型）
        request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
            Log.d(TAG, "Body: ${buffer.readString(charset)}")
        }

        // ==================== 执行请求 ====================
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime

        // ==================== 打印响应信息 ====================
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "✅ 响应: ${response.code} (耗时: ${duration}ms)")
        Log.d(TAG, "URL: ${response.request.url}")

        // 打印响应体（需要复制一份，避免消费原始流）
        response.body?.let { body ->
            val source = body.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            val charset = body.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            if (body.contentLength() != 0L) {
                val responseBody = buffer.clone().readString(charset)
                Log.d(TAG, "Response Body: $responseBody")
            }
        }

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        return response
    }
}
