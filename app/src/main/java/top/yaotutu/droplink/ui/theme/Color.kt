package top.yaotutu.droplink.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// 🎨 Droplink 现代配色方案
// 基于 SaaS/云服务最佳实践设计
// 风格：Glassmorphism + Professional
// ============================================

// === 主色调 - 信任蓝（Trust Blue） ===
// 用于主要按钮、重点元素、品牌识别
val Primary = Color(0xFF2563EB)      // 深信任蓝 - 主色
val PrimaryLight = Color(0xFF3B82F6) // 亮蓝 - 次要色
val PrimaryDark = Color(0xFF1E40AF)  // 深蓝 - 按压状态

// === 强调色 - 活力橙（Accent Orange） ===
// 用于 CTA 按钮、重要操作、吸引注意力
val Accent = Color(0xFFF97316)       // 橙色 - 行动号召
val AccentLight = Color(0xFFFB923C)  // 浅橙 - 悬停状态

// === 背景色系（Light Mode） ===
val BackgroundLight = Color(0xFFF8FAFC)      // 浅灰白 - 主背景
val SurfaceLight = Color(0xFFFFFFFF)         // 纯白 - 卡片表面
val SurfaceVariantLight = Color(0xFFF1F5F9)  // 浅灰 - 次要表面

// === 文本色系（Light Mode） ===
val TextPrimaryLight = Color(0xFF1E293B)     // 深灰蓝 - 主要文本
val TextSecondaryLight = Color(0xFF475569)   // 中灰 - 次要文本
val TextTertiaryLight = Color(0xFF94A3B8)    // 浅灰 - 提示文本

// === 边框与分隔线（Light Mode） ===
val BorderLight = Color(0xFFE2E8F0)          // 边框灰
val DividerLight = Color(0xFFCBD5E1)         // 分隔线

// === 语义色 ===
val Success = Color(0xFF10B981)      // 成功绿
val Warning = Color(0xFFF59E0B)      // 警告黄
val Error = Color(0xFFEF4444)        // 错误红
val Info = Color(0xFF3B82F6)         // 信息蓝

// === 深色模式配色 ===
val BackgroundDark = Color(0xFF0F172A)       // 深蓝黑 - 主背景
val SurfaceDark = Color(0xFF1E293B)          // 深灰蓝 - 卡片表面
val SurfaceVariantDark = Color(0xFF334155)   // 中灰蓝 - 次要表面

val TextPrimaryDark = Color(0xFFF1F5F9)      // 浅灰白 - 主要文本
val TextSecondaryDark = Color(0xFFCBD5E1)    // 中灰白 - 次要文本
val TextTertiaryDark = Color(0xFF94A3B8)     // 深灰白 - 提示文本

val BorderDark = Color(0xFF334155)           // 深色边框
val DividerDark = Color(0xFF475569)          // 深色分隔线

// === 玻璃态效果色（Glassmorphism） ===
// 用于创建半透明的玻璃效果卡片
val GlassLight = Color(0xCCFFFFFF)           // 80% 白色 + 透明度
val GlassDark = Color(0x99334155)            // 60% 深灰蓝 + 透明度

// ============================================
// 🎨 旧版兼容（已废弃，保留以防编译错误）
// ============================================
@Deprecated("使用新的配色方案", ReplaceWith("Primary"))
val Purple80 = Color(0xFFD0BCFF)
@Deprecated("使用新的配色方案", ReplaceWith("PrimaryLight"))
val PurpleGrey80 = Color(0xFFCCC2DC)
@Deprecated("使用新的配色方案", ReplaceWith("Accent"))
val Pink80 = Color(0xFFEFB8C8)

@Deprecated("使用新的配色方案", ReplaceWith("Primary"))
val Purple40 = Color(0xFF6650a4)
@Deprecated("使用新的配色方案", ReplaceWith("PrimaryDark"))
val PurpleGrey40 = Color(0xFF625b71)
@Deprecated("使用新的配色方案", ReplaceWith("Accent"))
val Pink40 = Color(0xFF7D5260)