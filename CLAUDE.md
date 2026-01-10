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

## 项目结构

```
app/src/main/java/top/yaotutu/droplink/
├── MainActivity.kt              # 主 Activity
└── ui/theme/                    # Compose 主题配置
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## 技术栈

- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose (BOM 2024.09.00)
- **主题**: Material3
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 36 (Android 14)
- **编译 SDK**: 36

## 关键特性

- **边缘到边缘显示**: 使用 `enableEdgeToEdge()` 实现全屏沉浸式体验
- **Compose 编译器**: 使用 Kotlin Compose Compiler 插件进行编译时优化
- **Java 兼容性**: Java 11

## 开发注意事项

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

规则：每当完成一个功能，请主动进行“Code Review”，指出代码是否符合 Android 性能优化标准（如避免不必要的重组、正确处理协程作用域等）。

规范：强调 Strings.xml 资源管理和 Modifier 链式调用的标准写法，拒绝硬编码。

## Rules
- 所有代码都必须有详细的中文注释