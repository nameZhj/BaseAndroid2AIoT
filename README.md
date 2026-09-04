# BaseAndroid2AIoT — 企业级 Android IoT 快速开发框架

 本文档由AI生成
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

### 1. 编译期协议依赖控制 (Compile-time Protocol Toggles)
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
- **大文件上传/下载专用 1 小时超时保障 (`HTTP_FILE_TRANSFER_TIMEOUT_SEC = 3600L`)**：
  为防止大固件 OTA 包或海量工控日志文件在长耗时传输时触发默认 30s 超时中断，底层采用独立客户端分流机制：
  - **常规 REST 请求 (GET/POST/PUT/DELETE)**：维持 30s 超时，保障普通接口故障时迅速报错与降级；
  - **大文件上传与下载 (`upload` / `download`)**：自动切换为专用长连接通道，`readTimeout`、`writeTimeout` 与 `callTimeout` 统一延展至 **1 小时 (3600秒)**，彻底杜绝慢速弱网或超大文件传输中断。

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

### 5. 集成 RecyclerView 适配器框架 (BRVAH 4)
- **选型**：GitHub 拥有 24.3k+ Stars 的 `BaseRecyclerViewAdapterHelper4` (`io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4`)。
- **原生与 Compose 混编**：
  在保持现代 Jetpack Compose 架构的同时，提供 `IotDeviceQuickAdapter` 演示原生高效列表开发，并通过 Compose `AndroidView` 实现顺畅混编与双向状态响应，支持普通模式与夜间模式色彩动态适配。

---

### 6. 全局统一设计系统 (Design System) 与普通/夜间双模式
为了让后续所有开发严格遵循统一的视觉基调，并彻底解决“文字/按钮/背景颜色相近而无法看清”的问题，项目严格遵循 **WCAG AAA 顶级对比度规范** 建立了全局设计系统令牌体系（Design Tokens）：

- **色彩令牌设计表**：
  | 视觉元素 (Design Tokens) | 普通模式 (Light Mode) | 夜间模式 (Dark Mode) | 对比度与可读性保障 |
  | :--- | :--- | :--- | :--- |
  | **主页面底色 (`background`)** | `#F1F5F9` (Slate-100) | `#0B0F19` (Obsidian) | 柔和浅青灰消除刺眼白光，夜间深邃黑曜石低功耗护眼 |
  | **卡片/容器 (`surface`)** | `#FFFFFF` (纯白 + 边框) | `#161B26` (Slate-900) | 容器与背景层次分明，绝不泛灰混淆 |
  | **主要标题字 (`textPrimary`)** | `#0F172A` (Slate-900) | `#F8FAFC` (Slate-50) | **对比度 > 15:1**，极其清晰锐利 |
  | **次要描述字 (`textSecondary`)**| `#334155` (Slate-700) | `#94A3B8` (Slate-400) | **对比度 > 7:1**，层级分明不刺眼 |
  | **主科技强调色 (`accentPrimary`)**| `#0284C7` (Sky-600) | `#00D4FF` (Neon Cyan) | 日间稳重深蓝，夜间高饱和电光青 |
  | **成功/在线色 (`accentGreen`)** | `#059669` (Emerald-600) | `#10B981` (Emerald-400) | 物联网在线与连接就绪指示 |
  | **危险/告警色 (`accentRed`)** | `#DC2626` (Red-600) | `#EF4444` (Red-500) | 核心复位、断开与删除操作高亮 |
  | **卡片轮廓描边 (`cardBorder`)** | `#CBD5E1` (Slate-300) | `#263346` | 明确边界，防止光线干扰下轮廓消融 |
  | **控制台终端 (`terminalBg/Text`)**| `#0F172A` / `#34D399` | `#010409` / `#39D353` | 保持工控专业沉浸感 |

- **全应用统一访问与持久化**：
  - 核心组件统一使用 `AppTheme.colors.*` 提取色彩；
  - `ThemeManager` 支持 `ThemeMode.LIGHT`、`ThemeMode.DARK`、`ThemeMode.SYSTEM`，通过 Jetpack DataStore 持久化保存；
  - 控制面板顶部提供**一键切换图标按钮**，动态自适应 Android 系统状态栏图标（深底配浅色图标，浅底配深色图标）。

---

### 7. 引入Dialog 框架 XPopup
针对 Android 物联网操作中频繁的确认、等待、参数配置与面板交互，引入 GitHub 截至 2026 年最流行的弹窗框架 **XPopup** (`com.github.li-xiaojun:XPopup:2.10.0`)，并封装全应用风格统一的弹窗体系：

1. **统一设计规范 Compose 弹窗组件 (`AppDialog.kt`)**：
   - **`AppConfirmDialog`**：二次确认与高危操作防误触弹窗（支持主色/危险红高亮按钮）；
   - **`AppLoadingDialog`**：沉浸式阻塞加载弹窗，专为物联网握手、OTA 下载等长耗时操作提供安全防护；
   - **`AppInputDialog`**：设备标识、参数配置输入弹窗，自带清空按钮与校验；
   - **`AppBottomSheetDialog`**：底部展开式设备抽屉，带手势拖拽条。
2. **原生 View / Activity 统一门面 (`XPopupBridge.kt`)**：
   - 封装 `XPopupBridge.showConfirm()`、`XPopupBridge.showLoading()`、`XPopupBridge.showInput()`，确保在混合栈或非 Compose 页面中呼出弹窗依然保持 100% 相同的设计规范与 Dark/Light 主题。

---

### 8. 耗时操作进度弹窗体系 (`AppProgressDialog` / `launchWithLoading`)
为提升工控与物联网场景下复杂长连接、大数据传输时的交互体验，提供了开箱即用的异步任务包装器：

- **开发者自主性与自由度**：
  - **启用/静默自由切换 (`showLoading`)**：开发者可自主选择每个耗时操作是否呼出进度弹窗（支持后台静默执行）；
  - **阻塞式 vs 非阻塞式自由配置 (`isBlocking`)**：
    - **阻塞式 (`isBlocking = true`)**：禁止用户点击外部背景与返回键（`dismissOnBackPress = false, dismissOnClickOutside = false`），防止重复点击触发并发网络冲击；适用于安全握手、密钥协商、固件 OTA 刷写等不可中断的工控事务；
    - **非阻塞式 (`isBlocking = false`)**：允许用户随时点击外部空白处或显式“取消本次操作”按钮，协同取消协程 Job，提升交互灵活度。
  - **进度形态无缝自适应**：
    - **循环转圈模式 (Indeterminate)**：适用于未知耗时操作；
    - **精准百分比模式 (0%~100%)**：内置 `LinearProgressIndicator`，实时呈现已传输字节、总字节数与进度百分比，适用于大文件上传下载。
- **协程调用规范**：
  ```kotlin
  // 一行代码发起带进度或静默的耗时任务
  launchWithLoading(
      title = "正在连接边缘设备...",
      isBlocking = true,
      showLoading = true
  ) { updateProgress ->
      // 业务逻辑...
      updateProgress(0.5f, "已完成 50%")
  }
  ```

---

### 9. 非阻塞式真实错误诊断与一键系统分享体系 (`AppErrorDialog` / `ErrorParser`)
工业物联网现场排查难、报障信息模糊往往是研发与实施的最大痛点。本工程摒弃传统“网络开小差了”之类的无用提示，提供极具现场诊断价值的专业错误弹窗体系：

- **非阻塞交互原则**：所有错误弹窗均为非阻塞式（`dismissOnClickOutside = true`），轻触外部或点击“我知道了”即可关闭，绝不卡死界面流程。
- **高保真结构化解析 (`ErrorParser`)**：
  - **网络层分类**：
    - `UnknownHostException`：DNS 解析失败或局域网主机名不可达
    - `ConnectException`：目标服务器拒绝连接（端口未开放/服务宕机）
    - `SocketTimeoutException`：TCP 握手超时或读写超时
    - `SSLException`：自签名证书链不受信或域名不匹配
  - **执行层分类**：
    - `HttpException`：精准提取 HTTP 状态码（400/401/403/404/500/502 等）与错误体
    - `ProtocolDisabledException`：编译期或运行时被禁用的协议拦截
- **一键唤起系统分享**：
  - 弹窗内嵌高对比度工控终端风格的“详细诊断报告展开面板”，完整列出：发生时间戳、设备品牌型号、Android 系统与 API 版本、当前真实网络（WIFI/蜂窝/有线）、核心根因堆栈；
  - 显式配置“**分享错误报告**”高亮按钮，点击后一键唤起系统原生分享面板（微信、QQ、钉钉、邮件、备忘录等），支持现场实施人员秒级将真实错误堆栈发送给后端或研发团队。

---

### 10. 面向 AI Agent 深度优化的工程架构与组件下沉
为让 AI Agent 能够以极高的能效读懂代码、检索资产并零样板代码复用搭建新功能，工程进行了深度架构重构：

1. **统一基类层沉淀 (`core/base/BaseViewModel.kt`)**：
   - 自动维护 `StateFlow<STATE>` 与 `SharedFlow<EVENT>`；
   - 通用集成 `launchWithLoading`、`ErrorParser` 错误拦截、非阻塞式错误弹窗驱动与系统一键分享；
   - Agent 新建页面仅需 10 行代码继承基类，无需写任何重复的基础设施样板代码。
2. **物联网多协议统一 (`core/iot/IotHub.kt`)**：
   - 集中聚合 `http`, `mqtt`, `redis`, `socket`, `config`，避免繁琐的多对象注入，一行注入即可调动全局协议通信。
3. **全局原子化 UI 组件库 (`core/ui/components/`)**：
   - 将卡片与按钮全面标准化下沉为 `AppCard`、`AppButton`、`AppSwitchRow`，暗/日间模式自适应与高对比度开箱即用。
4. **大文件组件化解耦 (`feature/demo/components/`)**：
   - 将原 1080 行的 `DashboardScreen.kt` 彻底拆解为若干 50~80 行的独立微组件，主屏幕瘦身为仅 180 行的高层骨架编排器；
5. **AI Agent 唯一权威开发手册 (`AGENTS.md`)**：
   - 根目录下唯一面向 AI Agent 的开发规范与架构速查手册，包含四大工程铁律、Token 经济学、拓扑索引、3 步起手式模板与自检清单。

---

## 🗂️ 项目目录结构

```
BaseAndroid2AIoT/
├── gradle/
│   └── libs.versions.toml             # 统一 Version Catalog 版本目录
├── build.gradle.kts                   # 根项目构建脚本
├── settings.gradle.kts                # 仓库配置（阿里镜像加速 + JitPack）
├── gradle.properties                  # 编译期协议裁剪开关与 Gradle 优化
├── AGENTS.md                          # ★ 面向 Agent 的唯一权威开发架构与行为准则手册
└── app/
    ├── build.gradle.kts               # 应用模块构建脚本（动态依赖与 SourceSets）
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml    # 权限、明文网络放行、FileProvider
        │   ├── res/
        │   │   ├── xml/network_security_config.xml # 明文与自签名证书安全配置
        │   │   └── xml/file_paths.xml              # FileProvider 安全共享路径
        │   └── kotlin/com/base/iot/
        │       ├── App.kt
        │       ├── MainActivity.kt    # 单 Activity 挂载与状态栏动态适配
        │       ├── ui/theme/          # AppTheme 令牌体系 (Light & Dark 高对比度)
        │       ├── core/
        │       │   ├── base/          # ★ 统一基类 (BaseViewModel / UiContract)
        │       │   ├── config/        # 运行时 DataStore 开关与 AppConfig 常量
        │       │   ├── diagnostics/   # 超长日志分段 (Lg)、错误解析 (ErrorParser)、崩溃抓取
        │       │   ├── storage/       # 缓存位置切换 (CacheLocationManager)、文件分享
        │       │   ├── ui/
        │       │   │   ├── components/# ★ 原子化通用组件 (AppCard / AppButton / AppSwitchRow)
        │       │   │   ├── dialog/    # 统一弹窗组件 (AppProgressDialog / AppErrorDialog / XPopupBridge)
        │       │   │   ├── theme/     # 主题模式持久化管理 (ThemeManager.kt)
        │       │   │   └── recycler/  # BRVAH 4 设备列表适配器 (IotDeviceQuickAdapter)
        │       │   ├── network/       # HTTP 通用接口定义 (HttpManager)
        │       │   └── iot/           # ★ 物联网统一门面 (IotHub) 与 MQTT/Redis/Socket 抽象
        │       └── feature/demo/      # 业务特性模块
        │           ├── DashboardScreen.kt    # 纯轻量骨架编排器 (~180行)
        │           ├── DashboardViewModel.kt # 继承 BaseViewModel 的业务控制器
        │           └── components/           # ★ 业务卡片微组件集 (高内聚、微体积、低 Token)
        ├── protocol_http/             # HTTP 真实驱动 (Retrofit + OkHttp + Okio)
        ├── protocol_http_stub/        # HTTP 零依赖 Stub 占位实现
        ├── protocol_mqtt/             # MQTT 真实驱动 (HiveMQ 异步客户端)
        ├── protocol_mqtt_stub/        # MQTT 零依赖 Stub 占位实现
        ├── protocol_redis/            # Redis 真实驱动 (Jedis 连接池与心跳)
        ├── protocol_redis_stub/       # Redis 零依赖 Stub 占位实现
        ├── protocol_socket/           # TCP Socket 原生工业长连接驱动
        └── protocol_socket_stub/      # TCP Socket 零依赖 Stub 占位实现
```

---

## 🔧 技术栈与版本规格

| 技术组件 | 框架 / 库 | 选用版本 | 作用说明 |
| :--- | :--- | :--- | :--- |
| **语言** | Kotlin | `2.0.21` | 100% 现代 Kotlin 编写 |
| **构建体系** | Gradle / AGP | `8.11.1` / `8.5.2` | Gradle 8+ 增量构建与配置缓存 |
| **UI 体系** | Jetpack Compose + M3 | BOM `2024.09.03` | 响应式 Material 3 设计 |
| **弹窗框架** | [XPopup](https://github.com/li-xiaojun/XPopup) | `2.10.0` | GitHub 15k+ 顶流通用弹窗库 |
| **列表框架** | [BRVAH 4](https://github.com/CymChad/BaseRecyclerViewAdapterHelper) | `4.1.4` | GitHub 24k+ 顶流列表适配器 |
| **架构组件** | ViewModel + Coroutines + Flow | `2.8.3` / `1.8.1` | MVVM 单向数据流架构 |
| **依赖注入** | [Hilt](https://github.com/google/dagger) | `2.51.1` | 依赖注入与组件生命周期管理 |
| **配置存储** | Jetpack DataStore | `1.1.1` | 响应式配置、主题模式与缓存策略持久化 |
| **HTTP 传输** | [Retrofit](https://github.com/square/retrofit) + [OkHttp](https://github.com/square/okhttp) | `2.11.0` / `4.12.0` | 支持常规 REST 与大文件 1 小时流式上传下载 |
| **MQTT 协议** | [HiveMQ MQTT Client](https://github.com/hivemq/hivemq-mqtt-client) | `1.3.3` | 高性能反应式 MQTT 3.1.1 客户端 |
| **Redis 协议** | [Jedis](https://github.com/redis/jedis) | `5.1.3` | 物联网控制指令与键值操作 |
| **图片加载** | [Coil](https://github.com/coil-kt/coil) | `2.7.0` | 协程驱动轻量图片加载 |
| **JSON 解析** | [Gson](https://github.com/google/gson) | `2.11.0` | 序列化与反序列化 |
| **TCP Socket** | 原生 NIO SocketChannel | JDK 17 | 工业级长连接心跳与断线重连 |
| **网络诊断** | 自研分段日志 + CrashHandler | 原生扩展 | 防止 Logcat 截断与本地异常落盘 |

---

## 📦 第三方开源框架声明与合规矩阵

本项目严格遵循开源法律合规要求，所有引入的核心第三方框架均采用**商业友好型宽松许可协议 (Permissive Licenses: Apache-2.0 / MIT)**，**零 GPL 强传染性风险**，支持企业商业闭源开发与合规交付：

| 框架 / 组件名 | 适用开源协议 | 商业闭源商用 | 官方项目地址 |
| :--- | :--- | :--- | :--- |
| **XPopup** | **Apache-2.0** | 允许 | [li-xiaojun/XPopup](https://github.com/li-xiaojun/XPopup) |
| **BRVAH 4** | **Apache-2.0** | 允许 | [CymChad/BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper) |
| **Retrofit** | **Apache-2.0** | 允许 | [square/retrofit](https://github.com/square/retrofit) |
| **OkHttp** | **Apache-2.0** | 允许 | [square/okhttp](https://github.com/square/okhttp) |
| **HiveMQ MQTT Client** | **Apache-2.0** | 允许 | [hivemq/hivemq-mqtt-client](https://github.com/hivemq/hivemq-mqtt-client) |
| **Jedis** | **MIT** | 允许 | [redis/jedis](https://github.com/redis/jedis) |
| **Coil** | **Apache-2.0** | 允许 | [coil-kt/coil](https://github.com/coil-kt/coil) |
| **Google Dagger Hilt** | **Apache-2.0** | 允许 | [google/dagger](https://github.com/google/dagger) |
| **Google Gson** | **Apache-2.0** | 允许 | [google/gson](https://github.com/google/gson) |
| **Kotlinx Coroutines** | **Apache-2.0** | 允许 | [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) |

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

## 📄 许可证与法律合规 (License & Compliance)

- **本项目许可协议**：本项目源代码遵循 [代码使用许可协议与商业授权限制条款](LICENSE)。
  - **非商业用途**：对个人学习、学术研究、非营利性技术交流完全免费开放。
  - **商业用途限制**：**任何商业用途（包括商业销售、硬件搭载量产、对外提供收费服务、企业闭源交付等）必须预先获得原作者的正式书面商业授权**。未经授权擅自商用构成侵犯版权。
- **法律合规声明与版权归属**：详见根目录下标准归属文件 [NOTICE](NOTICE)。
- **完整第三方依赖许可清单与合规说明**：详见 [OPEN_SOURCE_LICENSES.md](OPEN_SOURCE_LICENSES.md)。工程所引入的所有核心三方库均采用宽松许可证（Apache-2.0 / MIT），第三方库遵循各自独立的原生许可。
