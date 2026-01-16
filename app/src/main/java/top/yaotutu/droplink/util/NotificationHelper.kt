package top.yaotutu.droplink.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import top.yaotutu.droplink.MainActivity
import top.yaotutu.droplink.R

/**
 * 通知助手类
 *
 * React 概念对标：
 * - 类似于 Web Notification API 的封装层
 * - const notify = (title, body) => new Notification(title, { body })
 *
 * 核心职责：
 * 1. 创建通知 Channel（Android 8.0+ 必需）
 * 2. 显示分享成功/失败通知
 * 3. 处理通知权限（Android 13+）
 *
 * Android vs Web 通知差异：
 * - Web: 浏览器级别，一次性权限
 * - Android: 应用级别，可按 Channel 分组控制
 * - Android 8.0+ 必须先创建 Channel，类似"注册通知类型"
 *
 * 使用示例：
 * ```kotlin
 * val helper = NotificationHelper(context)
 * helper.createNotificationChannel()  // 应用启动时调用一次
 * helper.showShareSuccessNotification("https://example.com")
 * ```
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "NotificationHelper"

        // Channel ID（通知渠道标识）
        private const val CHANNEL_ID = "droplink_share_channel"

        // Notification ID（用于区分不同通知）
        private const val NOTIFICATION_ID_SUCCESS = 1001
        private const val NOTIFICATION_ID_ERROR = 1002
    }

    /**
     * 创建通知 Channel（Android 8.0+ 必需）
     *
     * Android 8.0 引入 NotificationChannel 概念：
     * - 类似 React 中的"事件分组"，用户可以独立控制每个 Channel 的行为
     * - Channel 设置包括：重要性、声音、振动、闪光灯等
     * - 一旦创建，某些属性（如重要性）只能由用户在系统设置中修改
     *
     * 最佳实践：
     * - 应在应用启动时调用一次（多次调用是安全的，系统会忽略重复创建）
     * - 不同类型的通知应创建不同的 Channel（如"消息通知"、"系统通知"）
     */
    fun createNotificationChannel() {
        // Android 8.0 (API 26) 以下版本不需要 Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_share),
                NotificationManager.IMPORTANCE_DEFAULT  // 默认重要性（有声音，会在状态栏显示）
            ).apply {
                description = context.getString(R.string.notification_channel_share_description)
                enableLights(true)  // 允许呼吸灯
                lightColor = Color.GREEN
                enableVibration(false)  // 禁用振动（分享通知无需打扰用户）
            }

            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * 显示分享成功通知
     *
     * @param url 分享的 URL
     *
     * 通知设计：
     * - 标题：简洁的成功提示
     * - 内容：显示分享的 URL（长文本会自动折叠）
     * - 点击行为：打开应用主页
     * - 自动消失：用户点击或滑动删除后消失
     */
    fun showShareSuccessNotification(url: String) {
        // 创建点击通知后的跳转 Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            // FLAG_ACTIVITY_NEW_TASK: 在新任务栈中启动（从非 Activity 上下文必需）
            // FLAG_ACTIVITY_CLEAR_TOP: 如果 MainActivity 已存在，清除其上方的所有 Activity
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // 创建 PendingIntent（延迟执行的 Intent，交给系统在点击通知时触发）
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,  // Request code（用于区分不同的 PendingIntent）
            intent,
            // FLAG_IMMUTABLE: Android 12+ 必需，防止恶意应用修改 Intent
            // FLAG_UPDATE_CURRENT: 如果已存在相同的 PendingIntent，更新它
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)  // 小图标（上传完成）
            .setContentTitle(context.getString(R.string.notification_share_success_title))
            .setContentText(context.getString(R.string.notification_share_success_message))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("URL: ${truncateUrl(url)}")  // 展开时显示完整 URL
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 优先级
            .setAutoCancel(true)  // 点击后自动消失
            .setContentIntent(pendingIntent)  // 点击通知的跳转行为
            .build()

        // 显示通知（需要通知权限）
        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_SUCCESS, notification)
            Log.d(TAG, "Success notification shown for: $url")
        } catch (e: SecurityException) {
            // Android 13+ 需要 POST_NOTIFICATIONS 权限
            Log.w(TAG, "No notification permission", e)
        }
    }

    /**
     * 显示分享失败通知
     *
     * @param errorMessage 错误信息
     *
     * 通知设计：
     * - 标题：明确的失败提示
     * - 内容：具体的错误原因
     * - 图标：错误图标（红色感叹号）
     * - 点击行为：打开应用，方便用户重试或查看详情
     */
    fun showShareErrorNotification(errorMessage: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)  // 小图标（错误）
            .setContentTitle(context.getString(R.string.notification_share_error_title))
            .setContentText(errorMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorMessage)  // 展开时显示完整错误信息
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_ERROR, notification)
            Log.d(TAG, "Error notification shown: $errorMessage")
        } catch (e: SecurityException) {
            Log.w(TAG, "No notification permission", e)
        }
    }

    /**
     * 截断过长的 URL
     *
     * @param url 原始 URL
     * @param maxLength 最大长度（默认 100）
     * @return 截断后的 URL
     *
     * 设计考虑：
     * - 通知展开区域有限，过长的 URL 会导致布局混乱
     * - 100 字符足够显示大部分常见 URL 的关键信息
     */
    private fun truncateUrl(url: String, maxLength: Int = 100): String {
        return if (url.length > maxLength) {
            url.substring(0, maxLength) + "..."
        } else {
            url
        }
    }
}
