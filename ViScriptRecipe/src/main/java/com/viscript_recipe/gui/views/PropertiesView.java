package com.viscript_recipe.gui.views;

import com.lowdragmc.lowdraglib2.configurator.accessors.BlockAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.FluidStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.*;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.texture.FluidStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.viscript_recipe.compat.create.canvas.SequencedAssemblyCanvas;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PropertiesView extends View {
    private final NavigationView navigationView;
    private final WorkBenchView workBenchView;
    private final ConfiguratorGroup content = new ConfiguratorGroup();
    private boolean rebuilding;

    private static final List<String> ITEM_ABILITY_CHOICES = List.of(
            "knife_dig",
            "axe_dig",
            "axe_strip",
            "shovel_dig",
            "pickaxe_dig",
            "sword_dig",
            "shears_dig"
    );

    public PropertiesView(NavigationView navigationView, WorkBenchView workBenchView) {
        super("viscript_recipe.view.recipe_properties", Icons.SETTINGS);
        this.navigationView = navigationView;
        this.workBenchView = workBenchView;
        addChild(createRoot());
        navigationView.addListener(NavigationView.SLOT_SELECTION_CHANGED, this::refresh);
        refresh();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        var scroller = new ScrollerView();
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(content.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(6);
        }));
        root.addChild(scroller);
        return root;
    }

    private void refresh() {
        if (rebuilding) return;
        rebuilding = true;
        try {
            content.clearAllChildren();
            var entry = navigationView.getSelectedEntry();
            if (entry == null) {
                content.addChild(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.no_entry")));
                return;
            }
            var canvas = workBenchView.getCanvas();
            switch (navigationView.getSlotSelection().kind()) {
                case RECIPE -> buildRecipeProperties(entry);
                case INGREDIENT -> canvas.buildIngredientProperties(content);
                case FLUID -> canvas.buildFluidProperties(content);
                case RESULT -> canvas.buildResultProperties(content);
                case EXTRA_ITEM -> canvas.buildExtraItemProperties(content);
                case CREATE_SEQUENCED_STEP -> ((SequencedAssemblyCanvas) canvas).buildSequencedStepProperties(content);
            }
        } finally {
            rebuilding = false;
        }
    }

    private void buildRecipeProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.recipe"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.type",
                        RecipeEditorUi.selector(
                                RecipeEditorTypes.availableInCategory(navigationView.getSelectedCategoryId()),
                                navigationView.getSelectedRecipeType(),
                                RecipeEditorType::displayName,
                                navigationView::setSelectedRecipeType
                        )),
                RecipeCanvas.switchField("viscript_recipe.config.entry.enabled",
                        entry.isEnabled(), value -> {
                    entry.setEnabled(value); navigationView.updateStatus(); // 更新状态栏
                }),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.recipe_id",
                        RecipeEditorUi.resourceLocationField(entry.getRecipeId(), entry::setRecipeId)),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.operation",
                        RecipeEditorUi.selector(List.of(RecipeOperation.values()),
                                entry.getOperation(), RecipeOperation::displayName, entry::setOperation))
        );
        if (workBenchView.supportsNotification()) {
            content.addChild(RecipeCanvas.switchField("viscript_recipe.config.recipe.show_notification",
                    workBenchView.showNotification(), workBenchView::setShowNotification));
        }
        if (workBenchView.getCanvas() != null) workBenchView.getCanvas().buildRecipeProperties(content);
    }

    public static UIElement createItemStackConfigurator(String nameKey, Supplier<ItemStack> supplier, Consumer<ItemStack> consumer) {
        var configurator = new ItemStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? ItemStack.EMPTY : stack.copy();
                },
                stack -> consumer.accept(stack == null ? ItemStack.EMPTY : stack.copy()),
                true, null, null
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    public static UIElement removeCountConfig(UIElement ui) {
        if (ui instanceof ConfiguratorGroup group) {
            Configurator toRemove = null;
            for (Configurator configurator : group.getConfigurators()) {
                if (configurator instanceof NumberConfigurator
                        && (configurator.getLabel().equals(Component.translatable("ldlib.gui.editor.configurator.count"))
                        || configurator.getLabel().equals(Component.translatable("ldlib.gui.editor.configurator.amount")))) {
                    toRemove = configurator;
                    break;
                }
            }
            if (toRemove != null) group.removeConfigurator(toRemove);
        }
        return ui;
    }

    public static UIElement createBlockConfigurator(String nameKey, Supplier<Block> supplier, Consumer<Block> consumer) {
        var configurator = new BlockAccessor().create(
                nameKey,
                () -> {
                    var block = supplier.get();
                    return block == null ? Blocks.AIR : block;
                },
                block -> consumer.accept(block == null ? Blocks.AIR : block),
                true, null, null
        );
        configurator.layout(layout -> layout.widthPercent(100));
        return configurator;
    }

    public static UIElement createFluidStackConfigurator(String nameKey, Supplier<FluidStack> supplier, Consumer<FluidStack> consumer) {
        var configurator = new FluidStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? FluidStack.EMPTY : stack.copy();
                },
                stack -> consumer.accept(stack == null ? FluidStack.EMPTY : stack.copy()),
                true, null, null
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    public static UIElement createItemTagConfigurator(ResourceLocation tagId, Consumer<TagKey<Item>> onUpdate) {
        var configurator = new TagKeySearchComponent.Item(
                "viscript_recipe.config.ingredient.value.tag",
                () -> itemTag(tagId),
                onUpdate,
                itemTag(defaultTag()),
                true
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    public static UIElement createFluidTagConfigurator(FluidIngredientData ingredient, Consumer<TagKey<Fluid>> onUpdate) {
        var configurator = new TagKeySearchComponent<>(
                "viscript_recipe.config.create.fluid_ingredient.tag",
                () -> fluidTag(ingredient.getTag()), onUpdate,
                fluidTag(defaultFluidTag()), true, BuiltInRegistries.FLUID,
                UIElementProvider.iconText(
                        PropertiesView::fluidTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    public static UIElement createRgbColorConfigurator(String nameKey, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        var configurator = new ColorConfigurator(nameKey, () -> opaqueRgb(supplier.get()),
                value -> consumer.accept(toRgb(value)), 0xFFFFFFFF, true
        );
        configurator.colorSelector.alphaSlider.setDisplay(false);
        configurator.layout(layout -> layout.widthPercent(100));
        return configurator;
    }

    private static int opaqueRgb(Integer color) {
        return 0xFF000000 | toRgb(color);
    }

    private static int toRgb(Integer color) {
        return color == null ? 0xFFFFFF : color & 0xFFFFFF;
    }

    public static IGuiTexture fluidTagIcon(TagKey<Fluid> tag) {
        var fluids = BuiltInRegistries.FLUID.getTag(tag)
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .filter(fluid -> fluid != Fluids.EMPTY)
                        .toList())
                .orElseGet(List::of);
        var sourceFluids = fluids.stream()
                .filter(fluid -> fluid.defaultFluidState().isSource())
                .toList();
        var displayFluids = sourceFluids.isEmpty() ? fluids : sourceFluids;
        if (displayFluids.isEmpty()) {
            return IGuiTexture.EMPTY;
        }
        var stacks = displayFluids.stream()
                .map(fluid -> new FluidStack(fluid, 1000))
                .filter(stack -> !stack.isEmpty())
                .toArray(FluidStack[]::new);
        return stacks.length == 0 ? IGuiTexture.EMPTY : new FluidStackTexture(stacks);
    }

    public static UIElement createItemAbilityConfigurator(String itemAbility, Consumer<String> onUpdate) {
        return RecipeEditorUi.fieldGroup(
                "viscript_recipe.config.ingredient.value.item_ability",
                RecipeEditorUi.selector(
                        ITEM_ABILITY_CHOICES,
                        itemAbility,
                        key -> Component.translatable("viscript_recipe.editor.item_ability." + key),
                        onUpdate
                )
        );
    }

    public static TagKey<Item> itemTag(ResourceLocation tag) {
        return TagKey.create(Registries.ITEM, tag == null ? defaultTag() : tag);
    }

    public static TagKey<Fluid> fluidTag(ResourceLocation tag) {
        return TagKey.create(Registries.FLUID, tag == null ? defaultFluidTag() : tag);
    }

    public static ResourceLocation defaultTag() {
        return new ResourceLocation("minecraft", "planks");
    }

    public static ResourceLocation defaultFluidTag() {
        return new ResourceLocation("c", "water");
    }

    public static Block ingredientBlock(RecipeIngredient ingredient) {
        return ingredient.toStack().getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : Blocks.AIR;
    }
}
