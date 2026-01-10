package top.yaotutu.droplink.data.network.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import top.yaotutu.droplink.data.network.dto.request.GotifyMessageRequest
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
}
