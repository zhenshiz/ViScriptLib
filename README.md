# ViScript Lib

ViScript Lib 是给 ViScript 系列附属模组复用的 NeoForge/LDLib2 工具库。它主要提供编辑器文件流程、服务端文件上传、物品比较、容器兼容和 LDLib2 RPC 早期访问器注册等基础能力。

当前项目目标版本：

- Minecraft `1.21.1`
- NeoForge `21.1.x`
- LDLib2 `2.2.x`
- Java `21`

## 功能概览

- **两类 LDLib2 编辑器基类**
  - `FunctionFileEditor`：工程文件和运行时文件一体，适合商店、简单配置等只有一个功能文件的编辑器。
  - `ProjectFileEditor`：工程文件和运行时文件分离，适合有 UI 专属数据、未编译 graph 或复杂编辑状态的编辑器。
- **编辑器文件路径约定**
  - 工程文件：`assets/<modid>/project`
  - 运行时/功能文件：`assets/<modid>/<domain>`
  - 运行时后缀由调用方配置，工程后缀自动派生为 `<suffix>proj`
- **服务端上传**
  - 编辑器菜单通过 LDLib2 RPC 把工程文件或运行时文件上传到服务端 assets 目录。
- **服务端文件扫描**
  - `EditorAssetFiles` 可以获取指定目录、指定后缀下的文件列表，适合命令补全和运行时加载。
- **物品比较**
  - `ItemUtil` 支持按全部组件、只包含指定组件、排除指定组件三种方式比较 `ItemStack`。
- **早期访问器注册**
  - `@ViScriptRegisterAccessors` 用于在 LDLib2 扫描 RPC 前注册 typed RPC 参数需要的 accessor。

## 编辑器创建

详细流程见 [docs/editor-guide.md](docs/editor-guide.md)。

最简单的功能文件编辑器通常只需要：

```java
public final class MyFunctionProjectType {
    public static final EditorFileFormat FORMAT = EditorFileFormat.of("my_mod", "shop", "shop");

    public static final FunctionFileProjectType TYPE = new FunctionFileProjectType(
            Icons.JSON,
            "my_mod.editor.shop.type",
            FORMAT,
            MyFunctionProject::new
    );
}
```

```java
public class MyFunctionEditor extends FunctionFileEditor {
    public MyFunctionEditor() {
        removeBottomWindow();
        registerFunctionFileType(MyFunctionProjectType.TYPE);
    }

    @Override
    protected Editor createNewEditorInstance() {
        return new MyFunctionEditor();
    }
}
```

如果工程文件和运行时文件需要分开，项目类实现 `IRuntimeFileProject`，编辑器继承 `ProjectFileEditor`。工程文件会保存到 `assets/<modid>/project/*.xxxproj`，运行时文件会保存或上传到 `assets/<modid>/<domain>/*.xxx`。

开发环境内置两个测试入口：

```mcfunction
/ldlib2_menu_test viscript_function_file_editor
/ldlib2_menu_test viscript_project_file_editor
```

## 早期 Accessor 注册

LDLib2 会在 `RPCPacketDistributor.init()` 阶段扫描所有 `@RPCPacket` 方法，并立刻为参数查找 accessor。这个阶段早于普通附属模组构造器里的 `NeoForge.EVENT_BUS.addListener(...)`，所以不要用 NeoForge 事件监听器注册 typed RPC 参数所需的 accessor。

附属模组应提供静态方法，并用 `@ViScriptRegisterAccessors` 标记：

```java
public final class MyModAccessors {
    private MyModAccessors() {
    }

    @ViScriptRegisterAccessors
    public static void register(RegisterAccessorEvent event) {
        event.register(ShopInfo.class, ShopInfo::new);
    }
}
```

数据类建议按 LDLib2 约定实现 `IPersistedSerializable`，字段使用 `@Persisted`：

```java
public class ShopInfo implements IPersistedSerializable {
    @Persisted
    public String id = "";
}
```

如果 accessor 只在某个可选模组加载时才需要注册，可以使用：

```java
@ViScriptRegisterAccessors(modId = "some_mod")
public static void registerCompat(RegisterAccessorEvent event) {
    event.register(SomeCompatInfo.class, SomeCompatInfo::new);
}
```

VSL 会在 LDLib2 `AccessorRegistries.init()` 之后、`RPCPacketDistributor.init()` 之前扫描并调用这些静态方法。

## 物品比较

`ItemUtil` 默认使用原版一致的完整组件比较：

```java
ItemUtil.getItemForPlayerCount(player, targetStack);
ItemUtil.removeItemForPlayer(player, targetStack, count);
```

需要自定义组件比较时传入 `ItemStackCompareMode` 和组件列表：

```java
ItemUtil.getItemForPlayerCount(
        player,
        targetStack,
        ItemStackCompareMode.EXCLUDE_COMPONENTS,
        List.of(DataComponents.DAMAGE)
);
```

三种比较模式：

- `ALL_COMPONENTS`：比较物品 id 和所有组件。
- `INCLUDE_COMPONENTS`：只比较传入列表中的组件，适合只关心枪械 id 等关键组件的场景。
- `EXCLUDE_COMPONENTS`：比较除传入列表以外的组件，适合忽略耐久等变化组件。

## 服务端文件列表

获取服务端指定运行时目录下的文件：

```java
public static final EditorFileFormat FORMAT = EditorFileFormat.of("viscript_recipe", "recipe", "recipe");

List<String> recipes = EditorAssetFiles.listRuntimeFiles(FORMAT, true);
```

如果服务端存在：

```text
assets/viscript_recipe/recipe/foo/bar.recipe
```

返回值会包含：

```text
foo/bar
```

需要解析回文件路径时：

```java
Path file = EditorAssetFiles.resolveRuntimeFile(FORMAT, "foo/bar", true);
```

## 测试

常用检查命令：

```bash
./gradlew compileJava
./gradlew runGameTestServer
```

如果 `runGameTestServer` 使用的 JDK 不支持 `-XX:+AllowEnhancedClassRedefinition`，可以指定 JetBrains Runtime 或移除对应 JVM 参数。
