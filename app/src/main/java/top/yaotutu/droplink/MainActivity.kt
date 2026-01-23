package top.yaotutu.droplink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.TimeoutCancellationException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import top.yaotutu.droplink.data.manager.SessionManager
import top.yaotutu.droplink.data.repository.GotifyRepository
import top.yaotutu.droplink.ui.navigation.AppNavGraph
import top.yaotutu.droplink.ui.navigation.NavRoutes
import top.yaotutu.droplink.ui.share.NavigationEvent
import top.yaotutu.droplink.ui.share.ShareLoadingScreen
import top.yaotutu.droplink.ui.share.ShareStatus
import top.yaotutu.droplink.ui.share.ShareUiState
import top.yaotutu.droplink.ui.share.ShareViewModel
import top.yaotutu.droplink.ui.share.ShareViewModelFactory
import top.yaotutu.droplink.ui.theme.DroplinkTheme
import top.yaotutu.droplink.util.Config

/**
 * 主 Activity
 * 应用的入口点，负责设置导航图和处理分享 Intent
 *
 * React 概念对标：
 * - 类似于 React 的 App.js + BrowserRouter
 * - MainActivity ≈ <App>
 * - NavHost ≈ <Routes>
 * - Composable Screen ≈ <Route>
 *
 * Android 生命周期方法：
 * - onCreate(): 创建时调用（类似于 React 的初次渲染）
 * - onNewIntent(): 当 Activity 已存在时，接收新 Intent（launchMode="singleTop" 时触发）
 */
class MainActivity : ComponentActivity() {

    // 创建 SessionManager 实例（单例模式）
    private val sessionManager by lazy {
        SessionManager.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🐛 调试：检查语言设置
        val config = resources.configuration
        val locale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
        Log.d(TAG, "=== Language Debug Info ===")
        Log.d(TAG, "Current Locale: $locale")
        Log.d(TAG, "Language: ${locale.language}")
        Log.d(TAG, "Country: ${locale.country}")
        Log.d(TAG, "Test string (app_name): ${getString(R.string.app_name)}")
        Log.d(TAG, "Test string (login_title): ${getString(R.string.login_welcome_title)}")
        Log.d(TAG, "============================")

        Log.d(TAG, "onCreate called")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "Intent type: ${intent?.type}")
        Log.d(TAG, "Intent extras: ${intent?.extras}")

        // 初始化配置
        Config.init(applicationContext)

        // 检查是否是分享 Intent
        val isShareIntent = isShareIntent(intent)

        if (isShareIntent) {
            // 前台分享模式：显示 Loading UI
            Log.d(TAG, "Entering foreground share mode with Loading UI")
            handleForegroundShare(intent)
            return
        }

        // 正常模式：显示 UI
        enableEdgeToEdge()
        setContent {
            DroplinkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 创建导航控制器
                    val navController = rememberNavController()

                    // 设置应用导航图（根据登录状态决定起始页面）
                    AppNavGraph(
                        navController = navController,
                        sessionManager = sessionManager,
                        startDestination = null  // 正常模式不自动导航到分享页面
                    )
                }
            }
        }
    }

    /**
     * 当 Activity 已在前台，但接收到新的 Intent 时调用
     * 这是 Android 特有的机制，React 中没有直接对应的概念
     *
     * 场景：用户在使用应用时，从浏览器分享内容到这个应用
     * 如果没有 launchMode="singleTop"，系统会创建新的 Activity 实例
     * 有了 singleTop，系统会复用现有实例，并调用这个方法
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(TAG, "onNewIntent called")
        Log.d(TAG, "New intent action: ${intent.action}")
        Log.d(TAG, "New intent type: ${intent.type}")

        // 更新 Activity 的 Intent
        setIntent(intent)

        // 检查是否是分享 Intent
        if (isShareIntent(intent)) {
            Log.d(TAG, "Received share intent in onNewIntent")
            // 前台处理分享数据（显示 Loading UI）
            handleForegroundShare(intent)
        }
    }

    /**
     * 处理前台分享（显示状态化 UI）
     *
     * React 概念对标：
     * - 类似于 React 的状态驱动 UI
     * - useState + 条件渲染：根据状态显示不同内容
     *
     * 工作流程：
     * 1. 显示 Loading UI（用户可见）
     * 2. 创建 ShareViewModel
     * 3. 解析 Intent 数据
     * 4. 等待解析完成（使用 Flow + withTimeout）
     * 5. 调用处理方法（执行网络请求）
     * 6. 更新 UI 状态（Success 或 Error）
     * 7. 延迟 1.5 秒让用户看到结果
     * 8. 移到后台并关闭 Activity
     *
     * Android 特性：
     * - Activity 保持前台：确保进程不被系统冻结，网络请求能正常完成
     * - 状态化 UI：根据状态自动更新界面
     * - MutableState：Compose 的响应式状态管理（必须在 Composable 作用域内）
     * - lifecycleScope: Activity 生命周期相关的协程作用域
     * - withTimeout: 防止无限等待，30 秒超时
     *
     * 关键改进：
     * - 使用 Composable 内部的 remember 管理状态，确保重组生效
     * - 用户可以直接在界面上看到成功或失败
     * - 所有操作完成后才关闭
     * - 解决国产 ROM 后台限制问题
     *
     * @param intent 分享 Intent
     */
    private fun handleForegroundShare(intent: Intent?) {
        Log.d(TAG, "handleForegroundShare: start")
        Log.d(TAG, "Activity stays in foreground with status UI")

        // 创建 ShareViewModel
        val repository = GotifyRepository(applicationContext)
        val viewModel = ShareViewModel(applicationContext, repository)

        // 显示状态化 UI（使用 Composable 内部的状态管理）
        enableEdgeToEdge()
        setContent {
            // 在 Composable 作用域内管理状态（类似 React 的 useState）
            var shareStatus by remember { mutableStateOf<ShareStatus>(ShareStatus.Loading) }

            DroplinkTheme {
                ShareLoadingScreen(status = shareStatus)
            }

            // 使用 LaunchedEffect 执行副作用（类似 React 的 useEffect）
            LaunchedEffect(Unit) {
                try {
                    // 解析 Intent
                    viewModel.handleShareIntent(intent)

                    // 设置超时（30 秒）
                    withTimeout(30_000) {
                        // 等待状态变为 Success 或 Error
                        viewModel.uiState
                            .filter { it is ShareUiState.Success || it is ShareUiState.Error }
                            .first()
                            .let { state ->
                                when (state) {
                                    is ShareUiState.Success -> {
                                        Log.d(TAG, "Parse success, processing in foreground")
                                        // 前台处理分享数据
                                        viewModel.processSharedDataInBackground(
                                            onSuccess = { url ->
                                                Log.d(TAG, "Share success: $url")
                                                // 在新协程中更新 UI 状态并延迟关闭
                                                lifecycleScope.launch {
                                                    // 更新 UI 状态为成功
                                                    shareStatus = ShareStatus.Success(url)
                                                    // 延迟 1.5 秒让用户看到成功提示
                                                    delay(1500)
                                                    // 完成后移到后台并关闭
                                                    moveTaskToBack(true)
                                                    finish()
                                                }
                                            },
                                            onError = { error ->
                                                Log.e(TAG, "Share error: $error")
                                                // 在新协程中更新 UI 状态并延迟关闭
                                                lifecycleScope.launch {
                                                    // 更新 UI 状态为失败
                                                    shareStatus = ShareStatus.Error(error)
                                                    // 延迟 2 秒让用户看到错误信息
                                                    delay(2000)
                                                    // 失败后也移到后台并关闭
                                                    moveTaskToBack(true)
                                                    finish()
                                                }
                                            }
                                        )
                                    }
                                    is ShareUiState.Error -> {
                                        Log.e(TAG, "Parse error: ${state.message}")
                                        // 更新 UI 状态为失败
                                        shareStatus = ShareStatus.Error(state.message)
                                        // 延迟 2 秒让用户看到错误信息
                                        delay(2000)
                                        moveTaskToBack(true)
                                        finish()
                                    }
                                    else -> {
                                        // 不应该到达这里（filter 已经过滤了其他状态）
                                        Log.w(TAG, "Unexpected state: $state")
                                        shareStatus = ShareStatus.Error("未知错误")
                                        delay(2000)
                                        moveTaskToBack(true)
                                        finish()
                                    }
                                }
                            }
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e(TAG, "Foreground share timeout", e)
                    shareStatus = ShareStatus.Error("处理超时，请重试")
                    delay(2000)
                    moveTaskToBack(true)
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Foreground share error", e)
                    shareStatus = ShareStatus.Error("处理失败: ${e.message}")
                    delay(2000)
                    moveTaskToBack(true)
                    finish()
                }
            }
        }
    }

    /**
     * 判断 Intent 是否是分享 Intent
     *
     * @param intent 要检查的 Intent
     * @return 如果是分享 Intent 返回 true
     */
    private fun isShareIntent(intent: Intent?): Boolean {
        if (intent == null) {
            Log.d(TAG, "Intent is null")
            return false
        }

        val action = intent.action
        val type = intent.type

        val result = (action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE) &&
                type != null

        Log.d(TAG, "isShareIntent check - action: $action, type: $type, result: $result")
        return result
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
