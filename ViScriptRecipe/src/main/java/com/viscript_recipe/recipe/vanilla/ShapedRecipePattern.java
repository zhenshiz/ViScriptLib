package com.viscript_recipe.recipe.vanilla;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public final class ShapedRecipePattern {
    @Getter
    static int maxWidth = 3;
    @Getter
    static int maxHeight = 3;

    /**
     * Expand the max width and height allowed in the deserializer.
     * This should be called by modders who add custom crafting tables that are larger than the vanilla 3x3.
     * @param width your max recipe width
     * @param height your max recipe height
     */
    public static void setCraftingSize(int width, int height) {
        if (maxWidth < width) maxWidth = width;
        if (maxHeight < height) maxHeight = height;
    }
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final int ingredientCount;

    public ShapedRecipePattern(int width, int height, NonNullList<Ingredient> ingredients) {
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        int i = 0;
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) i++;
        }
        this.ingredientCount = i;
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> key, String... pattern) {
        return of(key, List.of(pattern));
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> key, List<String> pattern) {
        Data shapedrecipepattern$data = new Data(key, pattern);
        return unpack(shapedrecipepattern$data).getOrThrow(false, s -> {});
    }

    private static DataResult<ShapedRecipePattern> unpack(Data data) {
        String[] astring = shrink(data.pattern);
        int i = astring[0].length();
        int j = astring.length;
        NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);
        CharSet charset = new CharArraySet(data.key.keySet());

        for (int k = 0; k < astring.length; k++) {
            String s = astring[k];

            for (int l = 0; l < s.length(); l++) {
                char c0 = s.charAt(l);
                Ingredient ingredient = c0 == ' ' ? Ingredient.EMPTY : data.key.get(c0);
                if (ingredient == null) {
                    return DataResult.error(() -> "Pattern references symbol '" + c0 + "' but it's not defined in the key");
                }

                charset.remove(c0);
                nonnulllist.set(l + i * k, ingredient);
            }
        }
        return !charset.isEmpty()
            ? DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + charset)
            : DataResult.success(new ShapedRecipePattern(i, j, nonnulllist));
    }

    @VisibleForTesting
    static String[] shrink(List<String> pattern) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for (int i1 = 0; i1 < pattern.size(); i1++) {
            String s = pattern.get(i1);
            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);
            j = Math.max(j, j1);
            if (j1 < 0) {
                if (k == i1) {
                    k++;
                }

                l++;
            } else {
                l = 0;
            }
        }

        if (pattern.size() == l) {
            return new String[0];
        } else {
            String[] astring = new String[pattern.size() - l - k];
            for (int k1 = 0; k1 < astring.length; k1++) {
                astring[k1] = pattern.get(k1 + k).substring(i, j + 1);
            }
            return astring;
        }
    }

    private static int firstNonSpace(String row) {
        int i = 0;
        while (i < row.length() && row.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    private static int lastNonSpace(String row) {
        int i = row.length() - 1;
        while (i >= 0 && row.charAt(i) == ' ') {
            i--;
        }
        return i;
    }

    public boolean matches(CraftingContainer input) {
        if (input.getItems().stream().filter(s -> !s.isEmpty()).toArray().length == this.ingredientCount) {
            if (input.getWidth() == this.width && input.getHeight() == this.height) {
                if (matches(input, true)) {
                    return true;
                }
                return this.matches(input, false);
            }
        }
        return false;
    }

    private boolean matches(CraftingContainer input, boolean symmetrical) {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                Ingredient ingredient;
                if (symmetrical) {
                    ingredient = this.ingredients.get(this.width - j - 1 + i * this.width);
                } else {
                    ingredient = this.ingredients.get(j + i * this.width);
                }

                ItemStack itemstack = input.getItem(j * width + i);
                if (!ingredient.test(itemstack)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public NonNullList<Ingredient> ingredients() {
        return this.ingredients;
    }

    public record Data(Map<Character, Ingredient> key, List<String> pattern) {
    }
}
