package com.viscript_lib.gui.settings;

import com.lowdragmc.lowdraglib2.configurator.ui.BooleanConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.StringConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.SelectorConfigurator;
import com.lowdragmc.lowdraglib2.editor.settings.Settings;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.ai.AiReasoning;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/**
 * 保存所有 VSL 编辑器共用的本地 AI 连接参数和编辑偏好。
 *
 * <p>设置沿用编辑器的应用、确定、取消和持久化流程，不写入工程文档，也不作为服务端权限。
 * 连接参数供 AI 会话读取；应用设置本身不会发送网络请求。
 */
@Getter
@Setter
public final class AiEditorSettings implements Settings {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ViScriptLib.MOD_ID, "ai");
    public static final Codec<AiEditorSettings> CODEC = PersistedParser.createCodec(AiEditorSettings::new);
    private static final String KEY = "settings.viscript_lib.ai.";

    @Persisted
    private boolean enabled;
    @Persisted
    private String endpoint = "http://127.0.0.1:11434/v1";
    @Persisted
    private String apiProtocol = "auto";
    @Persisted
    private String model = "";
    @Persisted
    private String reasoningEffort = "default";
    @Persisted
    private String apiKey = "";
    @Persisted
    private int requestTimeoutSeconds = 30;
    @Persisted
    private int maxContextEntries = 64;
    @Persisted
    private boolean allowProjectEdit;
    @Persisted
    private boolean allowRuntimeApply;
    @Persisted
    private boolean requireConfirmation = true;

    /**
     * 返回指定编辑器当前的 AI 设置。
     *
     * @param editor 拥有设置的编辑器
     * @return 已注册的设置；未注册时返回默认设置
     */
    public static AiEditorSettings of(Editor editor) {
        return editor.getEditorSettings().getSettings(ID)
                .filter(AiEditorSettings.class::isInstance)
                .map(AiEditorSettings.class::cast)
                .orElseGet(AiEditorSettings::new);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public String getPath() {
        return "AI";
    }

    @Override
    public void onLoaded(Editor editor) {
        requestTimeoutSeconds = Math.clamp(requestTimeoutSeconds, 1, 300);
        maxContextEntries = Math.clamp(maxContextEntries, 1, 128);
        if (!List.of("auto", "openai", "anthropic").contains(apiProtocol)) apiProtocol = "auto";
        if (!AiReasoning.VALUES.contains(reasoningEffort)) reasoningEffort = "default";
    }

    @Override
    public void onApply(Editor editor) {
        // 编辑器负责保存和回滚，AI 运行时按需读取当前设置，不在这里发送请求。
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        var enabled = new BooleanConfigurator(KEY + "enabled", this::isEnabled, this::setEnabled, false, true);
        enabled.toggle.setId("vsl_ai_enabled");
        father.addConfigurator(enabled);

        var protocol = new SelectorConfigurator<>(KEY + "apiProtocol", this::getApiProtocol, this::setApiProtocol,
                "auto", true, List.of("auto", "openai", "anthropic"), value -> KEY + "protocol." + value);
        protocol.selector.setId("vsl_ai_protocol");
        father.addConfigurator(protocol);

        var endpoint = new StringConfigurator(KEY + "endpoint", this::getEndpoint, this::setEndpoint,
                "http://127.0.0.1:11434/v1", true);
        endpoint.textField.setId("vsl_ai_endpoint");
        father.addConfigurator(endpoint);
        var model = new StringConfigurator(KEY + "model", this::getModel, this::setModel, "", true);
        model.textField.setId("vsl_ai_model");
        father.addConfigurator(model);
        var reasoning = new SelectorConfigurator<>(KEY + "reasoningEffort", this::getReasoningEffort,
                this::setReasoningEffort, "default", true, AiReasoning.VALUES, value -> KEY + "reasoning." + value);
        reasoning.setTips(KEY + "reasoningEffort.tip");
        reasoning.selector.setId("vsl_ai_reasoning_effort");
        reasoning.selector.selectorStyle(style -> style.maxItemCount(AiReasoning.VALUES.size()));
        father.addConfigurator(reasoning);
        var key = new StringConfigurator(KEY + "apiKey", this::getApiKey, this::setApiKey, "", true);
        key.setTips(KEY + "apiKey.tip");
        key.textField.setId("vsl_ai_api_key");
        key.textField.setFormatter(value -> Component.literal("*".repeat(value.length())));
        father.addConfigurator(key);

        var timeout = new NumberConfigurator(KEY + "requestTimeoutSeconds", this::getRequestTimeoutSeconds,
                value -> setRequestTimeoutSeconds(value.intValue()), 30, true).setRange(1, 300);
        timeout.setTips(KEY + "requestTimeoutSeconds.tip");
        timeout.textField.setId("vsl_ai_timeout");
        father.addConfigurator(timeout);
        var pageSize = new NumberConfigurator(KEY + "maxContextEntries", this::getMaxContextEntries,
                value -> setMaxContextEntries(value.intValue()), 64, true).setRange(1, 128);
        pageSize.textField.setId("vsl_ai_context_entries");
        father.addConfigurator(pageSize);

        var project = new BooleanConfigurator(KEY + "allowProjectEdit", this::isAllowProjectEdit,
                this::setAllowProjectEdit, false, true);
        project.setTips(KEY + "edit.tip");
        project.toggle.setId("vsl_ai_project_edit");
        father.addConfigurator(project);
        var runtime = new BooleanConfigurator(KEY + "allowRuntimeApply", this::isAllowRuntimeApply,
                this::setAllowRuntimeApply, false, true);
        runtime.setTips(KEY + "edit.tip");
        runtime.toggle.setId("vsl_ai_runtime_apply");
        father.addConfigurator(runtime);
        var confirmation = new BooleanConfigurator(KEY + "requireConfirmation", this::isRequireConfirmation,
                this::setRequireConfirmation, true, true);
        confirmation.toggle.setId("vsl_ai_confirmation");
        father.addConfigurator(confirmation);
    }
}
