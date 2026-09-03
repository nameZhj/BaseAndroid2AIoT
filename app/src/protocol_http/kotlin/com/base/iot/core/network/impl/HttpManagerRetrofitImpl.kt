package com.base.iot.core.network.impl

import com.base.iot.core.config.IotProtocolConfig
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.network.DownloadState
import com.base.iot.core.network.HttpManager
import com.base.iot.core.network.HttpResult
import com.base.iot.core.network.ProgressListener
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import com.base.iot.core.config.AppConfig
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ==================== 内部 Retrofit 动态接口 ====================

internal interface DynamicRetrofitService {
    @GET
    suspend fun getRaw(
        @Url url: String,
        @QueryMap params: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>

    @POST
    suspend fun postRaw(
        @Url url: String,
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>

    @PUT
    suspend fun putRaw(
        @Url url: String,
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>

    @DELETE
    suspend fun deleteRaw(
        @Url url: String,
        @QueryMap params: Map<String, String>,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>

    @PATCH
    suspend fun patchRaw(
        @Url url: String,
        @Body body: RequestBody,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>

    @Multipart
    @POST
    suspend fun uploadRaw(
        @Url url: String,
        @Part filePart: MultipartBody.Part,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>

    @Streaming
    @GET
    suspend fun downloadRaw(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

// ==================== 带进度监听的 RequestBody ====================

private class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: ProgressListener
) : RequestBody() {
    override fun contentType(): MediaType? = delegate.contentType()
    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val total = contentLength()
        val countingSink = object : ForwardingSink(sink) {
            private var bytesWritten = 0L

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount
                val percent = if (total > 0) (bytesWritten.toFloat() / total * 100f) else -1f
                onProgress(bytesWritten, total, percent)
            }
        }
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}

// ==================== 核心实现类 ====================

@Singleton
class HttpManagerRetrofitImpl @Inject constructor(
    private val retrofit: Retrofit,
    private val gson: Gson,
    private val protocolConfig: IotProtocolConfig
) : HttpManager {

    private val TAG = "HttpManager"

    override val isCompiled: Boolean get() = true
    override val isEnabled: Boolean get() = true

    private val service: DynamicRetrofitService by lazy {
        retrofit.create(DynamicRetrofitService::class.java)
    }

    private val fileTransferService: DynamicRetrofitService by lazy {
        val baseOkHttpClient = (retrofit.callFactory() as? OkHttpClient)
        val fileOkHttpClient = (baseOkHttpClient?.newBuilder() ?: OkHttpClient.Builder())
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(AppConfig.HTTP_FILE_TRANSFER_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.HTTP_FILE_TRANSFER_TIMEOUT_SEC, TimeUnit.SECONDS)
            .callTimeout(AppConfig.HTTP_FILE_TRANSFER_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()

        retrofit.newBuilder()
            .client(fileOkHttpClient)
            .build()
            .create(DynamicRetrofitService::class.java)
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    override suspend fun <T : Any> executeGet(
        url: String,
        queryParams: Map<String, String>?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = withContext(Dispatchers.IO) {
        assertProtocolReady()
        safeExecute("GET", url, type) {
            service.getRaw(url, queryParams ?: emptyMap(), headers ?: emptyMap())
        }
    }

    override suspend fun <T : Any> executePost(
        url: String,
        body: Any?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = withContext(Dispatchers.IO) {
        assertProtocolReady()
        val requestBody = createRequestBody(body)
        safeExecute("POST", url, type) {
            service.postRaw(url, requestBody, headers ?: emptyMap())
        }
    }

    override suspend fun <T : Any> executePut(
        url: String,
        body: Any?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = withContext(Dispatchers.IO) {
        assertProtocolReady()
        val requestBody = createRequestBody(body)
        safeExecute("PUT", url, type) {
            service.putRaw(url, requestBody, headers ?: emptyMap())
        }
    }

    override suspend fun <T : Any> executeDelete(
        url: String,
        queryParams: Map<String, String>?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = withContext(Dispatchers.IO) {
        assertProtocolReady()
        safeExecute("DELETE", url, type) {
            service.deleteRaw(url, queryParams ?: emptyMap(), headers ?: emptyMap())
        }
    }

    override suspend fun <T : Any> executePatch(
        url: String,
        body: Any?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = withContext(Dispatchers.IO) {
        assertProtocolReady()
        val requestBody = createRequestBody(body)
        safeExecute("PATCH", url, type) {
            service.patchRaw(url, requestBody, headers ?: emptyMap())
        }
    }

    override suspend fun <T : Any> executeUpload(
        url: String,
        file: File,
        paramName: String,
        formFields: Map<String, String>?,
        headers: Map<String, String>?,
        type: Type,
        onProgress: ProgressListener?
    ): HttpResult<T> = withContext(Dispatchers.IO) {
        assertProtocolReady()
        val startNs = System.nanoTime()
        Lg.i(TAG, "[HTTP-UPLOAD] [1/3 CONNECTING] → $url")
        Lg.i(
            TAG,
            "[HTTP-UPLOAD] [2/3 TRANSFER] 文件名称: [${file.name}], 文件大小: [${file.length()} 字节], 缓存路径: [${file.absolutePath}] (流媒体内容不打入Logcat防OOM)"
        )

        try {
            val fileMediaType = "application/octet-stream".toMediaTypeOrNull()
            val rawRequestBody = file.asRequestBody(fileMediaType)
            val wrappedBody = if (onProgress != null) {
                ProgressRequestBody(rawRequestBody, onProgress)
            } else {
                rawRequestBody
            }
            val filePart = MultipartBody.Part.createFormData(paramName, file.name, wrappedBody)

            val partMap = mutableMapOf<String, RequestBody>()
            formFields?.forEach { (k, v) ->
                partMap[k] = v.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            val result: HttpResult<T> = safeExecute("UPLOAD", url, type) {
                fileTransferService.uploadRaw(url, filePart, partMap, headers ?: emptyMap())
            }
            val tookMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            Lg.i(TAG, "[HTTP-UPLOAD] [3/3 CLOSED] 上传交互结束 (耗时: ${tookMs}ms)")
            result
        } catch (e: Exception) {
            Lg.e(TAG, "[HTTP-UPLOAD] [3/3 DISCONNECTED/ERROR] 上传异常: ${e.message}", e)
            HttpResult.Error(-1, e.message ?: "Upload exception", e)
        }
    }

    override fun download(
        url: String,
        destFile: File,
        headers: Map<String, String>?
    ): Flow<DownloadState> = flow {
        assertProtocolReady()
        val startNs = System.nanoTime()
        emit(DownloadState.Idle)
        Lg.i(TAG, "[HTTP-DOWNLOAD] [1/3 CONNECTING] 准备建立下载流 → $url")
        Lg.i(
            TAG,
            "[HTTP-DOWNLOAD] [2/3 TRANSFER] 目标文件名: [${destFile.name}], 缓存落盘路径: [${destFile.absolutePath}]"
        )

        try {
            val response = fileTransferService.downloadRaw(url, headers ?: emptyMap())
            if (!response.isSuccessful || response.body() == null) {
                val errMsg = "下载失败，HTTP状态码: ${response.code()}"
                Lg.e(TAG, "[HTTP-DOWNLOAD] [3/3 ERROR] $errMsg")
                emit(DownloadState.Error(errMsg))
                return@flow
            }

            val body = response.body()!!
            val totalBytes = body.contentLength()
            var bytesRead = 0L

            destFile.parentFile?.mkdirs()

            body.byteStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var lastPercent = -1f

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read

                        val percent = if (totalBytes > 0) (bytesRead.toFloat() / totalBytes * 100f) else -1f
                        if (percent - lastPercent >= 1f || bytesRead == totalBytes) {
                            lastPercent = percent
                            emit(DownloadState.Progress(bytesRead, totalBytes, percent))
                        }
                    }
                    output.flush()
                }
            }

            val tookMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            Lg.i(
                TAG,
                "[HTTP-DOWNLOAD] [3/3 CLOSED] 流式下载完成！文件名称: [${destFile.name}], 最终大小: [${destFile.length()} 字节], 缓存落盘路径: [${destFile.absolutePath}] (耗时: ${tookMs}ms)"
            )
            emit(DownloadState.Success(destFile))
        } catch (e: Exception) {
            Lg.e(TAG, "[HTTP-DOWNLOAD] [3/3 DISCONNECTED/ERROR] 下载异常: ${e.message}", e)
            emit(DownloadState.Error(e.message ?: "Download failed", e))
        }
    }.flowOn(Dispatchers.IO)

    // ==================== 辅助方法 ====================

    private suspend fun assertProtocolReady() {
        val switches = protocolConfig.switchesFlow.first()
        protocolConfig.assertEnabled("HTTP", isCompiled, switches.isHttpEnabled)
    }

    private fun createRequestBody(body: Any?): RequestBody {
        return when (body) {
            null -> ByteArray(0).toRequestBody(null, 0, 0)
            is RequestBody -> body
            is String -> body.toRequestBody(jsonMediaType)
            else -> gson.toJson(body).toRequestBody(jsonMediaType)
        }
    }

    private suspend fun <T : Any> safeExecute(
        method: String,
        url: String,
        type: Type,
        block: suspend () -> Response<ResponseBody>
    ): HttpResult<T> {
        val startNs = System.nanoTime()
        Lg.d(TAG, "[HTTP] [1/3 CONNECTING] → [$method] $url")
        return try {
            val response = block()
            val code = response.code()
            val tookMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            val bodyString = response.body()?.string() ?: ""

            if (response.isSuccessful) {
                // 防 OOM：截取前 256 字符，避免大响应打爆 Logcat
                val preview = if (bodyString.length > 256) "${bodyString.take(256)}... (共${bodyString.length}字)" else bodyString
                Lg.d(TAG, "[HTTP] [2/3 TRANSFER] ← $code (耗时: ${tookMs}ms): $preview")
                Lg.d(TAG, "[HTTP] [3/3 CLOSED] 交互正常结束 ($method $url)")

                val parsed: T = if (type == String::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    bodyString as T
                } else {
                    gson.fromJson(bodyString, type)
                }
                HttpResult.Success(parsed, code)
            } else {
                val errMsg = response.errorBody()?.string() ?: "HTTP error $code"
                Lg.e(TAG, "[HTTP] [3/3 ERROR] ← $code (耗时: ${tookMs}ms) $errMsg")
                HttpResult.Error(code, errMsg)
            }
        } catch (e: Exception) {
            Lg.e(TAG, "[HTTP] [3/3 DISCONNECTED/ERROR] 异常: ${e.message}", e)
            HttpResult.Error(-1, e.message ?: "Network error", e)
        }
    }
}
