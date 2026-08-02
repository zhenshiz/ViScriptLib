package com.viscript_recipe.recipe;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class RecipeFileLoader {
    private RecipeFileLoader() {
    }

    public static List<LoadedRecipeFile> loadAll(HolderLookup.Provider provider) {
        var root = RecipeAssetPaths.recipeDirectory();
        ensureDirectory(root);
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(RecipeAssetPaths.RECIPE_SUFFIX))
                    .sorted(Comparator.comparing(path -> normalize(root.relativize(path))))
                    .map(path -> load(root, path, provider))
                    .filter(loaded -> loaded.file() != null)
                    .toList();
        } catch (IOException e) {
            ViScriptRecipe.LOGGER.error("Failed to scan recipe asset directory {}", root, e);
            return List.of();
        }
    }

    private static LoadedRecipeFile load(Path root, Path path, HolderLookup.Provider provider) {
        try {
            var tag = NbtIo.read(path.toFile());
            if (tag == null) {
                ViScriptRecipe.LOGGER.warn("Skipping empty recipe file {}", path);
                return new LoadedRecipeFile(normalize(root.relativize(path)), path, null);
            }
            var file = new RecipeFile();
            file.deserializeNBT(provider, tag);
            return new LoadedRecipeFile(normalize(root.relativize(path)), path, file);
        } catch (IOException rawReadError) {
            try {
                var tag = NbtIo.readCompressed(path.toFile());
                var file = new RecipeFile();
                file.deserializeNBT(provider, tag);
                return new LoadedRecipeFile(normalize(root.relativize(path)), path, file);
            } catch (Exception compressedReadError) {
                ViScriptRecipe.LOGGER.error("Failed to load recipe file {}", path, compressedReadError);
                return new LoadedRecipeFile(normalize(root.relativize(path)), path, null);
            }
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to deserialize recipe file {}", path, e);
            return new LoadedRecipeFile(normalize(root.relativize(path)), path, null);
        }
    }

    public static void save(Path path, RecipeFile file, HolderLookup.Provider provider) throws IOException {
        var parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        NbtIo.write(file.serializeNBT(provider), path.toFile());
    }

    private static void ensureDirectory(Path root) {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            ViScriptRecipe.LOGGER.error("Failed to create recipe asset directory {}", root, e);
        }
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }

    public record LoadedRecipeFile(String relativePath, Path path, @Nullable RecipeFile file) {
    }
}
