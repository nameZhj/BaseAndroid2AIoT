# BaseAndroid2AIoT 开源许可证与合规声明手册 (Open Source Licenses & Legal Compliance)

> **法律合规保障**：本项目所有直接与间接引入的核心第三方框架均经过严格的开源许可证合规审查。**全部依赖组件均采用宽松型开源许可协议（Apache-2.0 或 MIT）**，**绝无 GPL/AGPL 等强传染性开源协议**，确保商业公司、工控企业与个人开发者均可 100% 合法合规商用、闭源集成与二次分发。

---

## ⚖️ 一、开源协议兼容性与风险评估

| 开源协议类型 | 传染性 (Copyleft) | 允许商用闭源 | 修改后公开源码要求 | 专利授权保护 | 本项目涉及组件 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Apache-2.0** | **无 (Permissive)** | **允许** | **不要求** (仅需保留版权与修改声明) | **包含明确专利授权条款** | Retrofit, OkHttp, HiveMQ, XPopup, BRVAH, Hilt, Coil, Gson, Kotlinx |
| **MIT** | **无 (Permissive)** | **允许** | **不要求** (仅需保留原作者版权声明) | 默许 | Jedis |
| **GPL / AGPL** | **强传染性** | 限制 | **强制要求全量开源** | 视版本而定 | **本项目 0 引入，彻底规避传染风险** |

---

## 📦 二、第三方框架详细许可证清单

| 框架 / 组件名 | 当前集成版本 | 所属机构 / 版权所有者 | 开源许可证 | 许可证原文链接 | 官方仓库地址 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Retrofit** | `2.11.0` | Square, Inc. | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [square/retrofit](https://github.com/square/retrofit) |
| **OkHttp** | `4.12.0` | Square, Inc. | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [square/okhttp](https://github.com/square/okhttp) |
| **HiveMQ MQTT Client** | `1.3.3` | HiveMQ GmbH | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [hivemq/hivemq-mqtt-client](https://github.com/hivemq/hivemq-mqtt-client) |
| **Jedis** | `5.1.3` | Jonathan Leibiusky & Redis | **MIT** | [License](https://opensource.org/licenses/MIT) | [redis/jedis](https://github.com/redis/jedis) |
| **XPopup** | `2.10.0` | 李晓俊 (li-xiaojun) | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [li-xiaojun/XPopup](https://github.com/li-xiaojun/XPopup) |
| **BRVAH 4** | `4.1.4` | 陈宇明 (CymChad) | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [CymChad/BRVAH](https://github.com/CymChad/BaseRecyclerViewAdapterHelper) |
| **Coil** | `2.7.0` | Coil Contributors | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [coil-kt/coil](https://github.com/coil-kt/coil) |
| **Google Dagger Hilt** | `2.51.1` | Google LLC & The Dagger Authors | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [google/dagger](https://github.com/google/dagger) |
| **Google Gson** | `2.11.0` | Google LLC | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [google/gson](https://github.com/google/gson) |
| **Kotlinx Coroutines** | `1.8.1` | JetBrains s.r.o. | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) |
| **Kotlinx Serialization** | `1.7.1` | JetBrains s.r.o. | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [Kotlin/kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) |
| **AndroidX Jetpack & Compose** | BOM `2024.09.03` | Google LLC / AOSP | **Apache-2.0** | [License](https://www.apache.org/licenses/LICENSE-2.0) | [AOSP](https://source.android.com/) |

---

## 🛡️ 三、企业商用与闭源集成合规指引 (Compliance Guidelines)

当您使用 `BaseAndroid2AIoT` 开发商业化应用、工控设备固件或闭源软件交付客户时，请遵循以下法律要求：

1. **保留版权声明 (Attribution Notice)**：
   - 本项目根目录已附带标准的 `NOTICE` 文件。在商业再分发时（如提供源码或合规文档），保留该 `NOTICE` 文件与原作者版权声明即可。
2. **闭源商用合法性**：
   - Apache-2.0 与 MIT 均为商业许可，您**无需**公开您的业务代码、私有工业算法或定制通信协议。
3. **免责与知识产权保护**：
   - 本项目及所引用的开源软件均按“现状”（AS-IS）提供，不包含任何明示或暗示的担保。
   - 依赖项自带明确的专利授权（Apache 2.0 Patent Grant），有效保护商业使用者免受专利诉讼侵扰。
4. **终端用户界面合规建议**：
   - 移动应用可在“设置 / 关于 / 开源许可”中展示第三方开源声明列表。本项目已内置相关弹窗与数据模型支撑。
