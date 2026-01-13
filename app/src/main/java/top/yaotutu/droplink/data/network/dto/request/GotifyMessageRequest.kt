package top.yaotutu.droplink.data.network.dto.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
@OptIn(ExperimentalSerializationApi::class)
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
 * 重要说明：
 * - droplink 字段使用 JsonElement，因为服务器返回的数据格式不一致
 * - content 可能是对象 {"type": "url", "value": "..."} 或字符串 "..."
 * - 在 ViewModel 中手动解析，提供容错处理
 *
 * @property droplink Droplink 应用自定义数据（动态 JSON 结构）
 */
@Serializable
data class GotifyExtras(
    val droplink: JsonElement
)

/**
 * Droplink 自定义数据（新格式）
 *
 * React 概念对标：
 * - interface DroplinkData { id?: string; timestamp?: number; sender?: string; content?: Content; actions?: Action[]; metadata?: Metadata }
 *
 * 重要说明：
 * - 所有字段均为可选，以兼容服务器上的不完整测试数据
 * - ViewModel 会验证必需字段（content, actions）并过滤掉无效消息
 * - 对于可选字段（id, timestamp, sender），ViewModel 会提供默认值
 *
 * @property id 消息唯一标识（可选，格式：msg_timestamp_randomString，如为 null 则使用 gotifyId 生成）
 * @property timestamp 消息时间戳（可选，毫秒，如为 null 则使用 Gotify 的 date 字段）
 * @property sender 发送者标识（可选，如 "android"，如为 null 则使用 "未知"）
 * @property content 消息内容（核心字段，如为 null 则消息将被过滤）
 * @property actions 动作列表（核心字段，如为 null 或空则消息将被过滤）
 * @property metadata 元数据（可选，包含标签等扩展信息）
 */
@Serializable
data class DroplinkData(
    val id: String? = null,
    val timestamp: Long? = null,
    val sender: String? = null,
    val content: ContentData? = null,
    val actions: List<ActionData>? = null,
    val metadata: MetadataData? = null
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
 * @property params 动作参数（可选，预留给后期扩展，如 { target: "chrome" }）
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ActionData(
    val type: String,
    @EncodeDefault val params: Map<String, String>? = null
)

/**
 * Droplink 元数据
 *
 * React 概念对标：
 * - type Metadata = { tags?: string[] }
 *
 * @property tags 标签列表（可选，如 ["github", "tools"]）
 */
@Serializable
data class MetadataData(
    val tags: List<String>? = null
)
