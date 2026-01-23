package top.yaotutu.droplink.ui.activity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.yaotutu.droplink.data.repository.ActivityRepository

/**
 * Activity ViewModel Factory
 *
 * React 对标：
 * - 类似 React 的 useActivity Hook 初始化
 * - const useActivity = () => { const repo = useMemo(() => new ActivityRepository(), []); return useMemo(() => new ActivityViewModel(repo), [repo]); }
 *
 * 职责：
 * - 创建 ActivityViewModel 实例
 * - 注入 ActivityRepository 依赖
 *
 * 使用示例：
 * ```kotlin
 * val viewModel: ActivityViewModel = viewModel(
 *     factory = ActivityViewModelFactory(LocalContext.current)
 * )
 * ```
 */
class ActivityViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            val repository = ActivityRepository(context)
            return ActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
