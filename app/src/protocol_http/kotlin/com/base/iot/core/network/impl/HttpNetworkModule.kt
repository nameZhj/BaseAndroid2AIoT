package com.base.iot.core.network.impl

import com.base.iot.core.config.AppConfig
import com.base.iot.core.diagnostics.Lg
import com.base.iot.core.network.HttpManager
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HttpBindingModule {
    @Binds
    @Singleton
    abstract fun bindHttpManager(impl: HttpManagerRetrofitImpl): HttpManager
}

@Module
@InstallIn(SingletonComponent::class)
object HttpProviderModule {

    private const val TAG = "HTTP-LOG"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // 智能防 OOM 日志拦截器：严格按时序记录生命周期，自动过滤文件/多媒体流正文
        val smartLoggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            val startNs = System.nanoTime()
            val reqBody = request.body
            val isMultipartOrBinary = reqBody?.contentType()?.let { type ->
                type.type.contains("multipart", true) ||
                        type.subtype.contains("octet-stream", true) ||
                        type.type.contains("image", true) ||
                        type.type.contains("video", true) ||
                        type.type.contains("audio", true)
            } ?: false

            Lg.i(TAG, "[HTTP] [1/3 CONNECTING] → ${request.method} ${request.url}")
            if (isMultipartOrBinary) {
                Lg.i(TAG, "[HTTP] [1/3 REQUEST] [文件/多媒体流] 类型: ${reqBody?.contentType()}, 大小: ${reqBody?.contentLength()} 字节 (已自动略过原始数据流防OOM)")
            }

            val response: Response
            try {
                response = chain.proceed(request)
            } catch (e: Exception) {
                Lg.e(TAG, "[HTTP] [3/3 DISCONNECTED/ERROR] 异常: ${e.message}", e)
                throw e
            }

            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            val respBody = response.body
            val isRespBinary = respBody?.contentType()?.let { type ->
                type.type.contains("octet-stream", true) ||
                        type.type.contains("image", true) ||
                        type.type.contains("video", true) ||
                        type.type.contains("audio", true)
            } ?: false

            if (isRespBinary) {
                Lg.i(TAG, "[HTTP] [2/3 TRANSFER] ← ${response.code} ${response.message} (${tookMs}ms) [多媒体/二进制流: ${respBody?.contentLength()} 字节]")
            } else {
                Lg.i(TAG, "[HTTP] [2/3 TRANSFER] ← ${response.code} ${response.message} (${tookMs}ms)")
            }
            Lg.i(TAG, "[HTTP] [3/3 CLOSED] 交互完成 (${tookMs}ms)")

            response
        }

        return OkHttpClient.Builder()
            .connectTimeout(AppConfig.HTTP_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(AppConfig.HTTP_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.HTTP_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .addInterceptor(smartLoggingInterceptor)
            .hostnameVerifier { hostname, session ->
                // 物联网特定系统约束处理：局域网私有 IP (192.168.x.x / 10.x.x.x / 127.0.0.1) 允许自签 IP 直连
                if (hostname.startsWith("192.168.") || hostname.startsWith("10.") ||
                    hostname == "localhost" || hostname == "127.0.0.1" || hostname == "10.0.2.2"
                ) {
                    true
                } else {
                    javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                }
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConfig.baseHttpUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}
