# AGENT_DIRECTIVES: BaseAndroid2AIoT
VERSION: 3.0 | TARGET: LLM_AGENT | STRICT_MODE: TRUE | ZERO_DEVIATION

## [1_MODULE_AND_GIT_BOUNDARIES]
1.1 GIT_SAFETY:
    - NEVER execute `git push` unless explicitly commanded by user in active prompt.
    - All changes must remain local: Edit -> Local Compile -> Local Commit.
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
    - FORBIDDEN:
      - Inline package paths in logic: `com.base.iot.R.string.xxx`, `com.base.iot.core.ui.theme.AppTheme.colors`.
      - Inline framework classes: `androidx.compose.ui.res.stringResource(...)`, `androidx.compose.foundation.layout.fillMaxSize()`.
      - Inline annotations: `@androidx.annotation.StringRes`, `@dagger.hilt.android.lifecycle.HiltViewModel`.
      - Inline utils / types: `kotlinx.coroutines.flow.update`, `android.widget.Toast.makeText(...)`.
    - MANDATORY:
      - Header Imports: 100% of symbols MUST be declared via `import` statements at the top of the file (`import com.base.iot.R`, `import androidx.annotation.StringRes`).
      - Code Body: ONLY use unqualified short symbol names (`R.string.xxx`, `stringResource(...)`, `@StringRes`).
2.3 TOKEN_ECONOMY_AND_FILE_GRANULARITY:
    - FILE_SIZE_LIMIT: 50~150 lines per Kotlin file. Files > 150 lines MUST be split.
    - VM_SPLIT: ViewModels split using same-package extension files (`fun MyViewModel.doSomething() = ...`).
    - SURGICAL_EDITS: Use `replace_file_content` / `multi_replace_file_content`. Avoid full file rewrites.
    - CONCISE_COMMENTS: High-signal architectural notes only. Zero decorative banners or redundant restatements.
2.4 LOG_AND_OOM_PREVENTION:
    - PROTOCOL_SEQUENCE: `[1/4 CONNECTING]` -> `[2/4 CONNECTED]` -> `[3/4 TRANSFER]` -> `[4/4 CLOSED]`.
    - FORBIDDEN: Printing binary, Base64, raw stream payloads, or full file buffers to Logcat.
    - CHUNKING: Long logs (>4000 chars) MUST use `Lg` chunked logger.

## [3_UI_SPEC_AND_LAYOUT_STABILITY]
3.1 THEME_WCAG_AAA:
    - FORBIDDEN: Hardcoded color values (e.g. `Color(0x...)`, `#FFFFFF`).
    - MANDATORY: Consume `AppTheme.colors.*` (native Views: `palette.xxx.toArgb()`).
    - CONTRAST: Contrast ratio > 15:1 primary text, > 7:1 secondary text across both Light & Dark modes.
3.2 COMPONENT_ANTI_DISTORTION_AND_OVERLAP_PREVENTION:
    - ROW_WEIGHT_PROTECTION:
      - Any variable-length or dynamic text inside a `Row` MUST declare `Modifier.weight(1f, fill = false)`.
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
      - Compact (< 600dp): Single-pane vertical scroll (`LazyColumn` or `verticalScroll`).
      - Expanded (>= 600dp landscape): Two-pane (`AdaptiveContentLayout`), split-screen, or responsive grid.
    - FEEDBACK_AND_A11Y:
      - 100% of clickable elements MUST provide immediate press/ripple visual feedback.
      - Decorative icons MUST set `contentDescription = null`. Functional icons MUST declare an `@StringRes` i18n description.

## [4_DIALOG_SPECIFICATION]
4.1 MANDATORY_TOP_RIGHT_CLOSE_TEXT_BUTTON:
    - ABSOLUTE_MANDATE: EVERY Dialog (Confirm, Input, Progress/Loading, Error, BottomSheet, Custom) MUST include a "Close" button at the top-right corner.
    - TEXT_ONLY: NO ICON ALLOWED for the close action. ONLY plain text displaying "关闭" (bound to `stringResource(R.string.btn_close)`).
    - TOUCH_AND_HEATMAP_SAFETY: The close text button MUST have adequate touch target area (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` or `contentPadding = PaddingValues(...)`).
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

## [6_DEMO_INDEX_AND_AUTO_PURGE]
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

## [7_TOPOLOGY]
Multi-Module Architecture:
1. `:core` (Android Library - `core/src/main/kotlin/com/base/iot/core/`):
   - `base/`: `BaseViewModel.kt`, `UiContract.kt`
   - `config/`: `AppConfig.kt` (single config authority, zero demo), `IotProtocolConfig.kt`
   - `iot/`: `IotHub.kt` (facade), `MqttManager.kt`, `RedisManager.kt`, `SocketManager.kt`
   - `network/`: `HttpManager.kt` (GET/POST/PUT/DELETE/Upload/Download abstractions)
   - `storage/`: `CacheLocationManager.kt`, `FileShareManager.kt`
   - `diagnostics/`: `Lg.kt`, `ErrorParser.kt`, `CrashHandler.kt`, `LogExporter.kt`
   - `ui/components/`: `AppCard.kt`, `AppButton.kt`, `AppSwitchRow.kt`
   - `ui/dialog/`: `AppProgressDialog.kt`, `AppErrorDialog.kt`, `AppConfirmDialog.kt`, `AppInputDialog.kt`, `AppBottomSheetDialog.kt`, `AppLoadingDialog.kt`, `XPopupBridge.kt`
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
- `CacheLocationManager`: Dynamic cache location switcher (internal/external/download), DataStore backed.
- `FileShareManager`: FileProvider sandboxed cross-process sharing (files, logs, diagnostic text).
- UI Primitives: `AppCard`, `AppButton`, `AppSwitchRow` (consume `AppTheme.colors.*`, built-in feedback).

## [10_VERIFICATION_AND_SELF_AUDIT]
- Mandatory compilation: `.\gradlew.bat compileDebugKotlin`
- Zero compile errors, zero warnings.
- Pre-delivery self-audit:
  [ ] Git safety: Zero `git push` executed unless explicitly commanded.
  [ ] String hygiene: Zero hardcoded string literals in UI/VM/dialogs. 100% in strings.xml (zh & en). No scattered configs.
  [ ] Import hygiene: Zero inline package paths / FQNs. All symbols strictly declared at file header.
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
  [ ] Demo hygiene: If in formal dev mode, zero demo files or DEMO_* constants remain.
