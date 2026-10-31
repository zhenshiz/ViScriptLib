package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import net.minecraft.network.chat.Component;

/** 提供 AI 独立对话窗口入口。 */
final class AiMenu extends MenuTab {
    AiMenu(ViScriptEditor editor) {
        super(editor);
    }

    @Override
    protected TreeBuilder.Menu createDefaultMenu() {
        return TreeBuilder.Menu.start()
                .leaf("viscript_lib.ai.open", () -> ((ViScriptEditor) editor).getAiChat().open());
    }

    @Override
    protected Component getComponent() {
        return Component.literal("AI");
    }
}
