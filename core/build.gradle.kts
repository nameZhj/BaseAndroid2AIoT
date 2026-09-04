plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val isHttpCompiled: Boolean = (project.findProperty("iot.protocol.http.enabled") as? String)?.toBoolean() ?: true
val isMqttCompiled: Boolean = (project.findProperty("iot.protocol.mqtt.enabled") as? String)?.toBoolean() ?: true
val isRedisCompiled: Boolean = (project.findProperty("iot.protocol.redis.enabled") as? String)?.toBoolean() ?: true
val isSocketCompiled: Boolean = (project.findProperty("iot.protocol.socket.enabled") as? String)?.toBoolean() ?: true

android {
    namespace = "com.base.iot.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("Boolean", "IS_HTTP_COMPILED", isHttpCompiled.toString())
        buildConfigField("Boolean", "IS_MQTT_COMPILED", isMqttCompiled.toString())
        buildConfigField("Boolean", "IS_REDIS_COMPILED", isRedisCompiled.toString())
        buildConfigField("Boolean", "IS_SOCKET_COMPILED", isSocketCompiled.toString())
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "ENABLE_LOG", "true")
        }
        release {
            isMinifyEnabled = false
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

    sourceSets {
        getByName("main") {
            val protocolDirs = mutableListOf<String>()
            protocolDirs.add(if (isHttpCompiled) "src/protocol_http/kotlin" else "src/protocol_http_stub/kotlin")
            protocolDirs.add(if (isMqttCompiled) "src/protocol_mqtt/kotlin" else "src/protocol_mqtt_stub/kotlin")
            protocolDirs.add(if (isRedisCompiled) "src/protocol_redis/kotlin" else "src/protocol_redis_stub/kotlin")
            protocolDirs.add(if (isSocketCompiled) "src/protocol_socket/kotlin" else "src/protocol_socket_stub/kotlin")
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
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.activity.compose)
    api(libs.androidx.window)
    api(libs.m3.window.size)

    // Compose BOM
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.ui.tooling.preview)
    api(libs.compose.material3)
    api(libs.compose.material.icons.extended)
    api(libs.compose.animation)

    // Hilt
    api(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Conditional Protocol Dependencies
    if (isHttpCompiled) {
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.gson)
        implementation(libs.okhttp)
        implementation(libs.okhttp.logging)
    }

    if (isMqttCompiled) {
        implementation(libs.hivemq.mqtt.client)
    }

    if (isRedisCompiled) {
        implementation(libs.jedis)
    }

    // Image Loading
    api(libs.coil.compose)

    // XPopup
    api(libs.xpopup)

    // DataStore
    api(libs.datastore.preferences)

    // Coroutines
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.core)

    // Serialization
    api(libs.kotlinx.serialization.json)
}
