package top.yaotutu.droplink.data.network.dto.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Gotify 消息发送请求
 *
 * API 文档示例：
 * ```
 * POST /message?token={appToken}
 * Content-Type: application/json
 *
 * {
 *   "title": "Droplink 测试",
 *   "message": "自动打开链接",
 *   "priority": 5,
 *   "extras": {
 *     "droplink": {
 *       "action": "openTab",
 *       "url": "https://github.com",
 *       "options": {
 *         "activate": true
 *       }
 *     }
 *   }
 * }
 * ```
 *
 * @property title 消息标题
 * @property message 消息内容（预览文本）
 * @property priority 优先级 0-10（5 为默认）
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
 * Droplink 自定义数据
 *
 * @property action 动作类型（固定为 "openTab"）
 * @property url 要打开的 URL
 * @property options 选项配置
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DroplinkData(
    @EncodeDefault val action: String = "openTab",
    val url: String,
    @EncodeDefault val options: DroplinkOptions = DroplinkOptions()
)

/**
 * Droplink 选项配置
 *
 * @property activate 是否激活标签页（默认 true）
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DroplinkOptions(
    @EncodeDefault val activate: Boolean = true
)
