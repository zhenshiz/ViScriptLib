package com.viscript_lib.gui.editor.dev;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.editor.settings.EditorSettings;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.ElementBounds;
import com.lowdragmc.lowdraglib2.uitest.TestContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.settings.AiEditorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

/** 验证真实编辑器的 AI 设置页、保存、跨编辑器读取和取消回滚。 */
@LDLRegisterClient(name = "vsl_ai_editor_settings", group = ViScriptLib.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class AiEditorSettingsScenario implements UIScenario {
    private static Path settingsPath() {
        return Path.of(System.getProperty("ldlib2.uitest.out")).resolve("ai-settings/editor.json");
    }

    @Override
    public void configure(ScenarioOptions options) {
        options.requiresWorld(false).guiScale(2);
    }

    @Override
    public void define(ScenarioBuilder s) {
        s.step("准备隔离的编辑器设置文件", ctx -> {
                    try {
                        Files.createDirectories(settingsPath().getParent());
                        Files.deleteIfExists(settingsPath());
                    } catch (java.io.IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .openScreen("打开功能文件编辑器", ctx -> {
                    var editor = new FunctionEditor();
                    ctx.put("editor", editor);
                    return new ModularUIScreen(ModularUI.of(UI.of(editor)), Component.empty());
                }).awaitModularUI()
                .check("AI 设置注册于 VSL 编辑器", ctx -> editor(ctx).editorSettings.getSettings(AiEditorSettings.ID).isPresent())
                .check("默认关闭且要求确认，推理强度跟随服务端", ctx -> !settings(ctx).isEnabled()
                        && settings(ctx).isRequireConfirmation() && settings(ctx).getReasoningEffort().equals("default"))
                .step("打开编辑器原生设置面板", ctx -> editor(ctx).openSettingsPanel())
                .step("定位设置树根节点", AiEditorSettingsScenario::locateRoot)
                .doubleClick("#vsl_test_settings_root")
                .step("定位 AI 分类", AiEditorSettingsScenario::locateCategory)
                .click("#vsl_test_ai_category").awaitElement("#vsl_ai_enabled")
                .click("#vsl_ai_enabled .__toggle_button__")
                .check("开关绑定编辑器设置", ctx -> settings(ctx).isEnabled());
        replace(s, "#vsl_ai_endpoint", "http://127.0.0.1:11434/v1");
        replace(s, "#vsl_ai_model", "vsl-test-model");
        selectReasoning(s, "low");
        replace(s, "#vsl_ai_api_key", "vsl-test-key");
        s.check("连接字段写入当前设置", ctx -> settings(ctx).getModel().equals("vsl-test-model")
                        && settings(ctx).getApiKey().equals("vsl-test-key") && settings(ctx).getReasoningEffort().equals("low"))
                .check("密钥显示为掩码", ctx -> ctx.el("#vsl_ai_api_key").as(TextField.class).getFormatter()
                        .apply("vsl-test-key").getString().equals("************"))
                .step("定位应用按钮", ctx -> locateButton(ctx, "ldlib.gui.tips.apply", "vsl_test_apply"))
                .ticks(2).click("#vsl_test_apply")
                .check("应用后文件含 AI 设置且不再脏", ctx -> {
                    try {
                        var saved = JsonParser.parseString(Files.readString(settingsPath())).getAsJsonObject();
                        return saved.has("viscript_lib:ai") && saved.getAsJsonObject("viscript_lib:ai")
                                .get("model").getAsString().equals("vsl-test-model") && saved.getAsJsonObject("viscript_lib:ai")
                                .get("reasoningEffort").getAsString().equals("low") && !editor(ctx).editorSettings.isDirty();
                    } catch (java.io.IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                }).screenshot("ai-editor-settings");
        replace(s, "#vsl_ai_model", "discard-this-model");
        selectReasoning(s, "high");
        s.step("定位取消按钮", ctx -> locateButton(ctx, "ldlib.gui.tips.cancel", "vsl_test_cancel"));
        clickClosingButton(s, "#vsl_test_cancel");
        s.check("取消恢复上次应用值与推理强度", ctx -> settings(ctx).getModel().equals("vsl-test-model")
                        && settings(ctx).getReasoningEffort().equals("low"))
                .closeScreen()
                .openScreen("打开工程文件编辑器验证共用设置", ctx -> {
                    var editor = new ProjectEditor();
                    ctx.put("editor", editor);
                    return new ModularUIScreen(ModularUI.of(UI.of(editor)), Component.empty());
                }).awaitModularUI()
                .check("另一类编辑器读取相同连接参数", ctx -> settings(ctx).isEnabled()
                        && settings(ctx).getModel().equals("vsl-test-model") && settings(ctx).getApiKey().equals("vsl-test-key")
                        && settings(ctx).getReasoningEffort().equals("low"))
                .step("再次打开设置", ctx -> editor(ctx).openSettingsPanel())
                .step("定位设置树根节点", AiEditorSettingsScenario::locateRoot)
                .doubleClick("#vsl_test_settings_root")
                .step("定位 AI 分类", AiEditorSettingsScenario::locateCategory)
                .click("#vsl_test_ai_category").awaitElement("#vsl_ai_model");
        replace(s, "#vsl_ai_model", "confirmed-model");
        selectReasoning(s, "max");
        s.step("定位确定按钮", ctx -> locateButton(ctx, "ldlib.gui.tips.confirm", "vsl_test_confirm"));
        clickClosingButton(s, "#vsl_test_confirm");
        s.check("确定保存并关闭设置", ctx -> !ctx.exists("#vsl_ai_model") && !editor(ctx).editorSettings.isDirty())
                .closeScreen()
                .step("重新创建编辑器验证确定结果", ctx -> {
                    var editor = new FunctionEditor();
                    ctx.check("确定值与推理强度持久化", AiEditorSettings.of(editor).getModel().equals("confirmed-model")
                            && AiEditorSettings.of(editor).getReasoningEffort().equals("max"));
                })
                .teardown("清理测试设置", ctx -> {
                    ctx.mc().setScreen(null);
                    try {
                        Files.deleteIfExists(settingsPath());
                    } catch (java.io.IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                });
    }

    private static Editor editor(TestContext ctx) {
        return ctx.get("editor");
    }

    private static AiEditorSettings settings(TestContext ctx) {
        return AiEditorSettings.of(editor(ctx));
    }

    private static void locateRoot(TestContext ctx) {
        ctx.query("tree-list label").withText(Component.translatable("editor.settings").getString())
                .one().element().setId("vsl_test_settings_root");
    }

    private static void locateCategory(TestContext ctx) {
        ctx.query("label").withText("AI").one().element().setId("vsl_test_ai_category");
    }

    private static void locateButton(TestContext ctx, String key, String id) {
        ctx.query("button").withText(Component.translatable(key).getString()).one().element().setId(id);
    }

    private static void selectReasoning(ScenarioBuilder s, String value) {
        String optionId = "vsl_test_reasoning_option_" + value;
        s.click("#vsl_ai_reasoning_effort").awaitElement(".__selector_dialog__")
                .step("定位推理档位 " + value, ctx -> ctx.query(".__selector_dialog__ label")
                        .withText(Component.translatable("settings.viscript_lib.ai.reasoning." + value).getString())
                        .one().element().getChildren().getFirst().setId(optionId));
        clickClosingButton(s, "#" + optionId);
        s.check("已选推理档位 " + value, ctx -> settings(ctx).getReasoningEffort().equals(value));
    }

    private static void clickClosingButton(ScenarioBuilder s, String selector) {
        // LDLib2 2.2.40 的按钮在按下时关闭弹窗，释放时必须复用按下前的坐标。
        s.hover(selector).step("按下 " + selector, ctx -> {
            var bounds = ctx.el(selector).bounds();
            ctx.put("closing-button-bounds", bounds);
            ctx.input().mouseDown(bounds.centerX(), bounds.centerY(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }).step("释放 " + selector, ctx -> {
            ElementBounds bounds = ctx.get("closing-button-bounds");
            ctx.input().mouseUp(bounds.centerX(), bounds.centerY(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
        });
    }

    private static void replace(ScenarioBuilder s, String selector, String value) {
        s.focus(selector)
                .keyDown(Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL)
                .key(GLFW.GLFW_KEY_A)
                .keyUp(Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL)
                .key(GLFW.GLFW_KEY_BACKSPACE).type(value);
    }

    /** 仅重定向设置文件，保留功能编辑器的生产 UI 和设置注册流程。 */
    private static final class FunctionEditor extends DevFunctionFileEditor {
        @Override
        protected EditorSettings createSettings() {
            return super.createSettings().setSettingsFile(settingsPath().toFile());
        }
    }

    /** 仅重定向设置文件，验证两类编辑器共用相同设置结构。 */
    private static final class ProjectEditor extends DevProjectFileEditor {
        @Override
        protected EditorSettings createSettings() {
            return super.createSettings().setSettingsFile(settingsPath().toFile());
        }
    }
}
