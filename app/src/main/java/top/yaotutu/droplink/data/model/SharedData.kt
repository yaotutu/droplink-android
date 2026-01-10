package top.yaotutu.droplink.data.model

import android.net.Uri

/**
 * 分享数据模型
 *
 * React 概念对标：
 * - 类似于 TypeScript 的 interface ShareData
 * - 封装从外部应用接收到的分享内容
 *
 * @property type 分享类型（文本/单个文件/多个文件）
 * @property text 文本内容（URL、纯文本等）
 * @property subject 分享主题/标题
 * @property fileUri 单个文件的 URI
 * @property fileUris 多个文件的 URI 列表
 * @property mimeType 文件的 MIME 类型（image/png, text/plain 等）
 */
data class SharedData(
    val type: ShareType,
    val text: String? = null,
    val subject: String? = null,
    val fileUri: Uri? = null,
    val fileUris: List<Uri>? = null,
    val mimeType: String? = null
) {
    /**
     * 判断是否包含有效数据
     */
    fun isValid(): Boolean {
        return when (type) {
            ShareType.TEXT -> !text.isNullOrBlank()
            ShareType.SINGLE_FILE -> fileUri != null
            ShareType.MULTIPLE_FILES -> !fileUris.isNullOrEmpty()
            ShareType.UNKNOWN -> false
        }
    }

    /**
     * 获取用户友好的描述
     */
    fun getDescription(): String {
        return when (type) {
            ShareType.TEXT -> "文本: ${text?.take(50) ?: ""}"
            ShareType.SINGLE_FILE -> "文件: ${mimeType ?: "未知类型"}"
            ShareType.MULTIPLE_FILES -> "多个文件: ${fileUris?.size ?: 0} 个"
            ShareType.UNKNOWN -> "未知类型"
        }
    }
}

/**
 * 分享类型枚举
 *
 * TEXT: 纯文本或 URL
 * SINGLE_FILE: 单个文件（图片、视频等）
 * MULTIPLE_FILES: 多个文件
 * UNKNOWN: 无法识别的类型
 */
enum class ShareType {
    TEXT,
    SINGLE_FILE,
    MULTIPLE_FILES,
    UNKNOWN
}
