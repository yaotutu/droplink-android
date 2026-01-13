package top.yaotutu.droplink.ui.login

import top.yaotutu.droplink.data.model.LoginMode
import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.util.Config

/**
 * 登录页面的 UI 状态
 * 类似于 React 的 State 对象
 *
 * React 对标：
 * - 类似于 const [loginState, setLoginState] = useState({...})
 * - loginMode 控制条件渲染（官方服务器表单 vs 自建服务器表单）
 */
data class LoginUiState(
    // === 通用字段 ===
    val loginMode: LoginMode = LoginMode.OFFICIAL,  // 当前登录模式（官方/自建）
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,

    // === 官方服务器模式字段 ===
    val email: String = "",
    val verificationCode: String = "",
    val serverAddress: String = Config.DEFAULT_API_BASE_URL,  // 认证服务器地址
    val isSendingCode: Boolean = false,
    val countdown: Int = 0,  // 倒计时秒数
    val emailError: String? = null,
    val verificationCodeError: String? = null,
    val serverAddressError: String? = null,

    // === 自建服务器模式字段 ===
    val gotifyServerUrl: String = Config.DEFAULT_GOTIFY_SERVER_URL,  // Gotify 服务器地址
    val selfHostedAppToken: String = "",       // 自建服务器的 appToken
    val selfHostedClientToken: String = "",    // 自建服务器的 clientToken
    val gotifyServerUrlError: String? = null,
    val appTokenError: String? = null,
    val clientTokenError: String? = null
)
