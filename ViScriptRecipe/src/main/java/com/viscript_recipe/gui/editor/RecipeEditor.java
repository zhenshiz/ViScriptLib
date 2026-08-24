package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorLayout;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.viscript_lib.gui.editor.EditorServerUploads;
import com.viscript_lib.gui.editor.EditorUploadAction;
import com.viscript_lib.gui.editor.FunctionFileEditor;
import com.viscript_lib.mixin.EditorWindowAccessor;
import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class RecipeEditor extends FunctionFileEditor {
    public static final ResourceLocation WINDOW_ID = ViScriptRecipe.id("recipe_editor");
    private static final float RESTORED_WIDTH_PERCENT = 69;

    public RecipeEditor() {
        registerProjectType(RecipeProjectType.TYPE);
        removeUnusedBottomWindow();
    }

    public static ModularUI createUI() {
        var root = EditorWindow.open(WINDOW_ID, RecipeEditor::new);
        configureRestoredBounds(root);
        return new ModularUI(UI.of(root))
                .shouldCloseOnEsc(false)
                .shouldCloseOnKeyInventory(false);
    }

    private static void configureRestoredBounds(EditorWindow window) {
        var minecraftWindow = Minecraft.getInstance().getWindow();
        var screenWidth = minecraftWindow.getGuiScaledWidth();
        var screenHeight = minecraftWindow.getGuiScaledHeight();
        var accessor = (EditorWindowAccessor) window;
        accessor.viscript_lib$setWindowLeft(-centeredRootOrigin(screenWidth));
        accessor.viscript_lib$setWindowTop(-centeredRootOrigin(screenHeight));
        accessor.viscript_lib$setWindowWidth(screenWidth * RESTORED_WIDTH_PERCENT / 100f);
        accessor.viscript_lib$setWindowHeight(screenHeight);
    }

    private static float centeredRootOrigin(float screenSize) {
        return Math.round((screenSize - 1f) / 2f);
    }

    @Override
    protected void initMenus() {
        super.initMenus();
        menuContainer.addChild(new RecipeMenu(this).createMenuTab());
    }

    @Override
    protected @Nonnull Editor createNewEditorInstance() {
        return new RecipeEditor();
    }

    @Override
    public void applyLayout(EditorLayout layout) {
        super.applyLayout(layout);
        removeUnusedBottomWindow();
    }

    @Override
    protected void onPrepareHistoryView() {
    }

    @Override
    protected void onPrepareInspectorView() {
    }

    @Override
    protected void onPrepareResourceView() {
    }

    @Override
    protected EditorUploadAction createServerUploadAction() {
        if (getCurrentProject() instanceof RecipeProject project) {
            return new RecipeUploadAction(project);
        }
        return null;
    }

    /**
     * Removes the unused bottom split after both the default layout and a persisted layout are built.
     */
    private void removeUnusedBottomWindow() {
        if (bottomWindow != rootWindow && bottomWindow.getAllViews().isEmpty()) {
            removeBottomWindow();
        }
    }

    private record RecipeUploadAction(RecipeProject project) implements EditorUploadAction {
        @Override
        public Component getDisplayName() {
            return Component.translatable("viscript_recipe.editor.upload_recipe_file");
        }

        @Override
        public String getDialogTitleKey() {
            return "viscript_recipe.editor.dialog.upload_recipe_file";
        }

        @Override
        public String getDefaultFileName() {
            return "";
        }

        @Override
        public String getSuffix() {
            return RecipeProjectType.FORMAT.runtimeSuffix();
        }

        @Override
        public void uploadToServer(String fileName) {
            project.saveCurrentVisualState();
            EditorServerUploads.uploadToServer(
                    RecipeProjectType.FORMAT,
                    fileName,
                    project.getRecipeFile().serializeNBT(Platform.getFrozenRegistry()));
        }
    }
}
