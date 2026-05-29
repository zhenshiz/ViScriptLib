# ViScript Lib 编辑器创建流程

本文介绍 ViScript Lib 目前提供的两种编辑器抽象：

- **功能文件一体型编辑器**：没有单独工程文件，LDLib2 的工程文件就是运行时文件。
- **工程文件分离型编辑器**：工程文件保存编辑器/UI 专属数据，运行时文件由工程文件导出或上传生成。

两种编辑器都使用 `EditorFileFormat` 描述文件位置和后缀：

```java
EditorFileFormat.of("modid", "domain", "suffix");
EditorFileFormat.compressed("modid", "domain", "suffix");
```

路径规则固定为：

- 工程文件：`assets/<modid>/project`
- 运行时/功能文件：`assets/<modid>/<domain>`
- 运行时后缀：`.<suffix>`
- 工程后缀：`.<suffix>proj`

多个编辑器可以共用同一个 `assets/<modid>/project` 工程目录，打开文件时会按后缀筛选，不需要再按编辑器类型拆工程子目录。

## 功能文件一体型

这种编辑器适合“运行时文件就是工程文件”的场景。比如商店模组只有 `.shop` 运行时文件，没有额外 UI 专属工程数据，就可以使用这一类。

### 需要创建的类

1. 创建项目数据类，实现 LDLib2 的 `IProject`。

项目的 `serializeProject(...)` 和 `deserializeProject(...)` 直接读写运行时文件内容。

```java
public class MyFunctionProject implements IProject {
    @Override
    public ProjectType getProjectType() {
        return MyFunctionProjectType.TYPE;
    }

    @Override
    public CompoundTag serializeProject(HolderLookup.Provider provider) {
        return runtimeData;
    }

    @Override
    public void deserializeProject(HolderLookup.Provider provider, CompoundTag nbt) {
        runtimeData = nbt;
    }
}
```

2. 创建 `FunctionFileProjectType`。

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

这个类型会把 LDLib2 的保存、另存为和打开目录指向 `assets/my_mod/shop`，文件后缀是 `.shop`。

3. 创建编辑器类，继承 `FunctionFileEditor`。

```java
public class MyFunctionEditor extends FunctionFileEditor {
    public MyFunctionEditor() {
        registerFunctionFileType(MyFunctionProjectType.TYPE);
    }

    @Override
    protected Editor createNewEditorInstance() {
        return new MyFunctionEditor();
    }
}
```

### 上传到服务端

如果需要上传到服务端，重写 `createServerUploadAction()`，返回一个 `EditorUploadAction`。上传动作内部把当前项目数据转换成 `CompoundTag`，再调用：

```java
EditorServerUploads.uploadToServer(MyFunctionProjectType.FORMAT, fileName, tag);
```

服务端会写入：

```text
assets/my_mod/shop/<fileName>.shop
```

### 菜单行为

`FunctionFileEditor` 会保留 LDLib2 File 菜单的基本顺序：

```text
New -> Open -> Save -> Save As
```

其中 Open、Save As 的根目录会定位到运行时文件目录。

上传菜单只有一个动作：

```text
上传到服务端
```

### 开发环境测试入口

项目内提供了一个 dev-only 示例：

```mcfunction
/ldlib2_menu_test viscript_function_file_editor
```

示例文件位置：

```text
assets/viscript_lib/test/*.test
```

## 删除 LDLib2 默认窗口

两种编辑器都继承自 `ViScriptEditor`，可以直接删除 LDLib2 默认拆出来的窗口。比如编辑器不需要资源 View 所在的底部窗口时：

```java
public MyFunctionEditor() {
    removeBottomWindow();
    registerFunctionFileType(MyFunctionProjectType.TYPE);
}
```

可用的窗口方法有：

- `removeLeftWindow()`：删除左侧窗口。
- `removeCenterWindow()`：删除中间窗口。
- `removeBottomWindow()`：删除底部窗口，LDLib2 的资源 View 默认在这里。
- `removeRightWindow()`：删除右侧窗口，LDLib2 的检查器和历史记录 View 默认在这里。
- `removeEditorWindows(leftWindow, bottomWindow)`：一次删除多个窗口。

删除窗口时会移除里面已有的 View 和布局回退记录，然后优先从 LDLib2 的拆分树里摘掉窗口。根节点直属窗口无法直接提升兄弟窗口时，会回退为隐藏窗口本身。

## 服务端文件列表

指令、重载逻辑或服务端运行时可以通过 `EditorAssetFiles` 扫描指定目录下的编辑器文件。比如获取服务端全部配方运行时文件：

```java
public static final EditorFileFormat FORMAT = EditorFileFormat.of("viscript_recipe", "recipe", "recipe");

List<String> recipes = EditorAssetFiles.listRuntimeFiles(FORMAT, true);
```

如果服务端存在：

```text
assets/viscript_recipe/recipe/foo/bar.recipe
```

返回的路径会是：

```text
foo/bar
```

通用目录也可以直接扫描：

```java
EditorAssetFiles.listAssetFiles("viscript_recipe", "recipe", "recipe", true);
```

需要把用户输入解析回文件路径时：

```java
Path file = EditorAssetFiles.resolveRuntimeFile(FORMAT, "foo/bar", true);
```

解析方法会清理非法字符并拒绝 `..`，避免路径逃出编辑器约定目录。命令补全层只需要自己决定是否加引号，例如 `recipes.forEach(builder::suggest)`。

## 工程文件分离型

这种编辑器适合工程文件和运行时文件不是同一个内容的场景。工程文件可以保存 UI 布局、编辑器注释、未编译 graph、调试数据等；运行时文件只保存服务端实际消费的数据。

### 需要创建的类

1. 创建项目数据类，实现 `IRuntimeFileProject`。

`serializeProject(...)` 保存工程文件，`serializeRuntimeFile(...)` 生成运行时文件。

```java
public class MyProject implements IRuntimeFileProject {
    @Override
    public ProjectType getProjectType() {
        return MyProjectType.TYPE;
    }

    @Override
    public CompoundTag serializeProject(HolderLookup.Provider provider) {
        return editorOnlyData;
    }

    @Override
    public void deserializeProject(HolderLookup.Provider provider, CompoundTag nbt) {
        editorOnlyData = nbt;
    }

    @Override
    public CompoundTag serializeRuntimeFile(HolderLookup.Provider provider) {
        return compiledRuntimeData;
    }
}
```

2. 创建 `ProjectFileProjectType`。

```java
public final class MyProjectType {
    public static final EditorFileFormat FORMAT = EditorFileFormat.of("my_mod", "shop", "shop");

    public static final ProjectFileProjectType TYPE = new ProjectFileProjectType(
            Icons.JSON,
            "my_mod.editor.shop_project.type",
            FORMAT,
            MyProject::new
    );
}
```

工程文件会保存到：

```text
assets/my_mod/project/*.shopproj
```

运行时文件会导出或上传到：

```text
assets/my_mod/shop/*.shop
```

3. 创建编辑器类，继承 `ProjectFileEditor`。

```java
public class MyProjectEditor extends ProjectFileEditor {
    public MyProjectEditor() {
        registerProjectFileType(MyProjectType.TYPE);
    }

    @Override
    protected Editor createNewEditorInstance() {
        return new MyProjectEditor();
    }
}
```

### File 菜单行为

`ProjectFileEditor` 会使用 LDLib2 的工程保存流程：

```text
New -> Open -> Save -> Save As
```

Open 和 Save As 的根目录是：

```text
assets/<modid>/project
```

另外 File 菜单会在当前项目打开后增加：

```text
导出运行时文件
```

这个动作会调用当前项目的 `serializeRuntimeFile(...)`，并把文件写入：

```text
assets/<modid>/<domain>/<fileName>.<suffix>
```

### 上传菜单行为

工程文件分离型编辑器的上传菜单有三个动作：

```text
上传工程文件
上传功能文件
上传工程和功能文件
```

三个动作都会询问文件名。

- 上传工程文件：写入 `assets/<modid>/project/<fileName>.<suffix>proj`
- 上传功能文件：写入 `assets/<modid>/<domain>/<fileName>.<suffix>`
- 上传工程和功能文件：使用同一个基础文件名，同时写入上面两个位置

上传通过 LDLib2 RPC 发送到服务端，服务端写完后会回传结果提示。

### 开发环境测试入口

项目内提供了一个 dev-only 示例：

```mcfunction
/ldlib2_menu_test viscript_project_file_editor
```

示例文件位置：

```text
assets/viscript_lib/project/*.ptestproj
assets/viscript_lib/test/*.ptest
```

## 选择建议

如果编辑器只需要编辑运行时数据，使用 `FunctionFileEditor`。它最简单，LDLib2 的 Save/Open 就是对运行时文件操作。

如果编辑器需要保存 UI 专属数据、未编译数据、编辑器注释、graph 原始信息，或者运行时文件需要经过编译生成，使用 `ProjectFileEditor`。它能把工程文件和运行时文件分开，并提供本地导出和服务端上传。

## 注意事项

- 不要使用 `.nbt` 作为通用后缀。每个编辑器域应配置自己的后缀，比如 `.shop`、`.dialog`。
- 工程文件后缀会自动从运行时后缀派生，比如 `.shop` 对应 `.shopproj`。
- 工程文件目录固定为 `assets/<modid>/project`，不要按编辑器域拆子目录。
- 上传到服务端是通过 `EditorServerUploads` 统一处理的，附属模组通常不需要再写自己的 C2S 包。
- 如果运行时文件需要兼容现有压缩 NBT 格式，使用 `EditorFileFormat.compressed(...)`。
