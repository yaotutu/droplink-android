package top.yaotutu.droplink.ui.login

import android.Manifest
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * 二维码扫描页面
 *
 * React 对标：
 * - 独立的全屏扫码页面
 * - 类似于微信扫一扫
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrCodeScannerScreen(
    onQrCodeScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 相机权限请求
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // 在组件首次加载时请求权限
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.login_qr_code_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // === 相机预览 ===
                CameraPreviewFullScreen(
                    onQrCodeScanned = onQrCodeScanned,
                    lifecycleOwner = lifecycleOwner
                )

                // === 扫描框覆盖层 ===
                ScanningOverlayFullScreen()

                // === 提示文本 ===
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.login_qr_code_scanning_hint),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.login_qr_code_auto_scan_hint),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // === 权限未授予提示 ===
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.login_qr_code_camera_permission_required),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.login_qr_code_camera_permission_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.login_qr_code_grant_permission))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.login_qr_code_back), color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * 全屏相机预览组件
 */
@Composable
fun CameraPreviewFullScreen(
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_qr_code_camera_init_failed),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = cameraError ?: "未知错误",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
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
 * 全屏扫描框覆盖层
 */
@Composable
fun ScanningOverlayFullScreen() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 扫描框尺寸（正方形，占屏幕宽度的 70%）
        val scanBoxSize = canvasWidth * 0.7f
        val scanBoxLeft = (canvasWidth - scanBoxSize) / 2
        val scanBoxTop = (canvasHeight - scanBoxSize) / 2

        // 绘制半透明遮罩（扫描框外的区域）
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        // 清除扫描框区域（使用 BlendMode.Clear）
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(scanBoxLeft, scanBoxTop),
            size = Size(scanBoxSize, scanBoxSize),
            cornerRadius = CornerRadius(24.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // 绘制扫描框边框
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(scanBoxLeft, scanBoxTop),
            size = Size(scanBoxSize, scanBoxSize),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // 绘制四个角的装饰线
        val cornerLength = 60.dp.toPx()
        val cornerWidth = 8.dp.toPx()
        val cornerColor = Color(0xFF4CAF50) // 绿色

        // 左上角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop),
            end = Offset(scanBoxLeft + cornerLength, scanBoxTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop),
            end = Offset(scanBoxLeft, scanBoxTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // 右上角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop),
            end = Offset(scanBoxLeft + scanBoxSize - cornerLength, scanBoxTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // 左下角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + cornerLength, scanBoxTop + scanBoxSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft, scanBoxTop + scanBoxSize - cornerLength),
            strokeWidth = cornerWidth
        )

        // 右下角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + scanBoxSize - cornerLength, scanBoxTop + scanBoxSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize - cornerLength),
            strokeWidth = cornerWidth
        )
    }
}
