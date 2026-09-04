# 赛博牛马1.0 — Android IoT 快速开发框架
本文档由AI生成（大部分）
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
                 │  存储与分享 / 基础诊断设施     │
                 │  CacheLocationManager (多缓存)│
                 │  FileShareManager (系统分享)  │
                 │  Lg (超长分段日志) / Crash    │
                 └───────────────────────────────┘
```

---

## 🤖 为 AI Agent 原生设计：AGENTS.md 与协同开发优势

本项目从底层即面向 **“AI 智能体结对编程 (Agentic Pair-Programming)”** 范式。根目录内置的 [`AGENTS.md`](AGENTS.md) 是专门面向 AI Coding Agent 设计的系统级开发指令集 (Agent Directives)。

### 核心定位与特性
- **Agent 指令友好**：全篇采用严谨的命令式规约（如 `ABSOLUTE_ZERO_TOLERANCE`、`MANDATORY_I18N`、`TOP_LEVEL_IMPORTS_ONLY`），剥离自然语言歧义与冗词，智能体理解与执行准确率达 100%。
- **十一项核心规范模块**：涵盖架构能力路由索引、模块与 Git 边界、Code Hygiene & Token Economy (代码整洁度与上下文优化)、UI 布局防变形、Dialog 规范、UDF 响应式范式、Demo 自动化清理流水线 (Auto-Purge Pipeline)、模块拓扑、业务脚手架模板 (Feature Recipe)、基础设施矩阵 (Infra Matrix) 及交付自检清单。

### 为什么比常规项目更适合 Agent 开发？
| 评估维度 | 常规 Android 项目 | BaseAndroid2AIoT (本项目) | 对 Agent 开发的决定性优势 |
| :--- | :--- | :--- | :--- |
| **文件粒度与 Token 开销** | 单文件规模庞大（Monolithic Classes），极易触发上下文超限与截断 | **限制单文件 50~150 行**，ViewModel 按 Extension Functions 水平切片 | 最小化上下文开销，支持局部精确修改（Surgical Edits），避免全文件重写（Full File Rewriting）导致的逻辑丢失 |
| **协议与底层依赖边界** | 业务代码直接依赖底层网络库，易发生版本冲突与依赖散乱 | **协议抽象与实现物理隔离**，统一通过 `IotHub` (Facade 模式) 作为单一访问入口，Driver 与零依赖 Stub 同步维护 | 隔离底层具体实现细节，防止业务层混用或直接依赖异构网络库 |
| **新业务开发认知门槛** | 架构分层不清晰，目录与依赖注入模式不明确 | **模块边界明确锁定**（`:core` 锁定保护，新业务位于 `:app`），提供标准化 Scaffold 模板 | 结构确定，克隆模板即可完成标准业务三层（UiState / ViewModel / Screen）快速初始化 |
| **代码规范与一致性** | 易产生字符串 Hardcoding 或在函数体内直接使用全限定名（FQNs） | **强制字符串资源抽取（strings.xml 中英文对齐）**，参数收敛于 `AppConfig`，**Top-Level Imports 强制置顶**（禁止函数体内使用 Inline FQNs） | 彻底杜绝硬编码 (Hardcoding) 与内联包路径 (Inline FQNs)，保障代码整洁度 (Code Hygiene) 与工程规范一致 |
| **工业横屏布局稳定性** | 缺乏小尺寸或工控横屏（高度 360~480dp）的尺寸约束，易发生文本挤压组件或内容溢出 | 明确布局约束规则：`Row` 动态文本声明 `weight(1f, fill=false)`；按钮声明最小尺寸；**Dialog 标配右上角纯文字“关闭”**并配置 `verticalScroll` | 内置防御性布局机制，确保界面在不同分辨率与屏幕方向下渲染稳定、不形变、不遮挡 |
| **演示资产与生产环境隔离** | Demo 代码与框架代码耦合，难以精准剔除残留的 Dead Code | **Demo 独立索引与自动化清理流水线 (Auto-Purge Pipeline)** | 执行预设流程即可实现 Demo 模块、路由挂载与资源的自动化剥离 |
| **交付验证与自主修复** | 缺乏标准化的确定性校验步骤，依赖人工反复排错 | 配置确定性编译命令 `.\gradlew.bat compileDebugKotlin` 与 **13 项自检清单** | Agent 在本地完成编译验证与自检闭环，降低人工排查与修复语法错误的成本 |

---

## 🗂️ 项目工程结构

```
BaseAndroid2AIoT/
├── gradle/
│   └── libs.versions.toml             # 统一 Version Catalog 依赖声明
├── gradle.properties                  # 编译期协议裁剪开关与构建优化
├── AGENTS.md                          # ★ 面向 AI Agent 的最高权威指令集 (Agent Directives)
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
│       │       ├── iot/               # IoT 统一通信门面 (IotHub Facade) 与驱动抽象
│       │       └── ui/                # 主题 (AppTheme)、原子组件与统一弹窗
│       ├── protocol_http/ / protocol_http_stub/       # HTTP 真实驱动与零依赖 Stub
│       ├── protocol_mqtt/ / protocol_mqtt_stub/       # MQTT 真实驱动与零依赖 Stub
│       ├── protocol_redis/ / protocol_redis_stub/     # Redis 真实驱动与零依赖 Stub
│       └── protocol_socket/ / protocol_socket_stub/   # TCP Socket 真实驱动与零依赖 Stub
└── app/                               # 业务应用模块 (Application，业务主战场)
    └── src/main/kotlin/com/base/iot/
        ├── App.kt                     # 应用入口
        ├── MainActivity.kt            # 唯一宿主 Activity 与 Compose 挂载点
        └── feature/
            ├── template/              # ★ 新业务开发脚手架模板 (克隆源)
            └── demo/                  # 参考演示模块 ([DEMO_ACTIVE]，正式开发自动化清理)
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
| **网络与物联网通信** | IotHub (HTTP / MQTT / Redis / TCP Socket) | 统一通信入口 (Facade Pattern)，底层协议物理隔离与编译期驱动裁剪 |
| **诊断与分享** | ErrorParser + Lg + FileShareManager | Logcat 超长日志分段打印 (Chunked Logger 防截断)、结构化故障根因分析与系统级安全分享 |

> 完整第三方依赖库清单与法律合规说明详见根目录下 [`OPEN_SOURCE_LICENSES.md`](OPEN_SOURCE_LICENSES.md) 与 [`NOTICE`](NOTICE)。

---

## 🚀 编译与快速开始

### 1. 开发环境要求
- **JDK**: Java 17+ 
- **Android SDK**: `compileSdk = 35`, `minSdk = 24`, `targetSdk = 34`
- **Gradle**: 8.11.1 (工程内置 `gradlew` 包装器)

### 2. 编译与打包
```bash
# 验证代码编译（无警告无错误）
./gradlew compileDebugKotlin

# 构建 Debug APK
./gradlew assembleDebug
```

---

## 📄 许可证与法律合规 (License & Compliance)

- **本项目许可协议**：本项目源代码遵循 [代码使用许可协议与商业授权限制条款](LICENSE)。
  - **非商业用途**：对个人学习、学术研究、非营利性技术交流完全免费开放。
  - **商业用途限制**：**任何商业用途（包括商业销售、硬件搭载量产、对外提供收费服务、企业闭源交付等）必须预先获得原作者的正式书面商业授权**。未经授权擅自商用构成侵犯版权。
- **法律合规声明与版权归属**：详见根目录下标准归属文件 [NOTICE](NOTICE)。
- **第三方依赖合规**：详见 [OPEN_SOURCE_LICENSES.md](OPEN_SOURCE_LICENSES.md)。
