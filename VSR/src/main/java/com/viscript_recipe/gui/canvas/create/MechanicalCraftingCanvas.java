package com.viscript_recipe.gui.canvas.create;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.create.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MechanicalCraftingCanvas extends RecipeCanvas<CreateMechanicalCraftingRecipeData> {
    private static final int MECHANICAL_CRAFTING_GRID_SIZE = 9;
    private static final int MECHANICAL_CRAFTING_SLOT_SIZE = 18;
    static final boolean useJeiMechanicalCraftingCanvas = CreateMechanicalCraftingCanvasFactory.hasJeiSkin();
    static final Label ingredientCountLabel = RecipeEditorUi.label(Component.empty());
    static final UIElement workstationIcon = createItemIcon(ItemStack.EMPTY, 96);

    public MechanicalCraftingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        var key = new LinkedHashMap<Character, RecipeIngredient>();
        for (var entry : data.getKey()) {
            var symbol = entry.getSymbol();
            if (symbol == null || symbol.length() != 1) {
                continue;
            }
            if (containsUnsupportedIngredientValue(entry.getIngredient())) selectedContainsUnsupportedIngredients = true;
            key.put(symbol.charAt(0), entry.getIngredient());
        }
        var height = data.normalizedHeight();
        var width = data.normalizedWidth();
        for (int row = 0; row < Math.min(height, data.getPattern().size()); row++) {
            var line = data.getPattern().get(row);
            if (line == null) {
                continue;
            }
            for (int col = 0; col < Math.min(width, line.length()); col++) {
                var symbol = line.charAt(col);
                var index = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var ingredient = symbol == ' ' ? RecipeIngredient.empty() : key.getOrDefault(symbol, RecipeIngredient.empty());
                if (symbol != ' ' && ingredient.isEmpty()) selectedContainsUnsupportedIngredients = true;
                setVisualIngredient(index, ingredient);
            }
        }
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        var itemSymbols = new LinkedHashMap<String, Character>();
        var keyEntries = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var symbolIndex = 0;
        var width = data.normalizedWidth();
        var height = data.normalizedHeight();
        for (int row = 0; row < height; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                var slot = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var ingredient = getVisualIngredient(slot);
                if (ingredient.isEmpty()) {
                    builder.append(' ');
                    continue;
                }
                var ingredientKey = RecipeImporter.ingredientKey(ingredient);
                var symbol = itemSymbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        builder.append(' ');
                        continue;
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    itemSymbols.put(ingredientKey, symbol);
                    keyEntries.add(ShapedKeyEntry.of(String.valueOf(symbol), ingredient));
                }
                builder.append(symbol);
            }
            pattern.add(builder.toString());
        }
        data.setResult(getVisualOutput(0).getItem());
        if (keyEntries.isEmpty()) {
            data.setPattern(new ArrayList<>());
            data.setKey(new ArrayList<>());
            return;
        }
        data.setPattern(pattern);
        data.setKey(keyEntries);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.mechanical_crafting"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.mechanical_crafting.width",
                        RecipeEditorUi.intField(data.normalizedWidth(), 1, 9, value -> {
                            data.setWidth(value);
                            navigationView.reloadCanvas();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.mechanical_crafting.height",
                        RecipeEditorUi.intField(data.normalizedHeight(), 1, 9, value -> {
                            data.setHeight(value);
                            navigationView.reloadCanvas();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.mechanical_crafting.accept_mirrored",
                        new Switch().setOn(data.isAcceptMirrored()).setOnSwitchChanged(data::setAcceptMirrored))
        );
    }

    @Override
    public UIElement createCanvas() {
        var outputSlot = useJeiMechanicalCraftingCanvas ? null : createOutputSlot(0, OUTPUT_SLOT_SIZE);
        var jeiOutputSlot = useJeiMechanicalCraftingCanvas ? createOutputSlot(0, 18) : null;
        if (jeiOutputSlot != null) configureJeiOverlaySlotVisual(jeiOutputSlot);
        return CreateMechanicalCraftingCanvasFactory.createCanvas(
                createMechanicalCraftingGrid(),
                workstationIcon,
                ingredientCountLabel,
                outputSlot, jeiOutputSlot
        );
    }

    private UIElement createMechanicalCraftingGrid() {
        var data = getData();
        int width = data.normalizedWidth(); int height = data.normalizedHeight();
        var mechanicalCraftingGrid = RecipeEditorUi.column().layout(layout -> {
            layout.width(mechanicalCraftingGridDimension(width));
            layout.height(mechanicalCraftingGridDimension(height));
            layout.paddingAll(mechanicalCraftingGridPadding());
            layout.gapAll(mechanicalCraftingGridGap());
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style
                .backgroundTexture(Sprites.BORDER_DARK)
                .tooltips(Component.translatable("viscript_recipe.editor.create.mechanical_crafting.input_grid")));

        for (int row = 0; row < height; row++) {
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(MECHANICAL_CRAFTING_SLOT_SIZE);
                layout.gapAll(mechanicalCraftingGridGap());
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            });
            for (int col = 0; col < width; col++) {
                var index = row * MECHANICAL_CRAFTING_GRID_SIZE + col;
                var slot = createIngredientSlot(index, MECHANICAL_CRAFTING_SLOT_SIZE);
                var cell = new UIElement().layout(layout -> {
                    layout.width(MECHANICAL_CRAFTING_SLOT_SIZE);
                    layout.height(MECHANICAL_CRAFTING_SLOT_SIZE);
                }).addChild(slot);
                rowElement.addChild(cell);
            }
            mechanicalCraftingGrid.addChild(rowElement);
        }
        return mechanicalCraftingGrid;
    }

    static int mechanicalCraftingGridDimension(int slots) {
        return mechanicalCraftingGridInnerDimension(slots) + mechanicalCraftingGridPadding() * 2;
    }

    static int mechanicalCraftingGridInnerDimension(int slots) {
        var normalized = Clamp.clamp(slots, 1, MECHANICAL_CRAFTING_GRID_SIZE);
        return MECHANICAL_CRAFTING_SLOT_SIZE * normalized + Math.max(0, normalized - 1) * mechanicalCraftingGridGap();
    }

    static int mechanicalCraftingGridPadding() {return useJeiMechanicalCraftingCanvas ? 0 : 4;}

    static int mechanicalCraftingGridGap() {return useJeiMechanicalCraftingCanvas ? 1 : 2;}
}
