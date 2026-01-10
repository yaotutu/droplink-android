package top.yaotutu.droplink.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.yaotutu.droplink.data.settings.AppSettings
import top.yaotutu.droplink.util.Config

/**
 * 设置页面 - 允许用户修改服务器地址等配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)

    var apiUrl by remember { mutableStateOf(appSettings.getApiBaseUrl()) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "服务器配置",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API 地址输入框
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                label = { Text("API 基础地址") },
                placeholder = { Text(Config.DEFAULT_API_BASE_URL) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "留空则使用默认地址：${Config.DEFAULT_API_BASE_URL}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (apiUrl.isBlank()) {
                        appSettings.resetToDefault()
                    } else {
                        appSettings.setApiBaseUrl(apiUrl)
                    }
                    showSuccessMessage = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存设置")
            }

            // 重置按钮
            Button(
                onClick = {
                    appSettings.resetToDefault()
                    apiUrl = Config.DEFAULT_API_BASE_URL
                    showSuccessMessage = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("恢复默认")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 成功提示
            if (showSuccessMessage) {
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        androidx.compose.material3.TextButton(
                            onClick = { showSuccessMessage = false }
                        ) {
                            Text("关闭")
                        }
                    }
                ) {
                    Text("设置已保存")
                }
            }
        }
    }
}
