package com.viscript_recipe.gui.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.create.data.CreateProcessingKind;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.vanilla.ShapedCraftingCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData.maxSize;
import static com.viscript_recipe.gui.views.PropertiesView.*;
import static com.viscript_recipe.recipe.RecipeHelper.itemsFromTag;

/**
 * 配方编辑器界面中间显示的画布 <p>
 * 主要作用：将原料和输出槽位显示在画布上，玩家编辑配方时，可见的物品等内容不会立即写入{@link #getData()}，以避免频繁地保存和加载。
 * 需要通过属性栏编辑的详细属性（如熔炉配方的烧制时间）会在编辑后立即写入配方数据。 <p>
 * 代码流程：当画布的实现类初始化时，会调用{@link #initVisualState()}方法构建所有画布的UI元素，随后调用{@link #load()}方法加载原料和产物数据到可见槽位上。
 * 当画布需要更新或者玩家切换到其他配方时，才会调用{@link #save()}方法将当前可见槽位的物品写入配方数据。 <p>
 * 若配方包含流体，需要继承{@link FluidRecipeCanvas}。
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class RecipeCanvas<D extends IVSRecipeData> extends UIElement {
    public static final char[] SHAPED_SYMBOLS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>/?|~".toCharArray();
    protected static NavigationView navigationView;
    protected static RecipeEntry entry;

    public static final int SLOT_SIZE = 24;
    public static final int JEI_SLOT_SIZE = 18;
    public static final int OUTPUT_SLOT_SIZE = 30;
    public static RecipeOutputData[] visualOutputs;
    public static boolean containsUnsupportedIngredients;

    public static final int MAX_INGREDIENT = maxSize() * maxSize();
    public static final int MAX_OUTPUT = 16;
    public static final IngredientDisplaySlot[] visualIngredientSlots = new IngredientDisplaySlot[MAX_INGREDIENT];
    public static final ItemSlot[] visualOutputSlots = new ItemSlot[MAX_OUTPUT];
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
        containsUnsupportedIngredients = false;
        Arrays.fill(visualIngredientSlots, null);
        Arrays.fill(visualOutputSlots, null);
        Arrays.fill(extraItemSlots, null);
        // 确保删除旧的槽位，再添加新的槽位
        addChildren(createCanvas());
        visualOutputs = defaultedArrays(new RecipeOutputData[MAX_OUTPUT], RecipeOutputData.empty());
    }

    public static <T> T[] defaultedArrays(T[] array, T defaultValue) {
        Arrays.fill(array, defaultValue);
        return array;
    }

    /**配方原料是否支持数量配置*/
    public boolean ingredientHasCount(int slotIndex) {return false;}

    public void buildRecipeProperties(UIElement content) {}

    public void buildIngredientProperties(UIElement content) {
        var ingredient = getSelectedIngredient();
        var availableKinds = availableIngredientKind();
        var selectedKind = availableKinds.contains(ingredient.getKind()) ? ingredient.getKind() : availableKinds.get(0);
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.ingredient"),
                selector("viscript_recipe.config.ingredient.value.kind", availableKinds, selectedKind,
                        IngredientValueKind::displayName, kind -> setSelectedIngredient(ingredient.setKind(kind)))
        );
        switch (selectedKind) {
            case ITEM -> {
                if (entry.isType(CreateProcessingKind.ITEM_APPLICATION.typeId()) && selectedSlotIndex() == 0) {
                    content.addChild(createBlockConfigurator("viscript_recipe.editor.create.item_application.base_block",
                            () -> ingredientBlock(ingredient), block -> {
                                if (block.asItem().getDefaultInstance().isEmpty()) return;
                                setSelectedIngredient(RecipeIngredient.item(new ItemStack(block)));
                            }
                    ));
                } else {
                    var configurator = createItemStackConfigurator("viscript_recipe.editor.ingredient.item_slot",
                            ingredient::toStack, stack -> setSelectedIngredient(RecipeIngredient.item(stack)));
                    content.addChild(removeCountConfig(configurator));
                }
            }
            case TAG -> content.addChild(createItemTagConfigurator(ingredient.getTag(),
                    tag -> setSelectedIngredient(RecipeIngredient.tag(tag.location()))));
            case ITEM_ABILITY -> content.addChild(createItemAbilityConfigurator(ingredient.getItemAbility(),
                    s -> setSelectedIngredient(RecipeIngredient.itemAbility(s))));
        }
        if (ingredientHasCount(selectedSlotIndex())) {
            content.addChild(intField("viscript_recipe.config.kaleidoscope_cookery.ingredient_count",
                    ingredient.getCount(), 1, Integer.MAX_VALUE,
                            value -> setSelectedIngredient(ingredient.setCount(value))));
        }
        if (this instanceof ShapedCraftingCanvas canvas) canvas.buildRemainderProperties(content);
    }

    public void buildFluidProperties(UIElement content) {}

    public void buildExtraItemProperties(UIElement content) {}

    private List<IngredientValueKind> availableIngredientKind() {
        return entry.isType(RecipeEditorTypes.FARMERSDELIGHT_CUTTING) && selectedSlotIndex() == 1
                ? List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG, IngredientValueKind.ITEM_ABILITY)
                : List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG);
    }

    public void buildResultProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.result"),
                createItemStackConfigurator("viscript_recipe.config.recipe.result",
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

    // ============================== 导航栏类方法 ==============================

    public static void selectSlot(SlotSelection selection) {navigationView.setSlotSelection(selection);}
    public static void selectRecipe() {navigationView.selectRecipe();}
    public static void reloadProperties() {navigationView.refreshPropertiesView();}
    public static void reloadCanvas() {navigationView.reloadCanvas();}

    // ============================== 槽位构建方法 ==============================

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
            selectSlot(SlotSelection.result(index));
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
        slot.registerValueListener(stack -> // 加这个事件是为了能够把xei拖拽过来的物品变成原料
                setVisualIngredient(index, RecipeIngredient.item(stack).setCount(stack.getCount())));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualIngredient(index, RecipeIngredient.empty());
                event.stopPropagation();
            }
            selectSlot(SlotSelection.ingredient(index));
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

    /**新建一个额外物品槽位，并且绑定到extraItemSlots[0]上*/
    public ItemSlot createExtraItemSlot(int size, Component... tips) {
        var slot = configureExtraItemSlot(createEditorSlot(size), tips);
        extraItemSlots[0] = slot;
        return slot;
    }

    public ItemSlot configureExtraItemSlot(ItemSlot slot, Component... tips) {
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                extraItemSlots[0].setValue(ItemStack.EMPTY);
                event.stopPropagation();
            }
            selectSlot(SlotSelection.EXTRA_ITEM);
        });
        slot.style(style -> style.tooltips(tips));
        return slot;
    }

    public static void configureJeiOverlaySlotVisual(ItemSlot slot) {
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
    }
    public static void configureJeiOverlaySlotVisual(ItemSlot... slots) {
        for (var slot : slots) configureJeiOverlaySlotVisual(slot);
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
            if (slot instanceof IngredientDisplaySlot ingredientSlot) draggedIngredient = ingredientSlot.getIngredient();

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
                ingredientSlot.setIngredient(payload.ingredient());
                return;
            }

            var copiedStack = payload.stack().copy();
            if (!slot.getSlot().mayPlace(copiedStack)) {
                event.stopPropagation();
                return;
            }
            if (slot instanceof IngredientDisplaySlot ingredientSlot) {
                ingredientSlot.setIngredient(RecipeIngredient.item(copiedStack));
            } else slot.setItem(copiedStack, true);
            event.stopPropagation();
        });
        return slot;
    }

    private record ItemSlotDragPayload(ItemSlot source, ItemStack stack, RecipeIngredient ingredient) {
    }

    // ============================== 槽位数据读写方法 ==============================

    public static int selectedSlotIndex() {return navigationView.getSlotSelection().index();}

    public void loadIngredientSlot(int index, RecipeIngredient ingredient) {
        if (containsUnsupportedIngredientValue(ingredient)) containsUnsupportedIngredients = true;
        setVisualIngredient(index, ingredient);
    }

    public RecipeIngredient getSelectedIngredient() {return getVisualIngredient(selectedSlotIndex());}
    public RecipeIngredient getVisualIngredient(int index) {
        try {
            return visualIngredientSlots[index].getIngredient().copy();
        } catch (Exception e) {
            return RecipeIngredient.empty();
        }
    }

    /**不需要自己copy ingredient*/
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        try {
            visualIngredientSlots[index].setIngredient(ingredient);
        } catch (Exception ignored) {
        }
    }
    public void setSelectedIngredient(RecipeIngredient ingredient) {
        setVisualIngredient(selectedSlotIndex(), ingredient);
        reloadProperties();
    }

    /**获取可视槽位从offset开始的count个ingredient
     * @param includeEmpty 默认不包含空ingredient*/
    public List<RecipeIngredient> getIngredients(int count, int offset, boolean... includeEmpty) {
        boolean bl = includeEmpty.length > 0 && includeEmpty[0];
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < count; i++) {
            var ingredient = getVisualIngredient(offset + i);
            if (bl || !ingredient.isEmpty()) ingredients.add(ingredient);
        }
        return ingredients;
    }
    /**获取可视槽位的count个ingredient
     * @param includeEmpty 默认不包含空ingredient*/
    public List<RecipeIngredient> getIngredients(int count, boolean... includeEmpty) {
        return getIngredients(count, 0, includeEmpty);
    }

    public void loadIngredients(List<RecipeIngredient> ingredients, int offset) {
        for (int i = 0; i < ingredients.size(); i++) loadIngredientSlot(offset + i, ingredients.get(i));
    }
    public void loadIngredients(List<RecipeIngredient> ingredients) {loadIngredients(ingredients, 0);}

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

    // ============================== UI元素构建方法 ==============================

    public static void tooltip(UIElement element, Component... tips) {
        element.style(style -> style.tooltips(tips));
    }

    public static void tooltip(UIElement element, String... tips) {
        element.style(style -> style.tooltips(tips));
    }

    public static void setTexture(UIElement element, ItemStack stack) {
        element.style(style -> style.backgroundTexture(new ItemStackTexture(stack.copyWithCount(1))));
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

    public static Label emptyLabel() {return RecipeEditorUi.label(Component.empty());}

    public static UIElement sectionTitle(String title) {return RecipeEditorUi.sectionTitle(title);}

    public static UIElement field(String key, UIElement element, Component... tooltip) {
        return RecipeEditorUi.fieldGroup(key, element, tooltip);
    }

    public static UIElement switchField(String key, boolean value, Consumer<Boolean> setter, Component... tooltip) {
        return switchField(key, value, setter, null, tooltip);
    }
    public static UIElement switchField(String key, boolean value,
                                        Consumer<Boolean> setter, Runnable onChange, Component... tooltip) {
        return field(key, new Switch().setOn(value, false).setOnSwitchChanged(bl -> {
            setter.accept(bl); if (onChange != null) onChange.run(); }), tooltip);
    }

    public static UIElement intField(String key, int value, int min, int max, Consumer<Integer> setter, Component... tooltip) {
        return intField(key, value, min, max, setter, null, tooltip);
    }
    public static UIElement intField(String key, int value, int min, int max,
                                     Consumer<Integer> setter, Runnable onChange, Component... tooltip) {
        return field(key, RecipeEditorUi.intField(value, min, max, i -> {
            setter.accept(i); if (onChange != null) onChange.run(); }), tooltip);
    }

    public static UIElement floatField(String key, float value, float min, float max,
                                       Consumer<Float> setter, Component... tooltip) {
        return floatField(key, value, min, max, setter, null, tooltip);
    }
    public static UIElement floatField(String key, float value, float min, float max,
                                       Consumer<Float> setter, Runnable onChange, Component... tooltip) {
        return field(key, RecipeEditorUi.floatField(value, min, max, f -> {
            setter.accept(f); if (onChange != null) onChange.run(); }), tooltip);
    }

    public static UIElement textField(String key, String value, Consumer<String> setter, Component... tooltip) {
        return textField(key, value, setter, null, tooltip);
    }
    public static UIElement textField(String key, String value,
                                      Consumer<String> setter, Runnable onChange, Component... tooltip) {
        return field(key, RecipeEditorUi.textField(value, s -> {
            setter.accept(s); if (onChange != null) onChange.run(); }), tooltip);
    }

    public static UIElement resourceField(String key, ResourceLocation value,
                                          Consumer<ResourceLocation> setter, Component... tooltip) {
        return resourceField(key, value, setter, null, tooltip);
    }
    public static UIElement resourceField(String key, ResourceLocation value,
                                          Consumer<ResourceLocation> setter, Runnable onChange, Component... tooltip) {
        return field(key, RecipeEditorUi.resourceLocationField(value, s -> {
            setter.accept(s); if (onChange != null) onChange.run(); }), tooltip);
    }

    public static <T> UIElement selector(String key, List<T> candidates, T value, Function<T, Component> name,
                                         Consumer<T> setter, Component... tooltip) {
        return selector(key, candidates, value, name, setter, null, tooltip);
    }
    public static <T> UIElement selector(String key, List<T> candidates, T value, Function<T, Component> name,
                                         Consumer<T> setter, Runnable onChange, Component... tooltip) {
        return field(key, RecipeEditorUi.selector(candidates, value, name, t -> {
            setter.accept(t); if (onChange != null) onChange.run(); }), tooltip);
    }
}