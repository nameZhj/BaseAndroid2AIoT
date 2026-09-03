# BaseAndroid2AIoT — 企业级 Android IoT 快速开发脚手架

> 100% Kotlin · Jetpack Compose + Material3 · BRVAH 4 · MVVM + Hilt · Multi-Protocol IoT · 编译期协议裁剪

---

## 📐 架构概览

```
┌────────────────────────────────────────────────────────────────────────┐
│                               UI Layer                                 │
│    Jetpack Compose + Material3 + Single Activity 架构                   │
│    AdaptiveContentLayout (手机/平板/工业大屏自适应双栏)                   │
│    BRVAH 4 (BaseRecyclerViewAdapterHelper 顶流列表混编)                │
│    SystemBarEffect (沉浸式透明状态栏与导航栏)                           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                            ViewModel Layer                             │
│    DashboardViewModel (MVVM + StateFlow + 协程挂起)                     │
│    Hilt 依赖注入协议接口抽象与缓存分享管理器                            │
└──────┬────────────────┬─────────────────┬────────────────┬─────────────┘
       │                │                 │                │
       ▼                ▼                 ▼                ▼
   HTTP 接口         MQTT 接口         Redis 接口       Socket 接口
   (HttpManager)    (MqttManager)     (RedisManager)   (SocketManager)
       │                │                 │                │
 ┌─────┴──────┐   ┌─────┴──────┐    ┌─────┴──────┐   ┌─────┴──────┐
 │  Retrofit  │   │   HiveMQ   │    │   Jedis    │   │  Native    │
 │   OkHttp   │   │   Netty    │    │CommonsPool │   │ TCP Socket │
 │    Okio    │   │            │    │            │   │            │
 └─────┬──────┘   └─────┬──────┘    └─────┬──────┘   └─────┬──────┘
       │                │                 │                │
       └────────────────┴────────┬────────┴────────────────┘
                                 │
                 ┌───────────────▼───────────────┐
                 │    编译期协议开关 (Properties) │
                 │    gradle.properties 依赖裁剪 │
                 │    未启用时第三方 SDK 彻底剔除 │
                 │    零依赖 Stub 占位保障包体最小│
                 └───────────────┬───────────────┘
                                 │
                 ┌───────────────▼───────────────┐
                 │  存储与分享 / 诊断日志基建     │
                 │  CacheLocationManager (多缓存)│
                 │  FileShareManager (系统分享)  │
                 │  Lg (时序防溢出日志) / Crash  │
                 └───────────────────────────────┘
```

---

## 🌟 核心特性与技术方案

### 1. 编译期协议依赖裁剪 (Compile-time Protocol Toggles)
针对工控和 AIoT 边缘设备对包体积及运行时内存的严苛要求，在 `gradle.properties` 中提供编译期开关：
```properties
# 物联网协议编译期开关
iot.protocol.http.enabled=true
iot.protocol.mqtt.enabled=true
iot.protocol.redis.enabled=false
iot.protocol.socket.enabled=true
```
- **依赖彻底剔除**：当开关设为 `false`，对应第三方重量级依赖（如 HiveMQ + Netty 约 15MB、Jedis 等）**100% 不会参与依赖解析与打包**。
- **动态 SourceSet 挂载**：`app/build.gradle.kts` 根据开关自动挂载 `protocol_xxx`（真实 SDK 驱动）或 `protocol_xxx_stub`（零依赖轻量 Stub 占位），确保代码零冗余且编译器符号完整。

---

### 2. Android 系统层 HTTP 协议风险约束解决方案
Android 系统对网络通信有严格的沙箱与安全限制，本项目针对物联网场景提供了完备的闭环方案：
- **Android 9.0+ (API 28+) 明文限制防御**：
  配置 `android:usesCleartextTraffic="true"` 并配合 `@xml/network_security_config`，彻底解决工控局域网或微控制器（ESP32/PLC）明文 `http://` 被系统阻断的问题。
- **自签名证书与局域网 IP 直连支持**：
  配置安全网络证书信任锚点（同时信任 `system` 与 `user` 根证书，支持抓包调试与企业私有自建 CA）。并在 OkHttp 中配置私有网段（`192.168.x.x`、`10.x.x.x`、`127.0.0.1`）主机名放行策略，支持直接通过 IP 访问自签名 HTTPS 设备。
- **局域网设备组播发现与防止睡眠掉线**：
  声明 `CHANGE_WIFI_MULTICAST_STATE` 权限（支持 mDNS / SSDP 设备局域网发现）及 `WAKE_LOCK` 权限（防止大文件 OTA 下载或长连接在锁屏休眠时中断）。

---

### 3. 全协议四段时序生命周期与防 OOM 日志规范
对 HTTP、MQTT、Redis、TCP Socket 统一遵循清晰的生命周期时序打印：
`[1/4 CONNECTING]` 握手连接中 ➔ `[2/4 CONNECTED]` 建立就绪 ➔ `[3/4 TRANSFER]` 数据收发 ➔ `[4/4 CLOSED/DISCONNECTED]` 挥手释放。

#### 防内存溢出 (OOM) 与数据脱敏：
- **严禁全量输出大数据量媒体/二进制流**：在 OkHttp 拦截器与 Socket/MQTT 驱动中，自动识别并过滤二进制流与多媒体数据。
- **文件与传输仅记录摘要**：对于文件上传与下载，仅规范打印 **文件名称、文件大小、落盘缓存路径、耗时与 HTTP 状态码**，防止 Logcat 内存暴涨与频繁 GC。

---

### 4. 自定义文件缓存位置与系统级文件分享
- **多策略缓存管理 (`CacheLocationManager`)**：
  支持用户在控制面板自由切换缓存落盘位置，并实时持久化至 Jetpack DataStore：
  1. **内部私有缓存 (`context.cacheDir`)**：随应用卸载自动清理，隔离性最高；
  2. **外部私有缓存 (`context.externalCacheDir`)**：适合固件升级包（OTA）与大型数据；
  3. **外部专属下载区 (`Environment.DIRECTORY_DOWNLOADS`)**：便于持久保存。
- **系统级文件分享 (`FileShareManager`)**：
  基于规范配置的 `FileProvider`（兼容 Android 7.0+ 及沙箱存储），支持唤起系统级原生分享面板（微信、QQ、系统发送、邮件等），支持一键分享日志及已下载的任意文件。

---

### 5. 集成 GitHub Star 最高的 RecyclerView 适配器框架 (BRVAH 4)
- **选型**：GitHub 拥有 24.3k+ Stars 的 `BaseRecyclerViewAdapterHelper4` (`io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4`)。
- **原生与 Compose 混编**：
  在保持现代 Jetpack Compose 架构的同时，提供 `IotDeviceQuickAdapter` 演示原生高效列表开发，并通过 Compose `AndroidView` 实现顺畅混编与双向状态响应。

---

## 🗂️ 项目目录结构

```
BaseAndroid2AIoT/
├── gradle/
│   └── libs.versions.toml             # 统一 Version Catalog 版本目录
├── build.gradle.kts                   # 根项目构建脚本
├── settings.gradle.kts                # 仓库配置（已配置国内阿里镜像加速）
├── gradle.properties                  # 编译期协议开关与 Gradle 优化配置
└── app/
    ├── build.gradle.kts               # 应用模块构建脚本（动态依赖与 SourceSets）
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml    # 权限、网络安全配置与 FileProvider
        │   ├── res/
        │   │   ├── xml/network_security_config.xml # 明文与自签名证书安全配置
        │   │   └── xml/file_paths.xml              # FileProvider 安全共享路径
        │   └── kotlin/com/base/iot/
        │       ├── App.kt
        │       ├── MainActivity.kt
        │       ├── core/
        │       │   ├── config/        # 运行时 DataStore 开关与 AppConfig 常量
        │       │   ├── diagnostics/   # 超长日志分段 (Lg)、崩溃抓取 (CrashHandler)
        │       │   ├── storage/       # 缓存位置切换 (CacheLocationManager)、文件分享
        │       │   ├── ui/recycler/   # BRVAH 4 设备列表适配器 (IotDeviceQuickAdapter)
        │       │   ├── network/       # HTTP 通用接口定义 (HttpManager)
        │       │   └── iot/           # MQTT / Redis / Socket 接口抽象
        │       └── feature/demo/      # Compose 仪表盘控制面板 (DashboardScreen/VM)
        ├── protocol_http/             # HTTP 真实驱动 (Retrofit + OkHttp + Okio)
        ├── protocol_http_stub/        # HTTP 零依赖 Stub 占位实现
        ├── protocol_mqtt/             # MQTT 真实驱动 (HiveMQ 异步客户端)
        ├── protocol_mqtt_stub/        # MQTT 零依赖 Stub 占位实现
        ├── protocol_redis/            # Redis 真实驱动 (Jedis 连接池与心跳)
        ├── protocol_redis_stub/       # Redis 零依赖 Stub 占位实现
        ├── protocol_socket/           # TCP Socket 真实长连接与心跳驱动
        └── protocol_socket_stub/      # TCP Socket 零依赖 Stub 占位实现
```

---

## 🔧 技术栈与版本规格

| 技术组件 | 框架 / 库 | 选用版本 | 作用说明 |
| :--- | :--- | :--- | :--- |
| **语言** | Kotlin | `2.0.21` | 100% 现代 Kotlin 编写 |
| **构建体系** | Gradle / AGP | `8.11.1` / `8.5.2` | Gradle 8+ 增量构建与配置缓存 |
| **UI 体系** | Jetpack Compose + M3 | BOM `2024.09.03` | 响应式 Material 3 设计 |
| **列表框架** | BRVAH 4 (BaseQuickAdapter) | `4.1.4` | GitHub 顶流强大列表适配器 |
| **架构组件** | ViewModel + Coroutines + Flow | `2.8.3` / `1.8.1` | MVVM 单向数据流架构 |
| **依赖注入** | Google Hilt | `2.51.1` | 依赖注入与组件生命周期管理 |
| **配置存储** | Jetpack DataStore | `1.1.1` | 响应式配置与缓存策略持久化 |
| **HTTP 传输** | Retrofit + OkHttp + Okio | `2.11.0` / `4.12.0` | 支持 GET/POST/PUT/DELETE 及大文件流式上传下载 |
| **MQTT 协议** | HiveMQ MQTT Client | `1.3.3` | 高性能反应式 MQTT 3.1.1 客户端 |
| **Redis 协议**| Jedis | `5.1.3` | 远控、参数下发与心跳检测 |
| **网络诊断** | 自研分段日志 + CrashHandler | 原生扩展 | 防止 Logcat 截断与本地异常落盘 |

---

## 🚀 编译与快速开始

### 1. 开发环境要求
- **JDK**: Java 17+ (推荐 Eclipse Adoptium OpenJDK 17)
- **Android SDK**: `compileSdk = 35`, `minSdk = 24`, `targetSdk = 34`
- **Gradle**: 8.11.1 (工程内置 `gradlew` 包装器)

### 2. 编译与打包
```bash
# 验证代码编译
./gradlew compileDebugKotlin

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```
产物输出路径：`app/build/outputs/apk/debug/app-debug.apk`。

---

## 📄 开源许可证
本项目遵循 [Apache 2.0 License](LICENSE)。
