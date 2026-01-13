package top.yaotutu.droplink.data.network.dto.response

import kotlinx.serialization.Serializable
import top.yaotutu.droplink.data.network.dto.request.GotifyExtras

/**
 * Gotify 获取消息列表的响应
 *
 * React 概念对标：
 * - type GetMessagesResponse = { messages: MessageDetail[]; paging: PagingInfo }
 *
 * API 端点:
 * GET /message?token={clientToken}&limit=100&since=0
 *
 * 使用示例:
 * ```kotlin
 * val response = gotifyApiService.getMessages(
 *     token = "clientToken123",
 *     limit = 50,
 *     since = null  // 首次加载
 * )
 * val messages = response.messages
 * val hasMore = response.paging.next != null
 * ```
 *
 * @property messages 消息列表
 * @property paging 分页信息
 */
@Serializable
data class GotifyGetMessagesResponse(
    val messages: List<GotifyMessageDetail>,
    val paging: PagingInfo
)

/**
 * Gotify 消息详情（完整格式，包含 extras）
 *
 * React 概念对标：
 * - type MessageDetail = { id: number; appid: number; title: string; message: string; priority: number; date: string; extras?: Extras }
 *
 * 与 GotifyMessageResponse 的区别：
 * - GotifyMessageResponse: 发送消息后的简单响应（无 extras，返回消息 ID）
 * - GotifyMessageDetail: 完整的消息对象（包含 extras，用于列表展示）
 *
 * 数据来源: GET /message
 *
 * 示例数据:
 * ```json
 * {
 *   "id": 123,
 *   "appid": 1,
 *   "title": "Droplink",
 *   "message": "https://github.com/yaotutu/droplink",
 *   "priority": 5,
 *   "date": "2024-01-10T10:30:00Z",
 *   "extras": {
 *     "droplink": {
 *       "id": "msg_1736524800000_abc123",
 *       "timestamp": 1736524800000,
 *       "sender": "android",
 *       "content": { "type": "url", "value": "https://..." },
 *       "actions": [{ "type": "openTab" }],
 *       "metadata": { "tags": ["github", "tools"] }
 *     }
 *   }
 * }
 * ```
 *
 * @property id Gotify 消息 ID（用于分页，通过 since 参数）
 * @property appid Gotify 应用 ID
 * @property title 消息标题（Gotify 原生字段，Droplink 中固定为 "Droplink"）
 * @property message 消息内容预览（Gotify 原生字段，Droplink 中为 URL 或文本摘要）
 * @property priority 优先级 0-10
 * @property date 消息时间（ISO 8601 格式，如 "2024-01-10T10:30:00Z"）
 * @property extras 扩展数据（可选，包含 droplink 自定义字段）
 */
@Serializable
data class GotifyMessageDetail(
    val id: Long,
    val appid: Int,
    val title: String,
    val message: String,
    val priority: Int,
    val date: String,
    val extras: GotifyExtras? = null
)

/**
 * 分页信息
 *
 * React 概念对标：
 * - type PagingInfo = { size: number; limit: number; since?: number; next?: string }
 *
 * Gotify 分页机制:
 * - 使用 `since` 参数进行分页（基于消息 ID）
 * - `since=0` 或省略：返回最新的消息
 * - `since=123`：返回 ID 小于 123 的消息（即更早的消息）
 * - `next` 为 null 表示没有更多数据
 *
 * 使用示例:
 * ```kotlin
 * // 首次加载
 * val response1 = getMessages(limit = 50)
 * val lastId = response1.messages.lastOrNull()?.id
 *
 * // 加载更多
 * if (response1.paging.next != null && lastId != null) {
 *     val response2 = getMessages(limit = 50, since = lastId)
 * }
 * ```
 *
 * @property size 当前返回的消息数量
 * @property limit 请求的限制数量（默认 100，范围 1-200）
 * @property since 上次请求的最后一条消息 ID（null 表示首次加载）
 * @property next 下一页 URL（null 表示无更多数据）
 */
@Serializable
data class PagingInfo(
    val size: Int,
    val limit: Int,
    val since: Long? = null,
    val next: String? = null
)
