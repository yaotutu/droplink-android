package top.yaotutu.droplink.ui.messages

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * 消息列表页面 - 现代高密度设计
 *
 * 设计理念：
 * - 高信息密度：紧凑间距（6dp），更多可见内容
 * - 扁平化：minimal elevation，依靠颜色和线条区分
 * - 强对比度：未读消息用左侧蓝条+粗体+深色背景
 * - 彩色头像：Material 色彩系统，提升视觉丰富度
 *
 * 参考设计：Telegram + Gmail 的混合风格
 *
 * @param modifier 修饰符
 * @param onMessageClick 消息点击回调
 * @param onRefresh 下拉刷新回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    modifier: Modifier = Modifier,
    onMessageClick: (Message) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val messages = getSampleMessages()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (messages.isEmpty()) {
                EmptyMessageState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp) // 极小间距
                ) {
                    items(messages) { message ->
                        MessageCard(
                            message = message,
                            onClick = { onMessageClick(message) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 消息卡片 - 现代扁平化设计
 *
 * 关键特性：
 * - 左侧蓝色指示器（4dp宽）表示未读
 * - 扁平化（0dp elevation）
 * - 紧凑布局（12dp padding）
 * - 彩色圆形头像
 * - 时间固定在右上角
 */
@Composable
fun MessageCard(
    message: Message,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // 完全扁平
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 未读指示器（左侧蓝条）
            if (!message.isRead) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 发送者头像（彩色背景）
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = getAvatarColor(message.id)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (message.senderAvatar.isNotEmpty()) {
                            AsyncImage(
                                model = message.senderAvatar,
                                contentDescription = "发送者头像",
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            // 显示首字母
                            Text(
                                text = message.senderName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 消息内容
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 第一行：发送者名 + 时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (message.isRead) {
                                FontWeight.Normal
                            } else {
                                FontWeight.Bold
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = message.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.isRead) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontSize = 12.sp,
                            fontWeight = if (message.isRead) {
                                FontWeight.Normal
                            } else {
                                FontWeight.SemiBold
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 标题
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (message.isRead) {
                            FontWeight.Normal
                        } else {
                            FontWeight.SemiBold
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 内容预览
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                }

                // 未读圆点（可选，现在用左侧蓝条替代）
                if (!message.isRead) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
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
                        contentDescription = "暂无消息",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "暂无消息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "当有新消息时，会在这里显示",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * 根据消息ID生成头像颜色
 * 使用 Material 色彩系统的不同色相
 */
fun getAvatarColor(messageId: String): Color {
    val colors = listOf(
        Color(0xFF2563EB), // 蓝色
        Color(0xFF10B981), // 绿色
        Color(0xFFF59E0B), // 琥珀色
        Color(0xFFEF4444), // 红色
        Color(0xFF8B5CF6), // 紫色
        Color(0xFF06B6D4), // 青色
        Color(0xFFEC4899), // 粉色
        Color(0xFF14B8A6)  // 青绿色
    )
    val index = messageId.hashCode() % colors.size
    return colors[index.coerceAtLeast(0)]
}

/**
 * 消息数据类
 */
data class Message(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean = false
)

/**
 * 获取示例消息数据
 */
fun getSampleMessages(): List<Message> {
    return listOf(
        Message(
            id = "1",
            senderName = "系统通知",
            senderAvatar = "",
            title = "欢迎使用 Droplink",
            content = "感谢您注册 Droplink！开始您的文件分享之旅吧。",
            time = "10:30",
            isRead = false
        ),
        Message(
            id = "2",
            senderName = "张三",
            senderAvatar = "",
            title = "您有一个新的文件分享",
            content = "张三向您分享了一个文件：项目文档.pdf",
            time = "昨天",
            isRead = true
        ),
        Message(
            id = "3",
            senderName = "系统提醒",
            senderAvatar = "",
            title = "存储空间即将用尽",
            content = "您的存储空间已使用 85%，建议升级套餐或清理文件。",
            time = "2天前",
            isRead = true
        ),
        Message(
            id = "4",
            senderName = "李四",
            senderAvatar = "",
            title = "新消息提醒",
            content = "李四向您发送了一条消息，请及时查看。",
            time = "3天前",
            isRead = false
        ),
        Message(
            id = "5",
            senderName = "王五",
            senderAvatar = "",
            title = "文件上传完成",
            content = "您上传的文件 report.xlsx 已处理完成。",
            time = "1周前",
            isRead = true
        )
    )
}
