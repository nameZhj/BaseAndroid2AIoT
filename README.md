# BaseAndroid2AIoT — Android IoT 快速开发框架

---

## 📐 架构概览

```
┌────────────────────────────────────────────────────────────────────────┐
│                               UI Layer                                 │
│    Jetpack Compose + Material3 + Single Activity 架构                   │
│    AdaptiveContentLayout (手机/平板/工业大屏自适应双栏)                   │
│    SystemBarEffect (沉浸式透明状态栏与导航栏)                           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                            ViewModel Layer                             │
│    BaseViewModel (UDF 架构 + StateFlow + 协程调度)                      │
│    Hilt 依赖注入协议接口抽象与存储分享管理器                            │
└──────┬────────────────┬─────────────────┬────────────────┬─────────────┘
       │                │                 │                │
       ▼                ▼                 ▼                ▼
   HTTP 接口         MQTT 接口         Redis 接口       Socket 接口
   (HttpManager)    (MqttManager)     (RedisManager)   (SocketManager)
       │                │                 │                │
 ┌─────┴──────┐   ┌─────┴──────┐    ┌─────┴──────┐   ┌─────┴──────┐
 │  Retrofit  │   │   HiveMQ   │    │   Jedis    │   │  Native    │
 │   OkHttp   │   │   Netty    │    │CommonsPool │   │ TCP Socket │
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

## 🤖 为 AI Agent 原生设计：AGENTS.md 与协同开发优势

本项目从底层即全面拥抱 **“AI 智能体结对编程 (Agentic Pair-Programming)”** 范式。根目录内置的 [`AGENTS.md`](AGENTS.md) 是专门面向大模型智能体设计的机器规约指令集。

### 核心定位与特性
- **机器语义友好**：全篇采用严谨的命令式规范（如 `ABSOLUTE_ZERO_TOLERANCE`、`MANDATORY_I18N`、`TOP_LEVEL_IMPORTS_ONLY`），剥离冗词，大模型理解与执行准确率达 100%。
- **十项核心规范模块**：涵盖模块与 Git 边界、代码卫生与 Token 经济、UI 布局防变形、Dialog 规范、UDF 范式、Demo 清退流水线、模块拓扑、业务克隆 Recipe、基建矩阵及自检清单。

### 为什么比常规项目更适合 Agent 开发？
| 评估维度 | 常规 Android 项目 | BaseAndroid2AIoT (本项目) | 对 Agent 开发的决定性优势 |
| :--- | :--- | :--- | :--- |
| **文件粒度与 Token 开销** | 单文件规模庞大（Monolithic Classes），极易触发上下文超限与截断 | **限制单文件 50~150 行**，ViewModel 按 Extension Functions 水平切片 | 最小化上下文开销，支持局部精确修改（Surgical Edits），避免全文件重写（Full File Rewriting）导致的逻辑丢失 |
| **协议与底层依赖边界** | 业务代码直接依赖底层网络库，易发生版本冲突与依赖散乱 | **协议抽象与实现物理隔离**，统一通过 `IotHub` 门面访问，Driver 与零依赖 Stub 同步维护 | 隔离底层具体实现，防止业务层混用异构网络库 |
| **新业务开发认知门槛** | 架构分层不清晰，目录与依赖注入模式不明确 | **模块边界明确锁定**（`:core` 锁定保护，新业务位于 `:app`），提供标准化 Scaffold 模板 | 结构确定，通过克隆模板即可完成标准业务三件套（UiState / ViewModel / Screen）初始化 |
| **代码规范与一致性** | 易产生字符串 Hardcoding 或在函数体内直接使用全限定名（FQNs） | **强制字符串资源抽取（strings.xml 中英文对齐）**，参数收敛于 `AppConfig`，**符号引用 100% 置顶声明** | 消除 Hardcoding 与内联导包等不良实践，保持代码风格与规范一致 |
| **工业横屏布局稳定性** | 缺乏小尺寸或工控横屏（高度 360~480dp）的尺寸约束，易发生文本挤压组件或内容溢出 | 明确布局约束规则：`Row` 动态文本声明 `weight(1f, fill=false)`；按钮声明最小尺寸；**Dialog 标配右上角纯文字“关闭”**并配置 `verticalScroll` | 内置防御性布局机制，确保界面在不同分辨率与屏幕方向下渲染稳定、不形变、不遮挡 |
| **演示资产与生产环境隔离** | Demo 代码与框架代码耦合，难以精准剔除残留的 Dead Code | **Demo 独立索引与自动化清理流水线 (Auto-Purge Pipeline)** | 执行预设流程即可实现 Demo 模块、路由挂载与资源的自动化剥离 |
| **交付验证与自主修复** | 缺乏标准化的确定性校验步骤，依赖人工反复排错 | 配置确定性编译命令 `.\gradlew.bat compileDebugKotlin` 与 **13 项自检清单** | Agent 在本地完成编译验证与自检闭环，降低人工排查与修复语法错误的成本 |

---

## 🌟 核心特性与技术方案

1. **编译期协议依赖裁剪 (Protocol Toggles)**：
   - 在 `gradle.properties` 中自由配置各协议开关；
   - 未启用的协议驱动（如 HiveMQ+Netty、Jedis 等）依赖 **100% 剔除不打包**，自动挂载零依赖 Stub 桩，保障符号完整与极致轻量。

2. **工业网络传输与超时保障**：
   - 针对工控/边缘设备配置明文放行（Cleartext Traffic）与局域网私有网段/自签名证书信任策略；
   - 普通请求维持 30s 快速失败；大文件上传下载专用通道自动延展至 **1 小时超时 (`HTTP_FILE_TRANSFER_TIMEOUT_SEC = 3600L`)**，杜绝慢速弱网中断。

3. **四段时序生命周期与防 OOM 日志**：
   - 协议通信统一输出 `[1/4 CONNECTING]` ➔ `[2/4 CONNECTED]` ➔ `[3/4 TRANSFER]` ➔ `[4/4 CLOSED]`；
   - 过滤大数据量多媒体/二进制流，超长日志（>4000字）自动分段，杜绝 Logcat 溢出与频繁 GC。

4. **多策略存储与系统级分享**：
   - `CacheLocationManager` 支持内部缓存、外部私有缓存与专属下载区动态切换（DataStore 持久化）；
   - `FileShareManager` 基于安全 `FileProvider` 实现跨进程文件与日志的一键原生分享。

5. **高可读设计系统与工业横屏适配**：
   - 遵循 WCAG AAA 规范，主文本对比度 > 15:1，提供高对比度日间/夜间模式（`ThemeManager` 一键响应式切换）；
   - 支持手机/平板/工业大屏单双栏自适应切换（`AdaptiveContentLayout`）。

6. **全场景标准化弹窗与诊断体系**：
   - 确认（`AppConfirmDialog`）、输入（`AppInputDialog`）、加载（`AppLoadingDialog`）、抽屉（`AppBottomSheetDialog`）风格统一，**标配右上角纯文字“关闭”按钮（无图标）**；
   - 异步耗时任务包装器（`launchWithLoading`）支持进度反馈与阻塞/非阻塞模式；
   - 结构化错误诊断（`ErrorParser` + `AppErrorDialog`）精准捕获网络与系统异常根因，支持现场一键分享排障报告。

---

## 🗂️ 项目工程结构

```
BaseAndroid2AIoT/
├── gradle/
│   └── libs.versions.toml             # 统一 Version Catalog 依赖声明
├── gradle.properties                  # 编译期协议裁剪开关与构建优化
├── AGENTS.md                          # ★ 面向 AI Agent 的最高权威开发指令集
├── core/                              # 基础框架模块 (Android Library，核心保护锁定)
│   └── src/
│       ├── main/
│       │   ├── res/                   # 框架公共资源 (多语言 strings.xml)
│       │   └── kotlin/com/base/iot/core/
│       │       ├── base/              # 统一基类 (BaseViewModel / UiContract)
│       │       ├── config/            # 全局配置中心 (AppConfig / IotProtocolConfig)
│       │       ├── diagnostics/       # 分段日志 (Lg)、错误解析 (ErrorParser)、崩溃捕获
│       │       ├── storage/           # 缓存策略 (CacheLocationManager)、文件分享
│       │       ├── network/           # HTTP 抽象 (HttpManager)
│       │       ├── iot/               # 物联网门面 (IotHub) 与驱动接口
│       │       └── ui/                # 主题 (AppTheme)、原子组件与统一弹窗
│       ├── protocol_http/ / protocol_http_stub/       # HTTP 真实驱动与零依赖桩
│       ├── protocol_mqtt/ / protocol_mqtt_stub/       # MQTT 真实驱动与零依赖桩
│       ├── protocol_redis/ / protocol_redis_stub/     # Redis 真实驱动与零依赖桩
│       └── protocol_socket/ / protocol_socket_stub/   # TCP Socket 真实驱动与零依赖桩
└── app/                               # 业务应用模块 (Application，业务主战场)
    └── src/main/kotlin/com/base/iot/
        ├── App.kt                     # 应用入口
        ├── MainActivity.kt            # 唯一宿主 Activity 与 Compose 挂载点
        └── feature/
            ├── template/              # ★ 新业务开发脚手架模板 (克隆源)
            └── demo/                  # 参考演示模块 ([DEMO_ACTIVE]，正式开发自动清退)
```

---

## 🔧 核心技术选型

| 层次 / 领域 | 核心选用方案 | 作用说明 |
| :--- | :--- | :--- |
| **编程语言与构建** | Kotlin 2.0 + Gradle 8 (KSP) | 现代响应式语法、Version Catalog 集中依赖与增量构建 |
| **界面与交互** | Jetpack Compose + Material 3 | 响应式 UI、WCAG AAA 高对比度设计系统与多端自适应布局 |
| **架构与异步** | MVVM + UDF (StateFlow / Coroutines) | 单向数据流、生命周期安全感知采集与全 IO 线程调度 |
| **依赖注入** | Google Dagger Hilt | 全局单例、基础设施注入与生命周期管控 |
| **数据持久化** | Jetpack DataStore | 类型安全的轻量配置、主题状态与缓存路径存储 |
| **网络与物联网通信** | IotHub (HTTP / MQTT / Redis / TCP Socket) | 统一通信门面，底层协议物理隔离与编译期驱动裁剪 |
| **诊断与分享** | ErrorParser + Lg + FileShareManager | 时序防 OOM 日志、结构化故障根因分析与系统级安全分享 |

> 完整第三方依赖库清单与法律合规说明详见根目录下 [`OPEN_SOURCE_LICENSES.md`](OPEN_SOURCE_LICENSES.md) 与 [`NOTICE`](NOTICE)。

---

## 🚀 编译与快速开始

### 1. 开发环境要求
- **JDK**: Java 17+ (推荐 Eclipse Adoptium OpenJDK 17)
- **Android SDK**: `compileSdk = 35`, `minSdk = 24`, `targetSdk = 34`
- **Gradle**: 8.11.1 (工程内置 `gradlew` 包装器)

### 2. 编译与打包
```bash
# 验证代码编译（无警告无错误）
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
- **第三方依赖合规**：详见 [OPEN_SOURCE_LICENSES.md](OPEN_SOURCE_LICENSES.md)。
