package top.yaotutu.droplink.util

/**
 * URL 提取工具
 *
 * 用途：
 * 从混乱的分享文本中智能提取 URL
 *
 * 背景问题：
 * Android 应用分享行为不统一，导致分享的文本格式各异：
 * - Chrome: "https://v2ex.com/"（纯 URL）
 * - Via 浏览器: "V2EX\nhttps://v2ex.com/"（标题 + 换行 + URL）
 * - Firefox: "V2EX https://v2ex.com/"（标题 + 空格 + URL）
 * - Twitter: "看看这个！https://..."（描述 + URL）
 *
 * React 对标：
 * - 类似前端处理用户输入的 sanitize/parse 函数
 * - const extractUrl = (text) => text.match(/https?:\/\/[^\s]+/)?.[0] || text
 *
 * 示例：
 * ```kotlin
 * val raw1 = "V2EX\nhttps://v2ex.com/"
 * val url1 = UrlExtractor.extract(raw1)  // "https://v2ex.com/"
 *
 * val raw2 = "https://v2ex.com/"
 * val url2 = UrlExtractor.extract(raw2)  // "https://v2ex.com/"
 *
 * val raw3 = "看看这个网站 https://v2ex.com/ 很不错"
 * val url3 = UrlExtractor.extract(raw3)  // "https://v2ex.com/"
 * ```
 */
object UrlExtractor {

    /**
     * URL 匹配正则表达式
     * 匹配 http:// 或 https:// 开头的 URL
     * [^\s]+ 表示匹配非空白字符（直到遇到空格、换行等）
     */
    private val URL_REGEX = Regex("https?://[^\\s]+")

    /**
     * 从文本中提取 URL
     *
     * 策略：
     * 1. 如果文本本身就是纯 URL → 直接返回
     * 2. 如果文本包含 URL → 提取第一个 URL
     * 3. 如果没有找到 URL → 返回原始文本（可能是纯文本分享）
     *
     * @param text 原始分享文本
     * @return 提取的 URL 或原始文本
     */
    fun extract(text: String?): String {
        // 空值处理
        if (text.isNullOrBlank()) {
            return ""
        }

        // 去除首尾空白
        val trimmed = text.trim()

        // 如果本身就是纯 URL（不包含空格、换行等），直接返回
        if (trimmed.matches(Regex("^https?://[^\\s]+$"))) {
            return trimmed
        }

        // 尝试从文本中提取 URL
        val matchResult = URL_REGEX.find(trimmed)

        return if (matchResult != null) {
            // 找到 URL，返回提取的 URL
            matchResult.value
        } else {
            // 没有找到 URL，返回原始文本（可能是纯文本分享）
            trimmed
        }
    }

    /**
     * 判断文本是否包含 URL
     *
     * @param text 待检查的文本
     * @return 是否包含 URL
     */
    fun containsUrl(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        return URL_REGEX.containsMatchIn(text)
    }

    /**
     * 提取文本中的所有 URL
     *
     * 使用场景：
     * 当分享的文本包含多个 URL 时（较少见）
     *
     * @param text 原始文本
     * @return URL 列表
     */
    fun extractAll(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        return URL_REGEX.findAll(text).map { it.value }.toList()
    }
}
