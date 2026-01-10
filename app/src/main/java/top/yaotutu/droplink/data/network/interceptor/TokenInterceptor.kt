package top.yaotutu.droplink.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import top.yaotutu.droplink.data.manager.TokenManager

// Token 自动注入拦截器
// 功能：自动在请求头中添加 Token（如果需要）
// 认证接口不需要 Token，自动跳过
// Gotify API 使用 X-Gotify-Key header
class TokenInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 获取当前用户的 Token
        val appToken = tokenManager.getAppToken()

        // 如果没有 Token 或者是认证接口（不需要 Token），直接放行
        if (appToken == null || isAuthEndpoint(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        // 添加 Authorization Header（Gotify 使用 X-Gotify-Key）
        val newRequest = originalRequest.newBuilder()
            .addHeader("X-Gotify-Key", appToken)
            .build()

        return chain.proceed(newRequest)
    }

    // 判断是否为认证接口（不需要 Token）
    private fun isAuthEndpoint(path: String): Boolean {
        return path.startsWith("/api/auth/")
    }
}
