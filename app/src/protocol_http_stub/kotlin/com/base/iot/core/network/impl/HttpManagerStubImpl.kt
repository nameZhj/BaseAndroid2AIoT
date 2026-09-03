package com.base.iot.core.network.impl

import com.base.iot.core.network.DownloadState
import com.base.iot.core.network.HttpManager
import com.base.iot.core.network.HttpResult
import com.base.iot.core.network.ProgressListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 当 HTTP 在编译期被关闭时使用的轻量 Stub 占位实现。
 * 完全不引入 Retrofit、OkHttp 及 Okio，保证零依赖、零体积增加。
 */
@Singleton
class HttpManagerStubImpl @Inject constructor() : HttpManager {
    override val isCompiled: Boolean get() = false
    override val isEnabled: Boolean get() = false

    private val disabledMessage =
        "HTTP 协议在编译期未被编译入包（对应框架已被彻底剔除以削减体积）。请在 gradle.properties 中开启 iot.protocol.http.enabled=true 后重新编译。"

    override suspend fun <T : Any> executeGet(
        url: String,
        queryParams: Map<String, String>?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = HttpResult.Error(-1, disabledMessage)

    override suspend fun <T : Any> executePost(
        url: String,
        body: Any?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = HttpResult.Error(-1, disabledMessage)

    override suspend fun <T : Any> executePut(
        url: String,
        body: Any?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = HttpResult.Error(-1, disabledMessage)

    override suspend fun <T : Any> executeDelete(
        url: String,
        queryParams: Map<String, String>?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = HttpResult.Error(-1, disabledMessage)

    override suspend fun <T : Any> executePatch(
        url: String,
        body: Any?,
        headers: Map<String, String>?,
        type: Type
    ): HttpResult<T> = HttpResult.Error(-1, disabledMessage)

    override suspend fun <T : Any> executeUpload(
        url: String,
        file: File,
        paramName: String,
        formFields: Map<String, String>?,
        headers: Map<String, String>?,
        type: Type,
        onProgress: ProgressListener?
    ): HttpResult<T> = HttpResult.Error(-1, disabledMessage)

    override fun download(
        url: String,
        destFile: File,
        headers: Map<String, String>?
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Error(disabledMessage))
    }
}
