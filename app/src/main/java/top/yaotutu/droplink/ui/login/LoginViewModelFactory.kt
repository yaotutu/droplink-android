package top.yaotutu.droplink.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.yaotutu.droplink.data.repository.AuthRepositoryImpl

/**
 * LoginViewModel 的 Factory
 * 用于创建带有依赖的 ViewModel 实例
 *
 * React 概念对标：
 * - 类似于 Context Provider 或自定义 Hook 工厂
 */
class LoginViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                authRepository = AuthRepositoryImpl(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
