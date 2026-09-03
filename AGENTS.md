# BaseAndroid2AIoT · Agent 核心开发规则与行为准则

> **核心目标**：本项目为完全面向 AI Agent 开发与维护的架构体系。所有在此工程工作的 Agent 必须严格贯彻**低 Token 消耗、高信噪比、极速检索与精准复用**的原则。

---

### 1. 远端推送铁律 (Git Remote Restriction)
- **严禁擅自推送到远端**：未经用户在当前会话中明确要求，**绝对禁止执行 `git push`**。
- 所有代码改动仅在本地修改、本地验证编译（`compileDebugKotlin` / `assembleDebug`）并在需要时进行本地 git commit，直到用户明确要求“推送到远端”方可执行推送。

---

### 2. Token 经济学与高能效检索 (Token Economy)
- **切片探索，严禁盲目全读**：
  - 查阅代码时优先配合 `grep_search` 定位目标行号，使用 `view_file` 指定 `StartLine` 与 `EndLine` 精准按需读取，严禁盲目全量加载大文件。
  - 检索代码时必须指定精确关键字与通配过滤（如限定 `*.kt` 或特定目录 `app/src/main/`），严禁全盘无差别扫描。
- **外科手术式精确改动 (Surgical Edits)**：
  - 严禁对大文件进行全量覆盖重写，必须使用 `replace_file_content` 定向替换差异代码块，节约 Diff 与文件 I/O 的 Token 消耗。
- **微组件拆分标准**：
  - 单个 Composable 函数或业务文件行数严格控制在 **50~150 行**。超过必须拆解至 `components/` 目录下，保证后续 Agent 单次读写仅消耗极低 Token。
- **高信噪比交付**：
  - 汇报保持高度精炼与专业，直奔核心决策、改动点、影响范围与验证指引，杜绝车轱辘话与背景复述。

---

### 3. 极简注释准则 (Lean Commenting)
- **严禁编写自解释的废话注释**：如 `// 发送事件`、`// 获取实例`、`// 更新状态` 等废话一律禁止。
- **严禁添加纯装饰性的符号分割线**：如 `// ==================== xxx ====================`。
- **仅保留高信噪比注释**：
  1. 架构隔离红线（如编译期裁剪、`src/main` 禁直引协议驱动等）；
  2. 非显而易见的关键参数语义（如 `isBlocking: true=不可取消防误触, false=允许外部取消`）；
  3. 底层协议时序规范与特殊异常降级边界。

---

### 4. 架构基建复用与零影子代码 (Reuse & Anti-Shadowing)
- **ViewModel 基建规范**：
  - 新建任何 ViewModel 必须继承 `BaseViewModel<STATE, EVENT>` 并注入 `IotHub`，严禁重复编写加载弹窗控制、错误解析与系统分享逻辑。
  - 参考模板：`feature/template/TemplateViewModel.kt`。
- **UI 组件库复用**：
  - 新增 UI 必须复用 `core/ui/components/`（`AppCard`, `AppButton`, `AppSwitchRow`）；
  - 严禁硬编码颜色（`Color(0xFF...)`），必须统一读取 `AppTheme.colors.*` 设计系统令牌，严格保证 WCAG AAA 高对比度（主文字 > 15:1，次文字 > 7:1）。
- **协议裁剪隔离红线**：
  - `src/main/` 属于通用模块，**绝对严禁直接 import 协议驱动三方库**（如 `retrofit2.*`、`com.hivemq.*`、`jedis.*`）！
  - 所有协议调用必须通过 `IotHub` 或接口抽象，确保任何协议开关关闭时 `src/main` 均能 100% 独立编译通过。
