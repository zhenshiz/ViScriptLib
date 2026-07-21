package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib2.test.ui.IMenuTest;
import com.viscript_lib.gui.editor.ViScriptEditorWindow;
import net.minecraft.world.entity.player.Player;

/**
 * 注册工程文件编辑器的开发环境菜单测试入口。
 */
@LDLRegister(
        name = "viscript_project_file_editor",
        registry = "ldlib2:menu_test",
        environment = RegistrationEnvironment.DEV_ONLY
)
public class DevProjectFileEditorMenuTest implements IMenuTest {
    @Override
    public ModularUI createUI(Player player) {
        var root = new ViScriptEditorWindow(DevProjectFileEditor::new)
                .setMinimizedBoundsPercent(0, 0, 70, 100)
                .removeDefaultScaleButton();
        return new ModularUI(UI.of(root), player)
                .shouldCloseOnEsc(false)
                .shouldCloseOnKeyInventory(false);
    }
}
