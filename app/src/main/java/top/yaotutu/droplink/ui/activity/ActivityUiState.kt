package top.yaotutu.droplink.ui.activity

import top.yaotutu.droplink.data.model.ActivityGroup
import top.yaotutu.droplink.data.model.ActivityType

/**
 * Activity 页面的 UI 状态
 *
 * React 对标：类似 Zustand Store 或 Redux State
 * - 使用密封类实现状态机模式（State Machine Pattern）
 * - 避免了 "loading + error + data" 的布尔值地狱（Boolean Flags Hell）
 */
sealed class ActivityUiState {
    /**
     * 空闲状态（初始状态）
     */
    object Idle : ActivityUiState()

    /**
     * 加载中状态
     */
    object Loading : ActivityUiState()

    /**
     * 成功状态（包含活动数据）
     *
     * @property groups 按日期分组的活动列表（TODAY, YESTERDAY 等）
     * @property selectedFilter 当前选中的筛选类型
     */
    data class Success(
        val groups: List<ActivityGroup>,
        val selectedFilter: ActivityType = ActivityType.ALL
    ) : ActivityUiState()

    /**
     * 错误状态
     */
    data class Error(val message: String) : ActivityUiState()
}
