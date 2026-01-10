package top.yaotutu.droplink.ui.share

import top.yaotutu.droplink.data.model.SharedData

/**
 * 分享页面 UI 状态
 *
 * React 概念对标：
 * - 类似于 React 组件的 state
 * - 使用 sealed class 确保状态的类型安全（类似 TypeScript 的 discriminated unions）
 */
sealed class ShareUiState {
    /**
     * 空闲状态：没有接收到分享数据
     */
    object Idle : ShareUiState()

    /**
     * 加载状态：正在解析分享数据
     */
    object Loading : ShareUiState()

    /**
     * 成功状态：已接收并解析分享数据
     * @param sharedData 解析后的分享数据
     */
    data class Success(val sharedData: SharedData) : ShareUiState()

    /**
     * 错误状态：解析失败或数据无效
     * @param message 错误信息
     */
    data class Error(val message: String) : ShareUiState()

    /**
     * 处理中状态：正在上传或处理分享的数据
     * @param progress 处理进度 0-100
     */
    data class Processing(val progress: Int = 0) : ShareUiState()

    /**
     * 完成状态：分享数据已成功处理
     */
    object Completed : ShareUiState()
}
