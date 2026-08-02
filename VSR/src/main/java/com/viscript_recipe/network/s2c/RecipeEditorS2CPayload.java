package com.viscript_recipe.network.s2c;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.editor.StructureTagClientData;
import com.viscript_recipe.recipe.RecipeAssetPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class RecipeEditorS2CPayload {
    public static final String OPEN_RECIPE_EDITOR_FILE = ViScriptRecipe.MOD_ID + ":open_recipe_editor_file";
    public static final String SYNC_STRUCTURE_TAGS = ViScriptRecipe.MOD_ID + ":sync_structure_tags";

    private RecipeEditorS2CPayload() {
    }

    /**
     * Replaces the client editor's structure tag catalog with an authoritative server snapshot.
     *
     * <p>The snapshot contains structure tag identifiers and the structure identifiers bound to each tag.
     *
     * @param  sender the RPC sender identifying the server
     * @param  snapshot the compound tag containing the encoded structure tag catalog
     */
    @RPCPacket(value = SYNC_STRUCTURE_TAGS, modId = ViScriptRecipe.MOD_ID)
    public static void syncStructureTags(RPCSender sender, CompoundTag snapshot) {
        StructureTagClientData.updateFromServer(snapshot);
    }

    @RPCPacket(value = OPEN_RECIPE_EDITOR_FILE, modId = ViScriptRecipe.MOD_ID)
    public static void openRecipeEditorFile(RPCSender sender, String relativePath, CompoundTag fileTag) {
        var editorWindow = getCurrentEditorWindow();
        if (editorWindow == null) {
            return;
        }
        var editor = editorWindow.getCurrentEditor();
        if (editor == null) {
            return;
        }

        var project = new RecipeProject();
        project.deserializeProject(Platform.getFrozenRegistry(), fileTag == null ? new CompoundTag() : fileTag);
        var file = RecipeAssetPaths.recipeDirectory().resolve(relativePath).normalize().toFile();
        try {
            editor.loadProject(project, file);
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to load recipe editor project from {}", file, e);
        }
    }

    private static EditorWindow getCurrentEditorWindow() {
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof ModularUIContainerScreen containerScreen
                && containerScreen.getMenu().getModularUI().ui.rootElement instanceof EditorWindow editorWindow) {
            return editorWindow;
        }
        if (screen instanceof ModularUIScreen modularUIScreen
                && modularUIScreen.modularUI.ui.rootElement instanceof EditorWindow editorWindow) {
            return editorWindow;
        }
        return null;
    }
}
