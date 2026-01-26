# Droplink Android

<div align="center">

一个基于 Jetpack Compose 的现代化 Android 应用，支持快速分享链接和消息到云端。

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-2024.09.00-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

## ✨ 特性

- 🚀 **快速分享**：从任何应用分享链接/文本到 Droplink，支持后台无 UI 模式
- 📱 **现代 UI**：100% Jetpack Compose + Material 3 设计
- 🔐 **邮箱验证码登录**：无需密码，安全便捷
- 💬 **消息管理**：查看和管理所有分享的内容
- 🌙 **Material You**：支持动态主题色
- 📡 **离线支持**：本地缓存用户会话

## 📸 截图

> TODO: 添加应用截图

## 🏗️ 技术栈

### 核心技术
- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose (BOM 2024.09.00)
- **架构**: MVVM (Model-View-ViewModel)
- **异步处理**: Kotlin Coroutines + Flow

### 主要依赖
- **网络**: Retrofit 2.9.0 + OkHttp 4.12.0
- **序列化**: Kotlinx Serialization 1.6.0
- **导航**: Compose Navigation 2.7.3
- **主题**: Material3

### 开发工具
- **构建工具**: Gradle 8.7 + Kotlin DSL
- **依赖管理**: Gradle Version Catalog
- **CI/CD**: GitHub Actions

## 📋 系统要求

- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 36 (Android 14)
- **编译 SDK**: 36

## 🚀 快速开始

### 克隆项目

```bash
git clone https://github.com/yourusername/droplink-android.git
cd droplink-android
```

### 构建项目

```bash
# 清理构建产物
./gradlew clean

# 构建 Debug 版本
./gradlew assembleDebug

# 构建 Release 版本
./gradlew assembleRelease
```

### 运行应用

```bash
# 安装到连接的设备
./gradlew installDebug

# 启动应用
adb shell am start -n top.yaotutu.droplink/.MainActivity
```

### 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行单元测试
./gradlew testDebugUnitTest

# 运行 Android 设备测试
./gradlew connectedDebugAndroidTest
```

## 🏛️ 架构设计

### MVVM 三层架构

```
UI Layer (View)                    ← @Composable 函数，纯展示，无业务逻辑
    ↓ StateFlow 订阅
Presentation Layer (ViewModel)     ← 状态管理 + 业务编排 + viewModelScope
    ↓ Repository 接口调用
Data Layer (Repository)            ← API 调用 + 数据转换 + 错误处理
    ↓ Retrofit
Network Layer                      ← HTTP 请求 + 拦截器 + 错误处理
```

### 项目结构

```
app/src/main/java/top/yaotutu/droplink/
├── MainActivity.kt                # 应用入口（支持双模式启动）
├── ui/                            # UI 层
│   ├── navigation/                # 导航配置
│   ├── login/                     # 登录模块
│   ├── messages/                  # 消息列表模块
│   ├── share/                     # 分享模块
│   ├── main/                      # 主容器
│   ├── profile/                   # 用户信息
│   ├── settings/                  # 设置
│   └── theme/                     # Material 3 主题
├── data/                          # 数据层
│   ├── repository/                # 仓库模式
│   ├── network/                   # 网络层
│   ├── dto/                       # 数据传输对象
│   └── model/                     # 业务模型
└── util/                          # 工具类
```

## 🔑 核心功能

### 1. 验证码登录

- 邮箱格式验证
- 60 秒倒计时防重复发送
- 自动保存用户会话和 Token

### 2. 后台分享模式

从其他应用分享内容到 Droplink 时：
- 自动隐藏到后台（无 UI 干扰）
- 直接发送到服务器
- 显示通知反馈结果
- 自动关闭 Activity

### 3. 消息列表

- 支持下拉刷新
- 分页加载更多
- 兼容两种消息格式（Droplink + 普通 Gotify）
- 智能时间格式化

## 🔧 配置

### 服务器地址

在 `util/AppSettings.kt` 中配置：

```kotlin
object AppSettings {
    private const val DEFAULT_API_BASE_URL = "http://111.228.1.24:3600"
    private const val DEFAULT_GOTIFY_BASE_URL = "http://111.228.1.24:2345"
}
```

### 签名配置

Release 版本需要配置签名：

1. 将密钥库文件放在项目根目录：`droplink-release.keystore`
2. 或在 GitHub Actions 中配置 Secrets：
   - `KEYSTORE_FILE`
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

## 📦 依赖管理

项目使用 Gradle Version Catalog 管理依赖，配置文件：`gradle/libs.versions.toml`

添加新依赖：

```toml
# libs.versions.toml
[versions]
retrofit = "2.9.0"

[libraries]
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.retrofit.core)
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发规范

1. **国际化**：禁止硬编码用户可见文本，必须使用 `strings.xml`
2. **架构**：严格遵循 MVVM，UI 与逻辑分离
3. **现代技术**：使用 Kotlin + Compose，拒绝 XML 布局
4. **代码注释**：所有代码必须有详细的中文注释

详细开发指南请查看 [CLAUDE.md](CLAUDE.md)

## 📄 许可证

[MIT License](LICENSE)

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Gotify](https://gotify.net/)
- [Retrofit](https://square.github.io/retrofit/)

---

<div align="center">
Made with ❤️ by Droplink Team
</div>
