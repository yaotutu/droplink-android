package top.yaotutu.droplink.ui.messages

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import top.yaotutu.droplink.data.network.dto.response.GotifyMessageDetail
import top.yaotutu.droplink.data.repository.GotifyRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * 消息列表 ViewModel
 *
 * React 概念对标：
 * - const useMessages = () => { const [state, setState] = useState({ status: 'idle', data: [] }); const loadMessages = async () => { ... }; return { state, loadMessages, refresh, loadMore }; }
 *
 * 核心职责：
 * 1. 管理消息列表 UI 状态（Loading、Success、Error 等）
 * 2. 处理首次加载、下拉刷新、分页加载
 * 3. 数据转换：API Model (GotifyMessageDetail) → UI Model (MessageItem)
 * 4. 兼容处理无 extras.droplink 的普通 Gotify 消息
 *
 * MVVM 架构中的位置：
 * - View (Composable) 订阅 uiState
 * - ViewModel 调用 Repository
 * - Repository 调用 API Service
 *
 * 状态管理：
 * - 使用 StateFlow 实现单向数据流
 * - viewModelScope 自动管理协程生命周期
 * - 状态转换：Idle → Loading → Success/Error → Refreshing/LoadingMore → Success/Error
 */
class MessageViewModel(
    private val gotifyRepository: GotifyRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MessageViewModel"
        private const val DEFAULT_PAGE_SIZE = 50
    }

    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState.Idle)
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    /**
     * 首次加载消息
     *
     * React 概念对标：
     * - useEffect(() => { loadMessages(); }, [])
     *
     * 调用时机：
     * - 进入消息列表页面时（LaunchedEffect(Unit)）
     * - 错误页面点击"重试"按钮时
     */
    fun loadMessages() {
        viewModelScope.launch {
            Log.d(TAG, "Loading messages...")
            _uiState.value = MessageUiState.Loading

            try {
                // 1. 检查 Token
                if (!gotifyRepository.hasValidToken()) {
                    _uiState.value = MessageUiState.Error(
                        message = "未登录，请先登录后查看消息"
                    )
                    return@launch
                }

                // 2. 获取消息（首次加载，since = null）
                val response = gotifyRepository.getMessages(
                    limit = DEFAULT_PAGE_SIZE,
                    since = null
                )

                // 3. 转换为 UI Model（使用 mapNotNull 过滤无效消息）
                val messageItems = response.messages.mapNotNull { detail ->
                    convertToMessageItem(detail)
                }

                // 4. 更新状态
                _uiState.value = MessageUiState.Success(
                    messages = messageItems,
                    hasMore = response.paging.next != null,
                    lastMessageId = response.messages.lastOrNull()?.id
                )

                Log.d(TAG, "Loaded ${messageItems.size} messages")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load messages", e)
                _uiState.value = MessageUiState.Error(
                    message = "加载失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 下拉刷新
     *
     * React 概念对标：
     * - const onRefresh = async () => { setRefreshing(true); await loadMessages(); setRefreshing(false); }
     *
     * 调用时机：
     * - 用户下拉列表触发刷新
     */
    fun refreshMessages() {
        viewModelScope.launch {
            Log.d(TAG, "Refreshing messages...")

            // 1. 获取当前消息列表（刷新时保持展示）
            val currentState = _uiState.value
            val currentMessages = when (currentState) {
                is MessageUiState.Success -> currentState.messages
                is MessageUiState.Error -> currentState.previousMessages ?: emptyList()
                else -> emptyList()
            }

            _uiState.value = MessageUiState.Refreshing(currentMessages)

            try {
                // 2. 获取最新消息
                val response = gotifyRepository.getMessages(
                    limit = DEFAULT_PAGE_SIZE,
                    since = null
                )

                // 3. 转换为 UI Model（使用 mapNotNull 过滤无效消息）
                val messageItems = response.messages.mapNotNull { detail ->
                    convertToMessageItem(detail)
                }

                // 4. 更新状态
                _uiState.value = MessageUiState.Success(
                    messages = messageItems,
                    hasMore = response.paging.next != null,
                    lastMessageId = response.messages.lastOrNull()?.id
                )

                Log.d(TAG, "Refreshed ${messageItems.size} messages")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh messages", e)
                _uiState.value = MessageUiState.Error(
                    message = "刷新失败: ${e.message}",
                    previousMessages = currentMessages
                )
            }
        }
    }

    /**
     * 加载更多消息（分页）
     *
     * React 概念对标：
     * - const loadMore = async () => { if (!hasMore || loading) return; const next = await api.getMessages({ since: lastId }); setMessages([...messages, ...next]); }
     *
     * 调用时机：
     * - 用户滚动到列表底部时（自动触发）
     */
    fun loadMoreMessages() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // 1. 检查是否可以加载更多
            if (currentState !is MessageUiState.Success || !currentState.hasMore) {
                Log.d(TAG, "Cannot load more: hasMore=${(currentState as? MessageUiState.Success)?.hasMore}")
                return@launch
            }

            val lastMessageId = currentState.lastMessageId ?: return@launch

            Log.d(TAG, "Loading more messages, since=$lastMessageId")
            _uiState.value = MessageUiState.LoadingMore(currentState.messages)

            try {
                // 2. 获取更早的消息
                val response = gotifyRepository.getMessages(
                    limit = DEFAULT_PAGE_SIZE,
                    since = lastMessageId
                )

                // 3. 转换为 UI Model（使用 mapNotNull 过滤无效消息）
                val newMessageItems = response.messages.mapNotNull { detail ->
                    convertToMessageItem(detail)
                }

                // 4. 合并消息列表
                val allMessages = currentState.messages + newMessageItems

                // 5. 更新状态
                _uiState.value = MessageUiState.Success(
                    messages = allMessages,
                    hasMore = response.paging.next != null,
                    lastMessageId = response.messages.lastOrNull()?.id
                )

                Log.d(TAG, "Loaded ${newMessageItems.size} more messages, total=${allMessages.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load more messages", e)
                _uiState.value = MessageUiState.Error(
                    message = "加载失败: ${e.message}",
                    previousMessages = currentState.messages
                )
            }
        }
    }

    /**
     * 数据转换：GotifyMessageDetail → MessageItem
     *
     * React 概念对标：
     * - const mapToUi = (detail) => { try { const data = parseJson(detail.extras?.droplink); if (!validate(data)) return null; return { id: ..., sender: ..., ... }; } catch { return null; } }
     *
     * 过滤规则（返回 null 表示过滤掉该消息）：
     * - Droplink 消息必须有有效的 content 和 actions 字段
     * - content 可能是对象 {"type": "url", "value": "..."} 或字符串 "..."
     * - actions 必须是数组且至少包含一个有效元素
     * - 普通 Gotify 消息（无 extras.droplink）保留并显示为"系统"消息
     *
     * 兼容性处理:
     * - 手动解析 JsonElement，处理动态数据格式
     * - 捕获所有解析异常，返回 null 进行过滤
     * - Droplink 消息中，id/timestamp/sender 为可选字段，提供默认值
     *
     * 设计原则：
     * - Droplink 消息：强调 sender、content、actions、timestamp、tags
     * - 普通消息：显示为"系统"发送的"文本"消息，无 actions
     */
    private fun convertToMessageItem(detail: GotifyMessageDetail): MessageItem? {
        val droplinkElement = detail.extras?.droplink

        return if (droplinkElement != null) {
            // Droplink 格式的消息 - 手动解析 JsonElement
            try {
                // 确保是 JsonObject
                if (droplinkElement !is JsonObject) {
                    Log.w(TAG, "Filtered invalid Droplink message (id=${detail.id}): droplink is not an object")
                    return null
                }

                val droplink = droplinkElement.jsonObject

                // 解析 content（可能是对象或字符串）
                val contentType: String
                val contentValue: String

                val contentElement = droplink["content"]
                if (contentElement == null) {
                    Log.w(TAG, "Filtered invalid Droplink message (id=${detail.id}): missing content")
                    return null
                }

                when (contentElement) {
                    is JsonObject -> {
                        // content 是对象: {"type": "url", "value": "..."}
                        contentType = contentElement["type"]?.jsonPrimitive?.content ?: "text"
                        contentValue = contentElement["value"]?.jsonPrimitive?.content ?: ""
                    }
                    is JsonPrimitive -> {
                        // content 是字符串: "【测试】您的验证码是 123457"
                        contentType = "text"
                        contentValue = contentElement.content
                    }
                    else -> {
                        Log.w(TAG, "Filtered invalid Droplink message (id=${detail.id}): content has invalid type")
                        return null
                    }
                }

                // 解析 actions（必须是数组）
                val actionsElement = droplink["actions"]
                if (actionsElement == null) {
                    Log.w(TAG, "Filtered invalid Droplink message (id=${detail.id}): missing actions")
                    return null
                }

                if (actionsElement !is kotlinx.serialization.json.JsonArray) {
                    Log.w(TAG, "Filtered invalid Droplink message (id=${detail.id}): actions is not an array")
                    return null
                }

                // 解析 actions 数组
                val actions = actionsElement.jsonArray.mapNotNull { actionElement ->
                    if (actionElement !is JsonObject) return@mapNotNull null
                    val type = actionElement["type"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val target = actionElement["params"]?.jsonObject?.get("target")?.jsonPrimitive?.content
                    ActionItem(
                        type = type,
                        target = target,
                        displayText = getActionDisplayText(type)
                    )
                }

                // 解析可选字段
                val messageId = droplink["id"]?.jsonPrimitive?.content ?: "msg_${detail.id}"
                val timestamp = droplink["timestamp"]?.jsonPrimitive?.content?.toLongOrNull()
                    ?: parseGotifyDate(detail.date)
                val sender = droplink["sender"]?.jsonPrimitive?.content ?: "未知"

                // 解析 tags（可选）
                val metadataElement = droplink["metadata"]
                val tags = if (metadataElement is JsonObject) {
                    val tagsArray = metadataElement["tags"]
                    if (tagsArray is kotlinx.serialization.json.JsonArray) {
                        tagsArray.mapNotNull { it.jsonPrimitive.content }.take(3)
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                MessageItem(
                    id = messageId,
                    gotifyId = detail.id,
                    sender = sender,
                    contentType = contentType,
                    contentValue = contentValue,
                    actions = actions,
                    timestamp = timestamp,
                    displayTime = formatTime(timestamp),
                    tags = tags,
                    isRead = false
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Droplink message (id=${detail.id})", e)
                null  // 过滤掉解析失败的消息
            }
        } else {
            // 普通 Gotify 消息（兼容模式）
            MessageItem(
                id = "gotify_${detail.id}",
                gotifyId = detail.id,
                sender = "系统",
                contentType = "text",
                contentValue = detail.message,
                actions = emptyList(),
                timestamp = parseGotifyDate(detail.date),
                displayTime = formatTime(parseGotifyDate(detail.date)),
                tags = emptyList(),
                isRead = false
            )
        }
    }

    /**
     * 格式化时间显示
     *
     * React 概念对标：
     * - const formatTime = (timestamp) => { const diff = Date.now() - timestamp; if (diff < 86400000) return format(timestamp, 'HH:mm'); ... }
     *
     * 规则:
     * - 今天: "10:30"
     * - 昨天: "昨天 10:30"
     * - 本周: "周一 10:30"
     * - 更早: "2024-01-08"
     */
    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val nowCalendar = Calendar.getInstance()

        return when {
            // 今天
            calendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR) &&
                    calendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            // 昨天
            diff < 2 * 24 * 60 * 60 * 1000 -> {
                "昨天 " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            // 本周
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "周一"
                    Calendar.TUESDAY -> "周二"
                    Calendar.WEDNESDAY -> "周三"
                    Calendar.THURSDAY -> "周四"
                    Calendar.FRIDAY -> "周五"
                    Calendar.SATURDAY -> "周六"
                    Calendar.SUNDAY -> "周日"
                    else -> ""
                }
                "$dayOfWeek " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            // 更早
            else -> {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    /**
     * 解析 Gotify 日期格式（ISO 8601）
     *
     * React 概念对标：
     * - const parseDate = (dateString) => new Date(dateString).getTime()
     *
     * 格式: "2024-01-10T10:30:00Z"
     * 返回: Unix timestamp (毫秒)
     */
    private fun parseGotifyDate(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date: $dateString", e)
            System.currentTimeMillis()
        }
    }

    /**
     * 获取动作显示文本
     *
     * React 概念对标：
     * - const getActionLabel = (type) => ({ openTab: '打开链接', archive: '归档', ... }[type] || type)
     *
     * @param actionType 动作类型（如 "openTab", "archive"）
     * @return 显示文本（如 "打开链接", "归档"）
     */
    private fun getActionDisplayText(actionType: String): String {
        return when (actionType) {
            "openTab" -> "打开链接"
            "archive" -> "归档"
            "download" -> "下载"
            "copy" -> "复制"
            else -> actionType
        }
    }

    /**
     * 重置状态（用于退出登录或切换账号）
     *
     * React 概念对标：
     * - const reset = () => setState({ status: 'idle', data: [] })
     */
    fun reset() {
        _uiState.value = MessageUiState.Idle
    }
}

/**
 * MessageViewModel 工厂
 *
 * React 概念对标：
 * - const MessageProvider = ({ children }) => { const repo = useGotifyRepo(); return <Context.Provider value={repo}>{children}</Context.Provider>; }
 *
 * 作用：
 * - 在 Compose 中创建 ViewModel 时传入 Context
 * - 自动注入 GotifyRepository 依赖
 *
 * 使用示例：
 * ```kotlin
 * @Composable
 * fun MessageScreen(
 *     viewModel: MessageViewModel = viewModel(
 *         factory = MessageViewModelFactory(LocalContext.current)
 *     )
 * ) { ... }
 * ```
 */
class MessageViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            val repository = GotifyRepository(context)
            @Suppress("UNCHECKED_CAST")
            return MessageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
