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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import top.yaotutu.droplink.data.manager.TokenManager
import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.R

/**
 * 个人中心页面 - 极简设计
 *
 * React 概念对标：
 * - const ProfileScreen = ({ user }) => { const tokens = useTokens(); return <View>...</View>; }
 *
 * 设计原则：
 * - 极简风格，key:value 格式展示信息
 * - 只显示核心数据：用户ID、邮箱、发送令牌、接收令牌
 * - 无复杂装饰，专注于信息展示
 *
 * @param user 用户数据
 * @param onBackClick 返回按钮回调
 * @param modifier 修饰符（用于接收 Scaffold 的 paddingValues）
 */
@Composable
fun ProfileScreen(
    user: User?,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager.getInstance(context) }

    // 获取 tokens
    var appToken by remember { mutableStateOf<String?>(null) }
    var clientToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        appToken = tokenManager.getAppToken()
        clientToken = tokenManager.getClientToken()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier // 使用传入的 modifier（包含 paddingValues）
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ===== 1. 简洁的用户头像区域 =====
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${user?.username ?: "User"}&background=2563EB&color=fff&size=256",
                    contentDescription = "用户头像",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 用户名
            Text(
                text = user?.username ?: "未知用户",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== 2. 账户信息卡片（key:value 格式）=====
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 用户 ID
                InfoRow("用户ID", user?.id ?: "N/A")

                // 邮箱地址
                InfoRow("邮箱地址", user?.email ?: "N/A")

                // 发送令牌（appToken）
                InfoRow("发送令牌", appToken ?: "未设置")

                // 接收令牌（clientToken）
                InfoRow("接收令牌", clientToken ?: "未设置")
            }
        }

        // 底部留白
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 信息行组件 - key:value 格式
 *
 * React 概念对标：
 * - const InfoRow = ({ label, value }) => <div><span>{label}:</span> <span>{value}</span></div>
 *
 * @param label 标签（key）
 * @param value 值（value）
 */
@Composable
fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 标签
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        // 值
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 个人中心页面（带 TopAppBar）
 *
 * React 概念对标：
 * - const ProfilePage = () => { return <><Header /><ProfileContent /></> }
 *
 * 设计原则：
 * - 每个页面独立管理自己的 TopAppBar
 * - TopAppBar 左上角添加返回按钮
 * - 底部添加退出登录按钮
 *
 * @param user 用户数据
 * @param onBackClick 返回按钮回调
 * @param onLogout 退出登录回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenWithTopBar(
    user: User?,
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    // 左上角返回按钮
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.profile_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 原有的个人中心内容
            ProfileScreen(
                user = user,
                onBackClick = onBackClick,
                modifier = Modifier.weight(1f)
            )

            // 底部退出登录按钮
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_logout),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
