package com.base.iot.feature.demo

/**
 * 演示模块专属配置常量（与基础框架 AppConfig 严格解耦）。
 * 仅用于 Dashboard 功能演示，正式业务开发时可随 demo 包直接删除。
 */
object DemoConfig {
    const val DEMO_HTTP_GET_URL = "https://httpbin.org/get"
    const val DEMO_HTTP_POST_URL = "https://httpbin.org/post"
    const val DEMO_HTTP_PUT_URL = "https://httpbin.org/put"
    const val DEMO_HTTP_DELETE_URL = "https://httpbin.org/delete"
    const val DEMO_DOWNLOAD_URL = "http://speedtest.tele2.net/100KB.zip"
    const val DEMO_MQTT_PUB_TOPIC = "iot/device/telemetry"
    const val DEMO_MQTT_SUB_TOPIC = "iot/device/cmd"
    const val DEMO_REDIS_KEY = "device:status"
    const val DEMO_UNREACHABLE_IP = "10.255.255.1"
    const val DEMO_UNREACHABLE_PORT = 80
    const val DEMO_TIMEOUT_MOCK_MS = 1500
    const val DEMO_DEFAULT_NODE_ID = "IOT_EDGE_DEV_ALPHA"
}
