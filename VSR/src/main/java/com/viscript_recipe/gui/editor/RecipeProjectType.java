package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.viscript_lib.gui.editor.EditorFileFormat;
import com.viscript_lib.gui.editor.FunctionFileProjectType;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeFile;
import com.viscript_recipe.recipe.RecipeAssetPaths;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class RecipeProjectType extends FunctionFileProjectType {
    public static final EditorFileFormat FORMAT = EditorFileFormat.of(
            ViScriptRecipe.MOD_ID,
            "recipes",
            RecipeAssetPaths.RECIPE_SUFFIX
    );
    public static final RecipeProjectType TYPE = new RecipeProjectType();

    private RecipeProjectType() {
        super(Icons.FILE, "viscript_recipe.project.recipe", FORMAT, RecipeProject::new);
    }

    @Override
    public IProject loadProjectFromFile(File file) throws Exception {
        var tag = NbtIo.read(file);
        if (tag == null) {
            return null;
        }
        var recipeFile = new RecipeFile();
        recipeFile.deserializeNBT(Platform.getFrozenRegistry(), tag);
        var project = new RecipeProject();
        project.setRecipeFile(recipeFile);
        return project;
    }

    @Override
    public void saveProjectToFile(IProject project, File file) throws Exception {
        var parent = file.toPath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (project instanceof RecipeProject recipeProject) {
            recipeProject.saveCurrentVisualState();
            NbtIo.write(recipeProject.getRecipeFile().serializeNBT(Platform.getFrozenRegistry()), file);
        }
    }

    @Override
    public boolean isProjectDirty(IProject project, File file) throws Exception {
        if (project instanceof RecipeProject recipeProject) {
            recipeProject.saveCurrentVisualState();
            if (!Files.exists(file.toPath())) {
                return true;
            }
            return !Objects.equals(
                    recipeProject.getRecipeFile().serializeNBT(Platform.getFrozenRegistry()),
                    NbtIo.read(file)
            );
        }
        return super.isProjectDirty(project, file);
    }
}
