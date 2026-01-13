package top.yaotutu.droplink.ui.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yaotutu.droplink.data.model.ShareType
import top.yaotutu.droplink.data.model.SharedData
import top.yaotutu.droplink.data.repository.GotifyRepository

/**
 * 分享 ViewModel
 *
 * React 概念对标：
 * - ViewModel ≈ Custom Hook + Context
 * - StateFlow ≈ useState + useReducer
 * - SharedFlow ≈ EventEmitter（单次事件）
 *
 * @property gotifyRepository Gotify 数据仓库（用于发送消息）
 */
class ShareViewModel(
    private val gotifyRepository: GotifyRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ShareViewModel"
    }

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    // 导航事件（用于通知 UI 层跳转到分享页面）
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun handleShareIntent(intent: Intent?) {
        viewModelScope.launch {
            Log.d(TAG, "handleShareIntent called")
            _uiState.value = ShareUiState.Loading
            delay(500)

            try {
                if (intent == null) {
                    Log.e(TAG, "Intent is null")
                    _uiState.value = ShareUiState.Error("未接收到分享数据")
                    return@launch
                }

                // 解析 Intent
                val sharedData = parseIntent(intent)
                Log.d(TAG, "Parsed share data: type=${sharedData.type}, text=${sharedData.text}")

                // 验证数据
                if (sharedData.isValid()) {
                    _uiState.value = ShareUiState.Success(sharedData)
                    // 发送导航事件
                    _navigationEvent.emit(NavigationEvent.NavigateToShare)
                    Log.d(TAG, "Share data is valid, emitted navigation event")
                } else {
                    Log.e(TAG, "Share data is invalid")
                    _uiState.value = ShareUiState.Error("分享数据无效")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing intent", e)
                _uiState.value = ShareUiState.Error("解析失败: ${e.message}")
            }
        }
    }

    private fun parseIntent(intent: Intent): SharedData {
        val action = intent.action
        val type = intent.type

        return when (action) {
            Intent.ACTION_SEND -> {
                when {
                    type?.startsWith("text/") == true -> {
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                        SharedData(
                            type = ShareType.TEXT,
                            text = text,
                            subject = subject,
                            mimeType = type
                        )
                    }
                    else -> {
                        // 修复：使用新的 API（Android 13+）或抑制弃用警告（Android 12-）
                        val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        }
                        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                        SharedData(
                            type = ShareType.SINGLE_FILE,
                            fileUri = fileUri,
                            subject = subject,
                            mimeType = type
                        )
                    }
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                // 修复：使用新的 API（Android 13+）或抑制弃用警告（Android 12-）
                val fileUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                }
                val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                SharedData(
                    type = ShareType.MULTIPLE_FILES,
                    fileUris = fileUris,
                    subject = subject,
                    mimeType = type
                )
            }

            else -> SharedData(type = ShareType.UNKNOWN)
        }
    }

    /**
     * 处理分享数据：发送到 Gotify
     */
    fun processSharedData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ShareUiState.Success) return@launch

            val sharedData = currentState.sharedData

            // 只处理文本/URL 分享
            if (sharedData.type != ShareType.TEXT || sharedData.text.isNullOrBlank()) {
                _uiState.value = ShareUiState.Error("暂不支持此类型的分享")
                return@launch
            }

            // 检查是否有 Token
            if (!gotifyRepository.hasValidToken()) {
                _uiState.value = ShareUiState.Error("未登录，请先登录后再分享")
                return@launch
            }

            try {
                Log.d(TAG, "Sending share data to Gotify...")
                // 设置处理中状态（模拟进度）
                _uiState.value = ShareUiState.Processing(0)
                delay(200)

                _uiState.value = ShareUiState.Processing(30)
                delay(200)

                // 发送到 Gotify（使用新格式）
                val response = gotifyRepository.sendUrlShare(
                    url = sharedData.text
                    // priority 使用默认值 5
                )

                Log.d(TAG, "Message sent successfully, ID: ${response.id}")

                _uiState.value = ShareUiState.Processing(80)
                delay(200)

                // 发送成功
                _uiState.value = ShareUiState.Processing(100)
                delay(500)

                _uiState.value = ShareUiState.Completed

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message", e)
                _uiState.value = ShareUiState.Error("发送失败: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = ShareUiState.Idle
    }

    /**
     * 后台处理分享数据（无 UI 模式）
     *
     * 与 processSharedData() 的区别：
     * - processSharedData(): 有 UI 模式，通过 StateFlow 更新 UI 状态
     * - processSharedDataInBackground(): 无 UI 模式，通过回调通知结果
     *
     * React 概念对标：
     * - 类似于 Promise-based 的异步函数：
     *   const processInBackground = async () => {
     *       try {
     *           const result = await sendToGotify(data)
     *           onSuccess(result)
     *       } catch (error) {
     *           onError(error.message)
     *       }
     *   }
     *
     * @param onSuccess 成功回调，参数为分享的 URL
     * @param onError 失败回调，参数为错误信息
     *
     * 使用场景：
     * - 从 MainActivity 后台模式调用
     * - 不需要 UI 展示进度
     * - 通过 Notification 通知用户结果
     *
     * 使用示例：
     * ```kotlin
     * viewModel.processSharedDataInBackground(
     *     onSuccess = { url ->
     *         notificationHelper.showShareSuccessNotification(url)
     *         finish()
     *     },
     *     onError = { error ->
     *         notificationHelper.showShareErrorNotification(error)
     *         finish()
     *     }
     * )
     * ```
     */
    fun processSharedDataInBackground(
        onSuccess: (url: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. 获取当前状态
                val currentState = _uiState.value
                if (currentState !is ShareUiState.Success) {
                    onError("未接收到分享数据")
                    return@launch
                }

                val sharedData = currentState.sharedData

                // 2. 验证数据类型（当前只支持文本/URL）
                if (sharedData.type != ShareType.TEXT || sharedData.text.isNullOrBlank()) {
                    onError("暂不支持此类型的分享")
                    return@launch
                }

                // 3. 检查登录状态
                if (!gotifyRepository.hasValidToken()) {
                    onError("未登录，请先登录后再分享")
                    return@launch
                }

                // 4. 发送到 Gotify
                Log.d(TAG, "Background processing: sending to Gotify...")
                val response = gotifyRepository.sendUrlShare(url = sharedData.text)

                Log.d(TAG, "Background processing: success, ID=${response.id}")
                onSuccess(sharedData.text)

            } catch (e: Exception) {
                Log.e(TAG, "Background processing failed", e)
                onError("发送失败: ${e.message}")
            }
        }
    }
}

/**
 * 导航事件
 */
sealed class NavigationEvent {
    object NavigateToShare : NavigationEvent()
}

/**
 * ShareViewModel 工厂
 */
class ShareViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
            val repository = GotifyRepository(context)
            @Suppress("UNCHECKED_CAST")
            return ShareViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
