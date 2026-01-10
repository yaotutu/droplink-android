package top.yaotutu.droplink.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 深色主题配色方案
 *
 * React 概念对标：
 * - ColorScheme ≈ CSS Variables / Theme Context
 * - 类似 styled-components 的 ThemeProvider
 *
 * Material 3 设计原则：
 * - primary: 主要品牌色，用于关键操作
 * - secondary: 次要色，用于辅助元素
 * - tertiary: 第三色，用于强调和对比
 * - surface: 表面色，用于卡片、对话框等
 * - background: 背景色
 */
private val DarkColorScheme = darkColorScheme(
    // === 主要颜色 ===
    primary = PrimaryLight,              // 主色：亮蓝（深色模式用亮色）
    onPrimary = Color.White,             // 主色上的内容色
    primaryContainer = PrimaryDark,       // 主色容器
    onPrimaryContainer = Color(0xFFD1E3FF), // 主色容器上的内容色

    // === 次要颜色 ===
    secondary = Color(0xFF94A3B8),       // 次要色：中性灰
    onSecondary = Color(0xFF1E293B),     // 次要色上的内容色
    secondaryContainer = Color(0xFF475569), // 次要容器
    onSecondaryContainer = Color(0xFFE2E8F0), // 次要容器上的内容色

    // === 第三颜色（强调色）===
    tertiary = Accent,                    // 强调色：活力橙
    onTertiary = Color.White,            // 强调色上的内容色
    tertiaryContainer = Color(0xFF7C2D12), // 强调色容器
    onTertiaryContainer = Color(0xFFFFEDD5), // 强调色容器上的内容色

    // === 背景与表面 ===
    background = BackgroundDark,          // 背景：深蓝黑
    onBackground = TextPrimaryDark,       // 背景上的内容色
    surface = SurfaceDark,                // 表面：深灰蓝
    onSurface = TextPrimaryDark,          // 表面上的内容色
    surfaceVariant = SurfaceVariantDark,  // 表面变体
    onSurfaceVariant = TextSecondaryDark, // 表面变体上的内容色

    // === 语义色 ===
    error = Error,                        // 错误红
    onError = Color.White,               // 错误色上的内容色
    errorContainer = Color(0xFF7F1D1D),  // 错误容器
    onErrorContainer = Color(0xFFFFE4E6), // 错误容器上的内容色

    // === 轮廓与边界 ===
    outline = BorderDark,                 // 轮廓边框
    outlineVariant = DividerDark,        // 轮廓变体

    // === 其他 ===
    scrim = Color.Black.copy(alpha = 0.5f) // 遮罩层
)

/**
 * 浅色主题配色方案
 *
 * 基于 SaaS 最佳实践的配色方案：
 * - 信任蓝作为主色
 * - 活力橙作为 CTA
 * - 浅灰白背景提升视觉舒适度
 */
private val LightColorScheme = lightColorScheme(
    // === 主要颜色 ===
    primary = Primary,                    // 主色：信任蓝
    onPrimary = Color.White,             // 主色上的内容色
    primaryContainer = Color(0xFFDDE8FF), // 主色容器（浅蓝）
    onPrimaryContainer = Color(0xFF1E3A8A), // 主色容器上的内容色

    // === 次要颜色 ===
    secondary = Color(0xFF64748B),       // 次要色：中性灰
    onSecondary = Color.White,           // 次要色上的内容色
    secondaryContainer = Color(0xFFE2E8F0), // 次要容器
    onSecondaryContainer = Color(0xFF1E293B), // 次要容器上的内容色

    // === 第三颜色（强调色）===
    tertiary = Accent,                    // 强调色：活力橙
    onTertiary = Color.White,            // 强调色上的内容色
    tertiaryContainer = Color(0xFFFFEDD5), // 强调色容器（浅橙）
    onTertiaryContainer = Color(0xFF7C2D12), // 强调色容器上的内容色

    // === 背景与表面 ===
    background = BackgroundLight,         // 背景：浅灰白
    onBackground = TextPrimaryLight,      // 背景上的内容色
    surface = SurfaceLight,               // 表面：纯白
    onSurface = TextPrimaryLight,         // 表面上的内容色
    surfaceVariant = SurfaceVariantLight, // 表面变体：浅灰
    onSurfaceVariant = TextSecondaryLight, // 表面变体上的内容色

    // === 语义色 ===
    error = Error,                        // 错误红
    onError = Color.White,               // 错误色上的内容色
    errorContainer = Color(0xFFFFE4E6),  // 错误容器（浅红）
    onErrorContainer = Color(0xFF7F1D1D), // 错误容器上的内容色

    // === 轮廓与边界 ===
    outline = BorderLight,                // 轮廓边框
    outlineVariant = DividerLight,       // 轮廓变体

    // === 其他 ===
    scrim = Color.Black.copy(alpha = 0.3f) // 遮罩层
)

/**
 * Droplink 主题 Composable
 *
 * @param darkTheme 是否使用深色主题（默认跟随系统）
 * @param dynamicColor 是否使用动态颜色（Android 12+ Material You）
 * @param content 应用内容
 *
 * React 概念对标：
 * - 类似 React 的 ThemeProvider
 * - darkTheme ≈ 主题切换状态
 * - colorScheme ≈ CSS Variables / Design Tokens
 *
 * Android 特性：
 * - Material You (Android 12+): 从壁纸提取颜色
 * - 状态栏颜色自动同步
 * - 深色模式支持
 */
@Composable
fun DroplinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 动态颜色（Material You）仅在 Android 12+ 可用
    // 注意：为了保持品牌一致性，建议默认关闭动态颜色
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 根据系统版本和用户设置选择配色方案
    val colorScheme = when {
        // Android 12+ 且启用动态颜色：使用系统壁纸颜色
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 使用自定义配色方案
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 获取当前 View 以设置状态栏颜色
    val view = LocalView.current
    if (!view.isInEditMode) {
        // SideEffect：在 Composition 成功后执行副作用
        // 用于同步状态栏和导航栏颜色
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()

            // 设置状态栏图标颜色（深色模式用亮色图标，浅色模式用深色图标）
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // 应用 Material 3 主题
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}