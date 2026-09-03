# BaseAndroid2AIoT · Agent 极速架构导航与开发手册 (Agent Architecture Handbook)

> **致 Agent 开发者**：本手册专为 AI 编程 Agent 量身定制。当你被调度到本项目进行代码检索、分析、Bug 修复或新功能开发时，**优先阅读本文档**即可秒级掌握全局架构拓扑、基建 API 入口与 3 步起手式开发范式，彻底消除盲目搜索与 Token 浪费。

---

## 🗺️ 架构拓扑与能力速查索引 (Where is What)

```
app/src/main/kotlin/com/base/iot/
├── core/
│   ├── base/                  # [核心基类] Agent 新建功能的第一入口
│   │   ├── BaseViewModel.kt   # 统一状态、耗时进度弹窗驱动、真实错误拦截与分享
│   │   └── UiContract.kt      # IUiState 与 IUiEvent 状态与事件契约
│   ├── iot/                   # [物联网协议门面]
│   │   ├── IotHub.kt          # ★ 统一聚合入口：http / mqtt / redis / socket / config
│   │   ├── MqttManager.kt     # MQTT 异步客户端抽象接口
│   │   ├── RedisManager.kt    # Redis 远控客户端抽象接口
│   │   └── SocketManager.kt   # TCP Socket 工业长连接抽象接口
│   ├── network/               # [HTTP 网络层]
│   │   └── HttpManager.kt     # GET/POST/PUT/DELETE 及分块上传、流式下载
│   ├── storage/               # [存储与分享]
│   │   ├── CacheLocationManager.kt # 自定义缓存区切换（内部/外部/下载区）
│   │   └── FileShareManager.kt     # 系统原生文件分享与错误文本分享
│   ├── diagnostics/           # [诊断与防护]
│   │   ├── Lg.kt              # 超长日志分段打印 (防截断、防内存溢出)
│   │   ├── ErrorParser.kt     # 结构化真实错误诊断与多维堆栈报告生成器
│   │   └── CrashHandler.kt    # 全局未捕获异常落盘
│   └── ui/                    # [统一设计系统与组件库]
│       ├── components/        # ★ 原子化全局通用组件 (AppCard / AppButton / AppSwitchRow)
│       ├── dialog/            # ★ 统一弹窗套件 (AppProgressDialog / AppErrorDialog / XPopupBridge)
│       └── theme/             # 主题持久化管理器 (ThemeManager)
├── ui/theme/                  # [设计系统令牌] AppTheme.colors (Dark/Light 高对比度)
└── feature/demo/              # [业务示例特性模块]
    ├── DashboardScreen.kt     # 仅 180 行的轻量骨架编排器
    ├── DashboardViewModel.kt  # 继承 BaseViewModel 的干净业务层
    └── components/            # ★ 业务组件解耦小文件 (每个仅 50~80 行，极低 Token 消耗)
```

---

## 🚀 Agent 极速开发范式（3 步新建一个业务功能）

当用户要求 Agent 新建一个功能模块（如 `DeviceControlScreen`）时，**严格按以下 3 步起手**，零多余样板代码：

### Step 1: 定义 State & Event（继承 `IUiState` 与 `IUiEvent`）
```kotlin
data class DeviceControlUiState(
    val deviceName: String = "PLC-01",
    val isRunning: Boolean = false,
    // 必须实现 IUiState 的 3 个标准属性：
    override val loadingConfig: LoadingConfig = LoadingConfig(),
    override val parsedError: ParsedError? = null,
    override val showErrorDialog: Boolean = false
) : IUiState

sealed class DeviceControlEvent : IUiEvent {
    data class ShowToast(val msg: String) : DeviceControlEvent()
}
```

### Step 2: 编写 ViewModel（继承 `BaseViewModel`，注入 `IotHub`）
```kotlin
@HiltViewModel
class DeviceControlViewModel @Inject constructor(
    application: Application,
    val iotHub: IotHub, // ★ 直接注入 IotHub，自动享有所有网络与协议能力
    fileShareManager: FileShareManager
) : BaseViewModel<DeviceControlUiState, DeviceControlEvent>(
    application = application,
    fileShareManager = fileShareManager,
    initialState = DeviceControlUiState()
) {
    override fun updateLoadingConfig(reducer: (LoadingConfig) -> LoadingConfig) {
        _uiState.update { it.copy(loadingConfig = reducer(it.loadingConfig)) }
    }
    override fun updateErrorState(error: ParsedError?, visible: Boolean) {
        _uiState.update { it.copy(parsedError = error, showErrorDialog = visible) }
    }

    // ★ 任何耗时操作直接调用 launchWithLoading，自动享有进度弹窗与真实错误拦截！
    fun startMotor() = launchWithLoading(title = "正在启动主电机...", isBlocking = true) {
        iotHub.socket.send("START_MOTOR_CMD")
    }
}
```

### Step 3: 组装 Compose Screen（复用 `AppCard` / `AppButton`，挂载标准弹窗）
```kotlin
@Composable
fun DeviceControlScreen(viewModel: DeviceControlViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = AppTheme.colors.background) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            AppCard(title = "电机控制面板", icon = Icons.Filled.ElectricBolt) {
                AppButton(
                    text = "启动主电机",
                    color = AppTheme.colors.accentGreen,
                    onClick = viewModel::startMotor
                )
            }
        }

        // ★ 必须挂载进度弹窗与错误弹窗（两行标准代码）：
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

## 🛑 Agent 五大核心工程红线 (Five Red Lines)

1. **协议裁剪红线 (SourceSets Isolation)**：
   - `src/main/` 属于通用模块，**绝对严禁直接 import 协议实现库**（如 `retrofit2.*`、`hivemq.*`、`jedis.*`）！
   - 所有协议通信必须通过 `IotHub` 或 `HttpManager`/`MqttManager` 抽象接口调用，保证 `gradle.properties` 关闭任何协议开关时 `src/main/` 均能 100% 独立编译。
2. **高对比度双模红线 (WCAG AAA)**：
   - 严禁硬编码颜色值（`Color(0xFF...)`），所有 UI 颜色必须统一读取 `AppTheme.colors.*` 设计系统令牌。
   - 新增组件必须在普通模式 (Light) 与夜间模式 (Dark) 下均保证主文本对比度 > 15:1，次要文本 > 7:1。
3. **弹窗统一技术栈规范**：
   - Compose 界面必须使用 `core/ui/dialog/` 下的统一组件；
   - 原生 View 界面必须调用 `XPopupBridge`；
   - 错误弹窗必须为**非阻塞式**，必须打印真实错误堆栈，必须包含系统分享按钮。
4. **日志防 OOM 铁律**：
   - 超长文本必须调用 `Lg` 分段打印；
   - **绝对严禁在 Logcat 中打印二进制、音视频流、大文件或 Base64 字节**；
   - 文件传输仅允许打印文件名、文件字节大小与落盘路径。
5. **Token 经济学与切片操作**：
   - 业务界面已拆分为 `feature/demo/components/` 下的多个微组件，修改时**只需定位并读取具体的 50~80 行微组件文件**，严禁大范围无脑全读。
   - 代码改动统一使用局部替换，绝不盲目全文件覆写。
