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
                context.getString(com.base.iot.R.string.err_dns_fail) to
                        context.getString(com.base.iot.R.string.err_dns_fail_desc, rootCause.message ?: "")
            }
            is ConnectException -> {
                context.getString(com.base.iot.R.string.err_conn_refused) to
                        context.getString(com.base.iot.R.string.err_conn_refused_desc, rootCause.message ?: "")
            }
            is SocketTimeoutException -> {
                context.getString(com.base.iot.R.string.err_timeout) to
                        context.getString(com.base.iot.R.string.err_timeout_desc, rootCause.message ?: "")
            }
            is SSLException -> {
                context.getString(com.base.iot.R.string.err_ssl_fail) to
                        context.getString(com.base.iot.R.string.err_ssl_fail_desc, rootCause.message ?: "")
            }
            is ProtocolDisabledException -> {
                context.getString(com.base.iot.R.string.err_protocol_disabled) to
                        context.getString(com.base.iot.R.string.err_protocol_disabled_desc, rootCause.message ?: "")
            }
            else -> {
                val className = rootCause.javaClass.name
                if (className == "retrofit2.HttpException") {
                    val code = try {
                        rootCause.javaClass.getMethod("code").invoke(rootCause) as? Int ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val msg = rootCause.message ?: ""
                    context.getString(com.base.iot.R.string.err_http_server, code) to
                            context.getString(com.base.iot.R.string.err_http_server_desc, code, msg)
                } else {
                    val simpleName = rootCause.javaClass.simpleName
                    context.getString(com.base.iot.R.string.err_unknown_runtime, simpleName) to
                            (rootCause.localizedMessage ?: context.getString(com.base.iot.R.string.err_unknown_runtime_desc))
                }
            }
        }

        val technicalSummary = "${rootCause.javaClass.name}: ${rootCause.message ?: "no message"}"

        val report = buildString {
            appendLine(context.getString(com.base.iot.R.string.err_report_header))
            appendLine(context.getString(com.base.iot.R.string.err_report_time, timeString))
            appendLine(context.getString(com.base.iot.R.string.err_report_category, type))
            appendLine(context.getString(com.base.iot.R.string.err_report_summary, technicalSummary))
            appendLine(context.getString(com.base.iot.R.string.err_report_advice, message))
            appendLine(context.getString(com.base.iot.R.string.err_report_device_info))
            appendLine(context.getString(com.base.iot.R.string.err_report_network, networkStatus))
            appendLine(context.getString(com.base.iot.R.string.err_report_hardware, Build.MANUFACTURER, Build.MODEL, Build.DEVICE))
            appendLine(context.getString(com.base.iot.R.string.err_report_os, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
            appendLine("■ Arch: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine(context.getString(com.base.iot.R.string.err_report_stack))
            appendLine(stackTraceString.trim())
            appendLine(context.getString(com.base.iot.R.string.err_report_footer))
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
                ?: return context.getString(com.base.iot.R.string.net_status_unknown)
            val network = cm.activeNetwork ?: return context.getString(com.base.iot.R.string.net_status_offline)
            val capabilities = cm.getNetworkCapabilities(network) ?: return context.getString(com.base.iot.R.string.net_status_unknown)

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> context.getString(com.base.iot.R.string.net_status_wifi)
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> context.getString(com.base.iot.R.string.net_status_ethernet)
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> context.getString(com.base.iot.R.string.net_status_cellular)
                else -> context.getString(com.base.iot.R.string.net_status_other)
            }
        } catch (e: Exception) {
            context.getString(com.base.iot.R.string.net_status_unknown) + ": ${e.message}"
        }
    }
}
