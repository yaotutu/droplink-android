package top.yaotutu.droplink.data.model

/**
 * 登录模式枚举
 *
 * React 对标：
 * - 类似于 TypeScript enum LoginMode { Official, SelfHosted, QrCode }
 * - 用于区分不同的登录认证方式
 *
 * 使用场景：
 * - UI 层根据 loginMode 条件渲染不同的表单
 * - ViewModel 层根据 loginMode 调用不同的认证逻辑
 */
enum class LoginMode {
    /**
     * 官方服务器模式（邮箱验证码登录）
     *
     * 流程：用户输入邮箱 → 发送验证码 → 输入验证码 → 后端验证并返回 tokens
     */
    OFFICIAL,

    /**
     * 自建服务器模式（Gotify 直连）
     *
     * 流程：用户提供 Gotify 地址 + appToken + clientToken → 调用 Gotify API 验证
     */
    SELF_HOSTED,

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
