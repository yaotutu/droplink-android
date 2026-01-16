# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Jetpack Compose 的 Android 应用,使用现代 Android 开发最佳实践。

## 构建命令

```bash
# 构建项目
./gradlew build

# 清理构建产物
./gradlew clean

# 运行所有测试
./gradlew test

# 运行单元测试
./gradlew testDebugUnitTest

# 运行 Android 设备测试
./gradlew connectedDebugAndroidTest

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 安装 Debug 版本到连接的设备
./gradlew installDebug

# 运行应用
./gradlew installDebug && adb shell am start -n top.yaotutu.droplink/.MainActivity
```

## 依赖管理

项目使用 **Gradle Version Catalog** (`gradle/libs.versions.toml`) 进行依赖版本管理。

添加新依赖时:
1. 在 `gradle/libs.versions.toml` 的 `[versions]` 部分添加版本号
2. 在 `[libraries]` 部分定义库
3. 在 `app/build.gradle.kts` 中使用 `libs.alias.name` 引用

示例:
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

## 架构设计

### MVVM 三层架构

**严格分离原则**：UI (View) 与逻辑 (ViewModel) 必须物理分文件存放

```
UI Layer (View)                    ← @Composable 函数，纯展示，无业务逻辑
    ↓ StateFlow 订阅
Presentation Layer (ViewModel)     ← 状态管理 + 业务编排 + viewModelScope
    ↓ Repository 接口调用
Data Layer (Repository)            ← API 调用 + 数据转换 + 错误处理
    ↓ Retrofit
Network Layer                      ← HTTP 请求 + 拦截器 + 错误处理
```

**React 概念对标**：
- `@Composable` ≈ JSX 组件（声明式 UI）
- `ViewModel` ≈ Custom Hooks + Zustand Store（状态管理）
- `StateFlow` ≈ useState + useEffect（响应式状态）
- `viewModelScope.launch` ≈ async/await（异步操作）
- `LaunchedEffect` ≈ useEffect（副作用）

### 项目结构

```
app/src/main/java/top/yaotutu/droplink/
├── MainActivity.kt                    # 应用入口（支持双模式启动：UI 模式 + 后台分享模式）
├── ui/                                # UI 层（100% Jetpack Compose）
│   ├── navigation/
│   │   ├── AppNavGraph.kt            # 导航图（类似 React Router）
│   │   └── NavRoutes.kt              # 路由常量定义
│   ├── login/
│   │   ├── LoginScreen.kt            # 登录 UI（邮箱 + 验证码）
│   │   ├── LoginViewModel.kt         # 登录逻辑（验证、倒计时、网络请求）
│   │   ├── LoginUiState.kt           # UI 状态数据类
│   │   └── LoginViewModelFactory.kt  # 工厂模式注入依赖
│   ├── messages/
│   │   ├── MessageScreen.kt          # 消息列表 UI（LazyColumn + 下拉刷新）
│   │   ├── MessageViewModel.kt       # 复杂数据转换逻辑（兼容两种消息格式）
│   │   ├── MessageUiState.kt         # 密封类状态（Loading, Success, Error...）
│   │   └── MessageViewModelFactory.kt
│   ├── share/
│   │   ├── ShareViewModel.kt         # 分享逻辑（支持 UI 模式 + 后台模式）
│   │   ├── ShareUiState.kt
│   │   └── ShareViewModelFactory.kt
│   ├── main/MainScreen.kt            # 主容器（抽屉导航 + 内容区域）
│   ├── profile/ProfileScreen.kt      # 用户信息页面
│   ├── settings/SettingsScreen.kt    # 设置页面
│   └── theme/                         # Material 3 主题配置
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
├── data/                              # 数据层
│   ├── repository/
│   │   ├── AuthRepository.kt         # 认证接口（依赖倒置）
│   │   ├── AuthRepositoryImpl.kt     # 认证实现（验证、验证码、用户验证）
│   │   └── GotifyRepository.kt       # Gotify 消息 API 封装
│   ├── network/
│   │   ├── RetrofitClient.kt         # Retrofit 单例工厂（双服务器配置）
│   │   ├── AuthApiService.kt         # 认证 API 接口
│   │   ├── GotifyApiService.kt       # Gotify API 接口
│   │   ├── TokenInterceptor.kt       # Token 自动注入拦截器
│   │   ├── LoggingInterceptor.kt     # 日志拦截器（仅 Debug）
│   │   └── ErrorHandler.kt           # 统一错误处理（HTTP 状态码转用户友好消息）
│   ├── dto/                           # 数据传输对象
│   │   ├── request/                  # 请求 DTO
│   │   │   ├── SendCodeRequest.kt
│   │   │   ├── VerifyRequest.kt
│   │   │   ├── GotifyMessageRequest.kt
│   │   │   ├── ContentData.kt
│   │   │   ├── ActionData.kt
│   │   │   └── DroplinkData.kt
│   │   └── response/                 # 响应 DTO
│   │       ├── AuthResponse.kt
│   │       ├── GotifyMessageResponse.kt
│   │       ├── GotifyGetMessagesResponse.kt
│   │       ├── GotifyMessageDetail.kt
│   │       └── ErrorResponse.kt
│   └── model/                         # 业务模型
│       ├── User.kt
│       ├── SharedData.kt
│       ├── MessageItem.kt
│       └── ActionItem.kt
└── util/                              # 工具类
    ├── SessionManager.kt             # 用户会话管理（单例）
    ├── TokenManager.kt               # Token 管理（单例）
    ├── NotificationHelper.kt         # 通知助手
    ├── AppSettings.kt                # 应用配置（动态服务器地址）
    └── DateFormatter.kt              # 智能时间格式化
```

## 技术栈

- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose (BOM 2024.09.00)
- **主题**: Material3
- **网络**: Retrofit 2.9.0 + OkHttp 4.12.0
- **序列化**: Kotlinx Serialization 1.6.0
- **导航**: Compose Navigation 2.7.3
- **状态管理**: StateFlow + ViewModel
- **异步处理**: Coroutines (协程) + viewModelScope
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 36 (Android 14)
- **编译 SDK**: 36

## 关键业务特性

### 1. 验证码登录流程

**完整流程** (`LoginViewModel` + `AuthRepository`)：
1. 前端验证邮箱格式（正则表达式）
2. 动态切换服务器地址（`AppSettings.setApiBaseUrl()` + `RetrofitClient.resetInstance()`）
3. 发送验证码到邮箱（`authRepository.sendVerificationCode(email)`）
4. 启动 60 秒倒计时（防止重复发送）
5. 验证邮箱 + 验证码（`authRepository.verify(email, code)`）
6. 保存 Token 到本地（`TokenManager.saveTokens(appToken, clientToken)`）
7. 保存用户会话（`SessionManager.saveUserSession(user)`）
8. 导航到主页面

**关键代码位置**：
- 验证逻辑：`data/repository/AuthRepositoryImpl.kt:validateEmail()`, `validateVerificationCode()`
- 倒计时：`ui/login/LoginViewModel.kt:startCountdown()`
- 网络请求：`data/network/AuthApiService.kt`

### 2. 消息列表数据转换

**核心挑战**：兼容两种消息格式
- **Droplink 格式**：JSON 嵌套结构（`extras.droplink.content.value`）
- **普通 Gotify 格式**：简单文本消息

**数据转换函数** (`MessageViewModel.kt:convertToMessageItem()`)：
1. 检测是否为 Droplink 消息（`detail.extras?.droplink`）
2. 手动解析 JsonElement（支持对象 + 字符串两种格式）
3. 提取 content、actions、metadata
4. 过滤掉解析失败的消息（返回 `null`）
5. 智能时间格式化（今天→"10:30", 昨天→"昨天 10:30", 本周→"周一 10:30", 更早→"2024-01-08"）

**分页机制**：
- 使用 `since` 参数传递上次最后一条消息的 ID
- `hasMore` 判断是否还有更多数据（`response.paging.next != null`）
- 追加而不是替换（`currentMessages + newMessages`）

**关键代码位置**：
- 数据转换：`ui/messages/MessageViewModel.kt:convertToMessageItem()`
- 分页加载：`ui/messages/MessageViewModel.kt:loadMoreMessages()`
- 时间格式化：`util/DateFormatter.kt:formatMessageDate()`

### 3. 后台分享功能（双模式启动）

**MainActivity 支持两种启动模式**：
1. **UI 模式**：用户点击应用图标，显示正常界面
2. **后台模式**：从其他应用分享 URL 到 Droplink，无 UI，直接发送到服务器并显示通知

**后台模式流程** (`MainActivity.kt:handleBackgroundShare()`)：
1. 检测 Intent 是否为分享（`intent.action == Intent.ACTION_SEND`）
2. 调用 `moveTaskToBack(true)` 隐藏到后台
3. 提取分享数据（URL、文本、文件）
4. 调用 `ShareViewModel.processSharedDataInBackground()`
5. 使用 `withTimeout(30_000)` 设置 30 秒超时
6. 等待状态变为 Success 或 Error（`uiState.filter().first()`）
7. 显示通知（成功或失败）
8. 自动关闭 Activity（`finish()`）

**关键代码位置**：
- 双模式检测：`MainActivity.kt:onCreate()` 中的 `isShareIntent()`
- 后台处理：`MainActivity.kt:handleBackgroundShare()`
- 分享逻辑：`ui/share/ShareViewModel.kt:processSharedDataInBackground()`
- 通知：`util/NotificationHelper.kt:showShareSuccessNotification()`

### 4. 网络层设计

**RetrofitClient 单例工厂**（双服务器配置）：
- **认证服务器**：`http://192.168.123.100:8080`（发送验证码、验证用户）
- **Gotify 服务器**：`http://111.228.1.24:18080`（发送/接收消息）

**拦截器链**：
1. **TokenInterceptor**：自动注入 `X-Gotify-Key` header（跳过认证端点）
2. **LoggingInterceptor**：仅 Debug 模式记录请求/响应日志

**统一错误处理** (`ErrorHandler.kt`)：
- `UnknownHostException` → "无法连接到服务器，请检查网络连接"
- `SocketTimeoutException` → "连接超时，请稍后重试"
- `HttpException(401)` → "认证失败，请重新登录"
- `HttpException(403)` → "没有权限执行此操作"
- `HttpException(404)` → "请求的资源不存在"
- `HttpException(500)` → "服务器错误，请稍后重试"
- `SerializationException` → "数据格式错误，请联系管理员"

**关键代码位置**：
- Retrofit 配置：`data/network/RetrofitClient.kt`
- Token 注入：`data/network/TokenInterceptor.kt`
- 错误处理：`data/network/ErrorHandler.kt`

## 关键设计模式

### 单例模式（线程安全）
- `RetrofitClient`：双重检查锁（DCL）+ `@Volatile`
- `SessionManager`、`TokenManager`：懒加载单例 + `synchronized`

### 工厂模式（依赖注入）
- `LoginViewModelFactory`、`MessageViewModelFactory`、`ShareViewModelFactory`
- 在 Composable 中使用：`viewModel(factory = MessageViewModelFactory(LocalContext.current))`

### 策略模式
- `ErrorHandler`：根据异常类型采用不同策略（网络错误、超时、HTTP 错误、解析错误）

### 仓库模式（Repository Pattern）
- `AuthRepository` 接口 + `AuthRepositoryImpl` 实现（依赖倒置）
- ViewModel 依赖接口，不依赖具体实现

## CI/CD 自动化

### GitHub Actions 工作流

**android-build.yml** — 持续集成：
- 触发条件：Push 到 main、创建 tag `v*`、Pull Request
- 流程：`./gradlew test` → `./gradlew assembleDebug` → `./gradlew assembleRelease`
- 上传 APK 到 GitHub Artifacts

**android-release.yml** — 自动发布：
- 触发条件：Push 到 main/dev、手动触发
- 需要配置 GitHub Secrets：
  - `KEYSTORE_FILE`：Base64 编码的密钥库文件
  - `KEYSTORE_PASSWORD`：密钥库密码
  - `KEY_ALIAS`：密钥别名
  - `KEY_PASSWORD`：密钥密码

**本地签名配置** (`app/build.gradle.kts`)：
- GitHub Actions：从环境变量读取 Base64 编码的密钥文件
- 本地开发：从项目根目录读取 `droplink-release.keystore`

## 状态管理模式

### UI 状态定义

**数据类状态** (`LoginUiState`)：
```kotlin
data class LoginUiState(
    val email: String = "",
    val verificationCode: String = "",
    val isLoading: Boolean = false,
    val countdown: Int = 0,
    val errorMessage: String? = null
)
```

**密封类状态** (`MessageUiState`)：
```kotlin
sealed class MessageUiState {
    object Idle : MessageUiState()
    object Loading : MessageUiState()
    data class Success(val messages: List<MessageItem>) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}
```

### 单向数据流

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

fun updateEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email)
}

// Composable
val uiState by viewModel.uiState.collectAsState()
TextField(
    value = uiState.email,
    onValueChange = { viewModel.updateEmail(it) }
)
```

**React 对标**：
```javascript
// React
const [email, setEmail] = useState("");

// Android
val email by viewModel.uiState.collectAsState()
viewModel.updateEmail(newValue)
```

## 开发注意事项

### 国际化规范

禁止硬编码任何用户可见的文本，必须使用 strings.xml 资源文件。

错误示例：
```kotlin
Text("欢迎来到 Droplink")  // 禁止
```

正确示例：
```kotlin
import androidx.compose.ui.res.stringResource
import top.yaotutu.droplink.R
Text(stringResource(R.string.login_welcome_title))
```

所有字符串必须在两个文件中定义：
- `app/src/main/res/values/strings.xml`（英文默认）
- `app/src/main/res/values-zh-rCN/strings.xml`（简体中文）

不同场景的使用方法：
- Composable：`stringResource(R.string.xxx)`
- ViewModel：`context.getString(R.string.xxx)`
- Activity：`getString(R.string.xxx)`

### Compose 开发
- 使用 `@Composable` 注解定义可组合函数
- 使用 `@Preview` 注解创建 UI 预览(需在 `debugImplementation` 中)
- 所有 UI 组件应遵循 Compose 最佳实践(无状态、可组合、可重用)

### 包名规范
- 应用包名: `top.yaotutu.droplink`
- 所有代码应放置在正确的包路径下

### 构建变体
- `debug`: 开发构建,包含调试信息
- `release`: 发布构建,默认未开启代码混淆

### 测试
- 单元测试位置: `app/src/test/`
- Android 测试位置: `app/src/androidTest/`
- 使用 JUnit 进行单元测试
- 使用 Espresso 和 Compose Testing 进行 UI 测试

## 常见任务

### 添加新依赖
1. 编辑 `gradle/libs.versions.toml`
2. 在 `app/build.gradle.kts` 的 `dependencies` 块中添加
3. 同步 Gradle

### 创建新的 Composable Screen
1. 在适当的包下创建新的 Kotlin 文件
2. 使用 `@Composable` 注解定义函数
3. 在 `MainActivity.kt` 或导航图中集成

### 修改应用主题
- 编辑 `app/src/main/java/top/yaotutu/droplink/ui/theme/` 下的文件
- `Color.kt`: 定义颜色方案
- `Type.kt`: 定义排版样式
- `Theme.kt`: 主题配置


Android 转型教练规则 (React To Android)
1. 概念对齐 (Concept Mapping)

规则：禁止从零解释基础编程概念。在讲解 Android 新概念时，必须强制对比 React 概念（例如：Compose 对标 JSX，ViewModel 对标 Custom Hooks/Store，LaunchedEffect 对标 useEffect）。

目标：利用已有的 Web 开发经验快速迁移知识，减少理解成本。

2. 架构至上 (Architecture First)

规则：代码必须严格遵循 官方最新 MVVM 架构。严禁在 Composable 函数中直接写复杂的业务逻辑或网络请求。

要求：UI (View) 与逻辑 (ViewModel) 必须物理分文件存放。必须解释为什么在 Android 中这种“分离”对于应对生命周期（Lifecycle）至关重要。

3. 现代技术栈 (Modern Stack Only)

规则：拒绝任何过时的技术方案（如 XML 布局、Java 语言、或传统的 findViewById）。

标准库：必须使用 Kotlin + Jetpack Compose + Coroutines (协程) + Hilt (依赖注入) + Retrofit (网络)。

4. 深度原理启发 (Mechanism Deep-Dive)

规则：不要只做代码生成器。在给出解决方案时，必须解释 Android 底层的“世界观”，特别是：主线程隔离 (Main Thread)、状态丢失与恢复 (State Restoration) 和 重组机制 (Recomposition)。

要求：经常性地提问以确认我是否理解了 Android 系统的“宿主”特性。

5. 最佳实践评审 (Best Practice Review)

规则：每当完成一个功能，请主动进行"Code Review"，指出代码是否符合 Android 性能优化标准（如避免不必要的重组、正确处理协程作用域等）。

## Rules

- 禁止硬编码任何用户可见的文本（中文字符串），必须使用 strings.xml 资源文件
- 所有字符串必须同时在 `values/strings.xml`（英文默认）和 `values-zh-rCN/strings.xml`（简体中文）中定义
- Composable 中使用 `stringResource(R.string.xxx)`，ViewModel 中使用 `context.getString(R.string.xxx)`
- 所有代码都必须有详细的中文注释