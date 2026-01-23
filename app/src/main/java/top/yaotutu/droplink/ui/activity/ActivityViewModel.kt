package top.yaotutu.droplink.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yaotutu.droplink.data.model.ActivityGroup
import top.yaotutu.droplink.data.model.ActivityItem
import top.yaotutu.droplink.data.model.ActivityType
import top.yaotutu.droplink.data.repository.ActivityRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

/**
 * Activity 页面的 ViewModel
 *
 * React 对标：
 * - StateFlow ≈ useState + useMemo（响应式状态）
 * - viewModelScope.launch ≈ async/await（异步操作）
 * - ViewModel ≈ Custom Hook + Zustand Store（状态管理 + 业务逻辑）
 *
 * 架构职责：
 * 1. 管理 UI 状态（StateFlow）
 * 2. 处理筛选逻辑
 * 3. 处理数据分组（按日期）
 * 4. 协调数据层（Repository）
 *
 * @param repository Activity 数据仓库
 */
class ActivityViewModel(
    private val repository: ActivityRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ActivityViewModel"
    }

    // Private mutable state - 仅 ViewModel 内部可修改
    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Idle)

    // Public immutable state - UI 层只读
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    // 保存所有加载的活动数据（用于筛选）
    private var allActivities: List<ActivityItem> = emptyList()

    init {
        // 页面初始化时加载数据
        Log.d(TAG, "ActivityViewModel init - loading activities")
        loadActivities()
    }

    /**
     * 加载活动数据
     *
     * React 对标：类似 useEffect(() => { fetchData() }, [])
     */
    fun loadActivities() {
        viewModelScope.launch {
            Log.d(TAG, "loadActivities() called")
            _uiState.value = ActivityUiState.Loading

            // 检查是否有有效 Token
            if (!repository.hasValidToken()) {
                Log.e(TAG, "No valid token found")
                _uiState.value = ActivityUiState.Error("未登录，请先登录")
                return@launch
            }

            Log.d(TAG, "Fetching activities from repository...")
            val result = repository.getActivities(limit = 100)

            result.fold(
                onSuccess = { activities ->
                    Log.d(TAG, "Successfully loaded ${activities.size} activities")
                    allActivities = activities
                    val groups = groupActivitiesByDate(activities)

                    Log.d(TAG, "Grouped into ${groups.size} date groups")
                    groups.forEach { group ->
                        Log.d(TAG, "Group '${group.dateLabel}': ${group.items.size} items")
                    }

                    _uiState.value = ActivityUiState.Success(
                        groups = groups,
                        selectedFilter = ActivityType.ALL
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to load activities: ${error.message}", error)
                    _uiState.value = ActivityUiState.Error(
                        message = error.message ?: "加载活动失败"
                    )
                }
            )
        }
    }

    /**
     * 切换筛选类型
     *
     * @param filterType 筛选类型（ALL, NOTION, TABS, FILES）
     */
    fun setFilter(filterType: ActivityType) {
        val currentState = _uiState.value
        if (currentState is ActivityUiState.Success) {
            // 根据筛选类型过滤
            val filteredActivities = if (filterType == ActivityType.ALL) {
                allActivities
            } else {
                allActivities.filter { it.type == filterType }
            }

            // 重新分组
            val groups = groupActivitiesByDate(filteredActivities)

            _uiState.value = currentState.copy(
                groups = groups,
                selectedFilter = filterType
            )
        }
    }

    /**
     * 按日期分组活动
     *
     * @param activities 活动列表
     * @return 分组后的列表（TODAY, YESTERDAY, 具体日期）
     */
    private fun groupActivitiesByDate(activities: List<ActivityItem>): List<ActivityGroup> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)

        val grouped = activities.groupBy { activity ->
            val activityDate = activity.timestamp.toLocalDate()
            when {
                activityDate == today -> "TODAY"
                activityDate == yesterday -> "YESTERDAY"
                else -> {
                    // 格式化为 "Oct 24"
                    val formatter = DateTimeFormatter.ofPattern("MMM dd")
                    activityDate.format(formatter)
                }
            }
        }

        return grouped.map { (dateLabel, items) ->
            ActivityGroup(
                dateLabel = dateLabel,
                items = items.sortedByDescending { it.timestamp }
            )
        }.sortedBy { group ->
            // 排序：TODAY > YESTERDAY > 其他日期
            when (group.dateLabel) {
                "TODAY" -> 0
                "YESTERDAY" -> 1
                else -> 2
            }
        }
    }
}
