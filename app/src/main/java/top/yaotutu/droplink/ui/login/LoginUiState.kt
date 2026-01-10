package top.yaotutu.droplink.ui.login

import top.yaotutu.droplink.data.model.User
import top.yaotutu.droplink.util.Config

/**
 * 登录页面的 UI 状态
 * 类似于 React 的 State 对象
 */
data class LoginUiState(
    val email: String = "",
    val verificationCode: String = "",
    val serverAddress: String = Config.DEFAULT_API_BASE_URL,  // 服务器地址,默认使用配置中的默认值
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val countdown: Int = 0,  // 倒计时秒数
    val errorMessage: String? = null,
    val emailError: String? = null,
    val verificationCodeError: String? = null,
    val serverAddressError: String? = null,  // 服务器地址验证错误信息
    val user: User? = null
)
