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
import top.yaotutu.droplink.ui.main.MainScreen
import top.yaotutu.droplink.ui.share.ShareScreen
import top.yaotutu.droplink.ui.share.ShareViewModel
import top.yaotutu.droplink.ui.share.ShareViewModelFactory

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
 * @param shareViewModel 分享功能的 ViewModel（可选，用于处理分享 Intent）
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier,
    shareViewModel: ShareViewModel? = null,
    startDestination: String? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination ?: if (sessionManager.isLoggedIn()) {
            NavRoutes.MAIN
        } else {
            NavRoutes.LOGIN
        },
        modifier = modifier
    ) {
        // 登录页面
        composable(route = NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { email ->
                    // 导航到主屏幕，并从栈中移除登录页面
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 主屏幕（包含消息列表和个人中心）
        composable(route = NavRoutes.MAIN) {
            val user = sessionManager.getUser()

            MainScreen(
                user = user,
                onLogout = {
                    // 清除会话并返回登录页
                    sessionManager.clearSession()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        // 分享接收页面
        composable(route = NavRoutes.SHARE) {
            // 使用传入的 ShareViewModel 或创建新的实例
            val context = LocalContext.current
            val viewModel = shareViewModel ?: viewModel<ShareViewModel>(
                factory = ShareViewModelFactory(context)
            )

            ShareScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    // 返回上一页或主屏幕
                    if (!navController.popBackStack()) {
                        navController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.SHARE) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
