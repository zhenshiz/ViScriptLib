# ViScript系列模组

这是ViScript系列模组的多合一项目。为了使用方便，本项目的项目结构相比传统的单模组项目模板有一些改动，主要包括：

- 所有子项目的构建文件夹都在根目录下的build文件夹下
- 所以子项目的runClient任务的游戏目录都在runs/client，runServer任务的游戏目录都在runs/server

### 移除子项目
在settings.gradle中移除对应的include语句。

### 新增子模块
在终端运行命令：git submodule add <子模块URL> 例如已经添加的VSR是这样添加的：
```bash
git submodule add https://github.com/zhenshiz/ViScriptRecipe.git
```
然后按照新建子项目的方法修改构建脚本，使其能被主项目识别。

### 新建子项目
新建一个文件夹，必须包含build.gradle和gradle.properties文件，然后在settings.gradle中添加新子项目的include语句。你可以参照ViScriptRecipe/build.gradle来写子项目的构建脚本。

如果子项目需要依赖另一个子项目，你需要在子项目的构建脚本中像这样添加依赖语句：（以ViScriptLib为例）
```gradle
dependencies {
    implementation project(":ViScriptLib")
}
```
如需将当前工程构建的 ViScriptLib 嵌入子项目，使用 VSL 提供的 `embeddedLibrary` 配置：
```gradle
dependencies {
    jarJar project(path: ":ViScriptLib", configuration: "embeddedLibrary")
}
```

Gradle 会先构建本地 VSL，再将生成的 JAR 嵌入附属模组。该配置保留与 Maven 发布一致的依赖标识，版本直接取自 VSL 的 `project.version`，由根目录 `gradle.properties` 中的 `mod_version` 控制。更新 VSL 只需维护这一个版本参数；各附属模组自身的版本仍由各自的 `gradle.properties` 管理。

### 构建
运行./gradlew buildAll 以构建所有子项目，然后你可以在项目根目录的build/libs文件夹下找到所有的构建产物。

运行./gradlew cleanLibs 以清理所有子项目的构建产物文件夹。

### 发布至 CurseForge

在根目录 `publish.env` 或环境变量中设置 `CURSEFORGE_TOKEN`，项目 ID 使用各子项目的 `publish_curseforge_project_id`。VSL 可以单独试运行和发布：

```bash
# 仅构建并显示请求信息，不访问发布接口。
bash ./gradlew :ViScriptLib:publishCurseforge -Ppublish_dry_run
# 实际上传 VSL 到 CurseForge。
bash ./gradlew :ViScriptLib:publishCurseforge
```

CF 发布任务使用 Java 21 自带的 HTTP 客户端，向官方 `legacy.curseforge.com` Upload API 的 `gameVersionNames` 字段提交版本名称、加载器、运行环境，并附上前置关系。注意 `gameVersions` 是另一字段，只接受整数 ID；发送前会校验两者类型，试运行也执行该校验。它不再查询旧版 CurseForgeGradle 使用的 `/api/game/version-types` 和 `/api/game/versions`，避免该查询被网站防护以 403 拦截。`publishMods` 仍聚合 CF 和 Modrinth；补发时可以只执行失败平台的任务。

如果上传接口本身返回 `Just a moment…` 防护页面，任务会明确报告网站防护拦截；这种响应不能证明 Token 无效。上传连接中断或成功响应缺少文件 ID 时不自动重试，应先检查作者后台的文件列表，避免重复发布。

本地协议测试使用临时工程、假 Token 和回环 HTTP 服务，不连接发布平台：

```bash
python3 gradle/tests/curseforge_upload_test.py
```
