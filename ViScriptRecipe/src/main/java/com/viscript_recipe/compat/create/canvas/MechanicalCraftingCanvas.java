package com.viscript_recipe.compat.create.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.RecipeHelper;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData.maxSize;

public class MechanicalCraftingCanvas extends RecipeCanvas<CreateMechanicalCraftingRecipeData> {
    private static final int MECHANICAL_CRAFTING_SLOT_SIZE = 18;
    static final boolean useJeiCanvas = CreateMechanicalCraftingCanvasFactory.hasJeiSkin();
    static final Label ingredientCountLabel = emptyLabel();
    static final UIElement workstationIcon =
            createItemIcon(RecipeHelper.registryItem("create:mechanical_crafter", Items.CRAFTING_TABLE), 96);

    public MechanicalCraftingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        var width = data.getWidth();
        var height = data.getHeight();
        ShapedGridHelper.loadGrid(this, data.getPattern(), data.getKey(), width, height, maxSize());
        setVisualOutput(0, data.getResult());
        updateIngredientCountLabel();
    }

    @Override
    public void save() {
        var data = getData();
        var width = data.getWidth();
        var height = data.getHeight();
        var patterns = ShapedGridHelper.saveGrid(this, width, height, maxSize());
        data.setPattern(patterns.pattern()).setKey(patterns.key());
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.create.mechanical_crafting"),
                intField("viscript_recipe.config.create.mechanical_crafting.width",
                        data.getWidth(), 1, maxSize(), data::setWidth, RecipeCanvas::reloadCanvas),
                intField("viscript_recipe.config.create.mechanical_crafting.height",
                        data.getHeight(), 1, maxSize(), data::setHeight, RecipeCanvas::reloadCanvas),
                switchField("viscript_recipe.config.create.mechanical_crafting.accept_mirrored",
                        data.isAcceptMirrored(), data::setAcceptMirrored)
        );
    }

    @Override
    public UIElement createCanvas() {
        var outputSlot = useJeiCanvas ? null : createOutputSlot(0, OUTPUT_SLOT_SIZE);
        var jeiOutputSlot = useJeiCanvas ? createOutputSlot(0, 18) : null;
        if (jeiOutputSlot != null) configureJeiOverlaySlotVisual(jeiOutputSlot);
        return CreateMechanicalCraftingCanvasFactory.createCanvas(
                createMechanicalCraftingGrid(),
                workstationIcon, ingredientCountLabel,
                outputSlot, jeiOutputSlot
        );
    }

    void updateIngredientCountLabel() {
        int ingredientCount = 0;
        for (var slot : visualIngredientSlots) {
            if (slot != null && !slot.getIngredient().isEmpty()) ingredientCount++;
        }
        ingredientCountLabel.setText(Component.literal(Integer.toString(ingredientCount)));
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        super.setVisualIngredient(index, ingredient);
        updateIngredientCountLabel();
    }

    private UIElement createMechanicalCraftingGrid() {
        var data = getData();
        int width = data.getWidth(); int height = data.getHeight();
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
                var index = row * maxSize() + col;
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
        var normalized = Clamp.clamp(slots, 1, maxSize());
        return MECHANICAL_CRAFTING_SLOT_SIZE * normalized + Math.max(0, normalized - 1) * mechanicalCraftingGridGap();
    }

    static int mechanicalCraftingGridPadding() {return useJeiCanvas ? 0 : 4;}

    static int mechanicalCraftingGridGap() {return useJeiCanvas ? 1 : 2;}
}
