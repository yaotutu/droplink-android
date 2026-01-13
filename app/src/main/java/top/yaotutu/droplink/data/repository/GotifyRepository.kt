package top.yaotutu.droplink.data.repository

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import top.yaotutu.droplink.data.manager.TokenManager
import top.yaotutu.droplink.data.network.RetrofitClient
import top.yaotutu.droplink.data.network.dto.request.ActionData
import top.yaotutu.droplink.data.network.dto.request.ContentData
import top.yaotutu.droplink.data.network.dto.request.DroplinkData
import top.yaotutu.droplink.data.network.dto.request.GotifyExtras
import top.yaotutu.droplink.data.network.dto.request.GotifyMessageRequest
import top.yaotutu.droplink.data.network.dto.response.GotifyGetMessagesResponse
import top.yaotutu.droplink.data.network.dto.response.GotifyMessageResponse
import top.yaotutu.droplink.util.MessageIdGenerator

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
     * 发送 URL 分享消息到 Gotify（新格式）
     *
     * @param url 要分享的 URL
     * @param priority 优先级 0-10（可选，默认 5）
     * @return 发送成功的消息响应
     * @throws Exception 如果 Token 不存在或网络请求失败
     *
     * 新格式说明：
     * - title 固定为 "Droplink"
     * - sender 固定为 "android"
     * - content.type 固定为 "url"
     * - actions 固定包含一个 openTab 动作
     *
     * 使用示例：
     * ```kotlin
     * val repository = GotifyRepository(context)
     * try {
     *     val response = repository.sendUrlShare(
     *         url = "https://github.com/yaotutu/droplink"
     *     )
     *     println("消息发送成功，ID: ${response.id}")
     * } catch (e: Exception) {
     *     println("发送失败: ${e.message}")
     * }
     * ```
     */
    suspend fun sendUrlShare(
        url: String,
        priority: Int = 5
    ): GotifyMessageResponse {
        // 1. 获取 Token
        val appToken = tokenManager.getAppToken()
            ?: throw IllegalStateException("未找到 Gotify Token，请先登录")

        // 2. 生成消息 ID 和时间戳
        val messageId = MessageIdGenerator.generate()
        val timestamp = System.currentTimeMillis()

        // 3. 构建 DroplinkData 对象
        val droplinkData = DroplinkData(
            id = messageId,
            timestamp = timestamp,
            sender = "android",  // 固定发送者
            content = ContentData(
                type = "url",    // 固定内容类型
                value = url
            ),
            actions = listOf(
                ActionData(
                    type = "openTab",  // 固定动作类型
                    params = null      // 暂不需要参数
                )
            )
        )

        // 4. 转换为 JsonElement
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val droplinkJson = json.parseToJsonElement(json.encodeToString(DroplinkData.serializer(), droplinkData))

        // 5. 构建请求
        val request = GotifyMessageRequest(
            title = "Droplink",  // 固定标题
            message = url,       // 消息预览文本为 URL
            priority = priority,
            extras = GotifyExtras(
                droplink = droplinkJson
            )
        )

        // 4. 发送请求
        return gotifyApiService.sendMessage(
            token = appToken,
            message = request
        )
    }

    /**
     * 获取消息列表
     *
     * React 概念对标：
     * - const getMessages = async (limit, since) => { const response = await api.getMessages({ limit, since }); return response.data; }
     *
     * @param limit 返回消息数量（默认 50，范围 1-200）
     * @param since 消息 ID 阈值（用于分页，null 表示首次加载）
     * @return 消息列表响应（包含 messages 和 paging 信息）
     * @throws IllegalStateException 如果 Client Token 不存在
     * @throws Exception 网络请求失败
     *
     * 使用示例:
     * ```kotlin
     * // 首次加载
     * val response = repository.getMessages(limit = 50)
     * val messages = response.messages
     * val hasMore = response.paging.next != null
     *
     * // 加载更多
     * val lastId = messages.lastOrNull()?.id
     * if (hasMore && lastId != null) {
     *     val nextResponse = repository.getMessages(
     *         limit = 50,
     *         since = lastId
     *     )
     * }
     * ```
     */
    suspend fun getMessages(
        limit: Int = 50,
        since: Long? = null
    ): GotifyGetMessagesResponse {
        // 1. 获取 Client Token
        val clientToken = tokenManager.getClientToken()
            ?: throw IllegalStateException("未找到 Client Token，无法获取消息列表")

        // 2. 调用 API（确保 limit 在有效范围内）
        return gotifyApiService.getMessages(
            token = clientToken,
            limit = limit.coerceIn(1, 200),  // 限制范围 1-200
            since = since
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
