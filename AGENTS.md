# AGENT_DIRECTIVES: BaseAndroid2AIoT
VERSION: 3.2 | TARGET: LLM_AGENT | TASK_ROUTED: TRUE

## [0_AGENT_TASK_ROUTING]
- Read this file first, then load only the Skill(s) relevant to the task.
- Adaptive UI: `.agent/skills/adaptive-layout/SKILL.md`
- Theme and components: `.agent/skills/theme-system/SKILL.md`
- Robot dashboard, repository, map coordinates and device integration: `.agent/skills/robot-dashboard/SKILL.md`
- Keep changes scoped; inspect callers before changing shared APIs.
- Verify with the narrowest relevant tests, then run `compileDebugKotlin` when build access is available.

## [0_CAPABILITY_ROUTING_INDEX]
0.1 KEYWORD_TO_FILE_ROUTER (Zero Search Overhead | High-Efficiency Architectural Router):
| 业务场景 / 需求关键字 | 核心目标路径 (Target Files / Packages) | 核心能力职责 |
| :--- | :--- | :--- |
| **HTTP / 上传下载 / 超时** | `core/.../network/HttpManager.kt` | 统一网络请求、1h 大文件流式传输 |
| **MQTT / HiveMQ / 订阅发布** | `core/.../iot/MqttManager.kt` | 物联网消息推送、Topic 监听与心跳 |
| **Redis / Jedis / 控制指令** | `core/.../iot/RedisManager.kt` | 内存高速指令通道与状态同步 |
| **TCP Socket / 工业长连接** | `core/.../iot/SocketManager.kt` | 原始 TCP 通信、心跳与断线重连 |
| **缓存路径 / SD卡 / 下载存储** | `core/.../storage/CacheLocationManager.kt` | DataStore 驱动的多存储路径动态切换 |
| **系统分享 / 崩溃报告 / 沙箱** | `core/.../storage/FileShareManager.kt` | FileProvider 原生安全跨进程分享 |
| **超长分段日志 / Logcat 防截断** | `core/.../diagnostics/Lg.kt` | 4000 字符自动分片输出与时序标记 |
| **错误诊断 / 根因解析 / 报错弹窗** | `core/.../diagnostics/ErrorParser.kt` | 系统/网络/硬件多维故障画像分析 |
| **主题日夜间 / 沉浸式系统栏** | `core/.../ui/theme/` & `MainActivity.kt` | WCAG AA 基线、状态栏图标适配 |
| **通用原子组件 / 弹窗体系** | `core/.../ui/components/` & `ui/dialog/` | AppCard, AppButton, AppProgressDialog 等 |
| **平板兼容 / Pad适配 / 双栏导航** | `core/.../ui/pad/` | Pad设备判定、弹窗防拉伸、主从双栏、侧边导航轨 |
| **Pad自适应规格 / 操纵杆 / 双栏** | `core/.../ui/adaptive/` | 平板端尺寸规范 (PadSpec)、受限宽度修饰符与响应式双栏 |
| **USB 串口 / 硬件直连 / CH340 / CDC** | `core/.../iot/SerialManager.kt` & `AndroidUsbSerialDriver.kt` | 本地 USB-Serial OTG 与远程 HTTP 串口双模通信、即插即用 |
| **新业务脚手架 (Scaffold)** | `app/.../feature/template/` | 业务克隆模板 (UiState, ViewModel, Screen) |
| **机器人巡检 / 地图坐标 / 任务 / 事件** | `app/.../feature/robot/` + `.agent/skills/robot-dashboard/SKILL.md` | Repository -> ViewModel -> StateFlow；地图原点、旋转、分辨率和 viewport 坐标转换 |

0.2 ROUTER_MAINTENANCE_RULES (Prevent Token Tax & Write Amplification):
    - TRIGGERS: Synchronously update this table ONLY when:
      1. Creating a new business feature via template clone (`feature/<name>/`).
      2. Adding a new infrastructure manager or protocol driver in `:core`.
      3. Promoting a production feature to the app mount point or removing an obsolete feature.
    - BYPASS: STRICTLY FORBIDDEN to update this table for bugfixes, UI tweaks, business logic updates, or strings.xml changes.

## [1_MODULE_AND_GIT_BOUNDARIES]
1.1 GIT_SAFETY:
    - NEVER execute `git push` unless explicitly commanded by user in active prompt.
    - Default to local changes. Push or update a remote branch only when the user explicitly requests it; direct-main edits require an explicit request and a verified base SHA.
1.2 PROTOCOL_ISOLATION:
    - FORBIDDEN in `src/main/`: `import retrofit2.*`, `import okhttp3.*`, `import com.hivemq.*`, `import redis.clients.jedis.*`.
    - ROUTING: All networking MUST route via `IotHub` or abstract managers (`HttpManager`, `MqttManager`, `RedisManager`, `SocketManager`).
    - DUAL_SYNC: Modifying protocol API requires synchronous updates to BOTH `src/protocol_<proto>/` (real driver) and `src/protocol_<proto>_stub/` (zero-dep stub).
    - DEPENDENCIES: Single source of truth is `gradle/libs.versions.toml` (`libs.xxx`).
    - RUNTIME_LOCK: If `isCompiled == true`, protocol switch is permanently locked ON at runtime.
1.3 MODULE_PLACEMENT:
    - NEW_FEATURE_PLACEMENT: 100% of new business feature code, UI screens, ViewModels, and state models MUST be developed in `:app` (`app/src/main/kotlin/com/base/iot/feature/<name>/`).
    - CORE_INTEGRITY_LOCK: DO NOT tamper with or modify existing base framework code in `:core` (`core/src/main/kotlin/com/base/iot/core/`) unless strictly necessary.
    - CORE_EXTENSION_PERMISSION: Only when adding new infrastructure-level capabilities, global protocols, cross-feature shared utilities, or design system tokens, should code be added to `:core`.

## [2_CODE_HYGIENE_AND_TOKEN_ECONOMY]
2.1 FORBID_HARDCODED_STRINGS:
    - ABSOLUTE_ZERO_TOLERANCE: Zero raw string literals in UI, ViewModels, Dialogs, or Components.
    - FORBIDDEN:
      - Hardcoded UI text: `Text("确认")`, `Button(text = "Cancel")`, `title = "Settings"`.
      - Hardcoded status / toast / log: `appendLog("下载成功")`, `Toast.makeText(..., "Error", ...)`.
      - Hardcoded endpoints / configs: `"http://..."`, `"192.168.1.1"`, `8080`, timeouts scattered in code.
    - MANDATORY_I18N:
      - 100% user-visible text MUST reside in `res/values/strings.xml` (zh default) AND `res/values-en/strings.xml` (en aligned).
      - Non-technical terminology MUST default to Chinese (zh).
      - Compose access: `stringResource(R.string.xxx)` or `stringResource(R.string.xxx, arg)`.
      - ViewModel access: `getString(R.string.xxx)` (via `BaseViewModel.getString`).
      - UiState models: Store `@StringRes val resId: Int = R.string.xxx` instead of formatted raw strings.
    - MANDATORY_CONFIG:
      - All IPs, ports, URLs, timeouts, retry counts, storage paths MUST reside in `AppConfig` (single authority).
2.2 FORBID_INLINE_PACKAGE_PATHS:
    - ABSOLUTE_ZERO_TOLERANCE: Zero inline Fully Qualified Names (FQNs) anywhere in code bodies.
    - TOP_LEVEL_IMPORTS_ONLY:
      - 100% of symbol imports MUST be declared in the file header (between `package` statement and class/function declarations).
      - NEVER import or inline package paths on-the-fly while coding inside functions, composables, or class bodies.
      - Any required class, interface, annotation, extension function, or constant MUST be registered via `import` at the top of the file BEFORE being used.
    - FORBIDDEN:
      - Inline package paths in logic: `com.base.iot.R.string.xxx`, `com.base.iot.core.ui.theme.AppTheme.colors`.
      - Inline framework classes: `androidx.compose.ui.res.stringResource(...)`, `androidx.compose.foundation.layout.fillMaxSize()`.
      - Inline annotations: `@androidx.annotation.StringRes`, `@dagger.hilt.android.lifecycle.HiltViewModel`.
      - Inline utils / types: `kotlinx.coroutines.flow.update`, `android.widget.Toast.makeText(...)`.
    - MANDATORY:
      - Header Imports: 100% of symbols MUST be declared via top-level `import` statements (`import com.base.iot.R`, `import androidx.annotation.StringRes`).
      - Code Body: ONLY use unqualified short symbol names (`R.string.xxx`, `stringResource(...)`, `@StringRes`).
      - Name Collision: If colliding names exist, alias them at the header (`import foo.Bar as FooBar`), NEVER fall back to inline FQNs.
2.3 TOKEN_ECONOMY_AND_FILE_GRANULARITY:
    - FILE_GRANULARITY: Split files by cohesive responsibility and navigation cost. Do not split solely to satisfy a line-count target; files over 200 lines require a brief review for separable responsibilities.
    - VM_SPLIT: ViewModels split using same-package extension files (`fun MyViewModel.doSomething() = ...`).
    - SURGICAL_EDITS: Use `replace_file_content` / `multi_replace_file_content`. Avoid full file rewrites.
    - CONCISE_COMMENTS: High-signal architectural notes only. Zero decorative banners or redundant restatements.
2.4 LOG_AND_OOM_PREVENTION:
    - PROTOCOL_SEQUENCE: `[1/4 CONNECTING]` -> `[2/4 CONNECTED]` -> `[3/4 TRANSFER]` -> `[4/4 CLOSED]`.
    - FORBIDDEN: Printing binary, Base64, raw stream payloads, or full file buffers to Logcat.
    - CHUNKING: Long logs (>4000 chars) MUST use `Lg` chunked logger.

## [3_UI_SPEC_AND_LAYOUT_STABILITY]
- Default theme: Arctic Industrial. Optional accent: Electric Blue. Use `ThemeManager.setThemeAccent()` and `AppTheme.colors`; never duplicate palette values in feature code.
3.1 THEME_ACCESSIBILITY:
    - FORBIDDEN: Hardcoded color values (e.g. `Color(0x...)`, `#FFFFFF`).
    - MANDATORY: Consume `AppTheme.colors.*` (native Views: `palette.xxx.toArgb()`).
    - CONTRAST: Meet WCAG AA for normal text (4.5:1) and large text (3:1); target higher contrast for critical status and industrial outdoor use. Validate actual foreground/background pairs in both themes.
3.2 COMPONENT_ANTI_DISTORTION_AND_OVERLAP_PREVENTION:
    - ROW_WEIGHT_PROTECTION:
      - Dynamic text in a `Row` MUST have an explicit width strategy (weight, bounded width, wrapping, or ellipsis) appropriate to adjacent controls. Do not apply weight mechanically.
      - FORBIDDEN: Allowing expanding text to push adjacent buttons, icons, or switches off-screen or squeeze them to 0 width.
    - FIXED_COMPONENT_SIZE_INTEGRITY:
      - Buttons, switches, status badges, and action triggers in horizontal rows MUST define non-collapsible bounds (e.g. `Modifier.defaultMinSize(minWidth = ...)` or explicit fixed bounds).
      - NEVER allow neighboring text elements to deform or truncate button touch targets.
    - HORIZONTAL_AND_SMALL_SCREEN_OVERFLOW_DEFENSE:
      - This project targets landscape / industrial screens (typical height 360~480dp).
      - Any vertical container (Dialog, Card content area, Settings list, Form) that could exceed screen height MUST be wrapped in `Modifier.verticalScroll(rememberScrollState())`.
      - Bottom action buttons (Confirm/Cancel/Close) MUST NEVER be pushed off-screen or clipped by device borders.
    - TRUNCATION_AND_MAXLINES:
      - Single-line identifiers, endpoints, topic paths, or payloads MUST specify `maxLines = 1` and `overflow = TextOverflow.Ellipsis`.
      - Multi-line text MUST declare explicit `maxLines` to prevent infinite expansion.
    - TOUCH_TARGET_SIZE:
      - Interactive elements (Buttons, Clickable Rows, Switches, TextButtons) MUST provide >= 48.dp x 48.dp touch area (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or equivalent padding).
    - SPACING_GRID_SYSTEM:
      - Strict 4dp/8dp incremental scale (`4.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, `32.dp`). FORBIDDEN: Arbitrary dimensions (`11.dp`, `13.7.dp`).
    - TYPOGRAPHY_AND_TEXT:
      - All typography sizes MUST use `.sp`, all layout dimensions/padding MUST use `.dp`.
      - Screen Titles: `20~22.sp` (Bold); Section Headers: `16~18.sp` (Bold/SemiBold); Body: `14~15.sp` (Normal/Medium); Captions/Badges: `11~13.sp` (Secondary text color).
    - SCAFFOLD_AND_INSETS:
      - All top-level screens MUST consume `Scaffold(paddingValues)` or explicitly apply system insets to prevent status/navigation bar overlap.
    - RESPONSIVE_AND_ADAPTIVE:
      - Compact (< 600dp): Single-pane.
      - Medium (600dp to < 840dp): single pane or a deliberately compact master-detail pattern.
      - Expanded (>= 840dp): two-pane or responsive grid when the content benefits from it.
      - Base the decision on the current app window, never orientation or physical device identity alone.
    - PAD_TABLET_ADAPTATION:
      - Layout classification uses `rememberPadDeviceClass()` from current app window width (Compact < 600dp, Medium < 840dp, Expanded >= 840dp). `smallestScreenWidthDp` MUST NOT decide active pane count.
      - Dialogs MUST apply `Modifier.padDialogBounds()` to prevent full-width stretching on large screens.
      - Large screen content containers should apply `padContentBounds()` to keep reading width centered.
      - Tablet layouts should utilize `PadMasterDetailLayout` or `PadNavigationScaffold` (NavigationRail).
    - FEEDBACK_AND_A11Y:
      - 100% of clickable elements MUST provide immediate press/ripple visual feedback.
      - Decorative icons MUST set `contentDescription = null`. Functional icons MUST declare an `@StringRes` i18n description.

## [4_DIALOG_SPECIFICATION]
4.1 DIALOG_DISMISSAL:
    - Every dialog MUST expose only the dismissal actions that make sense for its interaction.
    - Confirm dialogs must make Confirm and Cancel clear; blocking progress dialogs must not imply that an operation can be cancelled when it cannot.
    - Close placement and icon/text treatment follow the component layout and accessibility needs; do not force a top-right text button into every dialog.
    - TOUCH_TARGET: Any visible dismissal action MUST retain a minimum 48dp touch target.
4.2 TITLE_ROW_LAYOUT_STANDARD:
    - The Dialog header MUST use `Arrangement.SpaceBetween` and `Alignment.CenterVertically`:
      ```kotlin
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
      ) {
          Text(
              text = title,
              color = AppTheme.colors.textPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
          )
          TextButton(
              onClick = onDismiss,
              modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
              Text(
                  text = stringResource(R.string.btn_close),
                  color = AppTheme.colors.textSecondary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium
              )
          }
      }
      ```
4.3 BODY_SCROLL_SAFETY:
    - The Dialog internal column containing messages or form controls MUST apply `Modifier.verticalScroll(rememberScrollState())` to prevent clipping on landscape screens.

## [5_COMPOSE_AND_UDF_CODING_PARADIGM]
5.1 UNIDIRECTIONAL_DATA_FLOW (UDF):
    - State flows DOWN: UI screens accept immutable `UiState` and emit user intents via lambda callbacks / method references (`onClick = vm::executeTask`).
    - Events flow UP: ViewModel mutates state atomically via `_uiState.update { it.copy(...) }`.
5.2 LIFECYCLE_AWARE_COLLECTION:
    - Compose MUST collect StateFlow using `collectAsStateWithLifecycle()` (from `androidx.lifecycle.compose`). FORBIDDEN: Raw `collectAsState()` on background or network flows.
5.3 ZERO_CONTEXT_LEAK:
    - ViewModels MUST NEVER hold references to `Activity`, `Fragment`, `View`, or Compose scopes.
    - Access resources in ViewModels ONLY via `Application` context in `BaseViewModel` (`getString(R.string.xxx)`).
5.4 THREAD_DISPATCHING:
    - Network, socket, database, file I/O, or heavy computation MUST run on `Dispatchers.IO` (`withContext(Dispatchers.IO)`).
    - UI state mutations and toast/event emissions MUST run on Main thread (`viewModelScope.launch` default).
5.5 LIST_OPTIMIZATION:
    - LazyColumn / LazyRow items MUST supply a stable unique key (`items(items, key = { it.id })`).
5.6 STATE_IMMUTABILITY:
    - All `UiState` classes MUST be immutable `@Immutable data class` with `val` properties only. FORBIDDEN: Mutable collections (`ArrayList`, `mutableListOf()`) or `var` inside UiState.

## [6_PRODUCTION_FEATURE_INDEX]
STATUS: ROBOT_DASHBOARD_ACTIVE
- App mount point: `MainActivity.kt` -> `feature.robot.RobotDashboardScreen()`.
- UI entry: `feature/robot/RobotDashboardScreen.kt`.
- UI state and domain snapshots: `feature/robot/RobotDashboardModels.kt`.
- Repository contract and demo binding: `feature/robot/RobotDashboardRepository.kt`, `SampleRobotDashboardRepository.kt`, `RobotDashboardModule.kt`.
- Lifecycle-aware state holder: `feature/robot/RobotDashboardViewModel.kt`.
- Testable occupancy-grid geometry: `feature/robot/RobotMapGeometry.kt` with world/image/viewport coordinate conversions and unit tests.
- Map canvas and overlay primitives: `feature/robot/components/RobotMapCard.kt`.
- Task scope routing: read `.agent/skills/robot-dashboard/SKILL.md` for map, device telemetry, missions, events, video and adaptive dashboard work.
- SAMPLE_DATA_BOUNDARY: current device, mission, event, statistic and map values are sample-only. Never describe them as live telemetry or issue robot commands from sample data.
- MAP_BOUNDARY: current Canvas is a schematic route/event overlay, not a PGM / PCD parser or SLAM renderer. `OccupancyGridTransform` is only the tested coordinate foundation. Real map integration must preserve image encoding, occupancy thresholds, map resolution, origin pose, row orientation and use the same transform for every overlay.
- VIDEO_BOUNDARY: the video entry is a disconnected-state placeholder. RTSP / WebRTC requires a dedicated stream provider and lifecycle-aware player surface.
- `feature/template/` remains the clone source for new application features.

## [7_TOPOLOGY]
Multi-Module Architecture:
1. `:core` (Android Library - `core/src/main/kotlin/com/base/iot/core/`):
   - `base/`: `BaseViewModel.kt`, `UiContract.kt`
   - `config/`: `AppConfig.kt` (single config authority, zero demo), `IotProtocolConfig.kt`
   - `di/`: `SerialModule.kt` (Hilt 绑定 SerialManager 到 SerialManagerImpl)
   - `iot/`: `IotHub.kt` (facade 包含 serial), `MqttManager.kt`, `RedisManager.kt`, `SocketManager.kt`, `SerialManager.kt`, `SerialManagerImpl.kt`, `AndroidUsbSerialDriver.kt`, `RemoteHttpSerialDriver.kt`, `UsbDeviceProber.kt`, `UsbReceiverHelper.kt`
   - `network/`: `HttpManager.kt` (GET/POST/PUT/DELETE/Upload/Download abstractions)
   - `storage/`: `CacheLocationManager.kt`, `FileShareManager.kt`
   - `diagnostics/`: `Lg.kt`, `ErrorParser.kt`, `CrashHandler.kt`, `LogExporter.kt`
   - `ui/components/`: `AppCard.kt`, `AppButton.kt`, `AppSwitchRow.kt`
   - `ui/dialog/`: `AppProgressDialog.kt`, `AppErrorDialog.kt`, `AppConfirmDialog.kt`, `AppInputDialog.kt`, `AppBottomSheetDialog.kt`, `AppLoadingDialog.kt`, `XPopupBridge.kt`
   - `ui/adaptive/`: `PadSpec.kt`, `PadModifiers.kt`, `PadAdaptiveTwoPane.kt`
   - `ui/pad/`: `PadDeviceClassifier.kt`, `PadDialogModifiers.kt`, `PadMasterDetailLayout.kt`, `PadContentContainer.kt`, `PadNavigationScaffold.kt`
   - `ui/theme/`: `ThemeManager.kt`, `AppTheme.kt` (`AppTheme.colors`, `DarkAppColors`, `LightAppColors`)
   - `core/src/protocol_*/`: `protocol_http`, `protocol_mqtt`, `protocol_redis`, `protocol_socket` (real drivers & zero-dep stubs)
   - `core/src/main/res/values/strings.xml`: Pure core framework string resources (zh & en)

2. `:app` (Application - `app/src/main/kotlin/com/base/iot/`):
   - `App.kt`: Application entry, crash handler installation
   - `MainActivity.kt`: Single Activity, dynamic theme & system bars, Compose root
   - `feature/template/`: Scaffold source. Clone for new features (`TemplateUiState.kt`, `TemplateViewModel.kt`, `TemplateScreen.kt`).
   - `feature/robot/`: Active robot patrol dashboard.
   - `app/src/main/res/values/demo_strings.xml`: Demo string resources (zh & en).
   - `app/src/main/res/values/strings.xml`: Application metadata and template strings.
   - MOUNT_POINT: `MainActivity.kt` -> `RobotDashboardScreen()`

## [8_RECIPE: NEW_FEATURE]
Clone `feature/template/` -> `feature/<name>/`:
1. `UiState` (`<Feature>UiState.kt`, ~20 lines):
   ```kotlin
   data class MyUiState(
       val isRunning: Boolean = false,
       override val loadingConfig: LoadingConfig = LoadingConfig(),
       override val parsedError: ParsedError? = null,
       override val showErrorDialog: Boolean = false
   ) : IUiState
   sealed class MyEvent : IUiEvent { data class ShowToast(val msg: String) : MyEvent() }
   ```
2. `ViewModel` (`<Feature>ViewModel.kt`, ~50 lines):
   ```kotlin
   @HiltViewModel
   class MyViewModel @Inject constructor(
       app: Application, val iotHub: IotHub, share: FileShareManager
   ) : BaseViewModel<MyUiState, MyEvent>(app, share, MyUiState()) {
       override fun updateLoadingConfig(r: (LoadingConfig) -> LoadingConfig) { _uiState.update { it.copy(loadingConfig = r(it.loadingConfig)) } }
       override fun updateErrorState(err: ParsedError?, vis: Boolean) { _uiState.update { it.copy(parsedError = err, showErrorDialog = vis) } }
       fun startTask() = launchWithLoading(getString(R.string.task_title), isBlocking = true) { updateProgress ->
           updateProgress(0.5f, getString(R.string.processing))
           iotHub.socket.send(AppConfig.SOCKET_HEARTBEAT_PAYLOAD)
       }
   }
   ```
3. `Screen` (`<Feature>Screen.kt`, ~55 lines):
   ```kotlin
   @Composable
   fun MyScreen(vm: MyViewModel = hiltViewModel()) {
       val uiState by vm.uiState.collectAsStateWithLifecycle()
       Scaffold(containerColor = AppTheme.colors.background) { p ->
           Column(
               modifier = Modifier.fillMaxSize().padding(p).padding(16.dp).verticalScroll(rememberScrollState()),
               verticalArrangement = Arrangement.spacedBy(14.dp)
           ) {
               AppCard(title = stringResource(R.string.feature_title)) {
                   AppButton(text = stringResource(R.string.execute_btn), color = AppTheme.colors.accentPrimary, onClick = vm::startTask)
               }
           }
           AppProgressDialog(config = uiState.loadingConfig, onDismissRequest = vm::dismissLoading)
           AppErrorDialog(visible = uiState.showErrorDialog, error = uiState.parsedError, onDismiss = vm::dismissError, onShareReport = vm::shareErrorReport)
       }
   }
   ```

## [9_INFRA_MATRIX]
- `launchWithLoading(title, isBlocking)`: Standard async wrapper. `isBlocking=true` (anti-touch-through, non-cancelable), `false` (cancelable).
- `ErrorParser.parse()` + `AppErrorDialog`: Root-cause diagnostics dialog (OS/Hardware/Network/Stacktrace + system share).
- `iotHub.http`: HTTP large file transfer auto-timeout 1h (`HTTP_FILE_TRANSFER_TIMEOUT_SEC`), regular request 30s.
- `iotHub.serial`: Local USB OTG (`AndroidUsbSerialDriver` for CH34x/CP210x/FTDI/PL2303/CDC) & remote HTTP bridge dual-mode auto-routing.
- `CacheLocationManager`: Dynamic cache location switcher (internal/external/download), DataStore backed.
- `FileShareManager`: FileProvider sandboxed cross-process sharing (files, logs, diagnostic text).
- Pad adaptive: `rememberPadSpec()` + `PadAdaptiveTwoPane` + `PadMasterDetailLayout` + `padDialogBounds()`.
- UI Primitives: `AppCard`, `AppButton`, `AppSwitchRow` (consume `AppTheme.colors.*`, built-in feedback).

## [10_VERIFICATION_AND_SELF_AUDIT]
- Mandatory compilation: `.\gradlew.bat compileDebugKotlin`
- Zero compile errors, zero warnings.
- Pre-delivery self-audit:
  [ ] Git safety: Zero `git push` executed unless explicitly commanded.
  [ ] String hygiene: Zero hardcoded string literals in UI/VM/dialogs. 100% in strings.xml (zh & en). No scattered configs.
  [ ] Import hygiene: 100% symbols declared at file header before usage. Zero inline package paths / FQNs in code bodies.
  [ ] Token hygiene: Zero color hex, zero hardcoded endpoints/ports. 100% colors from `AppTheme.colors.*`.
  [ ] Module boundary: Zero protocol driver imports in `src/main/`. All new feature code strictly in `:app`.
  [ ] Dialog compliance: 100% Dialogs have a top-right plain text "关闭" button (NO icon). Adequate 48dp touch area.
  [ ] Layout stability: Dynamic texts in Rows declare `weight(1f, fill=false)`. Buttons/Switches protected from squeeze/deformation.
  [ ] Vertical overflow defense: Scrollable containers wrapped in `verticalScroll` to prevent clipping on landscape/small screens.
  [ ] UI spec hygiene: Touch targets >= 48dp, 4dp/8dp spacing scale, text sizes in `.sp`, layout in `.dp`.
  [ ] Insets hygiene: Top-level screens consume `Scaffold(paddingValues)` or insets to avoid system bar overlap.
  [ ] Paradigm hygiene: StateFlow collected via `collectAsStateWithLifecycle()`, zero Context/Activity leaks in VM.
  [ ] Thread hygiene: Background tasks & IO operations dispatched to `Dispatchers.IO`.
  [ ] Granularity: Every Kotlin file <= 150 lines.
  [ ] Router hygiene: If created new feature or added `:core` manager, updated `[0_CAPABILITY_ROUTING_INDEX]`. Otherwise untouched.
  [ ] Feature mount: MainActivity mounts the active production screen; sample-only data is visibly identified.
