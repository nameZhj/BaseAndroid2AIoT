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

8. AUTO_PURGE_DEMO_ON_FORMAL_DEV:
   - TRIGGER: When user instructs formal/business development ("开始正式开发", "实现业务需求"), Agent MUST automatically delete demo files and purge demo mounts/configs.
   - PURGE_TARGETS: Everything indexed in `[DEMO_INDEX_AND_AUTO_PURGE]`.
   - ZERO_RESIDUE: Formal production releases MUST have zero demo code remnants.

## [DEMO_INDEX_AND_AUTO_PURGE]
STATUS: DEMO_ACTIVE | TRIGGER: FORMAL_FEATURE_DEV -> AUTO_PURGE_ALL_DEMO
1. DEMO_FILE_REGISTRY (Action: DELETE directory & files on formal dev):
   - Package: `app/src/main/kotlin/com/base/iot/feature/demo/` (15 files):
     - `DashboardScreen.kt`: Demo dashboard UI entry
     - `DashboardPanes.kt`: Demo single/two pane layouts
     - `DashboardViewModel.kt`: Demo ViewModel core
     - `DashboardUiState.kt`: Demo UI state & contract
     - `DashboardHttpOps.kt`: Demo HTTP operations extension
     - `DashboardIotOps.kt`: Demo MQTT/Redis/Socket operations extension
     - `DashboardSettingsOps.kt`: Demo settings & dialog operations extension
     - `DashboardDiagnosticsOps.kt`: Demo crash & timeout diagnostics extension
     - `components/DashboardHeader.kt`: Demo header card
     - `components/DiagnosticsAndSettingsCards.kt`: Demo diagnostics cards
     - `components/LoadingAndErrorDemoCard.kt`: Demo progress & error cards
     - `components/ProtocolCards.kt`: Demo protocol switch cards
     - `components/StorageCards.kt`: Demo storage & cache cards
     - `components/TerminalLogCard.kt`: Demo terminal log card
     - `components/UnifiedDialogDemoCard.kt`: Demo dialogs card
   - Recycler Demo: `core/ui/recycler/IotDeviceQuickAdapter.kt` (used solely in demo StorageCards)
2. DEMO_CODE_REGISTRY (Action: STRIP demo blocks on formal dev):
   - Mount Point (`MainActivity.kt`):
     - Remove `import com.base.iot.feature.demo.DashboardScreen`
     - Replace `DashboardScreen()` with target feature screen (e.g. `<Feature>Screen()`)
   - Config Authority (`core/config/AppConfig.kt`):
     - Strip block `[DEMO_CONFIG_START]` -> `[DEMO_CONFIG_END]` (`DEMO_HTTP_*`, `DEMO_MQTT_*`, `DEMO_REDIS_*`, `DEMO_UNREACHABLE_*`)
   - Resource Bundles (`res/values/strings.xml` & `res/values-en/strings.xml`):
     - Strip block `[DEMO_STRINGS_START]` -> `[DEMO_STRINGS_END]`
3. AUTO_PURGE_PIPELINE (Step-by-step agent execution):
   - STEP 1: Scaffold production feature: Clone `feature/template/` -> `feature/<name>/`.
   - STEP 2: Remount `MainActivity.kt`: Replace `DashboardScreen()` with `feature.<name>.<Feature>Screen()`.
   - STEP 3: Delete demo folder: Remove `app/src/main/kotlin/com/base/iot/feature/demo/` completely.
   - STEP 4: Strip demo code blocks: Clean `AppConfig.kt` (`[DEMO_CONFIG_*]`) and `strings.xml` (`[DEMO_STRINGS_*]`).
   - STEP 5: Validate clean build: Run `.\gradlew.bat compileDebugKotlin` (MUST be exit code 0).

## [TOPOLOGY]
Root: `app/src/main/kotlin/com/base/iot/`
- `core/base/`: `BaseViewModel.kt`, `UiContract.kt`
- `core/config/`: `AppConfig.kt` (single config authority), `IotProtocolConfig.kt`
- `core/iot/`: `IotHub.kt` (facade), `MqttManager.kt`, `RedisManager.kt`, `SocketManager.kt`
- `core/network/`: `HttpManager.kt` (GET/POST/PUT/DELETE/Upload/Download abstractions)
- `core/storage/`: `CacheLocationManager.kt`, `FileShareManager.kt`
- `core/diagnostics/`: `Lg.kt`, `ErrorParser.kt`, `CrashHandler.kt`, `LogExporter.kt`
- `core/ui/components/`: `AppCard.kt`, `AppButton.kt`, `AppSwitchRow.kt`
- `core/ui/dialog/`: `AppProgressDialog.kt`, `AppErrorDialog.kt`, `AppConfirmDialog.kt`, `AppInputDialog.kt`, `AppBottomSheetDialog.kt`, `AppLoadingDialog.kt`
- `core/ui/theme/`: `ThemeManager.kt`, `AppTheme.colors` (Dark/Light tokens)
- `feature/template/`: Scaffold source. Clone for new features (`TemplateUiState.kt`, `TemplateViewModel.kt`, `TemplateScreen.kt`).
- `feature/demo/`: Reference split implementation ([DEMO_ACTIVE], purge on formal dev).
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
  [ ] Git safety: Zero `git push` executed.
  [ ] String hygiene: Zero hardcoded string literals in UI/VM/dialogs. 100% in strings.xml (zh & en). No scattered configs.
  [ ] Import hygiene: Zero inline package paths / FQNs. All symbols strictly declared at file header.
  [ ] Token hygiene: Zero color hex, zero hardcoded endpoints/ports.
  [ ] Module boundary: Zero protocol driver imports in `src/main/`.
  [ ] Granularity: Every Kotlin file <= 150 lines.
  [ ] Demo hygiene: If in formal dev mode, zero demo files or DEMO_* constants remain.
