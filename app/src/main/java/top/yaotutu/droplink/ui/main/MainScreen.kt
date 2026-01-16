package top.yaotutu.droplink.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import top.yaotutu.droplink.R
import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.ui.messages.MessageScreen
import top.yaotutu.droplink.ui.profile.ProfileScreen

/**
 * 主屏幕容器
 * 包含侧边抽屉导航和内容区域
 *
 * React 概念对标：
 * - 类似 React 的 Layout 组件 + Drawer
 * - ModalNavigationDrawer ≈ Ant Design Drawer
 * - NavigationDrawerItem ≈ Menu.Item
 *
 * MVVM 架构：
 * - View: MainScreen（纯 UI）
 * - 状态管理：通过 rememberSaveable 保存选中状态
 *
 * 设计改进：
 * - 使用 ModalDrawerSheet 提供更好的 Material 3 体验
 * - 优化抽屉内容的视觉层次
 * - 应用新配色方案
 *
 * @param user 用户信息
 * @param onLogout 退出登录回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User?,
    onLogout: () -> Unit = {}
) {
    // 抽屉状态
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // 当前选中的菜单项（默认为消息列表 = 0）
    var selectedNavItem by rememberSaveable { mutableIntStateOf(0) }

    // 导航菜单项
    val navItems = listOf(
        NavItem(
            title = stringResource(R.string.message_title),
            icon = Icons.Default.Email,
            onClick = { selectedNavItem = 0 }
        ),
        NavItem(
            title = stringResource(R.string.profile_title),
            icon = Icons.Default.Person,
            onClick = { selectedNavItem = 1 }
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(
                    topEnd = 24.dp,
                    bottomEnd = 24.dp
                ),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // 抽屉导航内容
                NavigationDrawerContent(
                    user = user,
                    navItems = navItems,
                    selectedNavItem = selectedNavItem,
                    onNavItemClicked = { index ->
                        selectedNavItem = index
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    },
                    onLogout = onLogout,
                    onCloseDrawer = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        },
        gesturesEnabled = true // 允许手势打开抽屉
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            navItems[selectedNavItem].title,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.login_mode_official) // 临时复用
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
            // 根据选中的菜单项显示不同内容
            when (selectedNavItem) {
                0 -> MessageScreen(
                    modifier = Modifier.padding(paddingValues)
                )
                1 -> ProfileScreen(
                    user = user,
                    onBackClick = onLogout,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * 抽屉导航内容
 * 优化后的导航抽屉，应用新的设计语言
 *
 * 设计改进：
 * - 添加渐变背景的用户信息头部
 * - 使用 Surface 和 Card 风格的头像
 * - 优化间距和圆角
 * - 改进退出登录按钮样式
 */
@Composable
fun NavigationDrawerContent(
    user: User?,
    navItems: List<NavItem>,
    selectedNavItem: Int,
    onNavItemClicked: (Int) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // === 品牌头部 ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // === 用户信息区域 ===
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 用户头像
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile_avatar),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 用户名
                Text(
                    text = user?.username ?: stringResource(R.string.common_ok),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 邮箱
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // === 导航菜单项 ===
        navItems.forEachIndexed { index, item ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (selectedNavItem == index) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                selected = selectedNavItem == index,
                onClick = {
                    onNavItemClicked(index)
                },
                modifier = Modifier.padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // === 退出登录按钮 ===
        NavigationDrawerItem(
            label = {
                Text(
                    text = stringResource(R.string.home_logout),
                    fontWeight = FontWeight.Bold
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.home_logout)
                )
            },
            selected = false,
            onClick = {
                onLogout()
                onCloseDrawer()
            },
            modifier = Modifier.padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                unselectedIconColor = MaterialTheme.colorScheme.error,
                unselectedTextColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

/**
 * 导航菜单项数据类
 */
data class NavItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
