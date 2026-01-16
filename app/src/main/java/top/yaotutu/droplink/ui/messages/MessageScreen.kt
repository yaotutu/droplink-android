package top.yaotutu.droplink.ui.messages

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import top.yaotutu.droplink.R

/**
 * 消息列表页面 - 基于 Droplink 消息格式
 *
 * React 概念对标：
 * - const MessagePage = () => { const { state, loadMessages, refresh } = useMessages(); useEffect(() => { loadMessages(); }, []); ... }
 *
 * 设计理念：
 * - 关注 extras.droplink 数据：sender、content、actions、timestamp、tags
 * - 弱化 Gotify 原生字段：title 和 message
 * - 兼容处理无 extras.droplink 的普通 Gotify 消息
 *
 * 状态处理：
 * - Loading: 首次加载，显示全屏加载指示器
 * - Success: 显示消息列表，支持下拉刷新和滚动加载更多
 * - Refreshing: 下拉刷新中，保留当前列表
 * - LoadingMore: 加载更多中，显示底部加载指示器
 * - Error: 错误状态，如果有之前的消息则保留显示
 *
 * @param modifier 修饰符
 * @param viewModel 消息列表 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    modifier: Modifier = Modifier,
    viewModel: MessageViewModel = viewModel(
        factory = MessageViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // 首次加载：当状态为 Idle 时自动触发加载
    LaunchedEffect(Unit) {
        if (uiState is MessageUiState.Idle) {
            viewModel.loadMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MessageUiState.Loading -> {
                // 加载骨架屏（Shimmer 效果）
                LoadingSkeleton()
            }
            is MessageUiState.Success -> {
                MessageList(
                    messages = state.messages,
                    hasMore = state.hasMore,
                    onRefresh = { viewModel.refreshMessages() },
                    onLoadMore = { viewModel.loadMoreMessages() }
                )
            }
            is MessageUiState.Refreshing -> {
                MessageList(
                    messages = state.currentMessages,
                    isRefreshing = true,
                    onRefresh = { /* 已在刷新中 */ }
                )
            }
            is MessageUiState.LoadingMore -> {
                MessageList(
                    messages = state.currentMessages,
                    isLoadingMore = true
                )
            }
            is MessageUiState.Error -> {
                if (state.previousMessages.isNullOrEmpty()) {
                    // 首次加载失败 - 显示错误页面
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = state.message)
                            Button(onClick = { viewModel.loadMessages() }) {
                                Text(stringResource(R.string.message_retry))
                            }
                        }
                    }
                } else {
                    // 刷新失败 - 保留之前的列表
                    MessageList(
                        messages = state.previousMessages,
                        onRefresh = { viewModel.refreshMessages() }
                    )
                }
            }
            else -> {}
        }
    }
}

/**
 * 消息列表组件
 *
 * React 概念对标：
 * - const MessageList = ({ messages, hasMore, isRefreshing, onRefresh, onLoadMore }) => { ... }
 *
 * 功能：
 * - 下拉刷新（PullToRefreshBox）
 * - 滚动加载更多（监听滚动位置，接近底部时触发）
 * - 空状态显示
 * - 加载更多指示器
 *
 * @param messages 消息列表
 * @param hasMore 是否有更多数据
 * @param isRefreshing 是否正在刷新
 * @param isLoadingMore 是否正在加载更多
 * @param onRefresh 下拉刷新回调
 * @param onLoadMore 加载更多回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageList(
    messages: List<MessageItem>,
    hasMore: Boolean = false,
    isRefreshing: Boolean = false,
    isLoadingMore: Boolean = false,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    // 监听滚动，触发加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (hasMore && !isLoadingMore && lastVisibleIndex != null) {
                    val totalItems = listState.layoutInfo.totalItemsCount
                    // 当滚动到倒数第 5 个时触发加载更多
                    if (lastVisibleIndex >= totalItems - 5) {
                        onLoadMore()
                    }
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (messages.isEmpty() && !isRefreshing) {
            // 空状态
            EmptyMessageState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    MessageCard(message = message)
                }

                // 加载更多指示器
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 消息卡片 - 极简 key:value 展示
 *
 * 设计原则：
 * - 只展示 extras 字段的原始数据
 * - key:value 格式，简洁直观
 * - 无额外装饰，纯数据展示
 *
 * @param message 消息数据
 */
@Composable
fun MessageCard(
    message: MessageItem,
    onClick: () -> Unit = {}
) {
    // 格式化 timestamp 为可读格式
    val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(message.timestamp))

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 显示所有字段，key:value 格式
            KeyValueRow("id", message.id)
            KeyValueRow("sender", message.sender)
            KeyValueRow("contentType", message.contentType)
            KeyValueRow("contentValue", message.contentValue)
            KeyValueRow("timestamp", formattedTimestamp)
            KeyValueRow("displayTime", message.displayTime)

            if (message.tags.isNotEmpty()) {
                KeyValueRow("tags", message.tags.joinToString(", "))
            }

            if (message.actions.isNotEmpty()) {
                KeyValueRow("actions", message.actions.joinToString(", ") {
                    "${it.type}${it.target?.let { t -> "($t)" } ?: ""}"
                })
            }
        }
    }
}

/**
 * Key-Value 行组件
 *
 * 简单的 key:value 展示
 */
@Composable
fun KeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$key: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 空状态组件 - 简洁现代
 */
@Composable
fun EmptyMessageState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // 图标容器 - 简化版
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = stringResource(R.string.message_empty_title),
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.message_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.message_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * 加载骨架屏 - Shimmer 效果
 *
 * React 概念对标：
 * - const LoadingSkeleton = () => { return <div className="shimmer">{[...].map(() => <Skeleton />)}</div>; }
 *
 * UX 最佳实践：
 * - 显示内容结构的占位符，而不是简单的加载指示器
 * - 使用 Shimmer 动画提示正在加载
 * - 动画速度：1000ms（平滑但不会让人觉得卡顿）
 * - 尊重用户的运动偏好设置（prefers-reduced-motion）
 *
 * 设计原则：
 * - 骨架屏应该反映真实内容的布局
 * - 使用渐变动画模拟光泽效果
 * - 颜色应该柔和，不刺眼
 */
@Composable
fun LoadingSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(5) {
            MessageCardSkeleton()
        }
    }
}

/**
 * 消息卡片骨架屏
 *
 * 模拟 key:value 格式的简洁布局
 */
@Composable
fun MessageCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 模拟 6 行 key:value 数据
            repeat(6) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Key 骨架
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Value 骨架
                    Box(
                        modifier = Modifier
                            .width(if (index % 3 == 0) 150.dp else 120.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

/**
 * Shimmer 效果 Modifier
 *
 * 创建一个从左到右的渐变光泽效果
 *
 * React 概念对标：
 * - 类似 CSS animation: shimmer 1s infinite
 * - 或使用 react-loading-skeleton 库
 *
 * 实现原理：
 * - 使用 infiniteRepeatable 创建无限循环动画
 * - 使用 Brush.linearGradient 创建渐变效果
 * - 通过改变渐变起点实现"光泽移动"效果
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    // 创建无限循环动画
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    // 获取主题颜色
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    // 创建渐变笔刷
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, translateAnim.value - 200f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    return this.background(brush)
}
