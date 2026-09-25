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
如需打包ViScriptLib，由于ViScriptLib已经发布到maven，为了保证Artifact ID一致，你需要这样写：
```gradle
dependencies {
    jarJar "com.zhenshiz:ViScriptLib-neoforge-${minecraft_version}:${vsl_jij_version}"
}
```

### 构建
运行./gradlew buildAll 以构建所有子项目，然后你可以在项目根目录的build/libs文件夹下找到所有的构建产物。

运行./gradlew cleanLibs 以清理所有子项目的构建产物文件夹。
