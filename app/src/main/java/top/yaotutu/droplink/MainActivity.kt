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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import top.yaotutu.droplink.data.manager.SessionManager
import top.yaotutu.droplink.ui.navigation.AppNavGraph
import top.yaotutu.droplink.ui.navigation.NavRoutes
import top.yaotutu.droplink.ui.share.NavigationEvent
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

    // 分享 ViewModel（需要在 Activity 级别持有，以便在多次接收分享时复用）
    private var shareViewModel: ShareViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "Intent type: ${intent?.type}")
        Log.d(TAG, "Intent extras: ${intent?.extras}")

        // 初始化配置
        Config.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            DroplinkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 创建导航控制器
                    val navController = rememberNavController()

                    // 创建或获取 ShareViewModel（使用 Factory）
                    val viewModel = viewModel<ShareViewModel>(
                        factory = ShareViewModelFactory(applicationContext)
                    )
                    shareViewModel = viewModel

                    // 检查是否是分享 Intent
                    val isShareIntent = isShareIntent(intent)
                    Log.d(TAG, "isShareIntent: $isShareIntent")

                    val startDestination = if (isShareIntent) {
                        Log.d(TAG, "Starting with SHARE destination")
                        // 如果是分享 Intent，直接导航到分享页面
                        // 在这里处理 Intent 数据
                        viewModel.handleShareIntent(intent)
                        NavRoutes.SHARE
                    } else {
                        Log.d(TAG, "Starting with default destination")
                        // 否则根据登录状态决定起始页面
                        null
                    }

                    // 监听导航事件（用于 onNewIntent 触发的导航）
                    LaunchedEffect(viewModel) {
                        viewModel.navigationEvent.collectLatest { event ->
                            when (event) {
                                is NavigationEvent.NavigateToShare -> {
                                    Log.d(TAG, "Received navigation event, navigating to SHARE")
                                    navController.navigate(NavRoutes.SHARE) {
                                        // 避免重复添加
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }

                    // 设置应用导航图
                    AppNavGraph(
                        navController = navController,
                        sessionManager = sessionManager,
                        shareViewModel = viewModel,
                        startDestination = startDestination
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
            // 处理分享数据
            shareViewModel?.handleShareIntent(intent)

            // TODO: 如果需要，可以在这里导航到分享页面
            // 但这需要访问 navController，可以通过事件总线或其他方式实现
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
