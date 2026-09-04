package com.base.iot.core.network

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.lang.reflect.Type

// ==================== 泛型响应与下载模型 ====================

/**
 * HTTP 请求结果密封类。
 */
sealed class HttpResult<out T> {
    data class Success<T>(val data: T, val code: Int = 200) : HttpResult<T>()
    data class Error(val code: Int, val message: String, val cause: Throwable? = null) : HttpResult<Nothing>()
}

/**
 * 文件下载进度与状态模型。
 */
sealed class DownloadState {
    data object Idle : DownloadState()
    data class Progress(val bytesRead: Long, val totalBytes: Long, val percent: Float) : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Error(val message: String, val cause: Throwable? = null) : DownloadState()
}

/**
 * 进度回调函数别名。
 */
typealias ProgressListener = (bytesTransferred: Long, totalBytes: Long, percent: Float) -> Unit

// ==================== HttpManager 接口抽象 ====================

/**
 * 企业级 HTTP 请求管理器通用接口。
 * 支持：GET / POST / PUT / DELETE / PATCH 以及大文件流式上传与下载。
 * 当编译期开关开启时，由 Retrofit + OkHttp + Okio 完整实现；
 * 当编译期开关关闭时，对应第三方库彻底从 APK 中剔除，由轻量 Stub 替代以缩减包体积。
 */
interface HttpManager {
    /** 编译期是否编译打包了该协议框架 */
    val isCompiled: Boolean

    /** 运行时协议是否处于开启状态 */
    val isEnabled: Boolean

    // ==================== RESTful 基础操作 ====================

    suspend fun <T : Any> executeGet(
        url: String,
        queryParams: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        type: Type
    ): HttpResult<T>

    suspend fun <T : Any> executePost(
        url: String,
        body: Any? = null,
        headers: Map<String, String>? = null,
        type: Type
    ): HttpResult<T>

    suspend fun <T : Any> executePut(
        url: String,
        body: Any? = null,
        headers: Map<String, String>? = null,
        type: Type
    ): HttpResult<T>

    suspend fun <T : Any> executeDelete(
        url: String,
        queryParams: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        type: Type
    ): HttpResult<T>

    suspend fun <T : Any> executePatch(
        url: String,
        body: Any? = null,
        headers: Map<String, String>? = null,
        type: Type
    ): HttpResult<T>

    // ==================== 文件上传与下载 ====================

    /**
     * Multipart 形式上传文件，带上传进度监听。
     */
    suspend fun <T : Any> executeUpload(
        url: String,
        file: File,
        paramName: String = "file",
        formFields: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        type: Type,
        onProgress: ProgressListener? = null
    ): HttpResult<T>

    /**
     * 流式下载文件到指定路径，返回响应式进度 Flow。
     */
    fun download(
        url: String,
        destFile: File,
        headers: Map<String, String>? = null
    ): Flow<DownloadState>
}

// ==================== 泛型内联扩展函数 ====================

suspend inline fun <reified T : Any> HttpManager.get(
    url: String,
    queryParams: Map<String, String>? = null,
    headers: Map<String, String>? = null
): HttpResult<T> = executeGet(url, queryParams, headers, object : TypeToken<T>() {}.type)

suspend inline fun <reified T : Any> HttpManager.post(
    url: String,
    body: Any? = null,
    headers: Map<String, String>? = null
): HttpResult<T> = executePost(url, body, headers, object : TypeToken<T>() {}.type)

suspend inline fun <reified T : Any> HttpManager.put(
    url: String,
    body: Any? = null,
    headers: Map<String, String>? = null
): HttpResult<T> = executePut(url, body, headers, object : TypeToken<T>() {}.type)

suspend inline fun <reified T : Any> HttpManager.delete(
    url: String,
    queryParams: Map<String, String>? = null,
    headers: Map<String, String>? = null
): HttpResult<T> = executeDelete(url, queryParams, headers, object : TypeToken<T>() {}.type)

suspend inline fun <reified T : Any> HttpManager.patch(
    url: String,
    body: Any? = null,
    headers: Map<String, String>? = null
): HttpResult<T> = executePatch(url, body, headers, object : TypeToken<T>() {}.type)

suspend inline fun <reified T : Any> HttpManager.upload(
    url: String,
    file: File,
    paramName: String = "file",
    formFields: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    noinline onProgress: ProgressListener? = null
): HttpResult<T> = executeUpload(url, file, paramName, formFields, headers, object : TypeToken<T>() {}.type, onProgress)
