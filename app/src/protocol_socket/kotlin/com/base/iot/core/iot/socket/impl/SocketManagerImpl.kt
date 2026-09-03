package com.base.iot.core.iot.socket.impl

import com.base.iot.core.config.AppConfig
import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.iot.SocketConnectionState
import com.base.iot.core.iot.SocketManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManagerImpl @Inject constructor(
    private val protocolConfig: IotProtocolConfig
) : SocketManager {

    private val TAG = "SocketManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val isCompiled: Boolean get() = true
    override val isEnabled: Boolean get() = _connectionState.value == SocketConnectionState.CONNECTED

    @Volatile
    private var socket: Socket? = null

    @Volatile
    private var writer: PrintWriter? = null

    @Volatile
    private var heartbeatJob: Job? = null

    @Volatile
    private var reconnectJob: Job? = null

    private var currentHost = AppConfig.SOCKET_HOST
    private var currentPort = AppConfig.SOCKET_PORT

    private val _connectionState = MutableStateFlow(SocketConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<SocketConnectionState> = _connectionState.asStateFlow()

    private val _incomingData = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64
    )
    override val incomingData: SharedFlow<String> = _incomingData.asSharedFlow()

    override suspend fun connect(host: String, port: Int) = withContext(Dispatchers.IO) {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("Socket", isCompiled, switches.isSocketEnabled)

        if (_connectionState.value == SocketConnectionState.CONNECTED) return@withContext

        currentHost = host
        currentPort = port
        _connectionState.value = SocketConnectionState.CONNECTING
        Lg.i(TAG, "[SOCKET] [1/4 CONNECTING] 发起 TCP 握手连接 → $host:$port (超时: ${AppConfig.SOCKET_CONNECT_TIMEOUT_MS}ms)")

        try {
            val s = Socket()
            s.connect(java.net.InetSocketAddress(host, port), AppConfig.SOCKET_CONNECT_TIMEOUT_MS)
            s.soTimeout = 0

            socket = s
            writer = PrintWriter(s.getOutputStream(), true)
            _connectionState.value = SocketConnectionState.CONNECTED
            Lg.i(TAG, "[SOCKET] [2/4 CONNECTED] TCP 握手成功！节点已就绪: $host:$port (本地端口: ${s.localPort})")

            startReader(s)
            startHeartbeat()
        } catch (e: Exception) {
            Lg.e(TAG, "[SOCKET] [4/4 ERROR] 连接失败: ${e.message}", e)
            _connectionState.value = SocketConnectionState.ERROR
            scheduleReconnect()
        }
    }

    override fun disconnect() {
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        Lg.i(TAG, "[SOCKET] [4/4 DISCONNECTING] 正在主动关闭 Socket 管道...")
        try {
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            Lg.w(TAG, "[SOCKET] [4/4 ERROR] 关闭异常: ${e.message}")
        } finally {
            socket = null
            writer = null
            _connectionState.value = SocketConnectionState.DISCONNECTED
            Lg.i(TAG, "[SOCKET] [4/4 CLOSED] Socket 连接已安全断开")
        }
    }

    override suspend fun send(data: String) = withContext(Dispatchers.IO) {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("Socket", isCompiled, switches.isSocketEnabled)

        val w = writer ?: throw IllegalStateException("Socket 未连接")
        val preview = if (data.length > 128) "${data.take(128)}...(${data.length}B)" else data
        Lg.d(TAG, "[SOCKET] [3/4 TRANSFER] [SEND] → 大小: ${data.length}B: $preview")
        w.println(data)
        if (w.checkError()) throw SocketException("写入失败，连接可能已断开")
    }

    override suspend fun sendBytes(data: ByteArray) = withContext(Dispatchers.IO) {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("Socket", isCompiled, switches.isSocketEnabled)

        val s = socket ?: throw IllegalStateException("Socket 未连接")
        Lg.i(TAG, "[SOCKET] [3/4 TRANSFER] [SEND_BYTES] → 数据大小: [${data.size} 字节] (原始二进制流已自动跳过 Logcat 防 OOM)")
        s.getOutputStream().write(data)
        s.getOutputStream().flush()
    }

    override fun receive(): Flow<String> = incomingData

    private fun startReader(s: Socket) {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                while (isActive && !s.isClosed) {
                    val line = reader.readLine() ?: break
                    val preview = if (line.length > 128) "${line.take(128)}...(${line.length}B)" else line
                    Lg.d(TAG, "[SOCKET] [3/4 TRANSFER] [RECV] ← 大小: ${line.length}B: $preview")
                    _incomingData.emit(line)
                }
            } catch (e: SocketException) {
                Lg.w(TAG, "[SOCKET] 读取中断: ${e.message}")
            } catch (e: Exception) {
                Lg.e(TAG, "[SOCKET] 读取异常: ${e.message}", e)
            } finally {
                if (_connectionState.value == SocketConnectionState.CONNECTED) {
                    _connectionState.value = SocketConnectionState.DISCONNECTED
                    scheduleReconnect()
                }
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && _connectionState.value == SocketConnectionState.CONNECTED) {
                delay(AppConfig.SOCKET_HEARTBEAT_INTERVAL_MS)
                try {
                    send(AppConfig.SOCKET_HEARTBEAT_PAYLOAD)
                } catch (e: Exception) {
                    _connectionState.value = SocketConnectionState.DISCONNECTED
                    scheduleReconnect()
                    break
                }
            }
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(AppConfig.SOCKET_RECONNECT_DELAY_MS)
            connect(currentHost, currentPort)
        }
    }
}
