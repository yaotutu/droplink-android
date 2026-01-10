package top.yaotutu.droplink.ui.share

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yaotutu.droplink.data.model.ShareType
import top.yaotutu.droplink.data.model.SharedData

/**
 * 分享接收页面
 *
 * React 概念对标：
 * - ShareScreen ≈ 一个 React 函数组件
 * - @Composable ≈ React.FC<Props>
 * - viewModel() ≈ useContext(ShareContext) 或自定义 Hook
 * - collectAsState() ≈ useSelector (从 Redux store 中获取状态)
 *
 * 特别注意：
 * 1. Compose 的重组（Recomposition）≈ React 的重新渲染
 * 2. remember ≈ useMemo（缓存值，避免不必要的重新计算）
 * 3. LaunchedEffect ≈ useEffect（副作用处理）
 *
 * @param onNavigateBack 返回上一页的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    viewModel: ShareViewModel = viewModel(
        factory = ShareViewModelFactory(LocalContext.current)
    ),
    onNavigateBack: () -> Unit
) {
    // 订阅 ViewModel 的状态
    // collectAsState() 会自动触发重组（类似 React 的 useState）
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("接收分享") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 根据不同的 UI 状态渲染不同的内容
            // 类似于 React 中的条件渲染
            when (val state = uiState) {
                is ShareUiState.Idle -> {
                    EmptyStateContent()
                }
                is ShareUiState.Loading -> {
                    LoadingContent()
                }
                is ShareUiState.Success -> {
                    ShareDataContent(
                        sharedData = state.sharedData,
                        onProcess = { viewModel.processSharedData() }
                    )
                }
                is ShareUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { /* 可以添加重试逻辑 */ }
                    )
                }
                is ShareUiState.Processing -> {
                    ProcessingContent(progress = state.progress)
                }
                is ShareUiState.Completed -> {
                    CompletedContent(onDone = onNavigateBack)
                }
            }
        }
    }
}

/**
 * 空状态：未接收到分享数据
 */
@Composable
private fun EmptyStateContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无分享数据",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从其他应用分享内容到这里",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/**
 * 加载状态：正在解析分享数据
 */
@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在解析分享数据...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 成功状态：显示分享数据详情
 */
@Composable
private fun ShareDataContent(
    sharedData: SharedData,
    onProcess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 分享类型卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (sharedData.type) {
                        ShareType.TEXT -> Icons.Default.Info
                        ShareType.SINGLE_FILE -> Icons.Default.Add
                        ShareType.MULTIPLE_FILES -> Icons.Default.List
                        ShareType.UNKNOWN -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (sharedData.type) {
                            ShareType.TEXT -> "文本分享"
                            ShareType.SINGLE_FILE -> "文件分享"
                            ShareType.MULTIPLE_FILES -> "多文件分享"
                            ShareType.UNKNOWN -> "未知类型"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sharedData.mimeType ?: "无类型信息",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 分享内容详情
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "内容详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 主题/标题
                if (!sharedData.subject.isNullOrBlank()) {
                    DetailItem(label = "主题", value = sharedData.subject)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 文本内容
                if (!sharedData.text.isNullOrBlank()) {
                    DetailItem(label = "内容", value = sharedData.text)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 文件信息
                if (sharedData.fileUri != null) {
                    DetailItem(label = "文件路径", value = sharedData.fileUri.toString())
                }

                // 多文件信息
                if (!sharedData.fileUris.isNullOrEmpty()) {
                    Text(
                        text = "文件列表 (${sharedData.fileUris.size} 个):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    sharedData.fileUris.forEachIndexed { index, uri ->
                        Text(
                            text = "${index + 1}. ${uri.lastPathSegment ?: uri.toString()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 操作按钮
        Button(
            onClick = onProcess,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("处理分享数据")
        }
    }
}

/**
 * 详情项组件
 */
@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 错误状态
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "出错了",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * 处理中状态
 */
@Composable
private fun ProcessingContent(progress: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.size(80.dp),
            strokeWidth = 8.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在处理... $progress%",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * 完成状态
 */
@Composable
private fun CompletedContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "处理完成！",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("完成")
        }
    }
}
