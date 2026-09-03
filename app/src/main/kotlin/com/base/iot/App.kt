package com.base.iot

import android.app.Application
import com.base.iot.core.diagnostics.CrashHandler
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 入口，由 Hilt 管理依赖注入。
 * 在此处完成全局初始化：崩溃拦截器安装。
 */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 安装全局崩溃拦截器
        CrashHandler.install(this)
    }
}
