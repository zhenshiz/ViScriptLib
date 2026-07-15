package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.viscript_recipe.data.RecipeFile;
import net.minecraft.network.chat.Component;

public class RecipeMenu extends MenuTab {
    public RecipeMenu(RecipeEditor editor) {
        super(editor);
    }

    @Override
    protected TreeBuilder.Menu createDefaultMenu() {
        return TreeBuilder.Menu.start()
                .leaf(Icons.EDIT_FILE, "viscript_recipe.editor.menu.recipe_namespace", this::editRecipeNamespace);
    }

    @Override
    protected Component getComponent() {
        return Component.translatable("viscript_recipe.editor.menu.recipe");
    }

    private void editRecipeNamespace() {
        if (!(editor.getCurrentProject() instanceof RecipeProject project)) {
            Dialog.showNotification("viscript_lib.editor.no_project", 2).show(editor.getModularUI());
            return;
        }
        var recipeFile = project.getRecipeFile();
        Dialog.stringEditorDialog(
                        "viscript_recipe.editor.dialog.recipe_namespace",
                        recipeFile.getRecipeNamespace(),
                        RecipeFile::isValidRecipeNamespace,
                        recipeFile::setRecipeNamespace)
                .show(editor.getModularUI());
    }
}
