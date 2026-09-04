# AGENT_DIRECTIVES: BaseAndroid2AIoT
VERSION: 2.0 | TARGET: LLM_AGENT | STRICT_MODE: TRUE | ZERO_DEVIATION

## [HARD_RULES]
1. GIT_SAFETY:
   - NEVER execute `git push` unless explicitly commanded by user in active prompt.
   - All changes must remain local: Edit -> Local Compile -> Local Commit.
2. PROTOCOL_ISOLATION:
   - FORBIDDEN in `src/main/`: `import retrofit2.*`, `import okhttp3.*`, `import com.hivemq.*`, `import redis.clients.jedis.*`.
   - ROUTING: All networking MUST route via `IotHub` or abstract managers (`HttpManager`, `MqttManager`, `RedisManager`, `SocketManager`).
   - DUAL_SYNC: Modifying protocol API requires synchronous updates to BOTH `src/protocol_<proto>/` (real driver) and `src/protocol_<proto>_stub/` (zero-dep stub).
   - DEPENDENCIES: Single source of truth is `gradle/libs.versions.toml` (`libs.xxx`).
   - RUNTIME_LOCK: If `isCompiled == true`, protocol switch is permanently locked ON at runtime.
3. THEME_WCAG_AAA:
   - FORBIDDEN: Hardcoded color values (e.g. `Color(0x...)`, `#FFFFFF`).
   - MANDATORY: Consume `AppTheme.colors.*` (native Views: `palette.xxx.toArgb()`).
   - CONTRAST: Contrast ratio > 15:1 primary text, > 7:1 secondary text across both Light & Dark modes.
4. LOG_OOM_PREVENTION:
   - PROTOCOL_SEQUENCE: `[1/4 CONNECTING]` -> `[2/4 CONNECTED]` -> `[3/4 TRANSFER]` -> `[4/4 CLOSED]`.
   - FORBIDDEN: Printing binary, Base64, raw stream payloads, or full file buffers to Logcat.
   - CHUNKING: Long logs (>4000 chars) MUST use `Lg` chunked logger.
5. FORBID_HARDCODED_STRINGS:
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

6. FORBID_INLINE_PACKAGE_PATHS:
   - ABSOLUTE_ZERO_TOLERANCE: Zero inline Fully Qualified Names (FQNs) anywhere in code bodies.
   - FORBIDDEN:
     - Inline package paths in logic: `com.base.iot.R.string.xxx`, `com.base.iot.core.ui.theme.AppTheme.colors`.
     - Inline framework classes: `androidx.compose.ui.res.stringResource(...)`, `androidx.compose.foundation.layout.fillMaxSize()`.
     - Inline annotations: `@androidx.annotation.StringRes`, `@dagger.hilt.android.lifecycle.HiltViewModel`.
     - Inline utils / types: `kotlinx.coroutines.flow.update`, `android.widget.Toast.makeText(...)`.
   - MANDATORY:
     - Header Imports: 100% of symbols MUST be declared via `import` statements at the top of the file (`import com.base.iot.R`, `import androidx.annotation.StringRes`).
     - Code Body: ONLY use unqualified short symbol names (`R.string.xxx`, `stringResource(...)`, `@StringRes`).

7. TOKEN_ECONOMY_AND_FILE_GRANULARITY:
   - FILE_SIZE_LIMIT: 50~150 lines per Kotlin file. Files > 150 lines MUST be split.
   - VM_SPLIT: ViewModels split using same-package extension files (`fun MyViewModel.doSomething() = ...`).
   - SURGICAL_EDITS: Use `replace_file_content` / `multi_replace_file_content`. Avoid full file rewrites.
   - CONCISE_COMMENTS: High-signal architectural notes only. Zero decorative banners or redundant restatements.

8. MODULE_BOUNDARY_AND_CODE_PLACEMENT:
   - NEW_FEATURE_PLACEMENT: 100% of new business feature code, UI screens, ViewModels, and state models MUST be developed in `:app` (`app/src/main/kotlin/com/base/iot/feature/<name>/`).
   - CORE_INTEGRITY_LOCK: DO NOT tamper with or modify existing base framework code in `:core` (`core/src/main/kotlin/com/base/iot/core/`) unless strictly necessary.
   - CORE_EXTENSION_PERMISSION: Only when adding new infrastructure-level capabilities, global protocols, cross-feature shared utilities, or design system tokens, should code be added to `:core`.

9. AUTO_PURGE_DEMO_ON_FORMAL_DEV:
   - TRIGGER: When user instructs formal/business development ("开始正式开发", "实现业务需求"), Agent MUST automatically delete demo files and purge demo mounts/configs.
   - PURGE_TARGETS: Everything indexed in `[DEMO_INDEX_AND_AUTO_PURGE]`.
   - ZERO_RESIDUE: Formal production releases MUST have zero demo code remnants.

10. ANDROID_UI_SPEC_AND_ACCESSIBILITY:
   - TOUCH_TARGET_SIZE: Interactive elements (Buttons, Clickable Rows, Switches, IconButtons) MUST provide >= 48.dp x 48.dp touch area (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or standard padding).
   - SPACING_GRID_SYSTEM: Strict 4dp/8dp incremental scale (`4.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, `32.dp`). FORBIDDEN: Arbitrary or odd dimensions (e.g. `11.dp`, `13.7.dp`, `19.dp`).
   - TYPOGRAPHY_AND_TEXT:
     - All typography sizes MUST use `.sp`, all layout dimensions/padding MUST use `.dp`.
     - Strict typographic hierarchy: Screen Titles `20~22.sp` (Bold), Section Headers `16~18.sp` (Bold/SemiBold), Body `14~15.sp` (Normal/Medium), Captions/Badges `11~13.sp` (Secondary text color).
     - Single-line truncation: Dynamic identifiers, endpoints, topic paths, or payloads MUST specify `maxLines = 1` and `overflow = TextOverflow.Ellipsis`.
   - SCAFFOLD_AND_INSETS:
     - All top-level screens MUST consume `Scaffold(paddingValues)` or explicitly apply system insets to prevent content overlapping status bars, navigation bars, or display cutouts.
   - RESPONSIVE_AND_ADAPTIVE:
     - Compact screens (< 600dp): Single-pane vertical scroll (`verticalScroll(rememberScrollState())` or `LazyColumn`).
     - Medium/Expanded screens (>= 600dp, landscape/tablets): Two-pane (`AdaptiveContentLayout`), split-screen, or grid. Zero content clipping on small screens, zero blown-out full-width stretch on large screens.
   - FEEDBACK_AND_A11Y:
     - 100% of clickable elements MUST provide immediate press/ripple visual feedback.
     - Decorative icons MUST set `contentDescription = null`. Functional icons MUST declare an `@StringRes` i18n description.

11. COMPOSE_AND_UDF_CODING_PARADIGM:
   - UNIDIRECTIONAL_DATA_FLOW (UDF):
     - State flows DOWN: UI screens accept immutable `UiState` and emit user intents via lambda callbacks / method references (`onClick = vm::executeTask`).
     - Events flow UP: ViewModel mutates state atomically via `_uiState.update { it.copy(...) }`.
   - LIFECYCLE_AWARE_COLLECTION:
     - Compose MUST collect StateFlow using `collectAsStateWithLifecycle()` (from `androidx.lifecycle.compose`). FORBIDDEN: Raw `collectAsState()` on background or network flows.
   - ZERO_CONTEXT_LEAK:
     - ViewModels MUST NEVER hold references to `Activity`, `Fragment`, `View`, or Compose scopes.
     - Access resources in ViewModels ONLY via `Application` context in `BaseViewModel` (`getString(R.string.xxx)`).
   - THREAD_DISPATCHING:
     - Network, socket, database, file I/O, or heavy computation MUST run on `Dispatchers.IO` (`withContext(Dispatchers.IO)`).
     - UI state mutations and toast/event emissions MUST run on Main thread (`viewModelScope.launch` default).
   - LIST_OPTIMIZATION:
     - LazyColumn / LazyRow items MUST supply a stable unique key (`items(items, key = { it.id })`).
   - STATE_IMMUTABILITY:
     - All `UiState` classes MUST be immutable `@Immutable data class` with `val` properties only. FORBIDDEN: Mutable collections (`ArrayList`, `mutableListOf()`) or `var` inside UiState.

## [DEMO_INDEX_AND_AUTO_PURGE]
STATUS: DEMO_ACTIVE | TRIGGER: FORMAL_FEATURE_DEV -> AUTO_PURGE_ALL_DEMO
1. DEMO_FILE_REGISTRY (Action: DELETE directory & files on formal dev):
   - Package: `app/src/main/kotlin/com/base/iot/feature/demo/` (16 files):
     - `DashboardScreen.kt`: Demo dashboard UI entry
     - `DashboardPanes.kt`: Demo single/two pane layouts
     - `DashboardViewModel.kt`: Demo ViewModel core
     - `DashboardUiState.kt`: Demo UI state & contract
     - `DashboardHttpOps.kt`: Demo HTTP operations extension
     - `DashboardIotOps.kt`: Demo MQTT/Redis/Socket operations extension
     - `DashboardSettingsOps.kt`: Demo settings & dialog operations extension
     - `DashboardDiagnosticsOps.kt`: Demo crash & timeout diagnostics extension
     - `DemoConfig.kt`: Demo-isolated endpoint & topic constants
     - `adapter/IotDeviceQuickAdapter.kt`: Demo device list RecyclerView adapter
     - `components/DashboardHeader.kt`: Demo header card
     - `components/DiagnosticsAndSettingsCards.kt`: Demo diagnostics cards
     - `components/LoadingAndErrorDemoCard.kt`: Demo progress & error cards
     - `components/ProtocolCards.kt`: Demo protocol switch cards
     - `components/StorageCards.kt`: Demo storage & cache cards
     - `components/TerminalLogCard.kt`: Demo terminal log card
     - `components/UnifiedDialogDemoCard.kt`: Demo dialogs card
   - Resource Bundles (Action: DELETE files on formal dev):
     - `app/src/main/res/values/demo_strings.xml`
     - `app/src/main/res/values-en/demo_strings.xml`
2. DEMO_CODE_REGISTRY (Action: REMOUNT on formal dev):
   - Mount Point (`MainActivity.kt`):
     - Remove `import com.base.iot.feature.demo.DashboardScreen`
     - Replace `DashboardScreen()` with target feature screen (e.g. `<Feature>Screen()`)
3. AUTO_PURGE_PIPELINE (Step-by-step agent execution):
   - STEP 1: Scaffold production feature: Clone `feature/template/` -> `feature/<name>/`.
   - STEP 2: Remount `MainActivity.kt`: Replace `DashboardScreen()` with `feature.<name>.<Feature>Screen()`.
   - STEP 3: Delete demo folder: Remove `app/src/main/kotlin/com/base/iot/feature/demo/` completely.
   - STEP 4: Delete demo strings: Remove `app/src/main/res/values/demo_strings.xml` and `app/src/main/res/values-en/demo_strings.xml`.
   - STEP 5: Validate clean build: Run `.\gradlew.bat compileDebugKotlin` (MUST be exit code 0).

## [TOPOLOGY]
Multi-Module Architecture:
1. `:core` (Android Library - `core/src/main/kotlin/com/base/iot/core/`):
   - `base/`: `BaseViewModel.kt`, `UiContract.kt`
   - `config/`: `AppConfig.kt` (single config authority, zero demo), `IotProtocolConfig.kt`
   - `iot/`: `IotHub.kt` (facade), `MqttManager.kt`, `RedisManager.kt`, `SocketManager.kt`
   - `network/`: `HttpManager.kt` (GET/POST/PUT/DELETE/Upload/Download abstractions)
   - `storage/`: `CacheLocationManager.kt`, `FileShareManager.kt`
   - `diagnostics/`: `Lg.kt`, `ErrorParser.kt`, `CrashHandler.kt`, `LogExporter.kt`
   - `ui/components/`: `AppCard.kt`, `AppButton.kt`, `AppSwitchRow.kt`
   - `ui/dialog/`: `AppProgressDialog.kt`, `AppErrorDialog.kt`, `AppConfirmDialog.kt`, `AppInputDialog.kt`, `AppBottomSheetDialog.kt`, `XPopupBridge.kt`
   - `ui/theme/`: `ThemeManager.kt`, `AppTheme.kt` (`AppTheme.colors`, `DarkAppColors`, `LightAppColors`)
   - `core/src/protocol_*/`: `protocol_http`, `protocol_mqtt`, `protocol_redis`, `protocol_socket` (real drivers & zero-dep stubs)
   - `core/src/main/res/values/strings.xml`: Pure core framework string resources (zh & en)

2. `:app` (Application - `app/src/main/kotlin/com/base/iot/`):
   - `App.kt`: Application entry, crash handler installation
   - `MainActivity.kt`: Single Activity, dynamic theme & system bars, Compose root
   - `feature/template/`: Scaffold source. Clone for new features (`TemplateUiState.kt`, `TemplateViewModel.kt`, `TemplateScreen.kt`).
   - `feature/demo/`: Reference split implementation ([DEMO_ACTIVE], purge on formal dev).
   - `app/src/main/res/values/demo_strings.xml`: Demo string resources (zh & en).
   - `app/src/main/res/values/strings.xml`: Application metadata and template strings.
   - MOUNT_POINT: `MainActivity.kt` -> `DashboardScreen()` ([DEMO_MOUNT_POINT], remount on formal dev)

## [RECIPE: NEW_FEATURE]
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
           Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
               AppCard(title = stringResource(R.string.feature_title)) {
                   AppButton(text = stringResource(R.string.execute_btn), color = AppTheme.colors.accentPrimary, onClick = vm::startTask)
               }
           }
           AppProgressDialog(config = uiState.loadingConfig, onDismissRequest = vm::dismissLoading)
           AppErrorDialog(visible = uiState.showErrorDialog, error = uiState.parsedError, onDismiss = vm::dismissError, onShareReport = vm::shareErrorReport)
       }
   }
   ```

## [INFRA_MATRIX]
- `launchWithLoading(title, isBlocking)`: Standard async wrapper. `isBlocking=true` (anti-touch-through, non-cancelable), `false` (cancelable).
- `ErrorParser.parse()` + `AppErrorDialog`: Root-cause diagnostics dialog (OS/Hardware/Network/Stacktrace + system share).
- `iotHub.http`: HTTP large file transfer auto-timeout 1h (`HTTP_FILE_TRANSFER_TIMEOUT_SEC`), regular request 30s.
- `CacheLocationManager`: Dynamic cache location switcher (internal/external/download), DataStore backed.
- `FileShareManager`: FileProvider sandboxed cross-process sharing (files, logs, diagnostic text).
- UI Primitives: `AppCard`, `AppButton`, `AppSwitchRow` (consume `AppTheme.colors.*`, built-in feedback).

## [VERIFICATION]
- Mandatory compilation: `.\gradlew.bat compileDebugKotlin`
- Zero compile errors, zero warnings.
- Pre-delivery self-audit:
  [ ] Git safety: Zero `git push` executed unless explicitly commanded.
  [ ] String hygiene: Zero hardcoded string literals in UI/VM/dialogs. 100% in strings.xml (zh & en). No scattered configs.
  [ ] Import hygiene: Zero inline package paths / FQNs. All symbols strictly declared at file header.
  [ ] Token hygiene: Zero color hex, zero hardcoded endpoints/ports. 100% colors from `AppTheme.colors.*`.
  [ ] Module boundary: Zero protocol driver imports in `src/main/`. All new feature code strictly in `:app`.
  [ ] UI spec hygiene: Touch targets >= 48dp, 4dp/8dp spacing scale, text sizes in `.sp`, layout in `.dp`.
  [ ] Insets hygiene: Top-level screens consume `Scaffold(paddingValues)` or insets to avoid system bar overlap.
  [ ] Paradigm hygiene: StateFlow collected via `collectAsStateWithLifecycle()`, zero Context/Activity leaks in VM.
  [ ] Thread hygiene: Background tasks & IO operations dispatched to `Dispatchers.IO`.
  [ ] Granularity: Every Kotlin file <= 150 lines.
  [ ] Demo hygiene: If in formal dev mode, zero demo files or DEMO_* constants remain.
