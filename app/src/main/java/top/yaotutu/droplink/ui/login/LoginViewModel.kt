package top.yaotutu.droplink.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yaotutu.droplink.data.model.LoginMode
import top.yaotutu.droplink.data.repository.AuthRepository
import top.yaotutu.droplink.data.settings.AppSettings

/**
 * 登录页面的 ViewModel
 * 负责处理验证码登录业务逻辑和倒计时
 *
 * React 概念对标：
 * - ViewModel ≈ Custom Hook + Context Store
 * - StateFlow ≈ useState + useContext
 * - viewModelScope ≈ useEffect cleanup
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val appSettings: AppSettings
) : ViewModel() {

    // 使用 MutableStateFlow 管理 UI 状态（类似 React 的 useState）
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 倒计时任务
    private var countdownJob: Job? = null

    /**
     * 更新邮箱输入
     */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }

    /**
     * 更新验证码输入
     */
    fun onVerificationCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(
            verificationCode = code,
            verificationCodeError = null,
            errorMessage = null
        )
    }

    /**
     * 更新服务器地址输入
     */
    fun onServerAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(
            serverAddress = address,
            serverAddressError = null,
            errorMessage = null
        )
    }

    /**
     * 发送验证码
     */
    fun sendVerificationCode() {
        val email = _uiState.value.email.trim()
        val serverAddress = _uiState.value.serverAddress.trim()

        // 验证邮箱
        val emailValidation = authRepository.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                emailError = emailValidation.errorMessage
            )
            return
        }

        // 验证服务器地址
        if (serverAddress.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                serverAddressError = "服务器地址不能为空"
            )
            return
        }

        // 保存服务器地址并重置 Retrofit 实例
        appSettings.setApiBaseUrl(serverAddress)
        top.yaotutu.droplink.data.network.RetrofitClient.resetInstance()

        // 开始发送验证码
        _uiState.value = _uiState.value.copy(isSendingCode = true)

        viewModelScope.launch {
            authRepository.sendVerificationCode(email)
                .onSuccess {
                    // 发送成功，开始倒计时
                    _uiState.value = _uiState.value.copy(
                        isSendingCode = false,
                        countdown = 60,
                        errorMessage = "验证码已发送到邮箱"  // 真实环境不再显示验证码
                    )

                    startCountdown()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSendingCode = false,
                        errorMessage = exception.message ?: "发送验证码失败"
                    )
                }
        }
    }

    /**
     * 开始倒计时（60秒）
     */
    private fun startCountdown() {
        // 取消之前的倒计时任务
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch {
            var count = 60
            while (count > 0) {
                delay(1000)
                count--
                _uiState.value = _uiState.value.copy(countdown = count)
            }
        }
    }

    /**
     * 使用验证码验证（注册/登录）
     */
    fun verify() {
        val email = _uiState.value.email.trim()
        val code = _uiState.value.verificationCode.trim()
        val serverAddress = _uiState.value.serverAddress.trim()

        // 验证邮箱
        val emailValidation = authRepository.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                emailError = emailValidation.errorMessage
            )
            return
        }

        // 验证验证码
        val codeValidation = authRepository.validateVerificationCode(code)
        if (!codeValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                verificationCodeError = codeValidation.errorMessage
            )
            return
        }

        // 验证服务器地址
        if (serverAddress.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                serverAddressError = "服务器地址不能为空"
            )
            return
        }

        // 保存服务器地址并重置 Retrofit 实例
        appSettings.setApiBaseUrl(serverAddress)
        top.yaotutu.droplink.data.network.RetrofitClient.resetInstance()

        // 开始验证
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            authRepository.verify(email, code)
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
                        errorMessage = exception.message
                    )
                }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // === 自建服务器模式方法 ===

    /**
     * 切换登录模式
     *
     * React 对标：
     * - const switchMode = (mode) => setLoginMode(mode)
     */
    fun switchLoginMode(mode: LoginMode) {
        _uiState.value = _uiState.value.copy(
            loginMode = mode,
            errorMessage = null,
            // 清空所有错误提示
            emailError = null,
            verificationCodeError = null,
            serverAddressError = null,
            gotifyServerUrlError = null,
            appTokenError = null,
            clientTokenError = null
        )
    }

    /**
     * 更新 Gotify 服务器地址输入
     */
    fun onGotifyServerUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(
            gotifyServerUrl = url,
            gotifyServerUrlError = null,
            errorMessage = null
        )
    }

    /**
     * 更新自建服务器 appToken 输入
     */
    fun onSelfHostedAppTokenChange(token: String) {
        _uiState.value = _uiState.value.copy(
            selfHostedAppToken = token,
            appTokenError = null,
            errorMessage = null
        )
    }

    /**
     * 更新自建服务器 clientToken 输入
     */
    fun onSelfHostedClientTokenChange(token: String) {
        _uiState.value = _uiState.value.copy(
            selfHostedClientToken = token,
            clientTokenError = null,
            errorMessage = null
        )
    }

    /**
     * 自建服务器登录
     *
     * React 对标：
     * - async function loginWithSelfHosted() { ... }
     *
     * 流程：
     * 1. 验证 3 个输入字段（Gotify 地址、appToken、clientToken）
     * 2. 调用 authRepository.verifySelfHostedTokens()
     * 3. 如果成功：设置 isLoginSuccess = true
     * 4. 如果失败：显示错误信息
     */
    fun loginWithSelfHosted() {
        val gotifyUrl = _uiState.value.gotifyServerUrl.trim()
        val appToken = _uiState.value.selfHostedAppToken.trim()
        val clientToken = _uiState.value.selfHostedClientToken.trim()

        // 1. 验证 Gotify 服务器地址
        val urlValidation = authRepository.validateGotifyServerUrl(gotifyUrl)
        if (!urlValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                gotifyServerUrlError = urlValidation.errorMessage
            )
            return
        }

        // 2. 验证 appToken
        val appTokenValidation = authRepository.validateToken(appToken)
        if (!appTokenValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                appTokenError = appTokenValidation.errorMessage
            )
            return
        }

        // 3. 验证 clientToken
        val clientTokenValidation = authRepository.validateToken(clientToken)
        if (!clientTokenValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                clientTokenError = clientTokenValidation.errorMessage
            )
            return
        }

        // 4. 开始验证
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            authRepository.verifySelfHostedTokens(gotifyUrl, appToken, clientToken)
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
                        errorMessage = exception.message
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 清理倒计时任务
        countdownJob?.cancel()
    }
}
