package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.viscript_lib.gui.components.search.MissingItemIdSearchBox;
import net.minecraft.network.chat.Component;

/**
 * 提供当前编辑器项目的异常物品修复操作。
 */
final class UnavailableItemsMenu extends MenuTab {

    UnavailableItemsMenu(ViScriptEditor editor) {
        super(editor);
    }

    @Override
    protected TreeBuilder.Menu createDefaultMenu() {
        return TreeBuilder.Menu.start()
                .leaf(Icons.DELETE,
                        "viscript_lib.editor.menu.delete_missing_item_id",
                        this::showDeleteMissingItemDialog);
    }

    @Override
    protected Component getComponent() {
        return Component.translatable("viscript_lib.editor.menu.unavailable_items");
    }

    private void showDeleteMissingItemDialog() {
        var editor = (ViScriptEditor) this.editor;
        if (editor.getCurrentProject() == null) {
            Dialog.showNotification("viscript_lib.editor.no_project", 2).show(editor.getModularUI());
            return;
        }

        var missingItemIds = editor.findCurrentMissingItemIds();
        if (missingItemIds.isEmpty()) {
            Dialog.showNotification("viscript_lib.editor.missing_item_cleanup.empty", 2)
                    .show(editor.getModularUI());
            return;
        }

        var searchBox = new MissingItemIdSearchBox(missingItemIds);
        searchBox.setId("viscript_lib_missing_item_id_search");
        searchBox.layout(layout -> layout.widthPercent(100).height(14));

        var dialog = new Dialog().setTitle("viscript_lib.editor.missing_item_cleanup.title");
        dialog.overlay.layout(layout -> layout.width(220));
        dialog.addContent(new Label()
                .setText("viscript_lib.editor.missing_item_cleanup.description")
                .textStyle(style -> style
                        .textWrap(TextWrap.WRAP)
                        .adaptiveWidth(false)
                        .adaptiveHeight(true))
                .layout(layout -> layout.widthPercent(100).minWidth(0)));
        dialog.addContent(searchBox);
        var confirmButton = new Button()
                .setOnClick(event -> {
                    var itemId = searchBox.getSelectedOrExactId();
                    if (itemId == null) {
                        Dialog.showNotification("viscript_lib.editor.missing_item_cleanup.select_required", 2)
                                .show(editor.getModularUI());
                        return;
                    }
                    try {
                        var removed = editor.deleteCurrentMissingItemId(itemId);
                        dialog.close();
                        Dialog.showNotification(
                                removed > 0
                                        ? "viscript_lib.editor.missing_item_cleanup.success"
                                        : "viscript_lib.editor.missing_item_cleanup.not_found",
                                2
                        ).show(editor.getModularUI());
                    } catch (RuntimeException exception) {
                        dialog.close();
                        Dialog.showNotification(
                                "editor.error",
                                "viscript_lib.editor.missing_item_cleanup.failed",
                                null
                        ).show(editor.getModularUI());
                    }
                })
                .setText("ldlib.gui.tips.confirm")
                .addClass("__confirm-button__");
        confirmButton.setId("viscript_lib_delete_missing_item_confirm");
        dialog.addButton(confirmButton);
        dialog.addButton(new Button()
                .setOnClick(event -> dialog.close())
                .setText("ldlib.gui.tips.cancel")
                .addClass("__cancel-button__"));
        dialog.show(editor.getModularUI());
    }
}
