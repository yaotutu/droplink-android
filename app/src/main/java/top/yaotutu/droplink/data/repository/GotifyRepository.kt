package top.yaotutu.droplink.data.repository

import android.content.Context
import top.yaotutu.droplink.data.manager.TokenManager
import top.yaotutu.droplink.data.network.RetrofitClient
import top.yaotutu.droplink.data.network.dto.request.DroplinkData
import top.yaotutu.droplink.data.network.dto.request.DroplinkOptions
import top.yaotutu.droplink.data.network.dto.request.GotifyExtras
import top.yaotutu.droplink.data.network.dto.request.GotifyMessageRequest
import top.yaotutu.droplink.data.network.dto.response.GotifyMessageResponse

/**
 * Gotify Repository
 *
 * React 概念对标：
 * - 类似于 services/gotifyService.ts
 * - export const sendNotification = async (url, title) => { ... }
 *
 * 核心职责：
 * 1. 封装 Gotify API 调用逻辑
 * 2. 构建消息请求
 * 3. 管理 Token
 * 4. 统一错误处理
 *
 * MVVM 架构中的位置：
 * - ViewModel 调用 Repository
 * - Repository 调用 API Service
 * - Repository 处理数据转换和错误
 */
class GotifyRepository(context: Context) {

    private val tokenManager = TokenManager.getInstance(context)
    private val gotifyApiService = RetrofitClient.getInstance(context).getGotifyApiService()

    /**
     * 发送 URL 分享消息到 Gotify
     *
     * @param url 要分享的 URL
     * @param title 分享标题（可选，默认为 "网页分享"）
     * @param message 消息预览文本（可选，默认使用 URL）
     * @param priority 优先级 0-10（可选，默认 5）
     * @return 发送成功的消息响应
     * @throws Exception 如果 Token 不存在或网络请求失败
     *
     * 使用示例：
     * ```kotlin
     * val repository = GotifyRepository(context)
     * try {
     *     val response = repository.sendUrlShare(
     *         url = "https://github.com",
     *         title = "GitHub 主页"
     *     )
     *     println("消息发送成功，ID: ${response.id}")
     * } catch (e: Exception) {
     *     println("发送失败: ${e.message}")
     * }
     * ```
     */
    suspend fun sendUrlShare(
        url: String,
        title: String = "网页分享",
        message: String = url,
        priority: Int = 5
    ): GotifyMessageResponse {
        // 1. 获取 Token
        val appToken = tokenManager.getAppToken()
            ?: throw IllegalStateException("未找到 Gotify Token，请先登录")

        // 2. 构建请求
        val request = GotifyMessageRequest(
            title = title,
            message = message,
            priority = priority,
            extras = GotifyExtras(
                droplink = DroplinkData(
                    action = "openTab",
                    url = url,
                    options = DroplinkOptions(activate = true)
                )
            )
        )

        // 3. 发送请求
        return gotifyApiService.sendMessage(
            token = appToken,
            message = request
        )
    }

    /**
     * 检查是否有有效的 Token
     *
     * @return true 如果有有效 Token，否则 false
     */
    fun hasValidToken(): Boolean {
        return tokenManager.hasValidToken()
    }

    /**
     * 获取当前 Token（用于调试）
     *
     * @return 当前的 appToken，如果不存在返回 null
     */
    fun getCurrentToken(): String? {
        return tokenManager.getAppToken()
    }
}
