# VSL AI 基础接口

VSL 提供编辑器 AI 对话窗口、兼容 OpenAI Chat Completions 与 Anthropic Messages 的 HTTP 客户端、客户端注册表查询，以及服务端知识查询与附属模组扩展协议。玩家发送消息后才请求配置中的模型服务；网络请求在后台执行，工具读取和工程操作回到客户端线程。

附属模组通过 `AiEditorAdapter` 描述编辑器用法、可读数据和允许的领域修改。当前已接入 ViScriptShop、ViScriptRecipe、ViScriptQuests 和 ViScriptNpc 的正式编辑器。没有适配器的编辑器仍可打开对话窗口，提供通用用法问答和注册表查询。

## 编辑器设置

所有继承 `ViScriptEditor` 的编辑器在原生“设置”面板中都有 **AI** 分类。设置项包括启用 AI、接口协议、服务地址、模型、推理强度、API 密钥、无响应超时、单页上下文条目数、允许编辑工程、允许应用运行时修改，以及应用前确认。

设置使用编辑器原有的“应用”“确定”“取消”流程：应用立即保存，确定保存并关闭，取消恢复上次应用的值。同一客户端的 VSL 附属编辑器共用这些参数，持久化到 `config/ldlib2/editor.json` 的 `viscript_lib:ai` 节点。

AI 默认关闭，两个编辑开关默认关闭，应用前确认默认开启。API 密钥在界面中以掩码显示，仅保存在本机编辑器设置，不随工程导出或上传。VSL 不注册 NeoForge 的 AI 配置项。

`AiEditorSettings.of(editor)` 返回当前设置。保存设置本身不会发起模型请求。服务地址填兼容服务的 API 基础地址，例如 `http://127.0.0.1:11434/v1`；也接受完整的 `/chat/completions` 地址。模型名称必须与服务端一致，不需要认证的本地服务可以留空密钥。服务需要支持所选协议的工具调用。“自动识别”会把含 `/anthropic` 或以 `/messages` 结尾的地址识别为 Anthropic，其余使用 OpenAI；也可手动选择协议。Anthropic 基础地址会自动补全 `/v1/messages`。

智谱 Coding Plan 可使用以下配置（已实际验证 `glm-5.3` 的连接和工具调用）：

| 设置 | 值 |
| --- | --- |
| 接口协议 | Anthropic 兼容，或自动识别 |
| 服务地址 | `https://open.bigmodel.cn/api/anthropic` |
| 模型 | `glm-5.3` |
| API 密钥 | 自己的 Coding Plan 密钥 |
| 无响应超时 | 建议 120 秒，按服务响应速度调整 |

回复采用 SSE 流式接收，正文会逐步显示。状态区区分等待响应、接收推理数据、接收工具参数、接收正文和执行工具，并显示请求次数、本轮耗时、接收字符数及距上次内容/网络数据的时间。状态刷新不会重建聊天正文或清除选区。推理正文不显示给玩家，协议所需的思考块、签名及 OpenAI 兼容接口的 `reasoning_content` 会保留并回传。

无响应超时按每次 HTTP 请求重新计时，检查连续没有网络数据或有效内容进展的时间。正文、推理数据、工具参数持续返回时可继续生成；只有心跳而没有新内容时会报“没有进展”并结束等待，不会无限显示思考中。该设置不限制整轮对话总时长。兼容服务返回完整 JSON 时按收到数据的时间检查空闲。超时或取消均不应用本轮草稿。

生成不再设置本地 token 预算、正文/工具参数字符数、响应字节数、上下文长度或工程快照大小上限；聊天历史也不再按固定条数裁剪。OpenAI 兼容请求省略 `max_tokens` 与 `max_completion_tokens`，由服务决定输出预算。Anthropic 协议要求输出预算时，使用 Models API `GET /v1/models/{model}` 返回的 `max_tokens`；GLM-5.3 使用[官方公布的最大输出 128K](https://docs.bigmodel.cn/cn/guide/models/text/glm-5.3)，即 131072，替代此前固定的 8192。兼容服务未提供模型元数据时省略预算，使用其服务默认值；若该服务同时要求显式预算，会展示服务端错误，需要服务提供模型能力信息。模型的输出、上下文与账号额度仍由服务端限制。查询分页只决定单页大小，不限制累计读取条目。

HTTP 错误会展示服务端结构化错误原因并遮盖配置的密钥，不直接显示未经解析的响应正文。模型达到单次输出上限（`max_tokens`/`length`）时，不执行截断的工具参数；客户端保留此前已成功的草稿批次，并继续请求模型重新生成，完成后继续本轮；恢复不设次数上限，玩家可以随时停止。工具记录会显示恢复过程；持续失败时区分输出上限、工具 JSON 不完整、响应格式不兼容与传输中断，不再统一显示 `Invalid model response`。模型名应与服务一致，例如 `glm-5.3` 中的连字符不能省略。

此地址对应智谱的 Anthropic 兼容接口。模型名称及套餐可用性以账号当前权限为准；VSL 不内置或分发密钥。

推理强度默认为“跟随服务端”，请求不添加强度参数。显式选择时，OpenAI 兼容接口发送 `reasoning_effort`；Anthropic 接口发送 `output_config.effort`。设置中保留 API 的原始档位名（none、minimal、low、medium、high、xhigh、max），实际支持范围由模型决定；Anthropic 的 effort 字段不支持 none/minimal。没有把界面中的 Ultra 假定成通用 API 档位，也不自动开启特定模型才支持的 adaptive thinking。

[GLM-5.3 官方文档](https://docs.bigmodel.cn/cn/guide/models/text/glm-5.3)规定始终开启思考，默认 max，标准 API 接受 low/high/max。其 Anthropic 兼容接口没有已确认的 effort 映射，因此该配置使用“跟随服务端”；需要指定强度时使用智谱的 OpenAI 兼容接口并选择 low/high/max。更高的强度通常会增加等待时间。OpenAI 和 Anthropic 字段按[官方 OpenAI SDK](https://github.com/openai/openai-python/blob/main/src/openai/types/chat/completion_create_params.py)与[官方 Anthropic SDK](https://github.com/anthropics/anthropic-sdk-python/blob/main/src/anthropic/types/output_config_param.py)核对；服务端仍可能拒绝具体模型不支持的档位。

## 打开对话与编辑工程

1. 在设置 → AI 中启用 AI、填写服务地址和模型，并点击“应用”或“确定”。
2. 点击编辑器顶部 **AI → 打开 AI 对话窗口**。默认使用 LDLib2 的独立操作系统窗口，可拖动标题栏、调整窗口大小；平台不支持独立窗口时使用编辑器内浮窗。
3. 输入问题，例如“怎样添加商品”，或在商店工程中要求“创建材料分类，加入 2 个钻石，价格 5，库存 20”。编辑请求还需要启用“允许编辑工程”。
4. AI 的工具调用先操作隔离草稿。成功结束后，草稿会临时显示到主编辑器中，可浏览分类和商品的实际效果；主编辑器顶部和聊天内均可“应用本轮”或“放弃修改”。应用后保留结果并增加一条原生历史，放弃后恢复预览前的完整内容，预览本身不增加历史。每批大小由模型结合服务端输出能力选择，不限制每次工具调用数、每批操作数、整轮请求次数或累计变更数，不要求先把所有注册表页读完再开始修改。预览期间暂停手动修改、保存、上传和历史快捷键。关闭“应用前确认”时，成功结束的一轮会自动应用。
5. 应用后，在聊天中点击“回退本轮”，或在编辑器原生操作历史中撤销、重做。每轮只增加一条 AI 历史动作，刷新界面不会产生额外历史条目。

空对话提示在聊天区水平、垂直居中。玩家消息显示为右侧气泡，AI 回复显示在左侧，并将常用 Markdown 标题、加粗、斜体、行内代码、列表、引用、代码块及表格渲染为富文本。玩家消息下方的小按钮复制原始全文；AI 回复不显示复制按钮，正文支持鼠标拖选及 Ctrl/Cmd+C、Ctrl/Cmd+A，所选显示文本不含 Markdown 标记，软换行不会混入复制结果。每轮的工具记录可展开或收起，注册表查询会标明物品、方块等类型、实际注册表 ID、搜索词及分页位置；标签查询单独标明“标签”。聊天中的变更说明保留实际旧值和新值。右侧轮次按钮可跳转，悬停时显示问题和回答摘要；底部“最新消息”返回对话末尾。阅读较早的消息时，工具返回不会自动拉到底部。

在聊天输入框按 **Ctrl+V / Cmd+V** 可粘贴系统剪贴板中的图片，也可点击“粘贴图片”。图片先显示为待发送缩略图，可逐张移除；点击“发送”后才随消息上传。支持只发送图片、文字配图和同条消息多张图片。缩略图单独缩小，发送的 PNG 保留原图尺寸与透明度。OpenAI 兼容接口使用 `image_url` 数据 URL，Anthropic 接口使用 `image` / base64 数据块；所选服务和模型需要支持图片输入，不支持时显示服务端错误。后续对话保留历史图片上下文；新对话会清除。附件仅随编辑器会话保存在内存中，不写入设置或工程文件。 macOS 和 Windows 使用系统原生剪贴板；Linux 需要桌面环境提供 `wl-paste`（Wayland）或 `xclip`（X11）。

聊天回退要求该轮是当前历史位置，且工程仍等于该轮应用后的状态；有后续手动修改时先撤销后续操作。进入预览前会比较请求开始时的工程快照，避免覆盖请求期间的手动修改；确认或放弃时再次比较预览状态，拒绝覆盖其他修改。切换工程后旧预览不能应用。停止请求或关闭聊天窗口会丢弃正在生成的草稿；迟到的响应不会应用。关闭聊天窗口不会关闭主编辑器中的待确认预览，仍可在编辑器顶部选择应用或放弃；新对话及关闭所属编辑器会撤回未确认预览。

工程文件的保存、导出及服务端上传仍由编辑器现有入口完成。“允许应用运行时修改”目前是预留偏好，本客户端聊天不提供服务器修改或上传工具。聊天记录随编辑器实例保存在内存中；新对话不清除已经应用的原生历史。

## 客户端领域适配器

在 `ViScriptEditor#createAiEditorAdapter()` 返回 `AiEditorAdapter`：

- `guide()`：向模型解释当前编辑器用法、字段含义和支持范围。
- `readSchema()`：声明读取工具的分页参数及领域分区、搜索词等参数。
- `describe(project, arguments, offset, limit)`：按参数分页提供领域数据，不能包含密钥和本机路径；默认委托旧版 `describe(project, offset, limit)`，保留现有适配器兼容性。
- `editSchema()`：描述 `edit_project` 的结构化参数。
- `edit(draft, arguments)`：严格校验参数并修改隔离草稿，返回实际变更说明；失败的工具调用不会保留该调用的部分改动。
- `createAction(editor, draft)`：创建 LDLib2 `EditAction`，执行与撤销都必须同步刷新界面，且不能递归添加历史。保留旧对象可让此前表单历史继续引用它们。
- `setPreviewMode(editor, previewing)`：预览时暂停修改领域数据，但仍允许浏览；切换检查器时不得生成历史。
- `refresh(editor)`：在原生历史完成跳转后刷新领域视图，不添加历史条目。

内置工具为 `read_editor`、`query_registry` 和启用编辑时的 `edit_project`。客户端注册表查询支持 ID/本地化名称搜索和分页；`kind: "tags"` 查询标签 ID、成员数及最多 16 个成员样例，`query: "#minecraft:logs"` 则分页查询准确标签的成员。`read_editor` 和 `query_registry` 的工具 schema 会提供当前页大小上限；模型请求超过上限的正整数会自动裁剪，结果中的 `limit` 是实际页大小，注册表分页沿返回的 `next_offset` 继续，避免跳项。非正数和非整数仍拒绝。只读取客户端已加载或同步的注册表；服务端私有数据仍需服务端知识提供器和权限检查。

商店适配器支持商店名称/快捷打开，分类新增/改名/删除，商品新增/修改/删除，以及实际物品、数量、货币价格、库存、经验与买卖类型。促销、阶段限制、交易命令及自定义组件不能通过本适配器新增或修改；未指定字段保留现有值。工具不能执行任意命令、访问任意文件或自动上传服务器。

## 已接入的编辑器

所有适配器都先修改隔离草稿，在主编辑器预览，并沿用“应用本轮／放弃修改”和整轮撤销重做。

| 编辑器 | 读取 | 修改 |
| --- | --- | --- |
| ViScriptShop | 商店、分类、商品 | 商店设置、分类和商品增删改 |
| ViScriptRecipe | `section=recipes` 配方；`section=types` 实际注册类型的说明、模板与 Schema | 文件设置；按配方 ID 新建、修改、删除；启用状态与 ADD/REPLACE/REMOVE；原版和已安装联动类型的数据 |
| ViScriptQuests | `section=graph` 节点、常量、端口、连线及变量；`section=catalog` 当前图可用节点 | 主图与小任务子图的节点、数据、位置、连线和变量；整批结果通过原生任务编译器 |
| ViScriptNpc：NPC 工程 | `section=config` 注册配置分区与 Schema；`section=graph/catalog` 行为树及节点参数 | 已声明的外观、装备、属性等持久化字段；行为树节点、参数、连线和变量；整批通过原生行为校验 |
| ViScriptNpc：独立行为 | 行为流程图、节点参数与目录 | 行为节点、参数、连线、常量和变量，自动同步可视图与运行定义 |
| ViScriptNpc：测试场景 | 本地场景文件尺寸 | 本地宽度、长度（4 至 32）；不执行场地、实体或调试操作 |

任务查询与操作的 `graph` 默认为 `root`；进入小任务后使用所属小任务节点的 UUID。目录按图的层级筛选，目标和奖励节点需要在小任务子图查询。节点常量使用读取结果中的准确键名，例如 `option_title`，修改时传其 `value` 的原始 JSON，不传包含 `type/value` 的包装对象。物品任务和奖励的 `item_stack` 指定物品，`item_count` 输入常量指定数量。

NPC 的 `set_data` 使用读取结果的 `section` ID 和 `fields` 部分字段。`npc_ai` 仅开放启用状态，行为通过图操作维护，避免图与运行定义分离。`add_node/set_node` 的 `parameters` 使用目录返回的原始参数类型；连接中的参数需要先断开连线再修改常量。测试场景修改只影响本地文件及其视图，真实测试维度仍由玩家操作和服务端权限校验管理。

### 配方类型注册的 AI 契约

`RecipeEditorType` 直接包含 `RecipeAiSupport`，配方接入者可以在原有 `registerEditorType(...)` 中声明 AI 能力，无需另写全局编辑器适配器。既有六参数构造器和 `RecipeEditorType.of(...)` 保留，默认使用 `RecipeAiSupport.AUTO`，读取实际持久化字段并校验解码与编译结果。

```java
registerEditorType(RecipeEditorType.of(
        MY_RECIPE, MY_CATEGORY, "example.recipe.type",
        MyRecipeData.class, MyRecipeCanvas.class
).withAiSupport(new RecipeAiSupport() {
    @Override
    public String guide() {
        return "duration uses ticks; ingredients accepts items or tags; result is a nonempty stack.";
    }

    @Override
    public void validate(RecipeEditorType type, IVSRecipeData data) {
        RecipeAiSupport.super.validate(type, data);
        if (((MyRecipeData) data).getDuration() <= 0) {
            throw new IllegalArgumentException("duration must be positive");
        }
    }
}));
```

可按需覆盖 `guide()`、`schema(type, data)`、`read(type, data)`、`update(type, data, fields)` 和 `validate(type, data)`。自定义编码应同时覆盖 Schema、读取和更新，确保模型看到的字段与更新契约一致；`editable()` 返回 `false` 可禁止该类型的 AI 修改。回调运行在客户端线程，更新目标是隔离草稿；不得在这些回调中写文件、上传、执行命令或修改服务器。原版有序合成注册已提供类型说明示例，客户端场景包含自定义读写与校验的完整回归。

## 服务端权限与知识查询

编辑器本地开关代表用户偏好，不构成服务器授权。`AiAccessContext.forPlayer`、`forSource` 或 `forServer` 为拥有至少二级命令权限的调用者提供只读注册表能力，不自动授予工程或运行时修改能力。修改必须经可信的服务端流程授权，不能采用客户端或模型自报的权限字段。

知识提供器和 Java API 供编辑器及后续运行时调用，不额外注册游戏内 AI 指令。内置注册表提供器的 JSON 请求格式为：

```json
{"operation":"get","type":"item","id":"minecraft:iron_ingot","include_tags":true}
```

支持 `types` 发现注册表、`list` 搜索条目、`get` 精确读取条目、`tags` 搜索标签、`tag` 分页读取标签成员。`namespace`、`query` 和 `tag` 可用于过滤列表。

常用类型包括 `item`、`block`、`fluid`、`entity_type`、`mob_effect`、`enchantment`、`biome`、`dimension`、`damage_type`、`sound_event`、`particle_type` 和 `attribute`。自定义注册表使用完整 ID，例如 `example_mod:custom_registry`。`biome`、`structure` 是对应 `minecraft:worldgen/...` 注册表的便捷别名。

条目和标签 ID 必须包含命名空间。注册表找不到 ID 时返回错误，不会回退为空气等默认条目。配方实际数据由 `RecipeManager` 管理，应通过 ViScriptRecipe 的后续知识提供器接入。

`query` 匹配 ID、翻译键或服务端可用的显示名。专用服务端通常没有玩家客户端的中文资源包；中文本地化搜索不保证可用，应优先使用稳定 ID、命名空间和标签。

## 分页和版本

列表请求的 `limit` 被限制为 1 至 128，编辑器运行时可按本地 `maxContextEntries` 设置请求更小的数据页。结果含有：

- `provider_id`、`schema_version`：提供器和数据结构版本。
- `entries`：当前数据页。
- `has_more`、`next_cursor`：是否继续以及下一页游标。
- `environment`：协议版本、Minecraft/VSL 版本、模组指纹和注册表版本。
- `successful`、`error`：查询状态。

下一页应保留原查询条件，仅把返回的 `next_cursor` 放入 `cursor`。不要解析或自己生成游标。游标绑定查询条件及数据版本；重载数据包、切换服务器或重新启动后，需要从第一页查询。

注册表返回稳定 ID、所属注册表、命名空间、翻译键、显示名和可选标签。物品附带默认物品栈的堆叠上限和最大耐久；方块附带对应物品 ID。查询不返回可变的游戏对象、玩家数据或完整 NBT 运行时对象图。

## Java 调用

服务入口是 `AiKnowledgeService.query`。读取 Minecraft 数据必须在服务端线程执行；模型请求应在后台执行，收到工具调用后再提交回服务端线程。

```java
server.submit(() -> {
    var context = AiAccessContext.forPlayer(player);
    var request = AiQueryRequest.get("item", "minecraft:iron_ingot");
    return AiKnowledgeService.query(RegistryKnowledgeProvider.ID, request, context).toJson();
});
```

使用 `AiQueryRequest.fromJson(JsonObject)` 读取 JSON 请求。解析器检查字段类型，拒绝未知字段；`include_tags` 是 JSON 字段名，Java 记录访问器为 `includeTags()`。

## 附属模组扩展

三个扩展点都使用 LDLib2 `AutoRegistry` 和 `@LDLRegister`。提供器类需要无参构造器；注册名建议使用带模组命名空间的名称，避免不同附属模组冲突。

| 扩展点 | 注册表常量 | 责任 |
| --- | --- | --- |
| `AiKnowledgeProvider` | `AiKnowledgeProvider.REGISTRY_ID` | 通过统一查询字段读取领域数据，不产生副作用 |
| `AiToolProvider` | `AiToolProvider.REGISTRY_ID` | 描述工具参数，校验、预览和应用结构化操作 |
| `AiEditorProvider` | `AiEditorProvider.REGISTRY_ID` | 描述工程/运行时文档，校验、预览和应用版本受控的修改 |

知识提供器实现 `id`、`descriptor` 和 `query`；需要世界状态的提供器覆盖 `requiredCapability` 为 `WORLD_READ`。该能力不会被默认上下文授予，必须由可信服务端运行时单独授权。

工具和编辑器描述包含 JSON Schema 及版本。请求中携带 `schemaVersion`；领域实现应拒绝不兼容版本。查询、校验和预览不得修改文件、工程或游戏状态。

编辑器请求使用逻辑目标名、文档种类和 `expectedRevision`。覆盖已有目标必须校验预期版本；只有新建目标可以不提供旧版本。应用时重新检查权限和版本，通过正常领域逻辑完成修改，并保留备份、使用原子写入及记录审计结果。

工具和编辑器的 `apply` 目前是供可信服务端运行时使用的 SPI，没有被暴露为命令、RPC 或模型工具。接入这些服务端接口的运行时必须遵守编辑器的确认设置，在要求确认时取得实际用户确认后再调用；不能把模型返回的 `confirmed: true` 当作用户授权，也不能直接调用通用编辑器上传入口绕过领域校验。

## 验证

```bash
JAVA_HOME=/path/to/java-21 bash gradlew :ViScriptLib:runClient -PldTest=vsl_ai_editor_settings -PldTestWindow=1280x720
```

此场景通过 LDLib2 的真实客户端测试驱动生产编辑器设置面板，检查 AI 页注册、开关、连接参数输入、推理强度选择、密钥掩码、应用保存、取消回滚、确定保存，以及两类 VSL 编辑器之间的共用配置。测试配置和游戏目录均位于 `build/ViScriptLib/ldlib2-uitest`，不会覆盖日常编辑器设置。

场景仅在开发环境注册，普通启动不会运行。Gradle 同时检查客户端退出结果和 `report.json`，缺失报告不会通过。


独立窗口与商店工具循环的集成测试：

```bash
JAVA_HOME=/path/to/java-21 bash gradlew :ViScriptShop:runClient -PldTest=shop_ai_chat,shop_ai_bulk,vsl_ai_chat_protocol,vsl_ai_editor_settings,vsl_ai_registry_query -PldTestWindow=1280x800
```

此场景启动仅绑定回环地址的确定性 HTTP 模型夹具，通过真实游戏外窗口输入、发送、应用和回退，验证工具查询、草稿隔离、原生撤销/重做、手动修改冲突、HTTP 错误、取消、消息角色、Markdown、复制/拖选、工具记录折叠、对话跳转与窗口关闭。批量场景在第一批成功后故意截断下一次回复，验证单批 1100 个商品、超过 32 次请求、超过 1024 项变更、多次截断恢复、一次应用/撤销，以及无效参数持续恢复时仍可停止。另验证超过旧上限的正文、工具参数、响应字节数、上下文和工程快照，以及 Anthropic 最大输出预算的实际请求字段。协议场景还验证 OpenAI 与 Anthropic 的 SSE、工具参数分片、签名思考块回传、持续推理及工具参数超过超时设置仍正常接收、只有心跳时的停滞结束、推理强度的实际请求字段、空闲超时、截断和流式取消。注册表场景在测试世界中遍历实际物品、标签及标签成员，检查裁剪后的分页不遗漏或重复。测试使用虚构密钥，不访问外部模型；它验证兼容协议及编辑行为，不代表对任意模型服务的在线兼容性验证。

2026-10-26 使用用户授权的智谱 `glm-5.3` 对虚构商店完成在线检查：读取工程、查询物品、生成改名草稿、应用、撤销及重做均通过。在线验证使用临时测试场景，密钥和场景在验证后移除，常规回归测试不需要外部服务。

领域适配器回归使用本机确定性 HTTP 夹具，不需要真实模型或密钥：

```bash
JAVA_HOME=/path/to/java-21 bash gradlew :ViScriptRecipe:runClient -PldTest=recipe_ai_adapter -PldTestWindow=1280x800
JAVA_HOME=/path/to/java-21 bash gradlew :ViScriptQuests:runClient -PldTest=quest_ai_adapter -PldTestWindow=1280x800
JAVA_HOME=/path/to/java-21 bash gradlew :ViScriptNpc:runClient -PldTest=npc_chat_ai_adapter,npc_action_chat_ai_adapter,npc_scene_chat_ai_adapter -PldTestWindow=1280x800
```

这些场景验证实际工具 Schema、读取、修改、领域校验、主编辑器预览、放弃恢复、一次应用、原生撤销重做，以及恢复后的视图。配方还覆盖注册时自定义 AI 契约；任务覆盖小任务子图；NPC 覆盖装备、嵌套配置、范围校验和独立行为。
