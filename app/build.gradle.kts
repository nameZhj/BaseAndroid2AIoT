plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// ==================== 读取编译期协议开关 ====================
val isHttpEnabled: Boolean = (project.findProperty("iot.protocol.http.enabled") as? String)?.toBoolean() ?: false
val isMqttEnabled: Boolean = (project.findProperty("iot.protocol.mqtt.enabled") as? String)?.toBoolean() ?: false
val isRedisEnabled: Boolean = (project.findProperty("iot.protocol.redis.enabled") as? String)?.toBoolean() ?: false
val isSocketEnabled: Boolean = (project.findProperty("iot.protocol.socket.enabled") as? String)?.toBoolean() ?: false

android {
    namespace = "com.base.iot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.base.iot"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // 注入编译期协议打包状态常量到 BuildConfig
        buildConfigField("Boolean", "IS_HTTP_COMPILED", isHttpEnabled.toString())
        buildConfigField("Boolean", "IS_MQTT_COMPILED", isMqttEnabled.toString())
        buildConfigField("Boolean", "IS_REDIS_COMPILED", isRedisEnabled.toString())
        buildConfigField("Boolean", "IS_SOCKET_COMPILED", isSocketEnabled.toString())
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            buildConfigField("Boolean", "ENABLE_LOG", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "ENABLE_LOG", "false")
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
        buildConfig = true
    }

    // ==================== 按需动态挂载源码集 ====================
    sourceSets {
        getByName("main") {
            val protocolDirs = mutableListOf<String>()
            protocolDirs.add(if (isHttpEnabled) "src/protocol_http/kotlin" else "src/protocol_http_stub/kotlin")
            protocolDirs.add(if (isMqttEnabled) "src/protocol_mqtt/kotlin" else "src/protocol_mqtt_stub/kotlin")
            protocolDirs.add(if (isRedisEnabled) "src/protocol_redis/kotlin" else "src/protocol_redis_stub/kotlin")
            protocolDirs.add(if (isSocketEnabled) "src/protocol_socket/kotlin" else "src/protocol_socket_stub/kotlin")
            kotlin.srcDirs(protocolDirs)
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.window)
    implementation(libs.m3.window.size)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ==================== 条件依赖引入（核心：未开启则绝不打包进 APK） ====================

    // HTTP 协议组件（开启时引入 Retrofit 与 OkHttp，关闭时不参与依赖解析）
    if (isHttpEnabled) {
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.gson)
        implementation(libs.okhttp)
        implementation(libs.okhttp.logging)
    }

    // MQTT 协议组件（开启时引入 HiveMQ 及底层 Netty 约 15MB 依赖，关闭时彻底剔除）
    if (isMqttEnabled) {
        implementation(libs.hivemq.mqtt.client)
    }

    // Redis 协议组件（开启时引入 Jedis，关闭时彻底剔除）
    if (isRedisEnabled) {
        implementation(libs.jedis)
    }

    // Image Loading
    implementation(libs.coil.compose)

    // RecyclerView & BRVAH (GitHub 顶级通用列表适配器框架)
    implementation(libs.androidx.recyclerview)
    implementation(libs.brvah)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Gson (通用序列化)
    implementation(libs.gson)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.tooling)
}
