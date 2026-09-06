package com.viscript_recipe.recipe.importer;

import com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.vanilla.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public final class RecipeImporter {
    private static final char[] SHAPED_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>/?|~".toCharArray();
    public static final RecipeImportHandler VANILLA_HANDLER = new RecipeImportHandler() {
        @Override
        public boolean canImport(Recipe<?> recipe) {
            if (recipe == null) {
                return false;
            }
            return recipe instanceof ShapedRecipe
                    || recipe instanceof ShapelessRecipe
                    || recipe instanceof AbstractCookingRecipe
                    || recipe instanceof StonecutterRecipe
                    || recipe instanceof SmithingTransformRecipe;
        }

        @Override
        public RecipeImportResult tryImport(Recipe<?> recipe, HolderLookup.Provider provider) throws RecipeImportException {
            if (recipe instanceof ShapedRecipe shaped) {
                return success(importShaped(recipe.getId(), shaped, provider));
            }
            if (recipe instanceof ShapelessRecipe shapeless) {
                return success(importShapeless(recipe.getId(), shapeless, provider));
            }
            if (recipe instanceof AbstractCookingRecipe cooking) {
                return success(importCooking(recipe.getId(), cooking, provider));
            }
            if (recipe instanceof StonecutterRecipe stonecutter) {
                return success(importStonecutting(recipe.getId(), stonecutter, provider));
            }
            if (recipe instanceof SmithingTransformRecipe smithing) {
                return success(importSmithingTransform(recipe.getId(), smithing));
            }
            return null;
        }
    };
    public static final List<RecipeImportHandler> HANDLERS = new ArrayList<>();

    private RecipeImporter() {
    }

    public static RecipeImportResult importRecipe(ResourceLocation recipeId) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.no_world");
        }
        return minecraft.level.getRecipeManager()
                .byKey(recipeId)
                .map(holder -> importHolder(holder, minecraft.level.registryAccess()))
                .orElseGet(() -> RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.not_found", recipeId.toString()));
    }

    public static boolean canImport(Recipe<?> holder) {
        if (holder == null) {
            return false;
        }
        for (var handler : HANDLERS) {
            if (handler.canImport(holder)) {
                return true;
            }
        }
        return false;
    }

    public static String recipeTypeName(RecipeHolder holder) {
        return holder == null || holder.getRecipeUsed() == null ? "" : recipeTypeName(holder.getRecipeUsed());
    }

    private static RecipeImportResult importHolder(Recipe<?> holder, HolderLookup.Provider provider) {
        try {
            for (var handler : HANDLERS) {
                if (!handler.canImport(holder)) {
                    continue;
                }
                var result = handler.tryImport(holder, provider);
                if (result != null) {
                    return result;
                }
            }
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.unsupported_type", recipeTypeName(holder));
        } catch (RecipeImportException exception) {
            return RecipeImportResult.failure(exception.component());
        }
    }

    public static RecipeImportResult success(RecipeEntry entry) {
        return RecipeImportResult.success(entry, imported(entry));
    }

    private static Component imported(RecipeEntry entry) {
        return Component.translatable(
                "viscript_recipe.editor.import_recipe.success",
                String.valueOf(entry.getRecipeId()),
                RecipeEditorTypes.get(entry.getType())
                        .map(type -> type.displayName().getString())
                        .orElse(entry.getType().toString())
        );
    }

    private static RecipeEntry importShaped(ResourceLocation id, ShapedRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe.getWidth() > 3 || recipe.getHeight() > 3) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.shaped_too_large", recipe.getWidth(), recipe.getHeight());
        }
        var shapedPattern = importShapedPattern(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
        var data = new ShapedCraftingRecipeData()
                .setShowNotification(recipe.showNotification())
                .setPattern(shapedPattern.pattern())
                .setKey(shapedPattern.key())
                .setRemainders(defaultRemainders(recipe.getWidth() * recipe.getHeight()))
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.CRAFTING_SHAPED).setData(data);
    }

    private static RecipeEntry importShapeless(ResourceLocation id, ShapelessRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (var ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                ingredients.add(importIngredient(ingredient));
            }
        }
        if (ingredients.size() > 9) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", ingredients.size(), 9);
        }
        var data = new ShapelessCraftingRecipeData()
                .setShowNotification(recipe.showNotification())
                .setIngredients(ingredients)
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.CRAFTING_SHAPELESS).setData(data);
    }

    private static RecipeEntry importCooking(ResourceLocation id, AbstractCookingRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var type = cookingEditorType(recipe);
        if (type == null) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", recipeTypeName(recipe));
        }
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var data = new CookingRecipeData()
                .setIngredient(importIngredient(ingredients.get(0)))
                .setResult(copyResult(recipe, provider))
                .setExperience(recipe.getExperience())
                .setCookingTime(Math.max(1, recipe.getCookingTime()));
        return baseEntry(id, type).setData(data);
    }

    @Nullable
    private static ResourceLocation cookingEditorType(AbstractCookingRecipe recipe) {
        if (recipe instanceof SmeltingRecipe || recipe.getType() == RecipeType.SMELTING) {
            return RecipeEditorTypes.SMELTING;
        }
        if (recipe instanceof BlastingRecipe || recipe.getType() == RecipeType.BLASTING) {
            return RecipeEditorTypes.BLASTING;
        }
        if (recipe instanceof SmokingRecipe || recipe.getType() == RecipeType.SMOKING) {
            return RecipeEditorTypes.SMOKING;
        }
        if (recipe instanceof CampfireCookingRecipe || recipe.getType() == RecipeType.CAMPFIRE_COOKING) {
            return RecipeEditorTypes.CAMPFIRE_COOKING;
        }
        return null;
    }

    private static RecipeEntry importStonecutting(ResourceLocation id, StonecutterRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var data = new StonecuttingRecipeData()
                .setShowNotification(recipe.showNotification())
                .setIngredient(importIngredient(ingredients.get(0)))
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.STONECUTTING).setData(data);
    }

    private static RecipeEntry importSmithingTransform(ResourceLocation id, SmithingTransformRecipe recipe) throws RecipeImportException {
        var data = new SmithingTransformRecipeData()
                .setShowNotification(recipe.showNotification())
                .setTemplate(importIngredient(recipe.template))
                .setBase(importIngredient(recipe.base))
                .setAddition(importIngredient(recipe.addition))
                .setResult(copyStack(recipe.result));
        return baseEntry(id, RecipeEditorTypes.SMITHING_TRANSFORM).setData(data);
    }

    public static RecipeEntry importMechanicalCrafting(ResourceLocation id, ShapedRecipe recipe, boolean acceptMirrored, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe.getWidth() > CreateMechanicalCraftingRecipeData.maxSize() || recipe.getHeight() > CreateMechanicalCraftingRecipeData.maxSize()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.shaped_too_large", recipe.getWidth(), recipe.getHeight());
        }
        var shapedPattern = importShapedPattern(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
        var data = new CreateMechanicalCraftingRecipeData()
                .setWidth(recipe.getWidth())
                .setHeight(recipe.getHeight())
                .setAcceptMirrored(acceptMirrored)
                .setPattern(shapedPattern.pattern())
                .setKey(shapedPattern.key())
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.CREATE_MECHANICAL_CRAFTING).setData(data);
    }

    public static ImportedShapedPattern importShapedPattern(List<Ingredient> ingredients, int width, int height) throws RecipeImportException {
        var ingredientSymbols = new LinkedHashMap<String, Character>();
        var key = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var symbolIndex = 0;
        for (int row = 0; row < height; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                var ingredient = ingredientAt(ingredients, col + row * width);
                if (ingredient.isEmpty()) {
                    builder.append(' ');
                    continue;
                }
                var imported = importIngredient(ingredient);
                var keyString = ingredientKey(imported);
                var symbol = ingredientSymbols.get(keyString);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", symbolIndex + 1, SHAPED_SYMBOLS.length);
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    ingredientSymbols.put(keyString, symbol);
                    key.add(ShapedKeyEntry.of(symbol, imported));
                }
                builder.append(symbol);
            }
            pattern.add(builder.toString());
        }
        return new ImportedShapedPattern(pattern, key);
    }

    private static Ingredient ingredientAt(List<Ingredient> ingredients, int index) {
        if (index < 0 || index >= ingredients.size()) {
            return Ingredient.EMPTY;
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? Ingredient.EMPTY : ingredient;
    }

    public static RecipeIngredient importIngredient(Ingredient ingredient) throws RecipeImportException {
        var imported = RecipeIngredient.empty();
        appendIngredientValues(imported, ingredient);
        if (imported.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        return imported;
    }

    public static List<RecipeIngredient> importIngredientList(List<Ingredient> ingredients, int max) throws RecipeImportException {
        var imported = new ArrayList<RecipeIngredient>();
        if (ingredients == null) {
            return imported;
        }
        if (ingredients.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", ingredients.size(), max);
        }
        for (var ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                imported.add(importIngredient(ingredient));
            }
        }
        return imported;
    }

    public static RecipeIngredient importItemStacks(List<ItemStack> stacks) throws RecipeImportException {
        var imported = RecipeIngredient.empty();
        if (stacks != null) {
            for (var stack : stacks) {
                appendItemValue(imported, stack);
            }
        }
        if (imported.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        return imported;
    }

    private static void appendIngredientValues(RecipeIngredient imported, Ingredient ingredient) throws RecipeImportException {
        if (ingredient == null || ingredient.isEmpty()) {
            return;
        }
        if (ingredient instanceof AbstractIngredient custom) {
            appendCustomIngredientValues(imported, custom);
            return;
        }
        for (var value : ingredient.values) {
            if (value instanceof Ingredient.ItemValue itemValue) {
                imported.setKind(IngredientValueKind.ITEM).setItem(itemValue.getItems().iterator().next().copyWithCount(1));
                return;
            } else if (value instanceof Ingredient.TagValue tagValue) {
                imported.setKind(IngredientValueKind.TAG).setTag(tagValue.tag.location());
                return;
            }
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
    }

    private static void appendCustomIngredientValues(RecipeIngredient imported, AbstractIngredient custom) throws RecipeImportException {
        if (custom instanceof CompoundIngredient compound) {
            for (var child : compound.getChildren()) {
                appendIngredientValues(imported, child);
            }
            return;
        }
        if (custom instanceof StrictNBTIngredient nbtIngredient) {
            for (var stack : Arrays.stream(nbtIngredient.getItems()).toList()) {
                appendItemValue(imported, stack);
            }
            return;
        }
        if (custom != null && custom.isSimple()) {
            for (var stack : Arrays.stream(custom.getItems()).toList()) {
                appendItemValue(imported, stack);
            }
            return;
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
    }

    private static void appendItemValue(RecipeIngredient imported, ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            imported.setKind(IngredientValueKind.ITEM).setItem(stack.copyWithCount(1));
        }
    }

    public static RecipeEntry baseEntry(ResourceLocation id, ResourceLocation type) {
        return new RecipeEntry()
                .setEnabled(true)
                .setOperation(RecipeOperation.REPLACE)
                .setRecipeId(id)
                .setType(type);
    }

    public static ItemStack copyResult(Recipe<?> recipe, HolderLookup.Provider provider) {
        return copyStack(recipe.getResultItem((RegistryAccess) provider));
    }

    public static ItemStack copyStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private static List<CraftingRemainderRule> defaultRemainders(int count) {
        var remainders = new ArrayList<CraftingRemainderRule>();
        for (int i = 0; i < count; i++) {
            remainders.add(CraftingRemainderRule.defaultRule());
        }
        return remainders;
    }

    public static String ingredientKey(RecipeIngredient ingredient) {
        if (ingredient.isEmpty()) return "empty";
        return switch (ingredient.getKind()) {
            case ITEM -> "item:" + hashItemStack(ingredient.getItem());
            case TAG -> "tag:" + ingredient.getTag();
            case ITEM_ABILITY -> "item_ability:" + ingredient.getItemAbility();
        };
    }

    private static int hashItemStack(ItemStack stack) {
        int i = stack.getItem().hashCode();
        i ^= stack.getCount();
        return stack.getTag() == null ? i : i ^ stack.getTag().hashCode();
    }

    private static String recipeTypeName(Recipe<?> recipe) {
        var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        var serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
        if (typeId != null) {
            return typeId + " / " + serializerId;
        }
        return String.valueOf(serializerId);
    }

    public record ImportedShapedPattern(List<String> pattern, List<ShapedKeyEntry> key) {
    }
}
