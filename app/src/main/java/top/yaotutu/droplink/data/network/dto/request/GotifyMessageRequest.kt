package top.yaotutu.droplink.data.network.dto.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Gotify 消息发送请求（新格式）
 *
 * API 文档示例：
 * ```
 * POST /message?token={appToken}
 * Content-Type: application/json
 *
 * {
 *   "title": "Droplink",
 *   "message": "https://github.com/yaotutu/droplink",
 *   "priority": 5,
 *   "extras": {
 *     "droplink": {
 *       "id": "msg_1736524800000_abc123",
 *       "timestamp": 1736524800000,
 *       "sender": "android",
 *       "content": {
 *         "type": "url",
 *         "value": "https://github.com/yaotutu/droplink"
 *       },
 *       "actions": [
 *         {
 *           "type": "openTab"
 *         }
 *       ]
 *     }
 *   }
 * }
 * ```
 *
 * @property title 消息标题（固定为 "Droplink"）
 * @property message 消息内容预览（URL 地址）
 * @property priority 优先级 0-10（默认 5）
 * @property extras 扩展数据（包含 droplink 自定义字段）
 */
@Serializable
data class GotifyMessageRequest(
    val title: String,
    val message: String,
    @EncodeDefault val priority: Int = 5,
    val extras: GotifyExtras
)

/**
 * Gotify 扩展数据
 *
 * @property droplink Droplink 应用自定义数据
 */
@Serializable
data class GotifyExtras(
    val droplink: DroplinkData
)

/**
 * Droplink 自定义数据（新格式）
 *
 * React 概念对标：
 * - interface DroplinkData { id: string; timestamp: number; sender: string; content: Content; actions: Action[] }
 *
 * @property id 消息唯一标识（格式：msg_timestamp_randomString）
 * @property timestamp 消息时间戳（毫秒）
 * @property sender 发送者标识（固定为 "android"）
 * @property content 消息内容
 * @property actions 动作列表
 */
@Serializable
data class DroplinkData(
    val id: String,
    val timestamp: Long,
    val sender: String,
    val content: ContentData,
    val actions: List<ActionData>
)

/**
 * 消息内容数据
 *
 * React 概念对标：
 * - type Content = { type: 'url' | 'text' | 'file', value: string }
 *
 * @property type 内容类型（当前固定为 "url"，后期可扩展为 "text", "file" 等）
 * @property value 内容值（URL 地址或其他内容）
 */
@Serializable
data class ContentData(
    val type: String,
    val value: String
)

/**
 * 动作数据
 *
 * React 概念对标：
 * - type Action = { type: 'openTab' | 'download', params?: Record<string, any> }
 *
 * @property type 动作类型（当前固定为 "openTab"）
 * @property params 动作参数（可选，预留给后期扩展，如 { activate: true }）
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ActionData(
    val type: String,
    @EncodeDefault val params: Map<String, String>? = null
)
