# BaseAndroid2AIoT 专属 Agent 开发行为准则 (Project-Specific Behavioral Directives)

> **定位与目标**：本文件为 `BaseAndroid2AIoT` 企业级 Android 物联网脚手架工程专属的资深架构师兼高级开发 Agent 行为规范。所有参与本工程代码编写、缺陷修复、架构重构或功能迭代的 Agent 必须无条件严格遵守。**首次进入项目请优先阅读 [AGENT_ARCHITECTURE.md](file:///e:/code/Android/BaseAndroid2AIoT/AGENT_ARCHITECTURE.md)**。核心理念：**极简主义、Token 经济学、100% 工程架构贴合、高对比度可读性、零编译破坏**。

---

### 1. Token 经济学与高能效精准检索 (Token Economy & Precision Retrieval)
- **微组件切片探索，严禁盲目全读**：
  - 业务模块已全部拆分为 `feature/demo/components/` 下的微组件（每个仅 50~80 行）。Agent 维护或扩展业务时，**只能定向加载具体的微组件文件**，严禁一次性加载大文件，单次操作节约 80% 以上 Token。
  - 检索代码时必须指定精确关键字与通配过滤（如限定 `*.kt` 或特定目录 `app/src/main/`），严禁进行宽泛无差别的全盘扫描。
- **基类复用，零影子代码**：
  - 新建任何页面必须继承 `BaseViewModel` 并注入 `IotHub`，必须复用全局通用组件 `AppCard` 与 `AppButton`，严禁重复编写加载弹窗控制或私有组件。
- **外科手术式精确改动 (Surgical Edits)**：
  - 严禁对大文件进行全量覆盖重写，必须使用 `replace_file_content` 定向替换差异代码块，最大限度节约 Diff 与文件 I/O 带来的 Token 消耗。
- **高信噪比交付 (High Signal-to-Noise Communication)**：
  - 回复与汇报需保持高度精炼与专业，直奔核心决策、改动点、影响范围与验证指引，杜绝冗长车轱辘话、无意义寒暄或大段复述已知背景。
- **阶梯式渐进验证以节省算力**：
  - 语法/依赖校验优先执行轻量级任务 `.\gradlew.bat compileDebugKotlin`（15s 内完成）；
  - 全量交付或打包验证时才执行 `.\gradlew.bat assembleDebug`。

---

### 2. 架构拓扑与协议裁剪隔离红线 (Compile-Time Pruning & SourceSets Isolation)
- **严禁在 `src/main/` 中直接依赖协议驱动三方库**：
  - `src/main/` 是通用业务与 UI 宿主，**只能依赖协议抽象接口**（`HttpManager`, `MqttManager`, `RedisManager`, `SocketManager`）；
  - **绝对红线**：严禁在 `src/main/` 的任何代码中直接 import 具体协议驱动库（如 `retrofit2.*`、`okhttp3.*`、`com.hivemq.*`、`redis.clients.jedis.*`）！必须确保 `gradle.properties` 中任何协议开关关闭时，`src/main` 都能无依赖独立编译通过。
- **驱动实现与桩实现成对同步 (SourceSets Parity)**：
  - 协议具体驱动位于 `src/protocol_<proto>/`，零依赖桩实现位于 `src/protocol_<proto>_stub/`；
  - 一旦修改协议接口方法签名，**必须同时同步修改对应的 Real 实现与 Stub 实现**，保持相同包名与类名，杜绝任何一侧编译报错。
- **依赖版本单一权威源**：
  - 所有依赖库与版本号必须统一在 `gradle/libs.versions.toml` 中声明并通过 `libs.xxx` 引用，严禁在 `build.gradle.kts` 中硬编码库坐标或版本字符串。

---

### 3. 全局统一设计系统 (Design System) 与普通/夜间双模式规范
- **严禁硬编码颜色与私有调色板**：
  - 所有 UI 元素必须统一引用 `AppTheme.colors.*` 设计系统令牌（如 `surface`, `background`, `textPrimary`, `textSecondary`, `accentCyan`, `cardBorder` 等）；
  - 严禁直接写死 `Color(0xFF...)`，严禁在 Composable 内部定义孤立的私有颜色常量。
- **WCAG AAA 级高对比度红线（绝不出现文字与底色相近看不清）**：
  - **普通模式 (Light)**：卡片纯白配合明朗边框，主标题文字为 Slate-900（对比度 > 15:1），辅助文字为 Slate-700（对比度 > 7:1），科技色为深青；
  - **夜间模式 (Dark)**：黑曜石底色配合暗夜蓝卡片，主标题文字为荧光皓白（对比度 > 15:1），辅助文字为浅灰蓝（对比度 > 7:1），科技色为霓虹青；
  - 新增任何按钮、卡片、列表项时，必须同时在 Light / Dark 两种模式下校验文字与底色反差，严禁浅底浅字、深底深字。
- **主题联动与系统状态栏自适应**：
  - 主题持久化必须接入 `ThemeManager`（基于 DataStore 响应式流）；
  - 系统状态栏与导航栏图标由 `SystemBarManager` 与主题状态联动动态反转（暗色底配浅色图标，浅色底配深色图标，绝不冲突）。

---

### 4. 弹窗组件、耗时进度与真实错误诊断体系规范
- **弹窗统一技术栈规范**：
  - Compose 页面：必须复用 `core/ui/dialog/` 下的统一弹窗体系（`AppConfirmDialog`, `AppProgressDialog`, `AppInputDialog`, `AppBottomSheetDialog`, `AppErrorDialog`）；
  - 原生 View/Activity 混合栈页面：必须通过 `XPopupBridge` 呼出，严格保持设计规范与 Dark/Light 主题一致。
- **耗时异步任务进度规范 (`launchWithLoading`)**：
  - 所有耗时操作必须封装为可通过配置控制的形态：
    1. **自主选择是否启用进度窗 (`showLoading: Boolean`)**：支持前台弹窗或后台静默执行；
    2. **自主选择阻塞式与非阻塞式 (`isBlocking: Boolean`)**：
       - **阻塞式 (`isBlocking = true`)**：不可点击外部、不可穿透、不可返回键取消，专为握手协商、密钥写入、固件 OTA 刷写等不可打断的关键操作提供安全防护；
       - **非阻塞式 (`isBlocking = false`)**：允许轻触外部或点击取消按钮，协同取消底层协程 Job；
    3. **双进度形态自适应**：循环转圈 (Indeterminate) 与 0%~100% 精确百分比模式。
- **非阻塞真实错误诊断与一键系统分享**：
  - 严禁向用户抛出模糊的“网络开小差”等无用提示；
  - 必须使用 `ErrorParser.parse()` 自动解析出包含时间戳、网络状态、设备型号与全量根因堆栈的真实报告；
  - 错误弹窗必须为**非阻塞式**（`dismissOnClickOutside = true`），且必须包含“**分享错误报告**”按钮，通过 `FileShareManager.shareText()` 唤起系统级原生分享面板。

---

### 5. 时序日志与防 Logcat 溢出/防 OOM 规范
- **四段有序生命周期日志**：
  所有协议连接与通信必须遵循时序：`[1/4 CONNECTING]` -> `[2/4 CONNECTED]` -> `[3/4 TRANSFER]` -> `[4/4 CLOSED/DISCONNECTED]`。
- **防 Logcat 溢出与防 OOM 铁律**：
  - 超长文本日志必须使用 `Lg.d/i/w/e` 分段输出；
  - **严禁向 Logcat 打印任何二进制文件流、音视频流、原始字节数组或大 Base64 数据**；
  - 文件上传/下载在日志中**仅允许打印文件名、文件字节大小、缓存落盘路径与耗时摘要**。

---

### 6. 防御性工程与资源成对释放
- **I/O 与通信句柄成对释放**：
  涉及 Socket 句柄、文件流、Channel、协程 Job 与广播监听器，必须在 `onCleared()` 或生命周期解绑时成对关闭并置空，杜绝内存泄漏和端口占用。
- **Android 系统级风险防御**：
  遵循 `network_security_config.xml`（明文放行与自签私网 IP 放行）、`FileProvider` 沙箱共享路径及 `WAKE_LOCK` 防休眠长连接掉线保障。

---

### 7. 严格闭环自检清单 (Pre-delivery Checklist)
在向用户交付成果或推送到 Git 远端前，必须对照本清单逐项自检：
- [ ] **检索与能效**：阅读是否按需切片？改动是否外科手术式局部替换？
- [ ] **编译裁剪安全**：`src/main/` 是否坚决未直接引用具体协议三方库？协议接口改动是否同步了 Stub 实现？
- [ ] **高对比度双模**：所有新增 UI 是否全部使用 `AppTheme.colors.*`？在普通与夜间模式下文字与底色对比度是否均 > 7:1，无任何模糊相近？
- [ ] **耗时与错误弹窗**：耗时操作是否支持阻塞/非阻塞可配？错误弹窗是否为非阻塞式并包含真实堆栈与一键系统分享？
- [ ] **日志防 OOM**：是否杜绝了在 Logcat 中打二进制/大数据？是否使用了 `Lg` 分段打印？
- [ ] **编译构建验证**：是否成功通过 `.\gradlew.bat compileDebugKotlin` / `assembleDebug` 验证？
- [ ] **远端同步检查**：README.md 与版本日志是否同步更新？代码与文档是否已正确推送至 GitHub 远端？