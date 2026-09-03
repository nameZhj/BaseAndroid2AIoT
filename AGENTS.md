# BaseAndroid2AIoT · AI Agent 全局开发架构与行为准则手册

> **定位与目标**：本手册是 `BaseAndroid2AIoT` 企业级 Android 物联网架构的唯一权威 AI 开发规范。所有在此工程执行需求分析、代码修改、重构、Bug 修复或新增功能的 AI Agent，**必须严格无条件遵守本手册**。核心理念：**极简主义、Token 经济学、100% 架构贴合、高对比度可读性、零编译破坏**。

---

## 🛑 一、五大核心工程铁律 (Non-Negotiable Directives)

### 1. 远端推送铁律 (Git Remote Restriction)
- **未经用户在当前会话中明确要求，绝对禁止执行 `git push`**。
- 所有代码改动仅在本地修改、本地编译验证（`compileDebugKotlin` / `assembleDebug`）并在需要时进行本地 git commit。只有当用户明确要求“推送到远端”时方可执行推送。

### 2. 协议裁剪与物理隔离红线 (SourceSets Isolation)
- `src/main/` 属于通用业务与 UI 宿主，**绝对严禁直接 import 具体协议驱动库**（如 `retrofit2.*`、`okhttp3.*`、`com.hivemq.*`、`redis.clients.jedis.*`）！
- 所有网络与协议通信必须通过 `IotHub` 或通用抽象接口（`HttpManager`, `MqttManager`, `RedisManager`, `SocketManager`）调用，确保 `gradle.properties` 关闭任何协议开关时，`src/main/` 均能 100% 独立编译。
- **驱动与桩实现必须严格对齐**：修改协议接口时，必须同步修改 `src/protocol_<proto>/`（真实驱动）与 `src/protocol_<proto>_stub/`（零依赖桩实现），保持相同类名与签名。
- **依赖版本单一权威源**：所有依赖必须在 `gradle/libs.versions.toml` 中声明并通过 `libs.xxx` 引用，严禁在构建脚本中硬编码坐标。
- **代码调用协议默认锁定常开铁律**：`gradle.properties` 是协议编译期的唯一配置源。只要协议被编译入包并在代码中调用（`isCompiled == true`），运行期必须默认锁定为常开，无法在运行期关闭，彻底杜绝误关导致业务中断。

### 3. WCAG AAA 级高对比度双模红线
- **严禁硬编码颜色值**（`Color(0xFF...)`），所有 UI 元素必须统一读取 `AppTheme.colors.*` 设计系统令牌（`surface`, `background`, `textPrimary`, `textSecondary`, `accentCyan`, `cardBorder` 等）。
- **普通模式 (Light)**：纯白卡片与清晰边框，主文字对比度 > 15:1，次级文字 > 7:1。
- **夜间模式 (Dark)**：黑曜石底色与暗夜蓝卡片，主文字为荧光皓白（> 15:1），次级文字为浅灰蓝（> 7:1）。
- 新增任何组件必须同时保证在 Light / Dark 双模下清晰可见，严禁浅底浅字或深底深字。

### 4. 日志防 OOM 与生命周期时序铁律
- **四段生命周期时序**：协议收发必须按时序记录：`[1/4 CONNECTING]` ➔ `[2/4 CONNECTED]` ➔ `[3/4 TRANSFER]` ➔ `[4/4 CLOSED/DISCONNECTED]`。
- **严禁在 Logcat 中打印二进制、音视频流、大文件或 Base64 字节**，防止内存溢出与 GC 停顿。
- 文件上传与下载仅允许记录：**文件名、文件字节大小、缓存路径、耗时与 HTTP 状态码**。超长文本必须调用 `Lg` 分段打印。

### 5. 零硬编码与多语言国际化红线 (Zero Hardcoding & I18n Directives)
- **UI 文本字典化 (Zero Hardcoded String)**：所有呈现给用户的界面文本、弹窗提示、按钮标签与状态说明，**绝对严禁**在 Composable 或 Java/Kotlin 逻辑中硬编码字面量！必须录入 `res/values/strings.xml`（默认中文）并在 `res/values-en/strings.xml`（英文）中对齐，在 Compose 中统一调用 `stringResource(R.string.xxx)` 读取。
- **视觉色彩令牌化 (Zero Hardcoded Color)**：无论是 Compose 组件还是原生 View / Adapter，严禁硬编码颜色值（`Color(0xFF...)` 或十六进制整型 `0xFF...`），必须统一读取 `AppTheme.colors.*` 语义令牌（原生 View 可使用 `palette.xxx.toArgb()`）。
- **配置与端点收拢 (Centralized Configuration)**：严禁在业务逻辑与 ViewModel 中散落硬编码 URL、IP、Port、MQTT Topic 或超时数值，必须统一下沉至 `AppConfig` 单一权威配置类中统一管理。

---

## ⚡ 二、Token 经济学与高能效开发准则 (Token Economy)

1. **按需切片探索，严禁盲目全读**：
   - 查阅代码时优先使用 `grep_search` 定位行号，配合 `view_file` 指定 `StartLine` 与 `EndLine` 精准按需读取，严禁盲目全量加载大文件。
   - 检索时必须指定精确关键词与文件通配规则（如限定 `*.kt` 或具体目录），杜绝全盘无差别扫描。
2. **外科手术式精确改动 (Surgical Edits)**：
   - 严禁对大文件全量覆写，必须使用 `replace_file_content` 定向替换差异代码块，大幅节约 Diff 与 I/O 消耗。
3. **微组件化拆分标准**：
   - 单个 Composable 函数或业务文件严格控制在 **50~150 行**。超过必须拆解至 `components/` 目录下，保证后续 Agent 单次读写仅消耗极低 Token。
4. **极简注释准则 (Lean Commenting)**：
   - **严禁编写自解释废话注释**（如 `// 发送事件`、`// 获取实例`、`// 更新状态`）。
   - **严禁添加装饰性符号大横幅**（如 `// ==================== xxx ====================`）。
   - **仅保留高信噪比关键信息**：架构隔离红线、非显而易见的参数语义（如 `isBlocking`）、复杂协议状态与异常边界。
5. **高信噪比沟通与交付**：
   - 汇报直奔核心决策、改动点、影响范围与验证指引，杜绝冗长车轱辘话与背景复述。

---

## 🗺️ 三、架构拓扑与能力速查 (Where is What)

```
app/src/main/kotlin/com/base/iot/
├── core/
│   ├── base/                  # [核心基类] 新建功能第一入口
│   │   ├── BaseViewModel.kt   # 统一状态流、耗时进度弹窗、真实错误拦截与系统分享
│   │   └── UiContract.kt      # IUiState 与 IUiEvent 标准接口契约
│   ├── iot/                   # [物联网协议门面]
│   │   ├── IotHub.kt          # ★ 核心门面：聚合 http / mqtt / redis / socket / config
│   │   ├── MqttManager.kt     # MQTT 异步客户端抽象
│   │   ├── RedisManager.kt    # Redis 远控客户端抽象
│   │   └── SocketManager.kt   # TCP Socket 原生工业长连接抽象
│   ├── network/               # [HTTP 网络层]
│   │   └── HttpManager.kt     # GET/POST/PUT/DELETE 及分块上传、流式下载
│   ├── storage/               # [存储与系统分享]
│   │   ├── CacheLocationManager.kt # 自定义缓存区切换（内部私有/外部私有/专属下载区）
│   │   └── FileShareManager.kt     # 系统原生文件分享与错误文本报告分享
│   ├── diagnostics/           # [诊断与防护]
│   │   ├── Lg.kt              # 超长日志分段打印 (防截断/防OOM)
│   │   ├── ErrorParser.kt     # 结构化异常解析与全量诊断报告生成器
│   │   └── CrashHandler.kt    # 全局未捕获异常落盘与崩溃日志导出
│   └── ui/                    # [统一设计系统与弹窗]
│       ├── components/        # ★ 原子化高对比度组件 (AppCard / AppButton / AppSwitchRow)
│       ├── dialog/            # ★ 统一弹窗套件 (AppProgressDialog / AppErrorDialog / AppConfirmDialog / XPopupBridge)
│       └── theme/             # 主题持久化 (ThemeManager)
├── ui/theme/                  # [设计系统令牌] AppTheme.colors (Dark/Light 动态响应)
├── feature/template/          # ★ [开箱即用脚手架模板] (每个约30行，新建业务直接参考克隆)
│   ├── TemplateUiState.kt
│   ├── TemplateViewModel.kt
│   └── TemplateScreen.kt
└── feature/demo/              # [演示特性模块]
    ├── DashboardScreen.kt     # 180 行轻量骨架编排器
    ├── DashboardViewModel.kt  # 业务控制器
    └── components/            # 7 个高内聚微组件 (每个 50~80 行，极低 Token 消耗)
```

---

## 🚀 四、Agent 极速开发范式（3 步新建一个业务功能）

Agent 在新建业务功能时，直接复用 `feature/template/` 模板，遵循 3 步起手式：

### Step 1: 定义 State & Event（继承 `IUiState` 与 `IUiEvent`）
```kotlin
data class MyFeatureUiState(
    val title: String = "My Feature",
    val isRunning: Boolean = false,
    // 实现 IUiState 的 3 个标准属性：
    override val loadingConfig: LoadingConfig = LoadingConfig(),
    override val parsedError: ParsedError? = null,
    override val showErrorDialog: Boolean = false
) : IUiState

sealed class MyFeatureEvent : IUiEvent {
    data class ShowToast(val msg: String) : MyFeatureEvent()
}
```

### Step 2: 编写 ViewModel（继承 `BaseViewModel`，注入 `IotHub`）
```kotlin
@HiltViewModel
class MyFeatureViewModel @Inject constructor(
    application: Application,
    val iotHub: IotHub, // ★ 注入 IotHub，直接享有所有协议能力
    fileShareManager: FileShareManager
) : BaseViewModel<MyFeatureUiState, MyFeatureEvent>(
    application = application,
    fileShareManager = fileShareManager,
    initialState = MyFeatureUiState()
) {
    override fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig) {
        _uiState.update { it.copy(loadingConfig = reducer(it.loadingConfig)) }
    }
    override fun updateErrorState(error: ParsedError?, visible: Boolean) {
        _uiState.update { it.copy(parsedError = error, showErrorDialog = visible) }
    }

    // ★ 调用 launchWithLoading 自动享有进度弹窗与非阻塞真实错误拦截！
    fun startTask() = launchWithLoading(title = "正在通信中...", isBlocking = true) { updateProgress ->
        updateProgress(0.5f, "已完成 50%")
        iotHub.socket.send("COMMAND")
    }
}
```

### Step 3: 组装 Compose Screen（复用 `AppCard` / `AppButton`，挂载标准弹窗）
```kotlin
@Composable
fun MyFeatureScreen(viewModel: MyFeatureViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = AppTheme.colors.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            AppCard(title = uiState.title, icon = Icons.Filled.ElectricBolt) {
                AppButton(text = "执行指令", color = AppTheme.colors.accentPrimary, onClick = viewModel::startTask)
            }
        }

        // ★ 必须挂载的两行标准弹窗：
        AppProgressDialog(config = uiState.loadingConfig, onDismissRequest = viewModel::dismissLoading)
        AppErrorDialog(
            visible = uiState.showErrorDialog,
            error = uiState.parsedError,
            onDismiss = viewModel::dismissError,
            onShareReport = viewModel::shareErrorReport
        )
    }
}
```

---

## 🛠️ 五、核心基建设施与使用规范

| 基建模块 | 关键类与调用方式 | 核心特性与注意事项 |
| :--- | :--- | :--- |
| **异步进度控制** | `BaseViewModel.launchWithLoading` | `isBlocking: Boolean`（true=防点击穿透/不可取消，false=可随时轻触外部取消）；支持 0%~100% 动态百分比上报。 |
| **真实错误诊断** | `ErrorParser.parse()` + `AppErrorDialog` | 拒绝模糊报错。诊断报告包含网络状态、设备信息与全量根因堆栈；必须为非阻塞式；内置系统一键分享。 |
| **多协议通信** | `IotHub` (`iotHub.http/mqtt/redis/socket/config`) | 统一注入门面。HTTP 大文件传输采用独立通道，超时自动提升至 1 小时 (`HTTP_FILE_TRANSFER_TIMEOUT_SEC = 3600L`)，常规请求维持 30s。 |
| **自定义缓存** | `CacheLocationManager` | 支持动态切换内部缓存、外部私有缓存与公共下载区，路径持久化至 DataStore。 |
| **统一文件分享** | `FileShareManager` | 基于 `FileProvider` 实现沙箱跨进程安全分享（APK、日志、下载数据包及错误诊断文本）。 |
| **高对比度 UI** | `AppCard`, `AppButton`, `AppSwitchRow` | 强制统一读取 `AppTheme.colors.*` 令牌，自带微动效与高对比度边框。 |

---

## 📋 六、严格闭环验证自检清单 (Pre-delivery Checklist)

在向用户交付成果前，必须逐项自检：
- [ ] **远端推送检查**：是否确认**未擅自执行 `git push`**（除非用户在当前提示词中显式明确要求）？
- [ ] **检索与能效检查**：是否精准定向切片阅读？是否使用了 `replace_file_content` 外科手术式改动？
- [ ] **零硬编码与国际化检查**：是否确认 UI 文本 100% 抽取至 `strings.xml`（中英双语对齐）？是否杜绝了颜色与端点硬编码？
- [ ] **编译隔离检查**：`src/main/` 是否 100% 零具体协议驱动三方库 import？
- [ ] **代码风格与注释检查**：是否清理了所有自解释废话注释与无用符号分割线？
- [ ] **设计系统对比度检查**：所有 UI 是否统一读取 `AppTheme.colors.*`？在普通与夜间模式下对比度是否均 > 7:1？
- [ ] **错误与进度弹窗检查**：错误弹窗是否为非阻塞式并包含真实堆栈与系统分享按钮？
- [ ] **可编译性验证**：是否成功通过 `.\gradlew.bat compileDebugKotlin`（轻量）与 `.\gradlew.bat assembleDebug`（全量打包）？
