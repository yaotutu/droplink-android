package top.yaotutu.droplink.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yaotutu.droplink.ui.theme.DroplinkTheme

/**
 * 分享状态
 *
 * React 概念对标：
 * - 类似于 React 的 Union Type 或 Discriminated Union
 * - 用于表示不同的 UI 状态
 */
sealed class ShareStatus {
    object Loading : ShareStatus()
    data class Success(val url: String) : ShareStatus()
    data class Error(val message: String) : ShareStatus()
}

/**
 * 分享状态界面
 *
 * React 概念对标：
 * - 类似于 React 的状态驱动 UI 组件
 * - 根据不同状态渲染不同内容
 *
 * 设计目的：
 * - 在分享时显示前台界面，避免进程被系统冻结
 * - 根据状态展示不同的 UI（Loading、Success、Error）
 * - 提供完整的视觉反馈，无需额外通知
 *
 * @param status 当前分享状态
 */
@Composable
fun ShareLoadingScreen(
    status: ShareStatus = ShareStatus.Loading
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            is ShareStatus.Loading -> LoadingContent()
            is ShareStatus.Success -> SuccessContent(status.url)
            is ShareStatus.Error -> ErrorContent(status.message)
        }
    }
}

/**
 * Loading 状态内容
 */
@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        // Loading 动画
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 主提示文字
        Text(
            text = "正在分享...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 警告提示
        Text(
            text = "请稍候，不要退出应用",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 额外说明
        Text(
            text = "正在发送到服务器...",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Success 状态内容
 */
@Composable
private fun SuccessContent(url: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        // 成功图标
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "成功",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF4CAF50) // 绿色
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 成功提示
        Text(
            text = "分享成功！",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // URL 显示（截断过长的 URL）
        Text(
            text = if (url.length > 50) "${url.take(47)}..." else url,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 提示信息
        Text(
            text = "内容已发送到服务器",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Error 状态内容
 */
@Composable
private fun ErrorContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        // 错误图标
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "错误",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 错误提示
        Text(
            text = "分享失败",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 错误信息
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 提示信息
        Text(
            text = "请稍后重试",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 预览：Loading 状态
 */
@Preview(showBackground = true)
@Composable
fun ShareLoadingScreenLoadingPreview() {
    DroplinkTheme {
        ShareLoadingScreen(ShareStatus.Loading)
    }
}

/**
 * 预览：Success 状态
 */
@Preview(showBackground = true)
@Composable
fun ShareLoadingScreenSuccessPreview() {
    DroplinkTheme {
        ShareLoadingScreen(ShareStatus.Success("https://example.com/very-long-url-that-needs-to-be-truncated"))
    }
}

/**
 * 预览：Error 状态
 */
@Preview(showBackground = true)
@Composable
fun ShareLoadingScreenErrorPreview() {
    DroplinkTheme {
        ShareLoadingScreen(ShareStatus.Error("网络连接失败，请检查网络设置"))
    }
}