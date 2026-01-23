# Droplink 国际化改造方案

> **React 开发者快速理解**：这个方案类似于在 React 项目中集成 `react-i18next`，将所有硬编码文本提取到资源文件中。

---

## 📚 目录

1. [技术原理](#技术原理)
2. [目录结构](#目录结构)
3. [已完成的工作](#已完成的工作)
4. [待改造的文件](#待改造的文件)
5. [改造步骤](#改造步骤)
6. [代码示例](#代码示例)
7. [测试验证](#测试验证)
8. [常见问题](#常见问题)

---

## 🧠 技术原理

### React vs Android 国际化对比

| React (i18next) | Android (Resources) | 说明 |
|-----------------|---------------------|------|
| `i18next.init()` | `LanguageManager.init()` | 初始化 |
| `t('login.title')` | `stringResource(R.string.login_title)` | 获取文本 |
| `en-US.json` | `values/strings.xml` | 英文资源 |
| `zh-CN.json` | `values-zh-rCN/strings.xml` | 中文资源 |
| `changeLanguage('zh-CN')` | `LanguageManager.setLocale(locale)` | 切换语言 |

### Android 国际化机制

```
系统启动时
  ↓
检查 Configuration.locale
  ↓
自动加载对应 values 目录
  ├─ values-zh-rCN/ → 中文用户
  ├─ values-en/ → 英文用户
  └─ values/ → 兜底（默认）
  ↓
应用显示对应语言
```

**关键特性**：
- **自动切换**：无需手动判断 `if (locale === 'zh')`
- **资源限定符优先级**：`values-zh-rCN` > `values-zh` > `values`
- **Compose 适配**：使用 `stringResource()` 自动响应语言变化

---

## 📁 目录结构

### 国际化后的资源目录

```
app/src/main/res/
├── values/                    # 英文（默认语言）
│   └── strings.xml            # ✅ 已完成（~143 行）
├── values-zh-rCN/             # 简体中文
│   └── strings.xml            # ✅ 已完成（~143 行）
└── values-night/              # 可选：夜间模式特定字符串
    └── strings.xml            # 待创建（可选）
```

### 代码文件分布

```
app/src/main/java/top/yaotutu/droplink/
├── util/
│   └── LanguageManager.kt     # ✅ 已完成（语言切换工具类）
├── ui/
│   ├── login/
│   │   └── LoginScreen.kt     # ⏳ 待改造（15+ 处硬编码）
│   ├── messages/
│   │   └── MessageScreen.kt   # ⏳ 待改造（5+ 处硬编码）
│   └── share/
│       └── ShareViewModel.kt  # ⏳ 待改造（10+ 处硬编码）
└── util/
    └── NotificationHelper.kt  # ⏳ 待改造（8+ 处硬编码）
```

---

## ✅ 已完成的工作

### 1. 创建字符串资源文件

#### 英文资源（`values/strings.xml`）
- ✅ 143 行字符串定义
- ✅ 覆盖所有页面（登录、主页、消息、分享、设置）
- ✅ 参数化字符串支持（`%s`、`%d`）
- ✅ 分组注释（按功能模块）

#### 中文资源（`values-zh-rCN/strings.xml`）
- ✅ 完整的中文翻译
- ✅ 与英文版一一对应
- ✅ 保留参数化字符串格式

### 2. 创建语言管理工具

#### LanguageManager.kt
- ✅ 动态语言切换
- ✅ 持久化用户偏好
- ✅ 支持跟随系统语言
- ✅ 兼容 Android 7.0+ 和 Android 13+

**核心 API**：
```kotlin
// 初始化（在 Application.onCreate() 中调用）
LanguageManager.init(context)

// 切换语言
LanguageManager.setLocale(context, Locale.SIMPLIFIED_CHINESE)

// 获取当前语言
val currentLocale = LanguageManager.getLocale(context)
```

---

## 📋 待改造的文件

### 优先级分类

| 优先级 | 文件 | 硬编码数量 | 改造难度 |
|--------|------|-----------|---------|
| **P0（高）** | `NotificationHelper.kt` | 8 处 | ⭐ 简单 |
| **P0（高）** | `ShareViewModel.kt` | 10 处 | ⭐ 简单 |
| **P1（中）** | `LoginScreen.kt` | 15 处 | ⭐⭐ 中等 |
| **P1（中）** | `MessageScreen.kt` | 5 处 | ⭐⭐ 中等 |
| **P2（低）** | 其他 UI 文件 | 少量 | ⭐ 简单 |

### 详细问题清单

#### 1. NotificationHelper.kt（8 处）

**当前代码**：
```kotlin
private const val CHANNEL_NAME = "分享通知"  // ❌ 硬编码
.setContentTitle("分享成功")                  // ❌ 硬编码
.setContentText("已发送到 Gotify")            // ❌ 硬编码
```

**改造后**：
```kotlin
import top.yaotutu.droplink.R.string.*

private const val CHANNEL_NAME = "droplink_share_channel"  // ✅ 已在 strings.xml
.setContentTitle(context.getString(R.string.notification_share_success_title))
.setContentText(context.getString(R.string.notification_share_success_message))
```

#### 2. ShareViewModel.kt（10 处）

**当前代码**：
```kotlin
_uiState.value = ShareUiState.Error("未接收到分享数据")  // ❌ 硬编码
_uiState.value = ShareUiState.Error("分享数据无效")        // ❌ 硬编码
_uiState.value = ShareUiState.Error("解析失败: ${e.message}")  // ❌ 硬编码
```

**改造后**：
```kotlin
// 在 ViewModel 中需要使用 Context 获取字符串
private val context: Context  // 通过构造函数注入

_uiState.value = ShareUiState.Error(
    context.getString(R.string.share_error_no_data)
)
_uiState.value = ShareUiState.Error(
    context.getString(R.string.share_error_invalid_data)
)
_uiState.value = ShareUiState.Error(
    context.getString(R.string.share_error_parse_failed, e.message)
)
```

#### 3. LoginScreen.kt（15 处）

**当前代码**：
```kotlin
Text("服务器地址")  // ❌ 硬编码
Text("Gotify 服务器地址")  // ❌ 硬编码
Text("App Token")  // ❌ 硬编码
Text("Client Token")  // ❌ 硬编码
```

**改造后**：
```kotlin
import androidx.compose.ui.res.stringResource

Text(stringResource(R.string.login_server_address_label))
Text(stringResource(R.string.login_gotify_server_label))
Text(stringResource(R.string.login_app_token_label))
Text(stringResource(R.string.login_client_token_label))
```

#### 4. MessageScreen.kt（5 处）

**当前代码**：
```kotlin
Text(text = "暂无消息")  // ❌ 硬编码
Text(text = "重试")  // ❌ 硬编码
```

**改造后**：
```kotlin
Text(stringResource(R.string.message_empty_title))
Text(stringResource(R.string.message_retry))
```

---

## 🛠️ 改造步骤

### 阶段 1：改造 NotificationHelper.kt（P0 优先级）

**步骤**：
1. 在 `NotificationHelper` 类中添加 `context.getString()` 调用
2. 替换所有硬编码的中文字符串
3. 测试通知功能是否正常

**预计时间**：10 分钟

### 阶段 2：改造 ShareViewModel.kt（P0 优先级）

**步骤**：
1. 修改 `ShareViewModel` 构造函数，添加 `Context` 参数
2. 在 `ShareViewModelFactory` 中传递 `Context`
3. 替换所有硬编码的错误消息
4. 测试分享功能是否正常

**预计时间**：15 分钟

### 阶段 3：改造 LoginScreen.kt（P1 优先级）

**步骤**：
1. 添加 `import androidx.compose.ui.res.stringResource`
2. 替换所有硬编码的 UI 文本
3. 特别注意 `LoginModeTabs` 中的硬编码
4. 测试登录页面显示是否正常

**预计时间**：20 分钟

### 阶段 4：改造 MessageScreen.kt（P1 优先级）

**步骤**：
1. 添加 `import androidx.compose.ui.res.stringResource`
2. 替换 `EmptyMessageState` 和错误提示中的硬编码
3. 测试消息列表显示是否正常

**预计时间**：10 分钟

### 阶段 5：其他文件（P2 优先级）

**步骤**：
1. 使用 `grep` 搜索剩余的硬编码中文字符串
2. 逐个文件替换
3. 全面回归测试

**预计时间**：30 分钟

---

## 💻 代码示例

### 示例 1：在 Composable 中使用 stringResource（推荐）

**❌ 改造前**：
```kotlin
@Composable
fun LoginForm() {
    Text(text = "邮箱")  // 硬编码
}
```

**✅ 改造后**：
```kotlin
import androidx.compose.ui.res.stringResource
import top.yaotutu.droplink.R

@Composable
fun LoginForm() {
    Text(text = stringResource(R.string.login_email_label))
}
```

**React 对标**：
```javascript
// React (i18next)
import { t } from 'i18next'

function LoginForm() {
    return <div>{t('login.email_label')}</div>
}
```

---

### 示例 2：在 ViewModel 中使用 Context.getString()

**❌ 改造前**：
```kotlin
class ShareViewModel(
    private val gotifyRepository: GotifyRepository
) : ViewModel() {
    fun handleError() {
        _uiState.value = ShareUiState.Error("发送失败")  // 硬编码
    }
}
```

**✅ 改造后**：
```kotlin
import android.content.Context
import top.yaotutu.droplink.R

class ShareViewModel(
    private val context: Context,  // 添加 Context 参数
    private val gotifyRepository: GotifyRepository
) : ViewModel() {
    fun handleError() {
        _uiState.value = ShareUiState.Error(
            context.getString(R.string.share_error_send_failed)
        )
    }
}

// 修改 Factory
class ShareViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class)) {
            val repository = GotifyRepository(context)
            @Suppress("UNCHECKED_CAST")
            return ShareViewModel(context, repository) as T  // 传递 Context
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

**React 对标**：
```javascript
// React (Custom Hook + i18next)
import { useTranslation } from 'react-i18next'

function useShare() {
    const { t } = useTranslation()

    const handleError = () => {
        setState({ error: t('share.error_send_failed') })
    }

    return { handleError }
}
```

---

### 示例 3：参数化字符串（动态内容）

**strings.xml 定义**：
```xml
<string name="share_error_parse_failed">Parse failed: %s</string>
```

**Kotlin 代码**：
```kotlin
// Composable 中
Text(
    stringResource(
        R.string.share_error_parse_failed,
        errorMessage
    )
)

// ViewModel 中
context.getString(
    R.string.share_error_parse_failed,
    e.message
)
```

**React 对标**：
```javascript
// React (i18next)
t('share.error_parse_failed', { error: errorMessage })

// en-US.json
{
  "share": {
    "error_parse_failed": "Parse failed: {{error}}"
  }
}
```

---

### 示例 4：在 NotificationHelper 中使用资源

**❌ 改造前**：
```kotlin
private const val CHANNEL_NAME = "分享通知"  // 硬编码

.setContentTitle("分享成功")
.setContentText("已发送到 Gotify")
```

**✅ 改造后**：
```kotlin
import top.yaotutu.droplink.R

fun createNotificationChannel() {
    val channel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.notification_channel_share),  // ✅ 使用资源
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = context.getString(R.string.notification_channel_share_description)
        // ...
    }
}

fun showShareSuccessNotification(url: String) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
        .setContentTitle(context.getString(R.string.notification_share_success_title))  // ✅
        .setContentText(context.getString(R.string.notification_share_success_message))  // ✅
        .build()
}
```

---

## 🧪 测试验证

### 方法 1：更改系统语言（推荐）

**步骤**：
1. 打开手机设置 → 系统 → 语言和输入法 → 语言
2. 切换到"English（United States）"
3. 重新打开 Droplink 应用
4. 检查所有页面是否显示为英文

**预期结果**：
- ✅ 登录页面：Welcome to Droplink
- ✅ 消息列表：No Messages
- ✅ 分享提示：Share Successful

### 方法 2：使用 LanguageManager 切换语言（高级）

**步骤**：
1. 在 `MainActivity.onCreate()` 中添加测试按钮
2. 调用 `LanguageManager.setLocale(context, Locale.SIMPLIFIED_CHINESE)`
3. 调用 `recreate()` 重新创建 Activity
4. 检查语言是否立即切换

**示例代码**：
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 测试：强制切换到英文
        LanguageManager.setLocale(this, Locale.ENGLISH)

        setContent {
            DroplinkTheme {
                // ...
            }
        }
    }
}
```

### 方法 3：使用 ADB 命令测试

```bash
# 切换到英文
adb shell "am start -n android.settings/.Settings\$LanguageSettingsActivity"

# 或者直接更改系统语言
adb shell "setprop persist.sys.locale en-US"
adb shell "setprop ctl.restart zygote"
```

---

## ❓ 常见问题

### Q1：为什么 Android 使用 `values-zh-rCN` 而不是 `values-zh-CN`？

**答**：Android 使用 ISO 639-1 语言代码 + ISO 3166-1 国家代码，格式为 `r` + 国家代码（大写）。
- `zh-rCN` → 简体中文（中国）
- `zh-rTW` → 繁体中文（台湾）
- `en-rUS` → 英文（美国）

### Q2：Compose 重组时语言会自动更新吗？

**答**：会的！`stringResource()` 是一个 `@Composable` 函数，会自动订阅语言变化并触发重组。

```kotlin
@Composable
fun MyScreen() {
    // 当语言变化时，这里会自动重新读取新的字符串
    val text = stringResource(R.string.login_welcome_title)

    Text(text)
}
```

### Q3：为什么 ViewModel 需要 Context 参数？

**答**：因为 `getString()` 是 `Context` 的方法。ViewModel 本身不持有 Context 引用（避免内存泄漏），所以需要通过构造函数传递。

**最佳实践**：
- 使用 `Application Context`（而非 `Activity Context`）避免内存泄漏
- 在 Factory 中传递 Context：`ViewModelFactory(application)`

### Q4：如何支持更多语言（如日语、韩语）？

**答**：只需创建对应的 `values-<language>` 目录和 `strings.xml` 文件：
```
res/
├── values-ja/   # 日语
│   └── strings.xml
├── values-ko/   # 韩语
│   └── strings.xml
└── values-fr/   # 法语
    └── strings.xml
```

Android 会自动根据系统语言加载对应资源。

### Q5：如何在设置页面添加语言切换？

**答**：参考以下代码：

```kotlin
@Composable
fun LanguageSettingScreen() {
    val currentLocale = LanguageManager.getLocale(LocalContext.current)
    val context = LocalContext.current

    Column {
        Text(text = "Language")

        // 语言选项
        SupportedLanguage.values().forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        LanguageManager.setLocale(context, language.locale)
                        (context as? Activity)?.recreate()  // 重新创建 Activity
                    }
            ) {
                RadioButton(
                    selected = currentLocale == language.locale,
                    onClick = {
                        LanguageManager.setLocale(context, language.locale)
                        (context as? Activity)?.recreate()
                    }
                )
                Text(text = language.displayName)
            }
        }
    }
}
```

---

## 📊 改造进度追踪

| 文件 | 状态 | 完成时间 |
|------|------|---------|
| `values/strings.xml` | ✅ 完成 | 2025-01-16 |
| `values-zh-rCN/strings.xml` | ✅ 完成 | 2025-01-16 |
| `LanguageManager.kt` | ✅ 完成 | 2025-01-16 |
| `NotificationHelper.kt` | ⏳ 待改造 | - |
| `ShareViewModel.kt` | ⏳ 待改造 | - |
| `LoginScreen.kt` | ⏳ 待改造 | - |
| `MessageScreen.kt` | ⏳ 待改造 | - |
| 其他 UI 文件 | ⏳ 待改造 | - |

---

## 🚀 下一步行动

1. ✅ **已完成**：创建字符串资源文件（英文 + 中文）
2. ✅ **已完成**：创建 LanguageManager 工具类
3. ⏳ **待进行**：改造 `NotificationHelper.kt`（P0 优先级）
4. ⏳ **待进行**：改造 `ShareViewModel.kt`（P0 优先级）
5. ⏳ **待进行**：改造 `LoginScreen.kt`（P1 优先级）
6. ⏳ **待进行**：改造 `MessageScreen.kt`（P1 优先级）
7. ⏳ **待进行**：全面测试与验证
8. ⏳ **待进行**：添加设置页面的语言切换功能（可选）

---

## 📚 参考资料

- [Android 官方文档 - 本地化](https://developer.android.com/guide/topics/resources/localization)
- [Compose 国际化最佳实践](https://developer.android.com/jetpack/compose/layouts/basics?hl=zh-cn)
- [Material Design 3 - 国际化指南](https://m3.material.io/styles/typography/understanding-typo)

---

**最后更新**：2025-01-16
**维护者**：Claude Code
**版本**：v1.0
