package top.yaotutu.droplink.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

/**
 * Activity 活动项的数据模型
 *
 * React 对标：类似 TypeScript 的 discriminated union type
 *
 * @property id 唯一标识符
 * @property type 活动类型（用于筛选）
 * @property title 活动标题（如 "Notion Saved", "Tab Opened"）
 * @property content 主要内容（引用文本、文件名等）
 * @property timestamp 时间戳
 * @property source 来源（如 "notion.so", "dribble.com"）
 * @property iconType 图标类型（决定显示什么图标和背景色）
 * @property actionButton 可选的操作按钮（如 "Retry"）
 */
data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val title: String,
    val content: String,
    val timestamp: LocalDateTime,
    val source: String? = null,
    val iconType: ActivityIconType,
    val actionButton: ActionButton? = null
)

/**
 * 活动类型枚举（用于筛选标签）
 */
enum class ActivityType {
    ALL,      // 全部
    NOTION,   // Notion 相关
    TABS,     // 标签页相关
    FILES     // 文件相关
}

/**
 * 图标类型密封类（定义图标和背景色）
 *
 * React 对标：类似 union type，但更强大（可携带数据）
 */
sealed class ActivityIconType(
    val backgroundColor: Color,
    val icon: String  // 使用 Material Icons 的名称
) {
    object NotionSaved : ActivityIconType(
        backgroundColor = Color(0xFFFFF4E6),  // 浅黄色
        icon = "note"
    )

    object TabOpened : ActivityIconType(
        backgroundColor = Color(0xFFE3F2FD),  // 浅蓝色
        icon = "tab"
    )

    object SyncFailed : ActivityIconType(
        backgroundColor = Color(0xFFFFEBEE),  // 浅红色
        icon = "sync_problem"
    )

    object ClipSaved : ActivityIconType(
        backgroundColor = Color(0xFFF3E5F5),  // 浅紫色
        icon = "content_paste"
    )

    object ReminderSet : ActivityIconType(
        backgroundColor = Color(0xFFE1F5FE),  // 浅蓝色
        icon = "event"
    )

    object FileUploaded : ActivityIconType(
        backgroundColor = Color(0xFFE8F5E9),  // 浅绿色
        icon = "description"
    )
}

/**
 * 操作按钮数据类
 */
data class ActionButton(
    val text: String,
    val onClick: () -> Unit
)

/**
 * 活动分组（按日期）
 */
data class ActivityGroup(
    val dateLabel: String,  // "TODAY", "YESTERDAY", "Oct 24, 2024"
    val items: List<ActivityItem>
)
