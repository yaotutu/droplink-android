package top.yaotutu.droplink.ui.messages

/**
 * 消息列表页面 UI 状态
 *
 * React 概念对标：
 * - type MessageState = { status: 'idle' | 'loading' | 'success' | 'refreshing' | 'loadingMore' | 'error'; data?: Message[]; error?: string }
 *
 * 状态转换流程:
 * ```
 * Idle → Loading → Success/Error
 *         ↓           ↓
 *      Error ←  Refreshing → Success/Error
 *                   ↓
 *              LoadingMore → Success/Error
 * ```
 *
 * 使用示例:
 * ```kotlin
 * when (val state = uiState) {
 *     is MessageUiState.Loading -> ShowLoadingIndicator()
 *     is MessageUiState.Success -> ShowMessageList(state.messages)
 *     is MessageUiState.Error -> ShowError(state.message)
 *     // ...
 * }
 * ```
 */
sealed class MessageUiState {
    /**
     * 初始状态（尚未加载）
     */
    object Idle : MessageUiState()

    /**
     * 首次加载中
     */
    object Loading : MessageUiState()

    /**
     * 加载成功
     *
     * @property messages 消息列表
     * @property hasMore 是否有更多数据可加载
     * @property lastMessageId 最后一条消息的 ID（用于分页）
     */
    data class Success(
        val messages: List<MessageItem>,
        val hasMore: Boolean = false,
        val lastMessageId: Long? = null
    ) : MessageUiState()

    /**
     * 下拉刷新中
     *
     * @property currentMessages 当前已有的消息列表（刷新时保持展示）
     */
    data class Refreshing(
        val currentMessages: List<MessageItem>
    ) : MessageUiState()

    /**
     * 加载更多中（分页）
     *
     * @property currentMessages 当前已有的消息列表（加载更多时保持展示）
     */
    data class LoadingMore(
        val currentMessages: List<MessageItem>
    ) : MessageUiState()

    /**
     * 错误状态
     *
     * @property message 错误信息
     * @property previousMessages 之前的消息列表（可选，刷新失败时保留）
     */
    data class Error(
        val message: String,
        val previousMessages: List<MessageItem>? = null
    ) : MessageUiState()
}

/**
 * 消息列表项（UI 数据模型）
 *
 * React 概念对标：
 * - type MessageItem = { id: string; gotifyId: number; sender: string; contentType: string; contentValue: string; actions: ActionItem[]; timestamp: number; displayTime: string; tags: string[]; isRead: boolean }
 *
 * 数据来源：从 GotifyMessageDetail 转换而来，专门用于 UI 显示
 *
 * 设计原则：
 * - 弱化 Gotify 原生字段（title, message）
 * - 强调 extras.droplink 字段（sender, content, actions, timestamp, metadata）
 *
 * @property id 消息唯一标识（来自 droplink.id）
 * @property gotifyId Gotify 原始消息 ID（用于分页）
 * @property sender 发送者（如 "android", "chrome"）
 * @property contentType 内容类型（如 "url", "text", "file"）
 * @property contentValue 内容值（URL 地址或文本内容）
 * @property actions 动作列表
 * @property timestamp 时间戳（毫秒）
 * @property displayTime 格式化的时间字符串（如 "10:30", "昨天 10:30", "2024-01-08"）
 * @property tags 标签列表（如 ["github", "tools"]）
 * @property isRead 是否已读
 */
data class MessageItem(
    val id: String,
    val gotifyId: Long,
    val sender: String,
    val contentType: String,
    val contentValue: String,
    val actions: List<ActionItem>,
    val timestamp: Long,
    val displayTime: String,
    val tags: List<String> = emptyList(),
    val isRead: Boolean = false
)

/**
 * 动作项（UI 数据模型）
 *
 * React 概念对标：
 * - type ActionItem = { type: string; target?: string; displayText: string }
 *
 * 数据来源：从 ActionData 转换而来
 *
 * @property type 动作类型（如 "openTab", "archive", "copy", "download"）
 * @property target 目标（从 params 中提取，如 "chrome", "notion-worker"）
 * @property displayText 显示文本（如 "打开链接", "归档"）
 */
data class ActionItem(
    val type: String,
    val target: String?,
    val displayText: String
)
