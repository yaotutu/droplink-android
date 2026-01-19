package top.yaotutu.droplink.data.mapper

import android.util.Log
import top.yaotutu.droplink.data.model.ActivityIconType
import top.yaotutu.droplink.data.model.ActivityItem
import top.yaotutu.droplink.data.model.ActivityType
import top.yaotutu.droplink.data.network.dto.response.GotifyMessageDetail
import top.yaotutu.droplink.data.network.dto.request.ActionData
import top.yaotutu.droplink.data.network.dto.request.ContentData
import top.yaotutu.droplink.data.network.dto.request.DroplinkData
import top.yaotutu.droplink.data.network.dto.request.GotifyExtras
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Activity 数据转换工具
 *
 * React 对标：
 * - const mapToActivity = (gotifyMsg) => { ... }
 *
 * 职责：
 * - 将 GotifyMessageDetail 转换为 ActivityItem
 * - 提取域名作为 source
 * - 根据 actions[0].type 分类活动类型
 * - 生成标题和图标
 */
object ActivityMapper {

    private const val TAG = "ActivityMapper"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 将 Gotify 消息转换为 ActivityItem
     *
     * 支持的 action 类型：
     * - "openTab" → 标签页相关
     * - "archive" → 根据 handler 判断（notion/obsidian/notes）
     *
     * @param message Gotify 消息详情
     * @return ActivityItem，如果数据不完整返回 null
     */
    fun mapToActivityItem(message: GotifyMessageDetail): ActivityItem? {
        Log.d(TAG, "Mapping message id=${message.id}")

        // 1. 解析 droplink 数据
        val droplinkData = parseDroplinkData(message.extras)
        if (droplinkData == null) {
            Log.w(TAG, "Failed to parse droplink data for message id=${message.id}")
            return null
        }

        // 2. 获取第一个 action（必需）
        val firstAction = droplinkData.actions?.firstOrNull()
        if (firstAction == null) {
            Log.w(TAG, "No actions found for message id=${message.id}")
            return null
        }

        Log.d(TAG, "Action type: ${firstAction.type}, params: ${firstAction.params}")

        // 3. 提取必需字段
        val id = droplinkData.id ?: "msg_${message.id}"
        val timestamp = droplinkData.timestamp?.let {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        } ?: run {
            // 如果没有 timestamp，尝试解析 Gotify 的 date 字段
            try {
                LocalDateTime.parse(message.date.removeSuffix("Z"))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse date for message id=${message.id}, using current time")
                LocalDateTime.now()
            }
        }

        // 4. 提取内容
        val content = droplinkData.content?.value ?: message.message

        // 5. 提取域名作为 source
        val source = extractDomain(content)

        // 6. 根据 action 类型确定活动类型、标题和图标
        val activityTypeInfo = determineActivityType(firstAction)

        Log.d(TAG, "Created activity: id=$id, type=${activityTypeInfo.first}, title=${activityTypeInfo.second}, source=$source")

        return ActivityItem(
            id = id,
            type = activityTypeInfo.first,
            title = activityTypeInfo.second,
            content = content,
            timestamp = timestamp,
            source = source,
            iconType = activityTypeInfo.third,
            actionButton = null // 暂不实现操作按钮
        )
    }

    /**
     * 解析 Droplink 数据（完全手动解析，避免序列化问题）
     *
     * @param extras Gotify 扩展数据
     * @return DroplinkData，解析失败返回 null
     */
    private fun parseDroplinkData(extras: GotifyExtras?): DroplinkData? {
        if (extras?.droplink == null) return null

        // 完全使用手动解析
        return parseDroplinkDataManually(extras.droplink)
    }

    /**
     * 手动解析 Droplink 数据（容错处理）
     */
    private fun parseDroplinkDataManually(element: kotlinx.serialization.json.JsonElement): DroplinkData? {
        if (element !is JsonObject) return null

        val jsonObject = element.jsonObject

        val id = jsonObject["id"]?.jsonPrimitive?.content
        val timestamp = jsonObject["timestamp"]?.jsonPrimitive?.content?.toLongOrNull()
        val sender = jsonObject["sender"]?.jsonPrimitive?.content

        // 解析 content
        val contentElement = jsonObject["content"]
        val content = if (contentElement is JsonObject) {
            val type = contentElement["type"]?.jsonPrimitive?.content ?: "unknown"
            val value = contentElement["value"]?.jsonPrimitive?.content ?: ""
            ContentData(type, value)
        } else {
            null
        }

        // 解析 actions
        val actionsArray = jsonObject["actions"]?.jsonArray
        val actions = if (actionsArray != null) {
            actionsArray.mapNotNull { actionElement ->
                if (actionElement is JsonObject) {
                    val actionObj = actionElement.jsonObject
                    val type = actionObj["type"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val paramsElement = actionObj["params"]
                    val params = if (paramsElement is JsonObject) {
                        paramsElement.mapKeys { it.key }
                            .mapValues { it.value.jsonPrimitive.content }
                    } else {
                        null
                    }
                    ActionData(type, params)
                } else {
                    null
                }
            }
        } else {
            null
        }

        // 解析 metadata
        val metadataElement = jsonObject["metadata"]
        val tags = if (metadataElement is JsonObject) {
            metadataElement["tags"]?.jsonArray?.map { it.jsonPrimitive.content }
        } else {
            null
        }

        return DroplinkData(
            id = id,
            timestamp = timestamp,
            sender = sender,
            content = content,
            actions = actions,
            metadata = top.yaotutu.droplink.data.network.dto.request.MetadataData(tags)
        )
    }

    /**
     * 根据 action 类型确定活动类型、标题和图标
     *
     * React 对标：
     * - const getActivityType = (action) => { switch(action.type) { case 'openTab': return [TABS, 'Tab Opened', TabOpened]; ... } }
     *
     * @param action 第一个 action
     * @return Triple(活动类型, 标题, 图标类型)
     */
    private fun determineActivityType(action: ActionData): Triple<ActivityType, String, ActivityIconType> {
        return when (action.type) {
            "openTab" -> Triple(
                ActivityType.TABS,
                "Tab Opened",
                ActivityIconType.TabOpened
            )

            "archive" -> {
                // 根据 handler 参数判断
                val handler = action.params?.get("handler")?.lowercase()
                when (handler) {
                    "notion" -> Triple(
                        ActivityType.NOTION,
                        "Notion Saved",
                        ActivityIconType.NotionSaved
                    )
                    "obsidian" -> Triple(
                        ActivityType.FILES,
                        "Obsidian Saved",
                        ActivityIconType.FileUploaded
                    )
                    "notes" -> Triple(
                        ActivityType.FILES,
                        "Notes Saved",
                        ActivityIconType.FileUploaded
                    )
                    else -> Triple(
                        ActivityType.FILES,
                        "File Saved",
                        ActivityIconType.FileUploaded
                    )
                }
            }

            else -> Triple(
                ActivityType.ALL,
                "Activity",
                ActivityIconType.ClipSaved
            )
        }
    }

    /**
     * 从 URL 或文本中提取域名
     *
     * @param url URL 或文本内容
     * @return 域名（如 "github.com"），如果不是 URL 返回 null
     */
    private fun extractDomain(url: String): String? {
        return try {
            val urlObj = URL(url)
            urlObj.host
        } catch (e: Exception) {
            // 如果不是有效的 URL，返回 null
            null
        }
    }
}
