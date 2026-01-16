package top.yaotutu.droplink.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * 语言管理器（国际化工具类）
 *
 * React 概念对标：
 * - LanguageManager ≈ i18next LanguageDetector（语言检测与切换）
 * - setLocale() ≈ i18next.changeLanguage('zh-CN')
 * - getLocale() ≈ i18next.language
 *
 * 核心功能：
 * 1. 动态切换应用语言（无需重启 Activity）
 * 2. 持久化用户语言偏好
 * 3. 支持跟随系统语言
 * 4. 兼容 Android 7.0+ 的 Locale API 变更
 *
 * Android 国际化原理：
 * - Android 根据 Configuration.locale 自动加载对应的 values-<language> 目录
 * - values-zh-rCN → 简体中文（中国）
 * - values-en → 英文（默认）
 * - values → 兜底语言（当找不到匹配语言时使用）
 *
 * 使用场景：
 * - 在 Settings 中提供语言切换选项
 * - 首次启动时根据系统语言自动选择
 * - 用户手动切换语言后立即生效
 *
 * 使用示例：
 * ```kotlin
 * // 1. 初始化（在 Application.onCreate() 中调用）
 * LanguageManager.init(context)
 *
 * // 2. 切换语言
 * LanguageManager.setLocale(context, Locale.SIMPLIFIED_CHINESE)
 *
 * // 3. 获取当前语言
 * val currentLocale = LanguageManager.getLocale(context)
 *
 * // 4. 重启 Activity 以应用新语言
 * recreate()
 * ```
 */
class LanguageManager private constructor() {

    companion object {
        private const val TAG = "LanguageManager"

        // SharedPreferences 文件名
        private const val PREFS_NAME = "language_prefs"

        // 语言偏好 Key
        private const val KEY_LANGUAGE = "selected_language"

        // 跟随系统语言的标记
        private const val VALUE_FOLLOW_SYSTEM = "follow_system"

        /**
         * 初始化语言管理器
         *
         * 应在 Application.onCreate() 中调用，确保应用启动时加载正确的语言
         *
         * @param context 应用上下文
         *
         * React 对标：
         * - 类似于在 React App 入口处调用 i18n.init()
         */
        fun init(context: Context) {
            val savedLanguage = getSavedLanguage(context)
            if (savedLanguage != VALUE_FOLLOW_SYSTEM) {
                // 用户手动选择过语言，应用该语言
                val locale = LocaleForamt.parse(savedLanguage)
                setLocale(context, locale)
            }
            // 否则跟随系统语言（默认行为）
        }

        /**
         * 设置应用语言
         *
         * @param context 上下文
         * @param locale 目标语言（如 Locale.SIMPLIFIED_CHINESE、Locale.ENGLISH）
         *
         * Android 版本兼容性处理：
         * - Android 7.0+ (API 24+): 使用 Configuration.setLocale()
         * - Android 13+ (API 33+): 使用 LocaleList
         *
         * React 对标：
         * - i18next.changeLanguage('zh-CN')
         */
        fun setLocale(context: Context, locale: Locale) {
            val resources = context.resources
            val configuration = Configuration(resources.configuration)

            // Android 7.0+ (API 24+) 使用 setLocale()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)

                // Android 13+ (API 33+) 使用 LocaleList（支持多语言区域）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val localeList = android.os.LocaleList(locale)
                    configuration.setLocales(localeList)
                }
            } else {
                // Android 7.0 以下版本（已过时，但保留兼容性）
                @Suppress("DEPRECATION")
                configuration.locale = locale
            }

            // 更新资源配置
            resources.updateConfiguration(configuration, resources.displayMetrics)

            // 保存用户选择
            saveLanguage(context, locale)
        }

        /**
         * 获取当前应用语言
         *
         * @param context 上下文
         * @return 当前语言 Locale
         *
         * React 对标：
         * - i18next.language（返回当前语言代码）
         */
        fun getLocale(context: Context): Locale {
            val savedLanguage = getSavedLanguage(context)
            return if (savedLanguage == VALUE_FOLLOW_SYSTEM) {
                // 跟随系统语言
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales[0]
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
            } else {
                // 用户手动选择的语言
                LocaleForamt.parse(savedLanguage)
            }
        }

        /**
         * 设置跟随系统语言
         *
         * @param context 上下文
         */
        fun setFollowSystem(context: Context) {
            saveLanguage(context, VALUE_FOLLOW_SYSTEM)
            setLocale(context, getSystemLocale())
        }

        /**
         * 获取系统语言
         *
         * @return 系统 Locale
         */
        fun getSystemLocale(): Locale {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleListCompat.getDefault()
            } else {
                @Suppress("DEPRECATION")
                Locale.getDefault()
            }
        }

        /**
         * 保存用户选择的语言
         *
         * @param context 上下文
         * @param locale 语言 Locale
         */
        private fun saveLanguage(context: Context, locale: Locale) {
            val prefs = getPrefs(context)
            prefs.edit().putString(KEY_LANGUAGE, locale.toLanguageTag()).apply()
        }

        /**
         * 保存语言标记（用于"跟随系统"）
         *
         * @param context 上下文
         * @param languageTag 语言标记（如 "zh-CN"、"en"、"follow_system"）
         */
        private fun saveLanguage(context: Context, languageTag: String) {
            val prefs = getPrefs(context)
            prefs.edit().putString(KEY_LANGUAGE, languageTag).apply()
        }

        /**
         * 获取保存的语言标记
         *
         * @param context 上下文
         * @return 语言标记（如 "zh-CN"、"en"、"follow_system"）
         */
        private fun getSavedLanguage(context: Context): String {
            val prefs = getPrefs(context)
            return prefs.getString(KEY_LANGUAGE, VALUE_FOLLOW_SYSTEM) ?: VALUE_FOLLOW_SYSTEM
        }

        /**
         * 获取 SharedPreferences 实例
         *
         * @param context 上下文
         * @return SharedPreferences
         */
        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * 语言格式化工具
     *
     * 用于解析和生成语言标记（如 "zh-CN"、"en"）
     */
    object LocaleForamt {
        /**
         * 解析语言标记
         *
         * @param languageTag 语言标记（如 "zh-CN"、"en"）
         * @return Locale 对象
         */
        fun parse(languageTag: String): Locale {
            return if (languageTag.contains("-")) {
                // 格式: zh-CN → 语言代码 + 国家代码
                val parts = languageTag.split("-")
                Locale(parts[0], parts[1])
            } else {
                // 格式: en → 仅语言代码
                Locale(languageTag)
            }
        }
    }

    /**
     * Android 13+ 的 LocaleList 兼容工具
     *
     * 用于统一不同 Android 版本的 Locale API
     */
    object LocaleListCompat {
        fun getDefault(): Locale {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.os.LocaleList.getDefault()[0] ?: Locale.ENGLISH
            } else {
                @Suppress("DEPRECATION")
                Locale.getDefault()
            }
        }
    }
}

/**
 * 语言选项枚举
 *
 * 定义支持的语言列表
 */
enum class SupportedLanguage(
    val displayName: String,
    val locale: Locale
) {
    SIMPLIFIED_CHINESE("简体中文", Locale.SIMPLIFIED_CHINESE),
    ENGLISH("English", Locale.ENGLISH),
    FOLLOW_SYSTEM("跟随系统", LanguageManager.getSystemLocale());

    companion object {
        /**
         * 根据 Locale 获取对应的语言选项
         *
         * @param locale 语言 Locale
         * @return 语言选项（如果未找到则返回 FOLLOW_SYSTEM）
         */
        fun fromLocale(locale: Locale): SupportedLanguage {
            return values().find { it.locale == locale } ?: FOLLOW_SYSTEM
        }
    }
}
