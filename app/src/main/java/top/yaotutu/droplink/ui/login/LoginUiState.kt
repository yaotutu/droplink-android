package top.yaotutu.droplink.ui.login

import top.yaotutu.droplink.data.model.User

/**
 * 登录页面的 UI 状态
 * 类似于 React 的 State 对象
 *
 * React 对标：
 * - 类似于 const [loginState, setLoginState] = useState({...})
 * - 只支持二维码登录模式
 */
data class LoginUiState(
    // === 通用字段 ===
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,

    // === 二维码登录模式字段 ===
    val isScanning: Boolean = false,              // 是否正在扫描
    val qrCodeError: String? = null,              // 二维码错误信息
    val cameraPermissionGranted: Boolean = false  // 相机权限是否已授予
)
