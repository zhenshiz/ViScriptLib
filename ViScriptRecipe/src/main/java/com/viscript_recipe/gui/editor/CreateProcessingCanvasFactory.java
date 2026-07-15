package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class CreateProcessingCanvasFactory {
    private CreateProcessingCanvasFactory() {
    }

    private static UIElement responsiveRow(int gap) {
        return RecipeEditorUi.row().layout(layout -> configureResponsiveRow(layout, gap));
    }

    private static void configureResponsiveRow(LayoutStyle layout, int gap) {
        layout.widthPercent(100);
        layout.flex(1);
        layout.gapAll(gap);
        layout.flexWrap(FlexWrap.WRAP);
        layout.alignItems(AlignItems.CENTER);
        layout.alignContent(AlignContent.CENTER);
        layout.justifyContent(AlignContent.CENTER);
    }

    private static void boundedWidth(LayoutStyle layout, int maxWidth) {
        layout.widthPercent(100);
        layout.maxWidth(maxWidth);
        layout.minWidth(0);
    }

    static UIElement createProcessingStack(UIElement... canvases) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        }).addChildren(canvases);
    }

    static UIElement createGenericProcessingCanvas(UIElement inputSide, UIElement machineColumn, UIElement outputSide) {
        return responsiveRow(14).addChildren(inputSide, machineColumn, arrowElement(), outputSide);
    }

    static UIElement createInputSide(UIElement itemGrid, UIElement fluidInputs) {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 128);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.item_inputs")),
                itemGrid,
                fluidInputs
        );
    }

    static UIElement createMachineColumn(UIElement machineIcon, Label machineLabel) {
        machineIcon.layout(layout -> {
            layout.width(36);
            layout.height(36);
        });
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 92);
            layout.height(80);
            layout.paddingAll(6);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChildren(machineIcon, machineLabel);
    }

    static UIElement createOutputSide(UIElement itemGrid, UIElement fluidOutputs) {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 156);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.outputs")),
                itemGrid,
                fluidOutputs
        );
    }

    static UIElement createSpoutCanvas(UIElement fluidInputSlot, UIElement ingredientSlot, UIElement outputSlot) {
        return responsiveRow(18).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 60);
                    layout.height(128);
                    layout.gapAll(8);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        framedSlot(fluidInputSlot, 42),
                        framedSlot(ingredientSlot, 42)
                ),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 132);
                    layout.height(168);
                    layout.gapAll(5);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        itemIcon(new ItemStack(itemFromRegistry("create:spout", Items.CRAFTING_TABLE)), 78),
                        downArrow(18, 18),
                        itemIcon(new ItemStack(itemFromRegistry("create:depot", Items.CAULDRON)), 70)
                ),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 70);
                    layout.height(128);
                    layout.gapAll(8);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        downArrow(30, 36),
                        framedSlot(outputSlot, 44)
                )
        );
    }

    static UIElement createDrainCanvas(UIElement ingredientSlot, UIElement fluidOutputSlot, UIElement outputSlot) {
        return responsiveRow(20).addChildren(
                framedSlot(ingredientSlot, 46),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 132);
                    layout.height(130);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(itemIcon(new ItemStack(itemFromRegistry("create:item_drain", Items.CAULDRON)), 92)),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 56);
                    layout.height(112);
                    layout.gapAll(8);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        framedSlot(fluidOutputSlot, 46),
                        framedSlot(outputSlot, 46)
                )
        );
    }

    static FanCanvas createFanCanvas(UIElement ingredientSlot, UIElement catalystIcon, Label catalystLabel, UIElement singleOutputSlot, UIElement multiOutputGrid) {
        var singleOutputPanel = RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 82);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")),
                singleOutputSlot
        );
        var multiOutputPanel = RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 24 * 3 + 20);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.outputs")),
                multiOutputGrid
        );
        catalystIcon.layout(layout -> {
            layout.width(34);
            layout.height(34);
        });
        var machinePanel = RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 126);
            layout.height(106);
            layout.paddingAll(6);
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChildren(
                        RecipeEditorUi.row().layout(layout -> {
                            layout.widthPercent(100);
                            layout.height(56);
                            layout.gapAll(5);
                            layout.alignItems(AlignItems.CENTER);
                            layout.justifyContent(AlignContent.CENTER);
                        }).addChildren(
                                itemIcon(new ItemStack(itemFromRegistry("create:encased_fan", Items.CRAFTING_TABLE)), 52),
                                catalystIcon
                        ),
                        catalystLabel
                );
        var root = responsiveRow(14).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.item_inputs")),
                        ingredientSlot
                ),
                machinePanel,
                rightArrow(40, 20),
                singleOutputPanel,
                multiOutputPanel
        );
        return new FanCanvas(root, singleOutputPanel, multiOutputPanel);
    }

    static UIElement createCrushingCanvas(UIElement ingredientSlot, UIElement outputRow) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(4);
            layout.paddingAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 72);
                    layout.gapAll(2);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(ingredientSlot, downArrow(18, 24)),
                RecipeEditorUi.row().layout(layout -> {
                    boundedWidth(layout, 120);
                    layout.minHeight(58);
                    layout.gapAll(0);
                    layout.flexWrap(FlexWrap.WRAP);
                    layout.alignItems(AlignItems.CENTER);
                    layout.alignContent(AlignContent.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        itemIcon(new ItemStack(itemFromRegistry("create:crushing_wheel", Items.CRAFTING_TABLE)), 58),
                        itemIcon(new ItemStack(itemFromRegistry("create:crushing_wheel", Items.CRAFTING_TABLE)), 58)
                ),
                outputRow
        );
    }

    static UIElement createMillingCanvas(UIElement ingredientSlot, UIElement outputRow) {
        return responsiveRow(12).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 56);
                    layout.height(124);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(ingredientSlot),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 84);
                    layout.height(124);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(itemIcon(new ItemStack(itemFromRegistry("create:millstone", Items.CRAFTING_TABLE)), 76)),
                rightArrow(28, 20),
                outputRow
        );
    }

    static UIElement createSawCanvas(UIElement ingredientSlot, UIElement sawOutputGrid, UIElement blockCuttingOutputGrid) {
        return responsiveRow(8).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 52);
                    layout.height(112);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(ingredientSlot),
                chevronLabel(),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 92);
                    layout.height(112);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(itemIcon(new ItemStack(itemFromRegistry("create:mechanical_saw", Items.CRAFTING_TABLE)), 82)),
                chevronLabel(),
                sawOutputGrid,
                blockCuttingOutputGrid
        );
    }

    static UIElement createAutoPackingCanvas(UIElement inputGrid, UIElement outputSlot) {
        return responsiveRow(14).addChildren(
                inputGrid,
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 118);
                    layout.height(146);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(itemIcon(new ItemStack(itemFromRegistry("create:mechanical_press", Items.CRAFTING_TABLE)), 84)),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 72);
                    layout.height(72);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                        .addChild(outputSlot)
        );
    }

    static UIElement createSandpaperCanvas(UIElement ingredientSlot, UIElement outputSlot) {
        return responsiveRow(14).addChildren(
                ingredientSlot,
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 112);
                    layout.height(118);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(itemIcon(new ItemStack(itemFromRegistry("create:sand_paper", Items.PAPER)), 76)),
                rightArrow(28, 28),
                outputSlot
        );
    }

    static UIElement createPressingCanvas(UIElement ingredientSlot, UIElement outputRow) {
        return responsiveRow(12).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 56);
                    layout.height(132);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(ingredientSlot),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 118);
                    layout.height(146);
                    layout.gapAll(8);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(itemIcon(new ItemStack(itemFromRegistry("create:mechanical_press", Items.CRAFTING_TABLE)), 84)),
                outputRow
        );
    }

    static UIElement createPressBasinCanvas(UIElement inputSide, UIElement machineStack, UIElement outputSide, UIElement heatPanel) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.minHeight(164);
                    layout.gapAll(18);
                    layout.flexWrap(FlexWrap.WRAP);
                    layout.alignItems(AlignItems.CENTER);
                    layout.alignContent(AlignContent.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(inputSide, machineStack, outputSide),
                heatPanel
        );
    }

    static UIElement createAutomaticBrewingCanvas(UIElement ingredientSlot, UIElement fluidInputSlot, UIElement fluidOutputSlot, UIElement heatPanel) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.minHeight(164);
                    layout.gapAll(18);
                    layout.flexWrap(FlexWrap.WRAP);
                    layout.alignItems(AlignItems.CENTER);
                    layout.alignContent(AlignContent.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        createAutomaticBrewingInputSide(ingredientSlot, fluidInputSlot),
                        createAutomaticBrewingMachineStack(),
                        rightArrow(34, 20),
                        createAutomaticBrewingOutputSide(fluidOutputSlot)
                ),
                heatPanel
        );
    }

    private static UIElement createAutomaticBrewingInputSide(UIElement ingredientSlot, UIElement fluidInputSlot) {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 82);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                ingredientSlot,
                createFluidColumn("viscript_recipe.editor.create.fluid_input_short", fluidInputSlot)
        );
    }

    private static UIElement createAutomaticBrewingMachineStack() {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 118);
            layout.height(158);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                itemIcon(new ItemStack(itemFromRegistry("create:mechanical_mixer", Items.CRAFTING_TABLE)), 82),
                itemIcon(new ItemStack(itemFromRegistry("create:basin", Items.CAULDRON)), 64)
        );
    }

    private static UIElement createAutomaticBrewingOutputSide(UIElement fluidOutputSlot) {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 82);
            layout.gapAll(6);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(createFluidColumn("viscript_recipe.editor.create.fluid_output_short", fluidOutputSlot));
    }

    private static UIElement createFluidColumn(String labelKey, UIElement fluidSlot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(52);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey)),
                fluidSlot
        );
    }

    static UIElement createPressInputSide(UIElement itemInputs, UIElement fluidInputs) {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 120);
            layout.gapAll(7);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(itemInputs, fluidInputs);
    }

    static UIElement createPressMachineStack(UIElement machineIcon) {
        machineIcon.layout(layout -> {
            layout.width(82);
            layout.height(82);
        });
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 118);
            layout.height(158);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                machineIcon,
                itemIcon(new ItemStack(itemFromRegistry("create:basin", Items.CAULDRON)), 64)
        );
    }

    static UIElement createPressOutputSide(UIElement itemOutputs, UIElement fluidOutputs) {
        return RecipeEditorUi.column().layout(layout -> {
            boundedWidth(layout, 118);
            layout.gapAll(7);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                downArrow(24, 28),
                itemOutputs,
                fluidOutputs
        );
    }

    static UIElement createPressHeatPanel(UIElement heatLabel) {
        return new UIElement().layout(layout -> {
            boundedWidth(layout, 340);
            layout.minHeight(34);
            layout.paddingAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChild(heatLabel);
    }

    static UIElement createDeployerCanvas(UIElement heldSlot, UIElement processedSlot, UIElement outputGrid) {
        return responsiveRow(12).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 56);
                    layout.height(132);
                    layout.gapAll(10);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        framedSlot(heldSlot, 46),
                        framedSlot(processedSlot, 46)
                ),
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 112);
                    layout.height(154);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(
                        itemIcon(new ItemStack(itemFromRegistry("create:deployer", Items.CRAFTING_TABLE)), 76),
                        downArrow(18, 20),
                        itemIcon(new ItemStack(itemFromRegistry("create:depot", Items.CAULDRON)), 64)
                ),
                rightArrow(40, 20),
                outputGrid
        );
    }

    static UIElement createManualApplicationCanvas(UIElement blockSlot, UIElement processStack, UIElement outputGrid) {
        return responsiveRow(12).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    boundedWidth(layout, 60);
                    layout.height(132);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(framedSlot(blockSlot, 54)),
                processStack,
                rightArrow(28, 28),
                outputGrid
        );
    }

    static UIElement framedSlot(UIElement slot, int size) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.width(size);
            layout.height(size);
            layout.paddingAll(Math.max(0, (size - 30) / 2));
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChild(slot);
    }

    private static UIElement itemIcon(ItemStack stack, int size) {
        return new UIElement().layout(layout -> {
            layout.width(size);
            layout.height(size);
        }).style(style -> style.backgroundTexture(new ItemStackTexture(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1))));
    }

    private static UIElement rightArrow(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    private static UIElement downArrow(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR));
    }

    private static UIElement arrowElement() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(16);
        }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }

    private static Label chevronLabel() {
        Label label = RecipeEditorUi.label(Component.literal(">"));
        label.textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color));
        label.layout(layout -> layout.width(18).height(24));
        return label;
    }

    private static Item itemFromRegistry(String id, Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return fallback;
        }
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }

    record FanCanvas(UIElement root, UIElement singleOutputPanel, UIElement multiOutputPanel) {
    }
}
