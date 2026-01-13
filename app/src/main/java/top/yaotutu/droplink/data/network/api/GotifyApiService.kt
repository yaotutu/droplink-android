package top.yaotutu.droplink.data.network.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import top.yaotutu.droplink.data.network.dto.request.GotifyMessageRequest
import top.yaotutu.droplink.data.network.dto.response.GotifyGetMessagesResponse
import top.yaotutu.droplink.data.network.dto.response.GotifyMessageResponse

/**
 * Gotify API 服务接口
 *
 * React 概念对标：
 * - 类似于定义 axios 请求函数
 * - const sendMessage = (token, data) => axios.post('/message', data, { params: { token } })
 *
 * 核心功能：
 * - 发送消息到 Gotify 服务器
 * - 支持通知推送到其他设备
 *
 * API 文档：https://gotify.net/docs/msgextras
 */
interface GotifyApiService {

    /**
     * 发送消息到 Gotify
     *
     * POST /message?token={appToken}
     *
     * @param token Gotify 应用 Token（用于认证）
     * @param message 消息内容
     * @return 发送成功后的消息信息
     *
     * 使用示例：
     * ```kotlin
     * val request = GotifyMessageRequest(
     *     title = "网页分享",
     *     message = "https://github.com",
     *     priority = 5,
     *     extras = GotifyExtras(
     *         droplink = DroplinkData(
     *             action = "openTab",
     *             url = "https://github.com",
     *             options = DroplinkOptions(activate = true)
     *         )
     *     )
     * )
     *
     * val response = apiService.sendMessage(
     *     token = "Ah17tk7rkgdueDR",
     *     message = request
     * )
     * ```
     */
    @POST("message")
    suspend fun sendMessage(
        @Query("token") token: String,
        @Body message: GotifyMessageRequest
    ): GotifyMessageResponse

    /**
     * 获取消息列表
     *
     * GET /message?token={clientToken}&limit=100&since=0
     *
     * @param token Client Token（用于认证，与 sendMessage 使用的 App Token 不同）
     * @param limit 返回消息数量（默认 100，范围 1-200）
     * @param since 消息 ID 阈值，返回 ID 小于此值的所有消息（用于分页，null 表示首次加载）
     * @return 消息列表和分页信息
     *
     * React 概念对标：
     * - const getMessages = (token, limit, since) => axios.get('/message', { params: { token, limit, since } })
     *
     * 分页机制：
     * - 首次加载：since = null，返回最新的 limit 条消息
     * - 加载更多：since = lastMessageId，返回更早的 limit 条消息
     * - 判断是否有更多：response.paging.next != null
     *
     * 使用示例：
     * ```kotlin
     * // 首次加载
     * val response1 = apiService.getMessages(
     *     token = "clientToken123",
     *     limit = 50
     * )
     *
     * // 加载更多
     * val lastId = response1.messages.lastOrNull()?.id
     * if (response1.paging.next != null && lastId != null) {
     *     val response2 = apiService.getMessages(
     *         token = "clientToken123",
     *         limit = 50,
     *         since = lastId
     *     )
     * }
     * ```
     */
    @GET("message")
    suspend fun getMessages(
        @Query("token") token: String,
        @Query("limit") limit: Int = 100,
        @Query("since") since: Long? = null
    ): GotifyGetMessagesResponse
}
