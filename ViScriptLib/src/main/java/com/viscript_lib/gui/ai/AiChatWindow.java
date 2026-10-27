package com.viscript_lib.gui.ai;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.window.ModularUIWindow;
import com.viscript_lib.gui.editor.ViScriptEditor;
import lombok.Getter;
import net.minecraft.network.chat.Component;

/** 将 AI 会话托管在 LDLib2 游戏外窗口中，并跟随所属编辑器关闭。 */
public final class AiChatWindow {
    private final ViScriptEditor editor;
    @Getter private final AiChatSession session;
    @Getter private ModularUIWindow window;
    private Dialog fallback;

    /**
     * 创建窗口控制器，首次打开时才分配原生窗口。
     *
     * @param editor 所属编辑器
     */
    public AiChatWindow(ViScriptEditor editor) {
        this.editor = editor;
        this.session = new AiChatSession(editor, editor.createAiEditorAdapter());
    }

    /** 打开聊天窗口；已打开时聚焦原窗口，不另建会话。 */
    public void open() {
        if (window != null && window.isOpen()) {
            window.window().focus();
            return;
        }
        if (fallback != null) return;
        var panel = new AiChatPanel(session, this::close);
        window = new ModularUIWindow(ModularUI.of(UI.of(panel)), Component.translatable("viscript_lib.ai.title").getString());
        window.setStyleSource(editor.getModularUI());
        window.setDragArea(panel.titleBar);
        window.setOnCloseRequested(this::close);
        if (!window.open(Integer.MIN_VALUE, Integer.MIN_VALUE, 760, 800, false)) {
            window = null;
            fallback = new Dialog().setAutoClose(false).setTitle("viscript_lib.ai.title");
            fallback.addContent(panel);
            fallback.windowMode(30, 30, 420, 380);
            fallback.show(editor.getModularUI());
        }
    }

    /** 关闭窗口会停止请求；已应用的原生历史和已生成的预览仍属于当前编辑器。 */
    public void close() {
        session.stop();
        session.setListener(() -> {});
        if (window != null) window.close();
        window = null;
        if (fallback != null) fallback.close();
        fallback = null;
    }
}
