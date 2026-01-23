package top.yaotutu.droplink.ui.login

import android.Manifest
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import top.yaotutu.droplink.R
import java.util.concurrent.Executors

/**
 * 二维码扫描器视图
 *
 * 技术栈：
 * - CameraX: 相机预览
 * - ML Kit: 二维码识别
 * - Accompanist Permissions: 权限请求
 *
 * React 对标：
 * - 类似于 react-qr-reader 组件
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrCodeScannerView(
    onQrCodeScanned: (String) -> Unit,
    onStopScanning: () -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 相机权限请求
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    ) { granted ->
        onCameraPermissionResult(granted)
    }

    // 在组件首次加载时请求权限
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // === 相机预览 ===
            CameraPreview(
                onQrCodeScanned = onQrCodeScanned,
                lifecycleOwner = lifecycleOwner
            )

            // === 扫描框覆盖层 ===
            ScanningOverlay()

            // === 停止扫描按钮 ===
            IconButton(
                onClick = onStopScanning,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.login_qr_code_stop_scanning),
                    tint = Color.White
                )
            }

            // === 提示文本 ===
            Text(
                text = stringResource(R.string.login_qr_code_scanning_hint),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        } else {
            // === 权限未授予提示 ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "需要相机权限才能扫描二维码",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("授予权限")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onStopScanning) {
                    Text("取消")
                }
            }
        }
    }
}

/**
 * 相机预览组件
 *
 * 使用 CameraX 实现相机预览和图像分析
 */
@Composable
fun CameraPreview(
    onQrCodeScanned: (String) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current
    var hasScanned by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    if (cameraError != null) {
        // 显示相机错误
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "相机初始化失败",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = cameraError ?: "未知错误",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    // 预览用例
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // 图像分析用例（二维码识别）
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                QrCodeAnalyzer { qrCode ->
                                    if (!hasScanned) {
                                        hasScanned = true
                                        onQrCodeScanned(qrCode)
                                    }
                                }
                            )
                        }

                    // 选择后置摄像头
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // 解绑所有用例
                    cameraProvider.unbindAll()

                    // 绑定用例到生命周期
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )

                    Log.d("QrCodeScanner", "相机初始化成功")

                } catch (e: Exception) {
                    Log.e("QrCodeScanner", "相机初始化失败", e)
                    cameraError = when (e) {
                        is IllegalArgumentException -> "设备不支持相机功能"
                        is IllegalStateException -> "相机已被其他应用占用"
                        else -> e.message ?: "未知错误"
                    }
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 二维码分析器
 *
 * 使用 ML Kit 识别二维码
 */
class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        when (barcode.valueType) {
                            Barcode.TYPE_TEXT,
                            Barcode.TYPE_URL -> {
                                barcode.rawValue?.let { value ->
                                    onQrCodeDetected(value)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeAnalyzer", "二维码识别失败", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

/**
 * 扫描框覆盖层
 *
 * 绘制半透明遮罩和扫描框
 */
@Composable
fun ScanningOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 扫描框尺寸（正方形）
        val scanBoxSize = minOf(canvasWidth, canvasHeight) * 0.7f
        val scanBoxLeft = (canvasWidth - scanBoxSize) / 2
        val scanBoxTop = (canvasHeight - scanBoxSize) / 2

        // 绘制半透明遮罩（扫描框外的区域）
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // 清除扫描框区域（使用 BlendMode.Clear）
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(scanBoxLeft, scanBoxTop),
            size = Size(scanBoxSize, scanBoxSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // 绘制扫描框边框
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(scanBoxLeft, scanBoxTop),
            size = Size(scanBoxSize, scanBoxSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )

        // 绘制四个角的装饰线
        val cornerLength = 40.dp.toPx()
        val cornerWidth = 6.dp.toPx()

        // 左上角
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft, scanBoxTop),
            end = Offset(scanBoxLeft + cornerLength, scanBoxTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft, scanBoxTop),
            end = Offset(scanBoxLeft, scanBoxTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // 右上角
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop),
            end = Offset(scanBoxLeft + scanBoxSize - cornerLength, scanBoxTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // 左下角
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + cornerLength, scanBoxTop + scanBoxSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft, scanBoxTop + scanBoxSize - cornerLength),
            strokeWidth = cornerWidth
        )

        // 右下角
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + scanBoxSize - cornerLength, scanBoxTop + scanBoxSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = Color.Green,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize - cornerLength),
            strokeWidth = cornerWidth
        )
    }
}
