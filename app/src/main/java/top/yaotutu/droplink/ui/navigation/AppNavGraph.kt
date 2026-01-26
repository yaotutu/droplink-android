package top.yaotutu.droplink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import top.yaotutu.droplink.data.manager.SessionManager
import top.yaotutu.droplink.ui.login.LoginScreen
import top.yaotutu.droplink.ui.login.QrCodeScannerScreen
import top.yaotutu.droplink.ui.login.LoginViewModelFactory
import top.yaotutu.droplink.ui.messages.MessageScreenWithTopBar
import top.yaotutu.droplink.ui.profile.ProfileScreenWithTopBar

/**
 * 应用导航图
 * 定义所有页面的导航路由
 *
 * React 概念对标：
 * - NavHost ≈ <Routes> + <Route>
 * - composable() ≈ <Route path="">
 * - navController.navigate() ≈ useNavigate()
 *
 * @param navController 导航控制器
 * @param sessionManager 会话管理器
 * @param modifier 修饰符
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    startDestination: String? = null
) {
    val context = LocalContext.current

    // 在 NavHost 外部创建共享的 LoginViewModel
    // 这样登录页面和扫码页面可以共享同一个 ViewModel 实例
    val loginViewModel: top.yaotutu.droplink.ui.login.LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context)
    )

    NavHost(
        navController = navController,
        startDestination = startDestination ?: if (sessionManager.isLoggedIn()) {
            NavRoutes.MESSAGES  // 已登录用户默认进入消息列表
        } else {
            NavRoutes.LOGIN
        },
        modifier = modifier
    ) {
        // 登录页面
        composable(route = NavRoutes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,  // 使用共享的 ViewModel
                onLoginSuccess = { email ->
                    // 导航到消息列表，并从栈中移除登录页面
                    navController.navigate(NavRoutes.MESSAGES) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToScanner = {
                    // 导航到扫码页面
                    navController.navigate(NavRoutes.QR_SCANNER)
                }
            )
        }

        // 二维码扫描页面
        composable(route = NavRoutes.QR_SCANNER) {
            QrCodeScannerScreen(
                onQrCodeScanned = { qrCode ->
                    // 处理扫描结果（使用共享的 ViewModel）
                    loginViewModel.onQrCodeScanned(qrCode)
                    // 返回登录页面
                    navController.popBackStack()
                },
                onNavigateBack = {
                    // 返回登录页面
                    navController.popBackStack()
                }
            )
        }

        // 消息列表页面
        composable(route = NavRoutes.MESSAGES) {
            MessageScreenWithTopBar(
                onProfileClick = {
                    // 导航到个人中心
                    navController.navigate(NavRoutes.PROFILE)
                }
            )
        }

        // 个人中心页面
        composable(route = NavRoutes.PROFILE) {
            val user = sessionManager.getUser()

            ProfileScreenWithTopBar(
                user = user,
                onBackClick = {
                    // 返回到消息列表
                    navController.popBackStack()
                },
                onLogout = {
                    // 清除会话并返回登录页
                    sessionManager.clearSession()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }  // 清空整个返回栈
                    }
                }
            )
        }
    }
}
