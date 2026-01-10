package top.yaotutu.droplink.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * Gotify 消息发送响应
 *
 * 成功响应示例：
 * ```json
 * {
 *   "id": 25,
 *   "appid": 5,
 *   "message": "自动打开链接",
 *   "title": "Droplink 测试",
 *   "priority": 5,
 *   "date": "2018-02-27T19:36:10.5045044+01:00"
 * }
 * ```
 *
 * @property id 消息 ID
 * @property appid 应用 ID
 * @property message 消息内容
 * @property title 消息标题
 * @property priority 优先级
 * @property date 发送时间
 */
@Serializable
data class GotifyMessageResponse(
    val id: Long,
    val appid: Int,
    val message: String,
    val title: String,
    val priority: Int,
    val date: String
)
