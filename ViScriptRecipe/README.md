# ViScriptRecipe

ViScriptRecipe 是一个面向 Minecraft 1.21.1 / NeoForge 的可视化配方编辑与覆盖模组。它的目标很直接：把原本需要手写 JSON、写 KubeJS、反复重启或重载数据包的配方修改，变成可以在游戏内通过编辑器完成的可视化流程。

玩家使用教程见：https://doc.mafuyu.moe/wiki/ViScriptRecipe

## 功能总览

- 游戏内可视化编辑 `.recipe` 文件，支持创建、替换、删除配方。
- 按工作站分类展示配方类型，联动模组只有在已安装时才会出现在编辑器中。
- 支持物品输入、物品标签输入、工具动作输入、流体输入、Create 流体标签输入、概率输出、数量输出等常见配方参数。
- 左侧配方列表支持新增、删除、复制到下方，适合批量制作相似配方。
- 支持通过配方 ID 导入当前世界已经加载的配方，兼容时会自动切换到对应配方类型并填充参数。
- 支持 `add`、`replace`、`remove` 三种操作，可以把 `.recipe` 当作一个轻量配方覆盖包使用。
- `/viscript_recipe reload` 只重新读取本模组的 `.recipe` 文件并应用覆盖，不执行完整数据包 reload。
- 普通重载会向客户端同步配方包和配方书状态；`/viscript_recipe reload full` 会额外同步标签包，适合需要 JEI 重建标签相关配方时使用。
- 提供 JEI 展示模式，可以只加载并展示 ViScriptRecipe 提供的配方，方便整合包作者检查当前配方包。
- 支持多个配方模组的专用编辑 UI 与导入器，包括 Create、Extended Crafting、Avaritia、Iron's Spells、Farmer's Delight、Ars Nouveau、Kaleidoscope Cookery、Ice and Fire、灾变、菌类感染：孢子、东方女仆和 Goety。

## 基本信息

- Minecraft：`1.21.1`
- Mod Loader：NeoForge `21+`
- Java：`21`
- 必需依赖：LDLib2
- 内置依赖：ViScriptLib 会随本模组打包
- 配方文件目录：`ldlib2/assets/viscript_recipe/recipes/*.recipe`
- 配置文件：`config/viscript_recipe_config.toml`

## 指令

| 指令 | 作用 |
| --- | --- |
| `/viscript_recipe editor` | 打开配方编辑器。 |
| `/viscript_recipe editor <file>` | 打开或创建指定 `.recipe` 文件，文件名支持补全。 |
| `/viscript_recipe reload` | 重新读取并应用本模组 `.recipe` 配方文件。 |
| `/viscript_recipe reload full` | 在普通重载基础上额外同步标签包，帮助 JEI 重建依赖标签的配方显示。 |
| `/viscript_recipe status` | 查看上一次加载的文件数、条目数、成功/跳过/失败数量。 |

编辑器目前只能在单人/集成服务器中打开。专用服务器可以把 `.recipe` 文件放到服务器的 `ldlib2/assets/viscript_recipe/recipes` 目录下，然后使用 `/viscript_recipe reload` 或 `/viscript_recipe reload full` 应用。

## 配方文件与操作

每个 `.recipe` 文件可以包含多个配方条目。每个条目都有自己的启用状态、配方 ID、配方类型和操作模式。

| 操作 | 作用 |
| --- | --- |
| `add` | 新增配方；如果 ID 已存在，会替换同名配方。 |
| `replace` | 替换配方；如果 ID 不存在，会作为新增配方应用。 |
| `remove` | 移除配方。 |

配方 ID 使用标准 `namespace:path` 格式。编辑器中导入已加载配方时，也使用这个 ID 查找当前世界 RecipeManager 中的配方。

## 编辑器能力

- 左侧是配方列表，可以按工作站分类查看、选择、新增、删除和复制配方。
- 中间是可视化工作区，会根据配方类型显示对应工作台、输入槽、输出槽、流体槽和箭头。
- 右侧是属性面板，用来编辑配方 ID、操作模式、具体参数、数量、时间、热量、概率、能量消耗等。
- 支持直接点击槽位编辑物品或流体，也支持在属性面板中精细修改。
- 支持物品标签；Create 的流体输入支持流体标签，标签输入会在槽位中轮换预览匹配物品或流体。
- 支持 Farmer's Delight 砧板工具动作输入，例如斧头、刀具等工具能力。
- 支持配方导入：输入或搜索已加载配方 ID，若本模组兼容该类型，会自动生成可编辑条目。
- 支持复制到下方：基于当前配方克隆一个新条目，并自动生成 `_copy` 后缀 ID，适合做少量修改。

## 重载与 JEI

`/viscript_recipe reload` 会重新读取 `ldlib2/assets/viscript_recipe/recipes` 下的 `.recipe` 文件，然后把结果应用到当前 RecipeManager。它不会触发完整数据包 reload，因此不会额外重载其它模组的战利品表、标签生成器或数据包监听器。

安装 Create 时，重载会同时刷新 Create 的配方查找缓存，避免机器侧继续命中已经删除或替换前的旧配方。

普通重载会向在线玩家发送新的配方包，并同步配方书。它不会同步标签包，因此卡顿更少，适合只改配方内容的开发流程。

`/viscript_recipe reload full` 会在普通重载基础上额外发送标签包，用来帮助 JEI 感知标签相关变化并刷新界面。这个模式会比普通重载更重，通常只在新增、删除或修改标签相关输入后需要使用。

配置项：

| 配置项 | 默认值 | 作用 |
| --- | --- | --- |
| `recipes.showcase_only_viscript_recipes` | `false` | 展示模式。开启后会清空基础配方，只加载 ViScriptRecipe 的 `.recipe` 配方，方便检查配方包。 |

## 支持的原版配方

| 工作站 | 支持的配方类型 |
| --- | --- |
| 工作台 | `minecraft:crafting_shaped`、`minecraft:crafting_shapeless` |
| 熔炉 | `minecraft:smelting` |
| 高炉 | `minecraft:blasting` |
| 烟熏炉 | `minecraft:smoking` |
| 营火 | `minecraft:campfire_cooking` |
| 切石机 | `minecraft:stonecutting` |
| 锻造台 | `minecraft:smithing_transform` |

原版有序、无序、切石和锻造转换配方支持解锁提示配置。烧炼类配方支持经验值和处理时间。

## 支持的联动配方

联动配方只会在对应模组已安装时注册到编辑器中。下面的类型 ID 以代码中已注册的 `RecipeEditorType` 为准，可作为 `.recipe` 条目的 `type` 对照。

### Iron's Spells 'n Spellbooks (`irons_spellbooks`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 炼金锅 | `irons_spellbooks:alchemist_cauldron_fill`、`irons_spellbooks:alchemist_cauldron_empty`、`irons_spellbooks:alchemist_cauldron_brew` |
| 奥术铁砧 | `irons_spellbooks:arcane_anvil_transform` |

炼金锅支持物品与流体输入、返回物品、副产物、输出流体和声音设置。奥术铁砧配方通过本模组的菜单覆盖逻辑生效，不直接写入原版 RecipeManager。

### Ice and Fire CE (`iceandfire`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 龙钢锻炉 | `iceandfire:dragonforge` |

龙钢锻炉支持火龙、冰龙、雷龙三种龙息类型，并可编辑输入、血液/材料、输出和锻造时间。

### Farmer's Delight (`farmersdelight`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 厨锅 | `farmersdelight:cooking` |
| 砧板 | `farmersdelight:cutting` |

厨锅支持多材料、容器、输出和烹饪参数。砧板支持输入材料、工具动作、多个输出、输出概率和声音设置。

### Create (`create`)

| 工作站或处理方式 | 支持的配方类型 |
| --- | --- |
| 粉碎轮 | `create:crushing` |
| 石磨 | `create:milling` |
| 动力锯 | `create:cutting`、`create:block_cutting` |
| 动力冲压机 | `create:auto_packing`、`create:pressing`、`create:compacting` |
| 砂纸 | `create:sandpaper_polishing` |
| 鼓风机 | `create:blasting`、`create:smoking`、`create:splashing`、`create:haunting` |
| 注液器 | `create:filling` |
| 分液池 | `create:emptying` |
| 动力搅拌器 | `create:mixing`、`create:automatic_shapeless`、`create:automatic_brewing` |
| 机械手 | `create:deploying` |
| 手动右键应用 | `create:item_application` |
| 动力合成器 | `create:mechanical_crafting` |
| 序列组装 | `create:sequenced_assembly` |

Create 处理配方支持物品输入、流体输入、物品输出、流体输出、处理时间、热量需求和保留手持物品等参数。带流体输入的 Create 配方可以使用具体流体或流体标签，当前包括 `create:filling`、`create:mixing`、`create:compacting`、`create:automatic_brewing`，以及序列组装中的注液步骤；`create:emptying` 的流体是输出，仍使用具体流体。动力搅拌、动力压缩和自动无序配方支持在单个物品槽中设置数量，保存时会展开为多个 Create `Ingredient`。`create:block_cutting` 会根据多个输出派生多个配方 ID。序列组装支持部署、冲压、切削和注液步骤。

### Extended Crafting (`extendedcrafting`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合成核心 | `extendedcrafting:combination` |
| 合成台 | `extendedcrafting:shaped_table`、`extendedcrafting:shapeless_table`、`extendedcrafting:ultimate_singularity` |
| 量子压缩机 | `extendedcrafting:compressor_recipe` |
| 末影合成器 | `extendedcrafting:shaped_ender_crafter`、`extendedcrafting:shapeless_ender_crafter` |
| 通量合成器 | `extendedcrafting:shaped_flux_crafter`、`extendedcrafting:shapeless_flux_crafter` |

Extended Crafting 合成台在编辑器里统一为一个工作台，通过配方数据中的层级和尺寸字段选择 `3x3`、`5x5`、`7x7`、`9x9`。旧的基础、高级、精英、终极类型 ID 会被兼容归一到当前合成台类型。

### Ars Nouveau (`ars_nouveau`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 附魔装置 | `ars_nouveau:enchanting_apparatus`、`ars_nouveau:armor_upgrade`、`ars_nouveau:enchantment` |
| 灌注室 | `ars_nouveau:imbuement` |
| 抄写台 | `ars_nouveau:glyph` |
| 粉碎 | `ars_nouveau:crush` |

附魔装置支持试剂、基座物品、魔源消耗和结果；灌注室支持输入、基座物品、魔源和结果；抄写台支持法术输入与经验消耗；粉碎支持多输出、概率和最大数量。

### Kaleidoscope Cookery (`kaleidoscope_cookery`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 炒锅 | `kaleidoscope_cookery:pot` |
| 汤锅 | `kaleidoscope_cookery:stockpot` |
| 石磨 | `kaleidoscope_cookery:millstone` |
| 砧板 | `kaleidoscope_cookery:chopping_board` |
| 蒸笼 | `kaleidoscope_cookery:steamer` |
| 茶壶 | `kaleidoscope_cookery:teapot` |

森罗物语配方支持各工作站专属参数，例如汤底、容器、翻炒次数、蒸制时间、茶汤流体、气泡颜色和模型 ID。

### Avaritia / Re-Avaritia (`avaritia`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合成台 | `avaritia:shaped_table`、`avaritia:shapeless_table`、`avaritia:no_consume_catalyst_shaped` |
| 中子压缩机 | `avaritia:compressor` |
| 终极锻造台 | `avaritia:extreme_smithing` |

Avaritia 合成台在编辑器里统一为一个工作台，通过配方数据中的层级和尺寸字段选择幽匿、下界、末地、终极合成台规格。旧的幽匿、下界、末地、终极类型 ID 会被兼容归一到当前合成台类型。

### L_Ender's Cataclysm / 灾变 (`cataclysm`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 机械融合铁砧 | `cataclysm:weapon_fusion` |
| 紫水晶祭坛 | `cataclysm:amethyst_bless` |

机械融合铁砧支持基础装备、融合材料和结果；紫水晶祭坛支持祭品、祝福时间和结果。两类配方均提供专用工作区与导入器。

### Fungal Infection: Spore / 真菌感染：孢子 (`spore`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 孢子手术台 | `spore:surgery`、`spore:grafting` |

手术配方提供 `4x4` 的 16 个材料槽；嫁接配方提供 3 个材料槽。两者都使用孢子手术台专用编辑 UI，并支持结果编辑和从已加载配方导入。

### Touhou Little Maid / 车万女仆 (`touhou_little_maid`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 祭坛 | `touhou_little_maid:altar_recipe` |

东方女仆祭坛支持材料、灵力消耗、结果、显示翻译键和输出实体类型。输出实体使用自动补全选择；普通物品输出使用 `minecraft:item`，也可选择女仆、闪电等祭坛原生支持的实体类型。

### Goety / 诡厄巫法 (`goety`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 诅咒注入器 | `goety:cursed_infuser_recipes` |
| 黑暗祭坛 | `goety:ritual` |
| 死灵火盆 | `goety:brazier` |
| 瓦解聚晶 | `goety:pulverize` |
| 女巫坩埚 | `goety:brewing` |

黑暗祭坛支持激活物、基座材料、灵魂消耗、仪式行为、召唤、献祭、转化、定位结构、附魔和研究等可选条件。女巫坩埚支持药水效果自动补全，并可选择不限制实体、实体标签或单个实体；后两种模式会显示相应的自动补全输入框。定位结构在界面中显示熟悉的结构 ID，但保存时会映射为 Goety Codec 所需的结构标签。

## 导入已加载配方

编辑器支持输入配方 ID 导入当前世界已经加载的配方。导入时会按已注册的导入器判断配方是否兼容；兼容时自动生成对应类型的 `RecipeEntry`，不兼容时会显示错误提示。

当前导入器覆盖原版配方，以及已安装联动模组中的 Iron's Spells、Ice and Fire、Farmer's Delight、Create、Extended Crafting、Ars Nouveau、Kaleidoscope Cookery、Avaritia、灾变、菌类感染：孢子、东方女仆和 Goety 的上述已实现类型。导入优先使用对应模组的专用导入器，而不是按 JSON 字段猜测配方结构。

部分复杂自定义材料表达可能无法导入，例如导入器暂不认识的自定义 Ingredient 或 FluidIngredient。遇到这种情况时，仍然可以手动在编辑器中重新创建配方。

## 注意事项

- 配方编辑器主要面向整合包作者和单人测试环境；专用服务器建议通过文件同步和 `/viscript_recipe reload` 使用。
- 联动模组没有安装时，对应工作站和配方类型不会出现在编辑器中。
- `.recipe` 文件是本模组自己的配方覆盖文件，不是原版 JSON 数据包文件。
- 展示模式 `recipes.showcase_only_viscript_recipes` 会清空基础配方后再应用 `.recipe`，更适合调试和展示，不建议在不了解效果时直接用于正式整合包。
- 如果只改配方内容，优先使用 `/viscript_recipe reload`；如果 JEI 对标签相关配方没有立刻刷新，再使用 `/viscript_recipe reload full`。
