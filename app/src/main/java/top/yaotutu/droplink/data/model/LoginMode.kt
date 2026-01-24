package top.yaotutu.droplink.data.model

/**
 * 登录模式枚举
 *
 * React 对标：
 * - 类似于 TypeScript enum LoginMode { QrCode }
 * - 用于区分不同的登录认证方式
 *
 * 使用场景：
 * - UI 层根据 loginMode 条件渲染不同的表单
 * - ViewModel 层根据 loginMode 调用不同的认证逻辑
 */
enum class LoginMode {
    /**
     * 二维码登录模式（扫描配置二维码）
     *
     * 流程：扫描二维码 → 解析 JSON → 验证数据 → 调用 Gotify API 验证 → 保存配置
     *
     * React 对标：
     * - 类似于 OAuth 扫码登录（微信、GitHub）
     */
    QR_CODE
}
