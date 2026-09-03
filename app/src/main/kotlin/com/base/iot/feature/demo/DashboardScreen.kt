package com.base.iot.feature.demo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.iot.core.storage.CacheLocationType
import com.base.iot.core.ui.AdaptiveContentLayout
import com.base.iot.core.ui.SystemBarEffect
import com.base.iot.core.ui.recycler.IotDeviceQuickAdapter

import com.base.iot.core.ui.dialog.*
import com.base.iot.ui.theme.AppTheme

// ==================== 统一色彩设计系统令牌映射 ====================
private val DarkBg @Composable get() = AppTheme.colors.background
private val CardBg @Composable get() = AppTheme.colors.surface
private val CardBorder @Composable get() = AppTheme.colors.cardBorder
private val AccentCyan @Composable get() = AppTheme.colors.accentCyan
private val AccentPurple @Composable get() = AppTheme.colors.accentPurple
private val AccentGreen @Composable get() = AppTheme.colors.accentGreen
private val AccentAmber @Composable get() = AppTheme.colors.accentAmber
private val AccentRed @Composable get() = AppTheme.colors.accentRed
private val TextPrimary @Composable get() = AppTheme.colors.textPrimary
private val TextSecondary @Composable get() = AppTheme.colors.textSecondary
private val TerminalBg @Composable get() = AppTheme.colors.terminalBg
private val TerminalText @Composable get() = AppTheme.colors.terminalText

// ==================== DashboardScreen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 沉浸式状态栏（页面级，深色图标）
    SystemBarEffect(
        statusBarColor = Color.Transparent,
        darkStatusBarIcons = false
    )

    // 全屏深色背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 顶部光晕装饰
        val purpleColor = AccentPurple
        val cyanColor = AccentCyan
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(purpleColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 600f
                ),
                radius = 600f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cyanColor.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width, size.height * 0.3f),
                    radius = 400f
                ),
                radius = 400f,
                center = Offset(size.width, size.height * 0.3f)
            )
        }

        AdaptiveContentLayout { adaptiveState ->
            if (adaptiveState.showTwoPanes) {
                // ============ 双栏布局（横屏 / 平板） ============
                Row(modifier = Modifier.fillMaxSize()) {
                    // 左栏：控制面板
                    ControlPanel(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight(),
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    // 分隔线
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(CardBorder)
                    )
                    // 右栏：终端日志
                    TerminalPanel(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight(),
                        logs = uiState.terminalLogs,
                        onClear = viewModel::clearTerminal
                    )
                }
            } else {
                // ============ 单栏布局（竖屏手机） ============
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 56.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { DashboardHeader(viewModel, uiState) }
                    item { LoadingAndErrorDemoCard(uiState, viewModel) }
                    item { UnifiedDialogDemoCard(uiState, viewModel) }
                    item { ProtocolSwitchesCard(uiState, viewModel) }
                    item { HttpTestCard(viewModel) }
                    item { CacheManagementCard(uiState, viewModel) }
                    item { BrvahRecyclerCard(uiState) }
                    item { MqttTestCard(uiState, viewModel) }
                    item { RedisTestCard(uiState, viewModel) }
                    item { SocketTestCard(uiState, viewModel) }
                    item { DiagnosticsCard(viewModel) }
                    item { UiSettingsCard(uiState, viewModel) }
                    item {
                        TerminalLogCard(
                            logs = uiState.terminalLogs,
                            onClear = viewModel::clearTerminal,
                            fillHeight = false
                        )
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }

        // ==================== 统一设计规范弹窗挂载 ====================

        AppConfirmDialog(
            visible = uiState.showConfirmDialog,
            title = "核心协议复位确认",
            message = "您正在请求重置物联网核心连接池与网关通道，操作将重新协商握手密钥，请确认是否执行？",
            confirmText = "确认复位",
            isDanger = true,
            onConfirm = viewModel::onConfirmDialogConfirmed,
            onDismiss = { viewModel.setConfirmDialog(false) }
        )

        AppLoadingDialog(
            visible = uiState.showLoadingDialog,
            message = "正在与物联网网关建立安全长连接..."
        )
        if (uiState.showLoadingDialog) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.setLoadingDialog(false)
            }
        }

        AppInputDialog(
            visible = uiState.showInputDialog,
            title = "修改边缘节点标识",
            hint = "如: ANDROID_AIOT_NODE_01",
            initialText = "IOT_EDGE_DEV_ALPHA",
            confirmText = "保存更改",
            onConfirm = viewModel::onInputDialogConfirmed,
            onDismiss = { viewModel.setInputDialog(false) }
        )

        AppBottomSheetDialog(
            visible = uiState.showBottomSheet,
            title = "物联网实时监控抽屉面板",
            onDismiss = { viewModel.setBottomSheet(false) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("● 当前生效协议: HTTP(REST), MQTT, TCP Socket", color = TextPrimary, fontSize = 14.sp)
                Text("● 缓存策略: ${uiState.currentCacheType.title}", color = TextSecondary, fontSize = 13.sp)
                Text("● 当前显示模式: ${uiState.themeMode.title}", color = AccentCyan, fontSize = 13.sp)
                Text("● 弹窗技术方案: XPopup 4 + Compose AppDialog 统一设计标准", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // ==================== 耗时操作进度与真实错误弹窗挂载 ====================

        AppProgressDialog(
            config = uiState.loadingConfig,
            onDismissRequest = viewModel::dismissLoading
        )

        AppErrorDialog(
            visible = uiState.showErrorDialog,
            error = uiState.parsedError,
            onDismiss = viewModel::dismissError,
            onShareReport = viewModel::shareErrorReport
        )
    }
}

// ==================== 双栏子布局 ====================

@Composable
private fun ControlPanel(
    modifier: Modifier,
    uiState: DashboardUiState,
    viewModel: DashboardViewModel
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { DashboardHeader(viewModel, uiState) }
        item { LoadingAndErrorDemoCard(uiState, viewModel) }
        item { UnifiedDialogDemoCard(uiState, viewModel) }
        item { ProtocolSwitchesCard(uiState, viewModel) }
        item { HttpTestCard(viewModel) }
        item { CacheManagementCard(uiState, viewModel) }
        item { BrvahRecyclerCard(uiState) }
        item { MqttTestCard(uiState, viewModel) }
        item { RedisTestCard(uiState, viewModel) }
        item { SocketTestCard(uiState, viewModel) }
        item { DiagnosticsCard(viewModel) }
        item { UiSettingsCard(uiState, viewModel) }
    }
}

@Composable
private fun TerminalPanel(
    modifier: Modifier,
    logs: List<String>,
    onClear: () -> Unit
) {
    Column(modifier = modifier.padding(16.dp)) {
        TerminalLogCard(
            logs = logs,
            onClear = onClear,
            fillHeight = true
        )
    }
}

// ==================== Header ====================

@Composable
private fun DashboardHeader(vm: DashboardViewModel, uiState: DashboardUiState) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 动态脉冲图标
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            Icon(
                imageVector = Icons.Filled.Hub,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "IoT 调试控制面板",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.themeMode.title} · Enterprise AIoT",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // 主题切换快捷按钮 (普通模式 / 夜间模式)
            IconButton(
                onClick = vm::toggleTheme,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceVariant)
                    .border(1.dp, CardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (AppTheme.colors.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "切换显示模式",
                    tint = if (AppTheme.colors.isDark) AccentAmber else AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==================== 统一风格弹窗展示卡片 ====================

@Composable
private fun UnifiedDialogDemoCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(
        title = "统一风格弹窗体系 (XPopup / AppDialog)",
        icon = Icons.Filled.SmartButton,
        iconTint = AccentPurple
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "针对物联网业务场景统一封装，在普通模式与夜间模式下均保证高对比度视觉质感：",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IotButton(
                    text = "确认/高危弹窗",
                    icon = Icons.Filled.CheckCircle,
                    color = AccentRed,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setConfirmDialog(true) }
                )
                IotButton(
                    text = "加载等待弹窗",
                    icon = Icons.Filled.HourglassTop,
                    color = AccentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setLoadingDialog(true) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IotButton(
                    text = "参数输入弹窗",
                    icon = Icons.Filled.EditNote,
                    color = AccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setInputDialog(true) }
                )
                IotButton(
                    text = "底部抽屉面板",
                    icon = Icons.Filled.VerticalAlignTop,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setBottomSheet(true) }
                )
            }
        }
    }
}

// ==================== 耗时操作进度与真实错误弹窗体系卡片 ====================

@Composable
private fun LoadingAndErrorDemoCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(
        title = "耗时操作进度与错误诊断体系",
        icon = Icons.Filled.HourglassBottom,
        iconTint = AccentCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "支持开发者自主配置启用/禁用、阻塞/非阻塞，错误弹窗非阻塞呈现真实堆栈并支持一键系统分享：",
                color = TextSecondary,
                fontSize = 11.sp
            )

            // 开发者开关行 1: 启用进度弹窗
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("启用耗时进度弹窗", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.enableLoadingDialog) "耗时操作展示进度弹窗" else "静默后台执行 (无遮罩)",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = uiState.enableLoadingDialog,
                    onCheckedChange = { vm.toggleEnableLoadingDialog() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentCyan
                    )
                )
            }

            // 开发者开关行 2: 阻塞式 vs 非阻塞式
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("弹窗交互模式", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.isBlockingDefault) "阻塞式 (防并发点击穿透/不可取消)" else "非阻塞式 (可点击外部或手动取消)",
                        color = if (uiState.isBlockingDefault) AccentAmber else AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = uiState.isBlockingDefault,
                    onCheckedChange = { vm.toggleBlockingDefault() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentAmber
                    )
                )
            }

            // 进度功能测试按钮
            Text("进度窗形态实测:", color = TextSecondary, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IotButton(
                    text = "阻塞式",
                    icon = Icons.Filled.Lock,
                    color = AccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testBlockingProgress
                )
                IotButton(
                    text = "非阻塞",
                    icon = Icons.Filled.LockOpen,
                    color = AccentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testNonBlockingProgress
                )
                IotButton(
                    text = "0%~100%",
                    icon = Icons.Filled.LinearScale,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testPercentageProgress
                )
            }

            // 真实错误与系统分享测试按钮
            Text("非阻塞真实错误诊断与一键分享实测:", color = TextSecondary, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IotButton(
                    text = "模拟网络超时",
                    icon = Icons.Filled.WifiOff,
                    color = AccentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testSimulateTimeoutError
                )
                IotButton(
                    text = "模拟协议禁用",
                    icon = Icons.Filled.ReportProblem,
                    color = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testSimulateProtocolDisabledError
                )
            }
        }
    }
}

// ==================== 协议开关卡片 ====================

@Composable
private fun ProtocolSwitchesCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(title = "协议开关", icon = Icons.Filled.ToggleOn, iconTint = AccentPurple) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ProtocolSwitchRow(
                label = "HTTP",
                compiled = uiState.switches.isHttpCompiled,
                enabled = uiState.switches.isHttpEnabled,
                connected = null,
                onToggle = vm::toggleHttp,
                activeColor = AccentCyan
            )
            ProtocolSwitchRow(
                label = "MQTT",
                compiled = uiState.switches.isMqttCompiled,
                enabled = uiState.switches.isMqttEnabled,
                connected = uiState.mqttConnected,
                onToggle = vm::toggleMqtt,
                activeColor = AccentGreen
            )
            ProtocolSwitchRow(
                label = "Redis",
                compiled = uiState.switches.isRedisCompiled,
                enabled = uiState.switches.isRedisEnabled,
                connected = uiState.redisConnected,
                onToggle = vm::toggleRedis,
                activeColor = AccentAmber
            )
            ProtocolSwitchRow(
                label = "Socket",
                compiled = uiState.switches.isSocketCompiled,
                enabled = uiState.switches.isSocketEnabled,
                connected = uiState.socketConnected,
                onToggle = vm::toggleSocket,
                activeColor = AccentPurple
            )
        }
    }
}

@Composable
private fun ProtocolSwitchRow(
    label: String,
    compiled: Boolean,
    enabled: Boolean,
    connected: Boolean?,
    onToggle: (Boolean) -> Unit,
    activeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 连接状态指示灯
            if (connected != null && compiled) {
                val dotColor = when {
                    !enabled -> TextSecondary.copy(alpha = 0.4f)
                    connected -> AccentGreen
                    else -> AccentRed
                }
                val infiniteTransition = rememberInfiniteTransition(label = "dot_$label")
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (connected && enabled) dotColor.copy(alpha = dotAlpha) else dotColor,
                            shape = CircleShape
                        )
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                color = if (compiled && enabled) TextPrimary else TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (!compiled) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CardBorder.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "未编译入包",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Switch(
            checked = enabled && compiled,
            onCheckedChange = onToggle,
            enabled = compiled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                uncheckedTrackColor = CardBorder,
                disabledCheckedTrackColor = CardBorder,
                disabledUncheckedTrackColor = CardBorder.copy(alpha = 0.5f)
            )
        )
    }
}

// ==================== HTTP 测试卡片 ====================

@Composable
private fun HttpTestCard(vm: DashboardViewModel) {
    IotCard(title = "HTTP 请求与传输测试", icon = Icons.Filled.Cloud, iconTint = AccentCyan) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 第一排：RESTful 常用指令
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IotButton(
                    text = "GET",
                    icon = Icons.Filled.Download,
                    color = AccentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testHttpGet
                )
                IotButton(
                    text = "POST",
                    icon = Icons.Filled.Upload,
                    color = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testHttpPost
                )
                IotButton(
                    text = "PUT",
                    icon = Icons.Filled.DriveFileRenameOutline,
                    color = AccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testHttpPut
                )
                IotButton(
                    text = "DELETE",
                    icon = Icons.Filled.Delete,
                    color = AccentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testHttpDelete
                )
            }
            // 第二排：大文件上传与流式下载
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IotButton(
                    text = "文件上传 (Multipart)",
                    icon = Icons.Filled.FileUpload,
                    color = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testHttpUpload
                )
                IotButton(
                    text = "文件流式下载",
                    icon = Icons.Filled.FileDownload,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::testHttpDownload
                )
            }
        }
    }
}

// ==================== MQTT 测试卡片 ====================

@Composable
private fun MqttTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(title = "MQTT 测试", icon = Icons.Filled.Router, iconTint = AccentGreen) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IotButton(
                text = if (uiState.mqttConnected) "已连接 ✓" else "连接 Broker",
                icon = if (uiState.mqttConnected) Icons.Filled.CheckCircle else Icons.Filled.Link,
                color = if (uiState.mqttConnected) AccentGreen else AccentCyan,
                modifier = Modifier.fillMaxWidth(),
                onClick = vm::connectMqtt
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IotButton(
                    text = "Publish",
                    icon = Icons.Filled.Send,
                    color = AccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = vm::mqttPublish
                )
                IotButton(
                    text = "Subscribe",
                    icon = Icons.Filled.Inbox,
                    color = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = vm::mqttSubscribeTest
                )
            }
        }
    }
}

// ==================== Redis 测试卡片 ====================

@Composable
private fun RedisTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(title = "Redis 远控", icon = Icons.Filled.Storage, iconTint = AccentAmber) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IotButton(
                text = if (uiState.redisConnected) "已连接 ✓" else "连接 Redis",
                icon = if (uiState.redisConnected) Icons.Filled.CheckCircle else Icons.Filled.Link,
                color = if (uiState.redisConnected) AccentGreen else AccentAmber,
                modifier = Modifier.fillMaxWidth(),
                onClick = vm::connectRedis
            )
            IotButton(
                text = "执行 SET / GET",
                icon = Icons.Filled.DataObject,
                color = AccentCyan,
                modifier = Modifier.fillMaxWidth(),
                onClick = vm::redisSendCommand
            )
        }
    }
}

// ==================== Socket 测试卡片 ====================

@Composable
private fun SocketTestCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(title = "Socket TCP", icon = Icons.Filled.Cable, iconTint = AccentPurple) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IotButton(
                text = if (uiState.socketConnected) "已连接 ✓" else "连接 Socket",
                icon = if (uiState.socketConnected) Icons.Filled.CheckCircle else Icons.Filled.Link,
                color = if (uiState.socketConnected) AccentGreen else AccentPurple,
                modifier = Modifier.fillMaxWidth(),
                onClick = vm::connectSocket
            )
            IotButton(
                text = "Ping-Pong 测试",
                icon = Icons.Filled.SwapHoriz,
                color = AccentCyan,
                modifier = Modifier.fillMaxWidth(),
                onClick = vm::socketPingPong
            )
        }
    }
}

// ==================== 诊断工具卡片 ====================

@Composable
private fun DiagnosticsCard(vm: DashboardViewModel) {
    IotCard(title = "诊断工具", icon = Icons.Filled.BugReport, iconTint = AccentRed) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IotButton(
                text = "超长日志测试",
                icon = Icons.Filled.Article,
                color = AccentAmber,
                modifier = Modifier.fillMaxWidth(),
                onClick = vm::triggerLongLog
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IotButton(
                    text = "触发崩溃",
                    icon = Icons.Filled.Warning,
                    color = AccentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::triggerCrash
                )
                IotButton(
                    text = "分享日志",
                    icon = Icons.Filled.Share,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::shareLogFiles
                )
            }
        }
    }
}

// ==================== UI 设置卡片 ====================

@Composable
private fun UiSettingsCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(title = "UI 与显示", icon = Icons.Filled.Tune, iconTint = AccentCyan) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IotButton(
                text = if (uiState.isImmersive) "退出沉浸" else "沉浸模式",
                icon = if (uiState.isImmersive) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                color = if (uiState.isImmersive) AccentAmber else AccentPurple,
                modifier = Modifier.weight(1f),
                onClick = vm::toggleImmersive
            )
        }
    }
}

// ==================== 终端日志卡片 ====================

@Composable
private fun TerminalLogCard(
    logs: List<String>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    fillHeight: Boolean = false
) {
    val listState = rememberLazyListState()
    // 每当 logs 更新，自动滚动到最底部
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Terminal,
                    contentDescription = null,
                    tint = TerminalText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "实时终端",
                    color = TerminalText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
            IconButton(onClick = onClear, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "清空日志",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.weight(1f) else Modifier.heightIn(min = 200.dp, max = 400.dp))
                .background(TerminalBg, RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "> 等待操作输出...",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(state = listState) {
                    items(logs) { line ->
                        Text(
                            text = "> $line",
                            color = when {
                                line.contains("❌") || line.contains("失败") || line.contains("异常") -> AccentRed
                                line.contains("✅") || line.contains("成功") || line.contains("已连接") -> AccentGreen
                                line.contains("⚠️") || line.contains("警告") -> AccentAmber
                                line.contains("RECV") || line.contains("←") -> AccentCyan
                                else -> TerminalText
                            },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== 通用卡片容器 ====================

@Composable
private fun IotCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 卡片标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

// ==================== 通用按钮 ====================

@Composable
private fun IotButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==================== 自定义缓存与文件分享卡片 ====================

@Composable
private fun CacheManagementCard(uiState: DashboardUiState, vm: DashboardViewModel) {
    IotCard(title = "自定义缓存位置与系统分享", icon = Icons.Filled.FolderZip, iconTint = AccentAmber) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "当前策略: ${uiState.currentCacheType.title}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "落盘路径: ${uiState.currentCachePath.ifEmpty { "初始化中..." }}",
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 策略单选 Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CacheLocationType.entries.forEach { type ->
                    val selected = uiState.currentCacheType == type
                    FilterChip(
                        selected = selected,
                        onClick = { vm.setCacheLocationType(type) },
                        label = {
                            Text(
                                text = when (type) {
                                    CacheLocationType.INTERNAL -> "内部缓存"
                                    CacheLocationType.EXTERNAL -> "外部缓存"
                                    CacheLocationType.EXTERNAL_DOWNLOADS -> "专属下载区"
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentAmber.copy(alpha = 0.2f),
                            selectedLabelColor = AccentAmber
                        )
                    )
                }
            }

            // 操作按钮行：分享最近下载文件与清空缓存
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IotButton(
                    text = "系统分享下载文件 (${uiState.cacheFilesCount})",
                    icon = Icons.Filled.Share,
                    color = AccentGreen,
                    modifier = Modifier.weight(1f),
                    onClick = vm::shareLatestDownloadedFile
                )
                IotButton(
                    text = "清空缓存区",
                    icon = Icons.Filled.DeleteSweep,
                    color = AccentRed,
                    modifier = Modifier.weight(1f),
                    onClick = vm::clearCache
                )
            }
        }
    }
}

// ==================== BRVAH 4 列表框架演示卡片 ====================

@Composable
private fun BrvahRecyclerCard(uiState: DashboardUiState) {
    IotCard(title = "BRVAH 4 顶流列表框架 (GitHub 24k+ ★)", icon = Icons.Filled.ListAlt, iconTint = AccentCyan) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "集成 BaseRecyclerViewAdapterHelper 4，原生 RecyclerView 与 Compose 无缝混编展示设备状态：",
                color = TextSecondary,
                fontSize = 11.sp
            )
            val isDark = AppTheme.colors.isDark
            AndroidView(
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        val adapter = IotDeviceQuickAdapter(isDark = isDark)
                        this.adapter = adapter
                        adapter.submitList(uiState.brvahDevices)
                    }
                },
                update = { recyclerView ->
                    (recyclerView.adapter as? IotDeviceQuickAdapter)?.let { adapter ->
                        adapter.isDark = isDark
                        adapter.submitList(uiState.brvahDevices)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}
