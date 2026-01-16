package top.yaotutu.droplink.ui.login

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yaotutu.droplink.R
import top.yaotutu.droplink.data.model.LoginMode

/**
 * 登录页面 Composable（验证码登录）
 *
 * React 概念对标：
 * - LoginScreen ≈ Login Page Component
 * - uiState ≈ React State (useState/useReducer)
 * - viewModel ≈ Custom Hook + Context（状态管理）
 *
 * MVVM 架构：
 * - View: LoginScreen + LoginForm（纯 UI）
 * - ViewModel: LoginViewModel（业务逻辑）
 * - State: LoginUiState（UI 状态）
 *
 * @param viewModel LoginViewModel 实例
 * @param onLoginSuccess 登录成功回调
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(LocalContext.current)
    ),
    onLoginSuccess: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 监听登录成功事件
    LaunchedEffect(uiState.isLoginSuccess) {
        val user = uiState.user
        if (uiState.isLoginSuccess && user != null) {
            // 自建服务器模式使用 username，官方服务器使用 email
            val displayName = if (uiState.loginMode == LoginMode.SELF_HOSTED) {
                user.username
            } else {
                user.email
            }
            onLoginSuccess(displayName)
        }
    }

    // 显示错误 Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // 渐变背景：从主色到次要色
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        // 登录表单
        LoginForm(
            uiState = uiState,
            // 官方服务器模式回调
            onEmailChange = viewModel::onEmailChange,
            onVerificationCodeChange = viewModel::onVerificationCodeChange,
            onServerAddressChange = viewModel::onServerAddressChange,
            onSendCodeClick = viewModel::sendVerificationCode,
            onLoginClick = viewModel::verify,
            onErrorDismiss = viewModel::clearError,
            // 自建服务器模式回调
            onLoginModeChange = viewModel::switchLoginMode,
            onGotifyServerUrlChange = viewModel::onGotifyServerUrlChange,
            onSelfHostedAppTokenChange = viewModel::onSelfHostedAppTokenChange,
            onSelfHostedClientTokenChange = viewModel::onSelfHostedClientTokenChange,
            onSelfHostedLoginClick = viewModel::loginWithSelfHosted
        )

        // Snackbar 提示
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

/**
 * 验证码登录表单组件
 * 纯 UI 组件，不包含业务逻辑
 *
 * 设计风格：
 * - Glassmorphism（玻璃态）卡片
 * - 柔和阴影和圆角
 * - 渐变背景
 * - 微动画反馈
 */
@Composable
fun LoginForm(
    uiState: LoginUiState,
    // === 官方服务器模式回调 ===
    onEmailChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onServerAddressChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onLoginClick: () -> Unit,
    onErrorDismiss: () -> Unit,
    // === 自建服务器模式回调 ===
    onLoginModeChange: (LoginMode) -> Unit,
    onGotifyServerUrlChange: (String) -> Unit,
    onSelfHostedAppTokenChange: (String) -> Unit,
    onSelfHostedClientTokenChange: (String) -> Unit,
    onSelfHostedLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // === Logo 区域 ===
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Droplink Logo",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === 标题 ===
        Text(
            text = stringResource(R.string.login_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.login_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // === 登录模式 Tab 切换 ===
        LoginModeTabs(
            selectedMode = uiState.loginMode,
            onModeSelected = onLoginModeChange,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // === 条件渲染：根据登录模式显示不同的表单 ===
        when (uiState.loginMode) {
            LoginMode.OFFICIAL -> {
                // === 官方服务器登录表单 ===
        // === 登录表单卡片 ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 服务器地址输入框
                OutlinedTextField(
                    value = uiState.serverAddress,
                    onValueChange = onServerAddressChange,
                    label = { Text(stringResource(R.string.login_server_address_label)) },
                    placeholder = { Text(stringResource(R.string.login_server_address_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = uiState.serverAddressError != null,
                    supportingText = uiState.serverAddressError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 邮箱输入框
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.login_email_label)) },
                    placeholder = { Text("example@email.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 验证码输入框 + 发送按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = uiState.verificationCode,
                        onValueChange = onVerificationCodeChange,
                        label = { Text(stringResource(R.string.login_verification_code_label)) },
                        placeholder = { Text("0000") },
                        isError = uiState.verificationCodeError != null,
                        supportingText = uiState.verificationCodeError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // 发送验证码按钮
                    OutlinedButton(
                        onClick = onSendCodeClick,
                        enabled = !uiState.isLoading && !uiState.isSendingCode && uiState.countdown == 0,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        if (uiState.isSendingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = if (uiState.countdown > 0) {
                                    "${uiState.countdown}s"
                                } else {
                                    stringResource(R.string.login_send_code)
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === 登录按钮 ===
        Button(
            onClick = onLoginClick,
            enabled = !uiState.isLoading && uiState.verificationCode.length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.login_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
            }
            LoginMode.SELF_HOSTED -> {
                // === 自建服务器登录表单 ===
                SelfHostedLoginForm(
                    uiState = uiState,
                    onGotifyServerUrlChange = onGotifyServerUrlChange,
                    onAppTokenChange = onSelfHostedAppTokenChange,
                    onClientTokenChange = onSelfHostedClientTokenChange,
                    onLoginClick = onSelfHostedLoginClick
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 登录模式 Tab 切换器
 *
 * React 对标：
 * - <Tabs> 组件
 * - selectedMode ≈ activeTab state
 */
@Composable
fun LoginModeTabs(
    selectedMode: LoginMode,
    onModeSelected: (LoginMode) -> Unit,
    enabled: Boolean = true
) {
    val tabs = listOf(
        LoginMode.OFFICIAL to stringResource(R.string.login_mode_official),
        LoginMode.SELF_HOSTED to stringResource(R.string.login_mode_self_hosted)
    )

    TabRow(
        selectedTabIndex = if (selectedMode == LoginMode.OFFICIAL) 0 else 1,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[if (selectedMode == LoginMode.OFFICIAL) 0 else 1]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        tabs.forEachIndexed { index, (mode, title) ->
            Tab(
                selected = selectedMode == mode,
                onClick = { if (enabled) onModeSelected(mode) },
                enabled = enabled,
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

/**
 * 自建服务器登录表单
 *
 * React 对标：
 * - 功能组件 SelfHostedLoginForm
 * - 纯 UI 组件，无业务逻辑
 */
@Composable
fun SelfHostedLoginForm(
    uiState: LoginUiState,
    onGotifyServerUrlChange: (String) -> Unit,
    onAppTokenChange: (String) -> Unit,
    onClientTokenChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // === 1. Gotify 服务器地址输入框 ===
            OutlinedTextField(
                value = uiState.gotifyServerUrl,
                onValueChange = onGotifyServerUrlChange,
                label = { Text(stringResource(R.string.login_gotify_server_label)) },
                placeholder = { Text(stringResource(R.string.login_gotify_server_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = uiState.gotifyServerUrlError != null,
                supportingText = uiState.gotifyServerUrlError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === 2. App Token 输入框 ===
            OutlinedTextField(
                value = uiState.selfHostedAppToken,
                onValueChange = onAppTokenChange,
                label = { Text(stringResource(R.string.login_app_token_label)) },
                placeholder = { Text(stringResource(R.string.login_app_token_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = uiState.appTokenError != null,
                supportingText = uiState.appTokenError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === 3. Client Token 输入框 ===
            OutlinedTextField(
                value = uiState.selfHostedClientToken,
                onValueChange = onClientTokenChange,
                label = { Text(stringResource(R.string.login_client_token_label)) },
                placeholder = { Text(stringResource(R.string.login_client_token_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = uiState.clientTokenError != null,
                supportingText = uiState.clientTokenError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // === 提示文本 ===
            Text(
                text = stringResource(R.string.login_self_hosted_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // === 登录按钮 ===
    Button(
        onClick = onLoginClick,
        enabled = !uiState.isLoading &&
                uiState.gotifyServerUrl.isNotEmpty() &&
                uiState.selfHostedAppToken.isNotEmpty() &&
                uiState.selfHostedClientToken.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
        } else {
            Text(
                text = stringResource(R.string.login_self_hosted_button),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
