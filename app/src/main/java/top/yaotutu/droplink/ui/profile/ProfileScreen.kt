package top.yaotutu.droplink.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import top.yaotutu.droplink.data.model.User

/**
 * 个人中心页面 Composable - 简洁版设计
 * Material Design 3 风格，只显示核心用户信息
 *
 * @param user 用户数据
 * @param onBackClick 返回按钮回调
 */
@Composable
fun ProfileScreen(
    user: User?,
    onBackClick: () -> Unit = {}
) {
    // 添加滚动支持，避免内容过多时无法查看
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // ===== 1. 用户信息头部卡片 =====
        ModernUserProfileHeader(user = user)

        Spacer(modifier = Modifier.height(24.dp))

        // ===== 2. 账户信息区域 =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 区域标题
            SectionTitle(title = "账户信息")

            // 用户 ID 卡片
            ModernInfoCard(
                icon = Icons.Default.Person,
                label = "用户 ID",
                value = user?.id ?: "N/A"
            )

            // 邮箱卡片
            ModernInfoCard(
                icon = Icons.Default.Email,
                label = "邮箱地址",
                value = user?.email ?: "N/A"
            )

            // Token 卡片
            ModernInfoCard(
                icon = Icons.Default.Settings,
                label = "应用令牌",
                value = if (user?.token != null) {
                    user.token.take(20) + "..."
                } else {
                    "N/A"
                }
            )
        }

        // 底部留白，避免内容贴底
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 现代化用户信息头部卡片
 * 使用卡片容器替代纯背景，添加阴影和圆角
 */
@Composable
fun ModernUserProfileHeader(user: User?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(vertical = 32.dp, horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像容器，添加外圈装饰
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // 外圈光晕效果
                    Surface(
                        modifier = Modifier.size(116.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {}

                    // 头像
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        shadowElevation = 12.dp
                    ) {
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?name=${user?.username ?: "User"}&background=6366f1&color=fff&size=256",
                            contentDescription = "用户头像",
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 用户名
                Text(
                    text = user?.username ?: "未知用户",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 邮箱
                Text(
                    text = user?.email ?: "未设置邮箱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 区域标题组件
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

/**
 * 现代化信息卡片组件
 * 改进的视觉设计和更好的间距
 */
@Composable
fun ModernInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
