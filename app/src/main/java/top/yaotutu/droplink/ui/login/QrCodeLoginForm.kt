package top.yaotutu.droplink.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.yaotutu.droplink.R

/**
 * 二维码登录表单
 *
 * React 对标：
 * - 功能组件 QrCodeLoginForm
 * - 包含扫码按钮、相机预览、错误提示
 */
@Composable
fun QrCodeLoginForm(
    uiState: LoginUiState,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onQrCodeScanned: (String) -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === 说明文本 ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_qr_code_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_qr_code_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // === 扫码按钮 ===
        if (!uiState.isScanning) {
            Button(
                onClick = onStartScanning,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.login_qr_code_scan_button))
            }
        }

        // === 相机预览（扫描中） ===
        if (uiState.isScanning) {
            QrCodeScannerView(
                onQrCodeScanned = onQrCodeScanned,
                onStopScanning = onStopScanning,
                onCameraPermissionResult = onCameraPermissionResult
            )
        }

        // === 错误提示 ===
        uiState.qrCodeError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
