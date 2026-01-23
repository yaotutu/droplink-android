import java.io.File
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)  // 新增：Kotlinx Serialization 插件
}

android {
    namespace = "top.yaotutu.droplink"
    compileSdk = 36

    defaultConfig {
        applicationId = "top.yaotutu.droplink"
        minSdk = 24
        targetSdk = 36
        // 版本号管理：优先从环境变量读取（CI/CD），否则使用默认值（本地开发）
        // GitHub Actions 会通过环境变量传递动态版本号
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0-local"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 配置：服务器地址（类似前端的 .env）
        buildConfigField("String", "AUTH_SERVER_URL", "\"http://111.228.1.24:3600\"")
        buildConfigField("String", "GOTIFY_SERVER_URL", "\"http://111.228.1.24:2345\"")
        buildConfigField("String", "TEST_VERIFICATION_CODE", "\"0000\"")
        buildConfigField("String", "API_KEY", "\"your_api_key_here\"")  // 保留原有的 API_KEY
    }

    // 签名配置（用于 Release 构建）
    signingConfigs {
        create("release") {
            // 从环境变量或 local.properties 读取签名信息
            storeFile = System.getenv("KEYSTORE_FILE")?.let { base64String ->
                // GitHub Actions 场景：从 Base64 解码密钥文件
                val keystoreFile = File(layout.buildDirectory.get().asFile, "release.keystore")
                keystoreFile.parentFile.mkdirs()
                keystoreFile.writeBytes(Base64.getMimeDecoder().decode(base64String))
                keystoreFile
            } ?: run {
                // 本地开发场景：从项目目录读取（如果存在）
                val localKeystore = rootProject.file("droplink-release.keystore")
                if (localKeystore.exists()) localKeystore else null
            }

            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            // Debug 也使用 release 签名（确保 CI/CD 构建的 APK 可以互相覆盖安装）
            if (System.getenv("KEYSTORE_PASSWORD") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 应用签名配置（仅在签名信息完整时）
            if (System.getenv("KEYSTORE_PASSWORD") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // 启用 BuildConfig
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 网络层依赖
    implementation(libs.retrofit.core)                      // Retrofit 核心
    implementation(libs.retrofit.kotlin.serialization)      // Kotlinx Serialization 转换器
    implementation(libs.okhttp.core)                        // OkHttp 核心
    implementation(libs.okhttp.logging)                     // 日志拦截器（调试用）
    implementation(libs.kotlinx.serialization.json)         // JSON 序列化

    // CameraX 依赖（二维码扫描）
    implementation(libs.androidx.camera.core)               // CameraX 核心
    implementation(libs.androidx.camera.camera2)            // Camera2 实现
    implementation(libs.androidx.camera.lifecycle)          // 生命周期集成
    implementation(libs.androidx.camera.view)               // 相机预览视图

    // ML Kit 二维码扫描
    implementation(libs.mlkit.barcode.scanning)             // 二维码识别

    // Accompanist 权限库
    implementation(libs.accompanist.permissions)            // 权限请求

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
