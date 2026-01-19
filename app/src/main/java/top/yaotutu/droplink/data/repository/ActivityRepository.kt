package top.yaotutu.droplink.data.repository

import android.content.Context
import top.yaotutu.droplink.data.mapper.ActivityMapper
import top.yaotutu.droplink.data.model.ActivityItem

/**
 * Activity Repository
 *
 * React 对标：
 * - 类似 services/activityService.ts
 * - export const getActivities = async () => { const messages = await gotifyApi.getMessages(); return messages.map(toActivity); }
 *
 * 核心职责：
 * 1. 从 Gotify 获取消息列表
 * 2. 转换为 ActivityItem
 * 3. 过滤无效数据
 * 4. 处理分页
 *
 * MVVM 架构中的位置：
 * - ActivityViewModel → ActivityRepository → GotifyRepository → GotifyApiService
 */
class ActivityRepository(context: Context) {

    private val gotifyRepository = GotifyRepository(context)

    /**
     * 获取活动列表
     *
     * @param limit 返回数量（默认 50）
     * @param since 用于分页的消息 ID
     * @return 活动列表
     */
    suspend fun getActivities(
        limit: Int = 50,
        since: Long? = null
    ): Result<List<ActivityItem>> {
        return try {
            // 1. 从 Gotify 获取消息
            val response = gotifyRepository.getMessages(limit, since)

            // 2. 转换为 ActivityItem
            val activities = response.messages
                .mapNotNull { message ->
                    ActivityMapper.mapToActivityItem(message)
                }

            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 检查是否有有效 Token
     */
    fun hasValidToken(): Boolean {
        return gotifyRepository.hasValidToken()
    }
}
