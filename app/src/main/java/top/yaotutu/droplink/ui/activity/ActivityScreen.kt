package top.yaotutu.droplink.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yaotutu.droplink.R
import top.yaotutu.droplink.data.model.ActivityItem
import top.yaotutu.droplink.data.model.ActivityType
import java.time.format.DateTimeFormatter

/**
 * Activity 页面 - 活动历史记录
 *
 * React 概念对标：
 * - const ActivityPage = () => { const { state, setFilter } = useActivity(); ... }
 *
 * 设计特点：
 * - 筛选标签（All, Notion, Tabs, Files）
 * - 按日期分组的活动列表
 * - 不同活动类型有不同的图标和颜色
 *
 * 注意：顶部标题栏由 MainScreen 的 TopAppBar 统一管理
 *
 * @param modifier 修饰符
 * @param viewModel Activity ViewModel
 */
@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // 首次加载
    LaunchedEffect(Unit) {
        if (uiState is ActivityUiState.Idle) {
            viewModel.loadActivities()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is ActivityUiState.Loading -> {
                // 加载状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ActivityUiState.Success -> {
                // 筛选标签
                FilterTabs(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )

                // 活动列表
                ActivityList(groups = state.groups)
            }
            is ActivityUiState.Error -> {
                // 错误状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = state.message)
                        Button(onClick = { viewModel.loadActivities() }) {
                            Text(stringResource(R.string.activity_retry))
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

/**
 * 筛选标签组件
 *
 * React 对标：
 * - const FilterTabs = ({ selected, onSelect }) => <Tabs>...</Tabs>
 *
 * 设计：
 * - All 标签选中时为蓝色填充
 * - 其他标签选中时为灰色边框
 * - 未选中标签为浅灰色边框
 *
 * @param selectedFilter 当前选中的筛选类型
 * @param onFilterSelected 筛选类型改变回调
 */
@Composable
fun FilterTabs(
    selectedFilter: ActivityType,
    onFilterSelected: (ActivityType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All 标签（特殊样式）
        FilterChip(
            label = stringResource(R.string.activity_filter_all),
            selected = selectedFilter == ActivityType.ALL,
            onClick = { onFilterSelected(ActivityType.ALL) },
            isPrimary = true
        )

        // Notion 标签
        FilterChip(
            label = stringResource(R.string.activity_filter_notion),
            selected = selectedFilter == ActivityType.NOTION,
            onClick = { onFilterSelected(ActivityType.NOTION) }
        )

        // Tabs 标签
        FilterChip(
            label = stringResource(R.string.activity_filter_tabs),
            selected = selectedFilter == ActivityType.TABS,
            onClick = { onFilterSelected(ActivityType.TABS) }
        )

        // Files 标签
        FilterChip(
            label = stringResource(R.string.activity_filter_files),
            selected = selectedFilter == ActivityType.FILES,
            onClick = { onFilterSelected(ActivityType.FILES) }
        )
    }
}

/**
 * 筛选标签单项
 *
 * @param label 标签文本
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param isPrimary 是否为主要标签（All 标签）
 */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = when {
            isPrimary && selected -> MaterialTheme.colorScheme.primary
            selected -> Color.Transparent
            else -> Color.Transparent
        },
        border = when {
            isPrimary && selected -> null
            selected -> androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.outline
            )
            else -> androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = when {
                isPrimary && selected -> Color.White
                selected -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 活动列表
 *
 * React 对标：
 * - const ActivityList = ({ groups }) => groups.map(group => <Group key={group.date}>...</Group>)
 *
 * @param groups 按日期分组的活动列表
 */
@Composable
fun ActivityList(
    groups: List<top.yaotutu.droplink.data.model.ActivityGroup>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groups.forEach { group ->
            // 日期标题
            item(key = "header_${group.dateLabel}") {
                Text(
                    text = group.dateLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // 该日期下的活动列表
            items(
                items = group.items,
                key = { it.id }
            ) { item ->
                ActivityListItem(item = item)
            }
        }

        // 底部提示
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.activity_end_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 活动列表项
 *
 * React 对标：
 * - const ActivityListItem = ({ item }) => <Card>...</Card>
 *
 * 设计：
 * - 左侧：圆形彩色图标（40dp）
 * - 中间：标题 + 内容 + 时间来源
 * - 右侧：可选的操作按钮
 *
 * @param item 活动数据
 */
@Composable
fun ActivityListItem(
    item: ActivityItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            ActivityIcon(iconType = item.iconType)

            // 中间内容区
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 标题
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 内容
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // 时间和来源
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 格式化时间
                    val formatter = DateTimeFormatter.ofPattern("h:mm a")
                    val formattedTime = item.timestamp.format(formatter)

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    // 来源（如果有）
                    if (item.source != null) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = item.source,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 右侧操作按钮（如果有）
            if (item.actionButton != null) {
                TextButton(
                    onClick = item.actionButton.onClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = item.actionButton.text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 活动图标组件
 *
 * 根据不同的活动类型显示不同颜色的圆形图标
 *
 * @param iconType 图标类型（包含背景色和图标名称）
 */
@Composable
fun ActivityIcon(
    iconType: top.yaotutu.droplink.data.model.ActivityIconType
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(iconType.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // 根据图标名称显示对应的 Material Icon
        // 注意：这里使用文本作为占位，实际应该使用 Material Icons
        Text(
            text = getIconEmoji(iconType.icon),
            fontSize = 18.sp
        )
    }
}

/**
 * 将图标名称映射为 Emoji（临时方案）
 *
 * TODO(future): 使用真实的 Material Icons
 *
 * @param iconName 图标名称
 * @return 对应的 Emoji
 */
@Composable
fun getIconEmoji(iconName: String): String {
    return when (iconName) {
        "note" -> "📝"
        "tab" -> "🔗"
        "sync_problem" -> "⚠️"
        "content_paste" -> "📋"
        "event" -> "📅"
        "description" -> "📄"
        else -> "📌"
    }
}
