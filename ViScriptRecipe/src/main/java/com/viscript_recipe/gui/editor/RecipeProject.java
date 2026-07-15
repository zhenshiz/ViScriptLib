package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.viscript_lib.gui.editor.IRuntimeFileProject;
import com.viscript_recipe.data.RecipeFile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RecipeProject implements IRuntimeFileProject {
    @Getter
    @Setter
    private RecipeFile recipeFile = new RecipeFile();
    @Getter
    private final Resources resources = Resources.of();

    @Nullable
    @Getter
    private Editor editor;
    @Nullable
    private RecipeNavigationView navigationView;
    @Nullable
    private CraftingWorkbenchView workbenchView;
    @Nullable
    private RecipePropertiesView propertiesView;

    @Override
    public ProjectType getProjectType() {
        return RecipeProjectType.TYPE;
    }

    @Override
    public void initNewProject() {
        recipeFile = new RecipeFile();
    }

    public void saveCurrentVisualState() {
        if (navigationView != null) {
            navigationView.saveCurrentVisualState();
        }
    }

    @Override
    public CompoundTag serializeProject(@NotNull HolderLookup.Provider provider) {
        return serializeRuntimeFile(provider);
    }

    @Override
    public CompoundTag serializeRuntimeFile(@Nonnull HolderLookup.Provider provider) {
        saveCurrentVisualState();
        return recipeFile.serializeNBT(provider);
    }

    @Override
    public void deserializeProject(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag nbt) {
        recipeFile = new RecipeFile();
        recipeFile.deserializeNBT(provider, nbt);
    }

    @Override
    public void onLoad(@Nonnull Editor editor) {
        IRuntimeFileProject.super.onLoad(editor);
        this.editor = editor;
        var controller = new RecipeEditorController(this);
        this.navigationView = new RecipeNavigationView(controller);
        this.workbenchView = new CraftingWorkbenchView(controller);
        this.propertiesView = new RecipePropertiesView(controller);
        editor.placeView(navigationView, () -> editor.leftWindow.getLeftTop());
        editor.placeView(workbenchView, () -> editor.centerWindow.getLeftTop());
        editor.placeView(propertiesView, () -> editor.rightWindow.getRightTop());
    }

    @Override
    public void onClosed(@Nonnull Editor editor) {
        IRuntimeFileProject.super.onClosed(editor);
        if (navigationView != null) {
            navigationView.removeSelf();
        }
        if (workbenchView != null) {
            workbenchView.removeSelf();
        }
        if (propertiesView != null) {
            propertiesView.removeSelf();
        }
        this.editor = null;
        this.navigationView = null;
        this.workbenchView = null;
        this.propertiesView = null;
    }
}
