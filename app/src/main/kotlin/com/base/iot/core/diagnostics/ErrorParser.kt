package com.base.iot.core.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.base.iot.core.config.ProtocolDisabledException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLException

data class ParsedError(
    val errorType: String,
    val friendlyMessage: String,
    val technicalSummary: String,
    val fullDiagnosticReport: String,
    val rawThrowable: Throwable
)

object ErrorParser {
    fun parse(context: Context, throwable: Throwable): ParsedError {
        val rootCause = getRootCause(throwable)
        val stackTraceString = getStackTraceString(throwable)
        val networkStatus = getNetworkStatus(context)
        val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

        val (type, message) = when (rootCause) {
            is UnknownHostException -> {
                "DNS 解析失败" to "无法解析目标服务器域名 (${rootCause.message})，请检查设备是否连接互联网或本地局域网 DNS 配置。"
            }
            is ConnectException -> {
                "连接被拒绝 (Connection Refused)" to "无法连接到目标主机与端口 (${rootCause.message})，请确认远程服务已启动且工控防火墙已放行。"
            }
            is SocketTimeoutException -> {
                "网络请求超时 (Timeout)" to "TCP 握手或读写超时 (${rootCause.message})，当前网络延迟过高或远程节点无响应。"
            }
            is SSLException -> {
                "SSL/TLS 证书校验失败" to "HTTPS/TLS 安全握手未通过 (${rootCause.message})，可能为自签名证书未加白或证书链过期。"
            }
            is ProtocolDisabledException -> {
                "通信协议未就绪/已禁用" to "当前所请求的通信协议 (${rootCause.message}) 在编译期或运行时被配置为禁用状态。"
            }
            else -> {
                val className = rootCause.javaClass.name
                if (className == "retrofit2.HttpException") {
                    val code = try {
                        rootCause.javaClass.getMethod("code").invoke(rootCause) as? Int ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val msg = rootCause.message ?: "HTTP 状态码异常"
                    "HTTP 服务端错误 [HTTP $code]" to "服务器返回异常状态码: $code $msg"
                } else {
                    val simpleName = rootCause.javaClass.simpleName
                    "系统执行异常 ($simpleName)" to (rootCause.localizedMessage ?: "发生未知运行时异常，请查阅完整堆栈报告。")
                }
            }
        }

        val technicalSummary = "${rootCause.javaClass.name}: ${rootCause.message ?: "no message"}"

        val report = buildString {
            appendLine("==================== 物联网终端错误诊断报告 ====================")
            appendLine("■ 发生时间: $timeString")
            appendLine("■ 错误类别: $type")
            appendLine("■ 核心摘要: $technicalSummary")
            appendLine("■ 友好建议: $message")
            appendLine("-------------------- 设备与环境信息 --------------------")
            appendLine("■ 当前网络: $networkStatus")
            appendLine("■ 硬件设备: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
            appendLine("■ 系统版本: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("■ 架构指令: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine("-------------------- 异常根因堆栈全景 --------------------")
            appendLine(stackTraceString.trim())
            appendLine("==========================================================")
        }

        return ParsedError(
            errorType = type,
            friendlyMessage = message,
            technicalSummary = technicalSummary,
            fullDiagnosticReport = report,
            rawThrowable = throwable
        )
    }

    private fun getRootCause(throwable: Throwable): Throwable {
        var cause: Throwable = throwable
        while (cause.cause != null && cause.cause !== cause) {
            cause = cause.cause!!
        }
        return cause
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun getNetworkStatus(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "未知 (ConnectivityManager 缺失)"
            val network = cm.activeNetwork ?: return "未连接任何网络 (离线)"
            val capabilities = cm.getNetworkCapabilities(network) ?: return "网络能力不可用"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI 局域网已连接"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "有线以太网 (Ethernet) 已连接"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动蜂窝数据已连接"
                else -> "其他连接方式已接入"
            }
        } catch (e: Exception) {
            "获取网络状态异常: ${e.message}"
        }
    }
}
