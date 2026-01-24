package top.yaotutu.droplink.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yaotutu.droplink.data.model.QrCodeValidationResult
import top.yaotutu.droplink.data.repository.AuthRepository

/**
 * 登录页面的 ViewModel
 * 负责处理二维码登录业务逻辑
 *
 * React 概念对标：
 * - ViewModel ≈ Custom Hook + Context Store
 * - StateFlow ≈ useState + useContext
 * - viewModelScope ≈ useEffect cleanup
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // 使用 MutableStateFlow 管理 UI 状态（类似 React 的 useState）
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // === 二维码登录模式方法 ===

    /**
     * 开始扫描二维码
     *
     * React 对标：
     * - const startScanning = () => setIsScanning(true)
     */
    fun startQrCodeScanning() {
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            qrCodeError = null,
            errorMessage = null
        )
    }

    /**
     * 停止扫描二维码
     */
    fun stopQrCodeScanning() {
        _uiState.value = _uiState.value.copy(
            isScanning = false
        )
    }

    /**
     * 处理扫描到的二维码内容
     *
     * @param qrCodeContent 二维码原始内容（JSON 字符串）
     *
     * 流程：
     * 1. 停止扫描
     * 2. 验证二维码数据
     * 3. 如果验证成功：直接登录
     * 4. 如果验证失败：显示错误信息
     */
    fun onQrCodeScanned(qrCodeContent: String) {
        // 停止扫描
        _uiState.value = _uiState.value.copy(isScanning = false)

        // 验证二维码数据
        when (val result = authRepository.validateQrCodeData(qrCodeContent)) {
            is QrCodeValidationResult.Success -> {
                // 验证成功，直接登录
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    qrCodeError = null
                )

                viewModelScope.launch {
                    authRepository.loginWithQrCode(result.data)
                        .onSuccess { user ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginSuccess = true,
                                user = user,
                                errorMessage = null
                            )
                        }
                        .onFailure { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                qrCodeError = exception.message,
                                errorMessage = exception.message
                            )
                        }
                }
            }
            is QrCodeValidationResult.Error -> {
                // 验证失败，显示错误信息
                _uiState.value = _uiState.value.copy(
                    qrCodeError = result.message,
                    errorMessage = result.message
                )
            }
        }
    }

    /**
     * 更新相机权限状态
     */
    fun onCameraPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            cameraPermissionGranted = granted
        )

        if (!granted) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Camera permission required"  // 这个错误消息会在 UI 层显示，应该使用 stringResource
            )
        }
    }
}
