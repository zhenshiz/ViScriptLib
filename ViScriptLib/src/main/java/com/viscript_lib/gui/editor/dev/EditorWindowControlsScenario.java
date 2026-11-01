package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.settings.EditorSettings;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ElementBounds;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.TestContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.editor.ViScriptEditorWindow;
import com.viscript_lib.mixin.EditorWindowAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

/** 验证缩放配置不会移除最小化按钮，以及菜单精简后的实际入口。 */
@LDLRegisterClient(name = "vsl_editor_window_controls", group = ViScriptLib.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class EditorWindowControlsScenario implements UIScenario {
    private static final ResourceLocation WINDOW_ID = ResourceLocation.fromNamespaceAndPath(ViScriptLib.MOD_ID, "window_controls_test");
    private static final String SCALE_BUTTON = ".__viscript_editor-window_scale-button__";

    @Override
    public void configure(ScenarioOptions options) {
        options.requiresWorld(false).guiScale(2);
    }

    @Override
    public void define(ScenarioBuilder s) {
        s.openScreen("打开生产窗口并重复设置缩放区域", ctx -> {
                    var root = ViScriptEditorWindow.open(WINDOW_ID, FunctionEditor::new)
                            .setMinimizedBoundsPercent(0, 0, 70, 100)
                            .setMinimizedBoundsPercent(0, 0, 70, 100);
                    var editor = root.getCurrentEditor();
                    editor.loadProject(DevFunctionFileProjectType.TYPE.newEmptyProject(), null);
                    ctx.put("root", root);
                    ctx.put("editor", editor);
                    ctx.put("project", editor.getCurrentProject());
                    ctx.put("before", editor.getCurrentProject().serializeNBT(Platform.getFrozenRegistry()).copy());
                    editor.menuContainer.getChildren().getFirst().setId("vsl_test_file_menu");
                    return screen(root);
                }).awaitModularUI().ticks(3)
                .check("保留游戏外窗口、最小化、关闭和缩放四个按钮", ctx -> editor(ctx).buttonContainer.getChildren().size() == 4)
                .check("保留原生最小化按钮", ctx -> minimizeButton(editor(ctx)) != null)
                .check("缩放按钮只有一个", ctx -> ctx.count(SCALE_BUTTON) == 1)
                .step("反复隐藏和恢复缩放按钮", ctx -> root(ctx).removeDefaultScaleButton()
                        .setDefaultScaleButtonVisible(true).clearMinimizedBounds().setMinimizedBoundsPercent(0, 0, 70, 100))
                .check("切换策略不会删除原生按钮或重复添加按钮", ctx -> editor(ctx).buttonContainer.getChildren().size() == 4
                        && minimizeButton(editor(ctx)) != null && ctx.count(SCALE_BUTTON) == 1)
                .click("#viscript_lib_ai_menu")
                .check("AI 菜单只保留打开对话窗口", ctx -> menuLabels(ctx).equals(java.util.List.of(
                        Component.translatable("viscript_lib.ai.open").getString())))
                .click("#viscript_lib_ai_menu")
                .click("#vsl_test_file_menu")
                .check("文件菜单仍有原生设置入口", ctx -> menuLabels(ctx).stream()
                        .anyMatch(label -> label.startsWith(Component.translatable("editor.settings").getString())))
                .check("文件菜单保留 JSON 剪贴板导入导出", ctx -> menuLabels(ctx).containsAll(java.util.List.of(
                        Component.translatable("viscript_lib.editor.export").getString(),
                        Component.translatable("viscript_lib.editor.import").getString())))
                .click("#vsl_test_file_menu")
                .click(SCALE_BUTTON).ticks(3)
                .check("缩放按钮可缩到指定区域", ctx -> !root(ctx).isMaximized()
                        && root(ctx).window.getSizeWidth() < ctx.mc().getWindow().getGuiScaledWidth())
                .screenshot("window-controls-restored")
                .step("定位最小化按钮", ctx -> {
                    var button = minimizeButton(editor(ctx));
                    if (button == null) throw new IllegalStateException("Missing minimize control");
                    button.setId("vsl_test_minimize");
                }).hover("#vsl_test_minimize")
                .step("按下最小化按钮", ctx -> {
                    var bounds = ctx.el("#vsl_test_minimize").bounds();
                    ctx.put("minimize-bounds", bounds);
                    ctx.input().mouseDown(bounds.centerX(), bounds.centerY(), 0);
                }).step("释放最小化按钮", ctx -> {
                    ElementBounds bounds = ctx.get("minimize-bounds");
                    ctx.input().mouseUp(bounds.centerX(), bounds.centerY(), 0);
                }).check("最小化关闭界面并缓存原窗口", ctx -> !(ctx.mc().screen instanceof ModularUIScreen)
                        && EditorWindowAccessor.viscript_lib$getMinimizedWindows().get(WINDOW_ID) == root(ctx))
                .openScreen("从最小化缓存恢复", ctx -> {
                    var restored = ViScriptEditorWindow.open(WINDOW_ID, FunctionEditor::new);
                    ctx.check("恢复同一窗口实例", restored == root(ctx));
                    return screen(restored);
                }).awaitModularUI().ticks(3)
                .check("恢复后项目与数据保持原样", ctx -> editor(ctx).getCurrentProject() == ctx.get("project")
                        && editor(ctx).getCurrentProject().serializeNBT(Platform.getFrozenRegistry()).equals(ctx.get("before")))
                .check("恢复后最小化按钮仍在", ctx -> minimizeButton(editor(ctx)) != null)
                .click(SCALE_BUTTON).ticks(3)
                .check("缩放按钮仍能最大化", ctx -> root(ctx).isMaximized())
                .screenshot("window-controls-maximized")
                .step("创建第二类编辑器", ctx -> {
                    var editor = root(ctx).createNewEditor(ProjectEditor::new);
                    editor.loadProject(DevProjectFileProjectType.TYPE.newEmptyProject(), null);
                    ctx.put("editor", editor);
                    editor.menuContainer.getChildren().getFirst().setId("vsl_test_project_file_menu");
                }).ticks(3)
                .check("新编辑器保留最小化按钮", ctx -> minimizeButton(editor(ctx)) != null
                        && editor(ctx).buttonContainer.getChildren().size() == 4)
                .click("#vsl_test_project_file_menu")
                .check("工程编辑器保留运行时导出", ctx -> menuLabels(ctx).contains(
                        Component.translatable("viscript_lib.editor.menu.export_runtime_file").getString()))
                .check("工程编辑器保留 JSON 剪贴板导入导出", ctx -> menuLabels(ctx).containsAll(java.util.List.of(
                        Component.translatable("viscript_lib.editor.export").getString(),
                        Component.translatable("viscript_lib.editor.import").getString())))
                .teardown("移除测试窗口缓存", ctx -> {
                    EditorWindowAccessor.viscript_lib$getMinimizedWindows().remove(WINDOW_ID);
                    ctx.mc().setScreen(null);
                });
    }

    private static ModularUIScreen screen(EditorWindow root) {
        return new ModularUIScreen(ModularUI.of(UI.of(root)).shouldCloseOnEsc(false).shouldCloseOnKeyInventory(false), Component.empty());
    }

    private static java.util.List<String> menuLabels(TestContext ctx) {
        return ctx.query("menu label").list().stream().map(element -> element.as(TextElement.class).getText().getString()).toList();
    }

    private static UIElement minimizeButton(Editor editor) {
        return editor.buttonContainer.getChildren().stream().filter(button -> button.getChildren().stream()
                .anyMatch(icon -> icon.hasClass("__button_pre-icon__") && icon.getStyle().backgroundTexture() == Icons.WINDOW_MINIMIZE))
                .findFirst().orElse(null);
    }

    private static ViScriptEditorWindow root(TestContext ctx) { return ctx.get("root"); }
    private static Editor editor(TestContext ctx) { return ctx.get("editor"); }

    private static final class FunctionEditor extends DevFunctionFileEditor {
        @Override
        protected EditorSettings createSettings() { return isolatedSettings(super.createSettings()); }
    }
    private static final class ProjectEditor extends DevProjectFileEditor {
        @Override
        protected EditorSettings createSettings() { return isolatedSettings(super.createSettings()); }
    }
    private static EditorSettings isolatedSettings(EditorSettings settings) {
        return settings.setSettingsFile(Path.of(System.getProperty("ldlib2.uitest.out"), "window-controls-unused-settings.json").toFile());
    }
}
