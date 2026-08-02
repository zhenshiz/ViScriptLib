package com.viscript_recipe.gui.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.create.CreateProcessingKind;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.gui.canvas.vanilla.ShapedCraftingCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.viscript_recipe.gui.views.PropertiesView.*;

@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
public abstract class RecipeCanvas<D extends IVSRecipeData> extends UIElement {
    public static final char[] SHAPED_SYMBOLS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>/?|~".toCharArray();
    protected static NavigationView navigationView;
    protected static RecipeEntry entry;

    protected static final Map<IngredientDisplaySlot, Integer> ingredientDragSlotIndices = new IdentityHashMap<>();
    public static final int SLOT_SIZE = 24;
    public static final int JEI_SLOT_SIZE = 18;
    public static final int OUTPUT_SLOT_SIZE = 30;
    public static RecipeIngredient[] visualIngredientData;
    public static RecipeOutputData[] visualOutputs;
    public static boolean selectedContainsUnsupportedIngredients;

    public static final IngredientDisplaySlot[] visualIngredientSlots = new IngredientDisplaySlot[81];
    public static final ItemSlot[] visualOutputSlots = new ItemSlot[15];
    // 额外的槽位，用于显示如机械动力序列装配的中间产物和农夫乐事的容器等物品
    public static final ItemSlot[] extraItemSlots = new ItemSlot[1];

    public RecipeCanvas(NavigationView navigationView, RecipeEntry entry) {
        RecipeCanvas.navigationView = navigationView;
        RecipeCanvas.entry = entry;
    }

    public D getData() {return entry.getData();}

    public abstract void load();

    public abstract void save();

    public abstract UIElement createCanvas();

    public void initVisualState() {
        addChildren(createCanvas());
        visualIngredientData = defaultedArrays(new RecipeIngredient[81], RecipeIngredient.empty());
        visualOutputs = defaultedArrays(new RecipeOutputData[15], RecipeOutputData.empty());
    }

    public static <T> T[] defaultedArrays(T[] array, T defaultValue) {
        Arrays.fill(array, defaultValue);
        return array;
    }

    public void buildRecipeProperties(UIElement content) {}

    public void buildIngredientProperties(UIElement content) {
        var ingredient = getSelectedIngredient();
        var availableKinds = availableIngredientKind();
        var selectedKind = availableKinds.contains(ingredient.getKind()) ? ingredient.getKind() : availableKinds.get(0);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ingredient"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ingredient.value.kind",
                        RecipeEditorUi.selector(
                                availableKinds, selectedKind,
                                IngredientValueKind::displayName,
                                kind -> setIngredientKind(ingredient, kind)
                        ))
        );
        switch (selectedKind) {
            case ITEM -> {
                if (entry.isType(CreateProcessingKind.ITEM_APPLICATION.typeId()) && selectedSlotIndex() == 0) {
                    content.addChild(createBlockConfigurator(
                            "viscript_recipe.editor.create.item_application.base_block",
                            () -> ingredientBlock(ingredient),
                            block -> {
                                if (block.asItem().getDefaultInstance().isEmpty()) return;
                                setSelectedIngredient(RecipeIngredient.item(new ItemStack(block)));
                            }
                    ));
                } else content.addChild(createItemStackConfigurator(
                        "viscript_recipe.editor.ingredient.item_slot",
                        ingredient::toStack,
                        stack -> setSelectedIngredient(RecipeIngredient.item(stack))
                ));
            }
            case TAG -> content.addChild(createItemTagConfigurator(ingredient.getTag(),
                    tag -> setSelectedIngredient(RecipeIngredient.tag(tag.location()))));
            case ITEM_ABILITY -> content.addChild(createItemAbilityConfigurator(ingredient.getItemAbility(),
                    s -> setSelectedIngredient(RecipeIngredient.itemAbility(s))));
        }
        if (this instanceof ShapedCraftingCanvas canvas) canvas.buildRemainderProperties(content);
    }

    public void buildFluidProperties(UIElement content) {}

    public void buildExtraItemProperties(UIElement content) {}

    private List<IngredientValueKind> availableIngredientKind() {
        return entry.isType(FarmersDelightRecipeEditorTypes.CUTTING) && navigationView.getSlotSelection().index() == 1
                ? List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG, IngredientValueKind.ITEM_ABILITY)
                : List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG);
    }

    private void setIngredientKind(RecipeIngredient ingredient, IngredientValueKind kind) {
        ingredient.setKind(kind);
        setSelectedIngredient(ingredient);
        navigationView.refreshPropertiesView();
    }

    public void buildResultProperties(UIElement content) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.result"),
                createItemStackConfigurator(
                        "viscript_recipe.config.recipe.result",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput
                )
        );
    }

    protected static boolean containsUnsupportedIngredientValue(RecipeIngredient ingredient) {
        return switch (ingredient.getKind()) {
            case ITEM -> ingredient.getItem() == null;
            case TAG -> itemsFromTag(ingredient.getTag()).length == 0;
            case ITEM_ABILITY -> ingredient.getItemAbility().isBlank();
        };
    }

    public static ItemStack[] itemsFromTag(ResourceLocation tag) {
        if (tag == null) return new ItemStack[0];
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag))
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new))
                .orElseGet(() -> new ItemStack[0]);
    }

    public static FluidStack[] fluidsFromTag(ResourceLocation tag, int amount) {
        if (tag == null) return new FluidStack[0];
        return BuiltInRegistries.FLUID.getTag(TagKey.create(Registries.FLUID, tag))
                .map(holders -> displayFluidsFromHolders(holders.stream()
                        .map(Holder::value)
                        .toList(), Math.max(1, amount)))
                .orElseGet(() -> new FluidStack[0]);
    }

    private static FluidStack[] displayFluidsFromHolders(List<Fluid> fluids, int amount) {
        var sourceFluids = fluids.stream().filter(fluid -> fluid.defaultFluidState().isSource()).toList();
        var displayFluids = sourceFluids.isEmpty() ? fluids : sourceFluids;
        return displayFluids.stream()
                .map(fluid -> new FluidStack(fluid, amount))
                .filter(stack -> !stack.isEmpty())
                .toArray(FluidStack[]::new);
    }

    public static ItemStack itemFromAbility(String itemAbility) {
        return switch (itemAbility) {
            case "axe_dig", "axe_strip" -> new ItemStack(Items.IRON_AXE);
            case "shovel_dig" -> new ItemStack(Items.IRON_SHOVEL);
            case "pickaxe_dig" -> new ItemStack(Items.IRON_PICKAXE);
            case "sword_dig" -> new ItemStack(Items.IRON_SWORD);
            case "shears_dig" -> new ItemStack(Items.SHEARS);
            default -> new ItemStack(itemFromRegistry("farmersdelight:iron_knife", Items.IRON_SWORD));
        };
    }

    public static Item itemFromRegistry(String id, Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) return fallback;
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }

    @Deprecated
    public ItemSlot createEditorSlot(int size) {
        return enableShiftDragCopy((ItemSlot) new ItemSlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                }));
    }

    @Deprecated
    public ItemSlot configureResultSlot(int index) {
        var slot = visualOutputSlots[index];
        slot.registerValueListener(stack -> setVisualOutput(index, stack));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualOutput(index, RecipeOutputData.empty());
                event.stopPropagation();
            }
            navigationView.setSlotSelection(SlotSelection.result(index));
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.result_slot")));
        return slot;
    }

    /**新建一个输出槽位，并且绑定到visualOutputSlots[index]上*/
    public ItemSlot createOutputSlot(int index, int size) {
        visualOutputSlots[index] = createEditorSlot(size);
        return configureResultSlot(index);
    }

    /**新建一个原料槽位，并且绑定到visualIngredientSlots[index]上*/
    public IngredientDisplaySlot createIngredientSlot(int index, int size) {
        visualIngredientSlots[index] = createIngredientSlot(size);
        return configureIngredientSlot(index);
    }

    @Deprecated
    public IngredientDisplaySlot createIngredientSlot(int size) {
        return enableShiftDragCopy((IngredientDisplaySlot) new IngredientDisplaySlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                }));
    }

    @Deprecated
    public IngredientDisplaySlot configureIngredientSlot(int index) {
        var slot = visualIngredientSlots[index];
        ingredientDragSlotIndices.put(slot, index);
        slot.registerValueListener(stack -> setVisualIngredient(index, RecipeIngredient.item(stack)));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualIngredient(index, RecipeIngredient.empty());
                event.stopPropagation();
            }
            navigationView.setSlotSelection(SlotSelection.ingredient(index));
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ingredient_slot", index + 1
        )));
        return slot;
    }

    public static void removeUIFirstEvent(UIElement element, String type, boolean useCapture) {
        var listeners = useCapture ? element.getCaptureListeners(type) : element.getBubbleListeners(type);
        if (listeners.isEmpty()) return;
        element.removeEventListener(type, listeners.get(0), useCapture);
    }

    public ItemSlot configureExtraItemSlot(ItemSlot slot, Component tip) {
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                extraItemSlots[0].setValue(ItemStack.EMPTY);
                event.stopPropagation();
            }
            navigationView.setSlotSelection(SlotSelection.EXTRA_ITEM);
        });
        slot.style(style -> style.tooltips(tip));
        return slot;
    }

    public static void configureJeiOverlaySlotVisual(ItemSlot slot) {
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
    }

    private <T extends ItemSlot> T enableShiftDragCopy(T slot) {
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button != 0 || !Screen.hasShiftDown()) {
                return;
            }
            var stack = slot.getValue();
            if (stack.isEmpty()) return;

            var draggedStack = stack.copy();
            RecipeIngredient draggedIngredient = null;
            if (slot instanceof IngredientDisplaySlot ingredientSlot) {
                var ingredientIndex = ingredientDragSlotIndices.get(ingredientSlot);
                if (ingredientIndex != null) draggedIngredient = getVisualIngredient(ingredientIndex);
            }
            int dragPreviewSize = 18;
            var dragHandler = slot.startDrag(
                    new ItemSlotDragPayload(slot, draggedStack, draggedIngredient),
                    new ItemStackTexture(draggedStack)
            );
            dragHandler.setDragTexture(
                    -dragPreviewSize / 2f, -dragPreviewSize / 2f,
                    dragPreviewSize, dragPreviewSize
            );
            event.stopImmediatePropagation();
        });
        slot.addEventListener(UIEvents.DRAG_PERFORM, event -> {
            var draggingObject = event.dragHandler == null ? null : event.dragHandler.getDraggingObject();
            if (!(draggingObject instanceof ItemSlotDragPayload payload)) {
                return;
            }
            if (payload.source() == slot || payload.stack().isEmpty()) {
                event.stopPropagation();
                return;
            }

            if (slot instanceof IngredientDisplaySlot ingredientSlot && payload.ingredient() != null) {
                var ingredientIndex = ingredientDragSlotIndices.get(ingredientSlot);
                if (ingredientIndex != null) {
                    setVisualIngredient(ingredientIndex, payload.ingredient());
                    event.stopPropagation();
                    return;
                }
            }

            var copiedStack = payload.stack().copy();
            if (!slot.getSlot().mayPlace(copiedStack)) {
                event.stopPropagation();
                return;
            }
            if (slot instanceof IngredientDisplaySlot ingredientSlot) {
                ingredientSlot.clearTagDisplayStacks();
            }
            slot.setItem(copiedStack, true);
            event.stopPropagation();
        });
        return slot;
    }

    private record ItemSlotDragPayload(ItemSlot source, ItemStack stack, RecipeIngredient ingredient) {
    }

    public static int selectedSlotIndex() {return navigationView.getSlotSelection().index();}

    public void loadIngredientSlot(int index, RecipeIngredient ingredient) {
        if (containsUnsupportedIngredientValue(ingredient)) selectedContainsUnsupportedIngredients = true;
        setVisualIngredient(index, ingredient);
    }

    public RecipeIngredient getSelectedIngredient() {return getVisualIngredient(selectedSlotIndex());}
    public RecipeIngredient getVisualIngredient(int index) {
        try {
            return visualIngredientData[index].copy();
        } catch (Exception e) {
            return RecipeIngredient.empty();
        }
    }

    /**不需要自己copy ingredient*/
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        try {
            ingredient = normalizeIngredient(ingredient.copy());
            visualIngredientData[index] = ingredient;
            if (matches(visualIngredientSlots[index].getValue(), ingredient.toStack())) return;
            visualIngredientSlots[index].setIngredient(ingredient);
        } catch (Exception ignored) {
        }
    }
    public void setSelectedIngredient(RecipeIngredient ingredient) {
        setVisualIngredient(selectedSlotIndex(), ingredient);
    }

    public RecipeOutputData getSelectedOutput() {return getVisualOutput(selectedSlotIndex());}
    public RecipeOutputData getVisualOutput(int index) {
        try {
            return visualOutputs[index].copy();
        } catch (Exception e) {
            return RecipeOutputData.empty();
        }
    }

    /**不需要自己copy output*/
    public void setVisualOutput(int index, RecipeOutputData output) {
        try {
            visualOutputs[index] = output.copy();
            if (matches(visualOutputSlots[index].getValue(), output.getItem())) return;
            visualOutputSlots[index].setItem(output.getItem(), false);
        } catch (Exception ignored) {
        }
    }
    public void setVisualOutput(int index, ItemStack item) {
        setVisualOutput(index, getVisualOutput(index).setItem(item));
    }
    public void setVisualOutput(int index, float chance) {
        setVisualOutput(index, getVisualOutput(index).setChance(chance));
    }
    public void setVisualOutput(int index, ItemStack item, float chance) {
        setVisualOutput(index, RecipeOutputData.of(item, chance));
    }
    public void setSelectedOutput(ItemStack item) {setVisualOutput(selectedSlotIndex(), item);}
    public void setSelectedOutput(float chance) {setVisualOutput(selectedSlotIndex(), chance);}

    public ItemStack getExtraItem(int index) {
        try {
            return extraItemSlots[index].getValue().copy();
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }
    public ItemStack getExtraItem() {return getExtraItem(0);}

    public void setExtraItem(int index, ItemStack item) {
        try {
            extraItemSlots[index].setItem(item.copyWithCount(1), true);
        } catch (Exception ignored) {
        }
    }
    public void setExtraItem(ItemStack item) {setExtraItem(0, item);}

    public static boolean matches(ItemStack s1, ItemStack s2) {
        if (s1 == null || s2 == null) return false;
        return ItemStack.matches(s1, s2);
    }

    public static RecipeIngredient normalizeIngredient(RecipeIngredient ingredient) {
        // todo 带数量的原料
        if (supportsCountedItemInputs(getCreateProcessingKind())) return ingredient;
        ingredient.setItem(ingredient.getItem().copyWithCount(1));
        return ingredient;
    }

    public static UIElement createItemIcon(ItemStack stack, int size) {
        return new UIElement().layout(layout -> {
            layout.width(size);
            layout.height(size);
        }).style(style -> style.backgroundTexture(new ItemStackTexture(stack.copyWithCount(1))));
    }

    public static UIElement createDownArrowElement(int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR));
    }

    protected static CreateProcessingKind getCreateProcessingKind() {
        return CreateProcessingKind.byType(entry.getType()).orElse(null);
    }

    protected static boolean supportsCountedItemInputs(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.MIXING
                || kind == CreateProcessingKind.COMPACTING
                || kind == CreateProcessingKind.AUTOMATIC_SHAPELESS;
    }
}