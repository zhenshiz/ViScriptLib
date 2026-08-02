package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.CraftingRemainderMode;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import com.viscript_recipe.data.vanilla.ShapedCraftingRecipeData;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ShapedCraftingCanvas extends RecipeCanvas<ShapedCraftingRecipeData> {
    public static CraftingRemainderRule[] visualRemainders;

    public ShapedCraftingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void initVisualState() {
        super.initVisualState();
        visualRemainders = defaultedArrays(new CraftingRemainderRule[9], CraftingRemainderRule.defaultRule());
    }

    @Override
    public void load() {
        var data = getData();
        var key = new LinkedHashMap<Character, ItemStack>();
        for (var entry : data.getKey()) {
            var symbol = entry.getSymbol();
            if (symbol == null || symbol.length() != 1) {
                continue;
            }
            if (containsUnsupportedIngredientValue(entry.getIngredient())) selectedContainsUnsupportedIngredients = true;
            key.put(symbol.charAt(0), entry.getIngredient().toStack());
        }
        for (int row = 0; row < Math.min(3, data.getPattern().size()); row++) {
            var line = data.getPattern().get(row);
            for (int col = 0; col < Math.min(3, line.length()); col++) {
                var symbol = line.charAt(col);
                var stack = key.getOrDefault(symbol, ItemStack.EMPTY);
                if (line.charAt(col) != ' ' && stack.isEmpty()) selectedContainsUnsupportedIngredients = true;
                setVisualIngredient(row * 3 + col, ingredientForSymbol(data.getKey(), symbol));
                setVisualRemainder(row * 3 + col, remainderForPatternSlot(row, col));
            }
        }
        setVisualOutput(0, data.getResult());
    }

    public static RecipeIngredient ingredientForSymbol(List<ShapedKeyEntry> key, char symbol) {
        if (symbol == ' ') return RecipeIngredient.empty();
        for (var entry : key) {
            if (entry.getSymbol().length() == 1 && entry.getSymbol().charAt(0) == symbol) {
                return entry.getIngredient();
            }
        }
        return RecipeIngredient.empty();
    }

    private CraftingRemainderRule remainderForPatternSlot(int row, int col) {
        var shaped = getData();
        var width = shaped.getPattern().isEmpty() ? 0 : shaped.getPattern().get(0).length();
        var remainderIndex = row * width + col;
        if (remainderIndex < 0 || remainderIndex >= shaped.getRemainders().size()) {
            return CraftingRemainderRule.defaultRule();
        }
        var remainder = shaped.getRemainders().get(remainderIndex);
        return remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
    }

    @Override
    public void save() {
        var data = getData();
        var itemSymbols = new LinkedHashMap<String, Character>();
        var keyEntries = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var remainders = new ArrayList<CraftingRemainderRule>();
        var symbolIndex = 0;
        for (int row = 0; row < 3; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                var slot = row * 3 + col;
                var ingredient = getVisualIngredient(slot);
                if (ingredient.isEmpty()) {
                    builder.append(' ');
                    remainders.add(CraftingRemainderRule.defaultRule());
                    continue;
                }
                var ingredientKey = RecipeImporter.ingredientKey(ingredient);
                var symbol = itemSymbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) continue;
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    itemSymbols.put(ingredientKey, symbol);
                    keyEntries.add(ShapedKeyEntry.of(String.valueOf(symbol), ingredient));
                }
                builder.append(symbol);
                remainders.add(getVisualRemainder(slot));
            }
            pattern.add(builder.toString());
        }
        data.setResult(getVisualOutput(0).getItem());
        if (keyEntries.isEmpty()) {
            data.setPattern(new ArrayList<>());
            data.setKey(new ArrayList<>());
            data.setRemainders(new ArrayList<>());
            return;
        }
        data.setPattern(pattern);
        data.setKey(keyEntries);
        data.setRemainders(remainders);
    }

    @Override
    public UIElement createCanvas() {
        return BasicRecipeCanvasFactory.createCraftingCanvas(createGrid(), createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }

    private UIElement createGrid() {
        return RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE, (index, row, col) ->
                createIngredientSlot(index, SLOT_SIZE));
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        try {
            ingredient = normalizeIngredient(ingredient.copy());
            visualIngredientData[index] = ingredient;
            setVisualRemainder(index, CraftingRemainderRule.defaultRule());
            if (matches(visualIngredientSlots[index].getValue(), ingredient.toStack())) return;
            visualIngredientSlots[index].setIngredient(ingredient);
        } catch (Exception ignored) {
        }
    }

    public static CraftingRemainderRule getVisualRemainder(int index) {
        try {
            return visualRemainders[index].copy();
        } catch (Exception e) {
            return CraftingRemainderRule.defaultRule();
        }
    }

    public static void setVisualRemainder(int index, CraftingRemainderRule rule) {
        try {
            visualRemainders[index] = rule;
        } catch (Exception ignored) {
        }
    }

    public void buildRemainderProperties(UIElement content) {
        var remainder = getVisualRemainder(selectedSlotIndex());
        var mode = remainder.getMode();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.remainder"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.remainder.mode",
                        RecipeEditorUi.selector(
                                List.of(CraftingRemainderMode.values()), mode,
                                CraftingRemainderMode::displayName, value -> {
                                    var updated = remainder.copy().setMode(value);
                                    if (value != CraftingRemainderMode.REPLACE) updated.setItem(ItemStack.EMPTY);
                                    setVisualRemainder(selectedSlotIndex(), updated);
                                    navigationView.refreshPropertiesView();
                                }
                        ),
                        Component.translatable("viscript_recipe.editor.remainder.tip.default"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.consume"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.replace"))
        );
        if (mode == CraftingRemainderMode.REPLACE) {
            content.addChild(PropertiesView.createItemStackConfigurator(
                    "viscript_recipe.config.remainder.item",
                    () -> remainder.getItem() == null ? ItemStack.EMPTY : remainder.getItem().copy(),
                    stack -> {
                        var updated = remainder.copy().setMode(CraftingRemainderMode.REPLACE).setItem(stack);
                        setVisualRemainder(selectedSlotIndex(), updated);
                    }
            ));
        }
    }
}
