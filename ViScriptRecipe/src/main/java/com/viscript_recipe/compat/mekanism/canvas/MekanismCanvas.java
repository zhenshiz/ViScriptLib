package com.viscript_recipe.compat.mekanism.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.viscript_recipe.compat.mekanism.data.*;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import mekanism.api.chemical.ChemicalType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Consumer;

/**索引：
 * 原料：0，1--物品原料，2，3--化学原料
 * 输出：0，1--物品输出，2，3--化学输出
 * 液体：0--液体输入，2--液体输出
 */
@SuppressWarnings("UnstableApiUsage")
public class MekanismCanvas extends FluidRecipeCanvas<MekanismRecipeData> {
    static ChemicalDisplay[] chemicals = new ChemicalDisplay[4];
    Runnable refresh = Runnables.doNothing();

    public MekanismCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return true;}

    MekanismRecipeKind kind() {return MekanismRecipeKind.byType(entry.getType()).orElse(null);}

    MekanismChemicalIngredientData getChemicalInput(int index) {return chemicals[index].input;}
    MekanismChemicalStackData getChemicalOutput(int index) {return chemicals[index + 2].output;}
    void setChemicalInput(int index, MekanismChemicalIngredientData input) {
        chemicals[index].setInput(input);
        refresh.run();
    }
    void setChemicalOutput(int index, MekanismChemicalStackData output) {chemicals[index + 2].setOutput(output);}

    static boolean isChemicalSlot() {return selectedSlotIndex() == 2 || selectedSlotIndex() == 3;}
    static int chemicalIndex() {return isChemicalSlot() ? selectedSlotIndex() - 2 : 0;}
    MekanismChemicalIngredientData selectedChemicalI() {return getChemicalInput(chemicalIndex());}
    MekanismChemicalStackData selectedChemicalO() {return getChemicalOutput(chemicalIndex());}
    void setSelectedChemicalI(MekanismChemicalIngredientData input) {setChemicalInput(chemicalIndex(), input);}
    void setSelectedChemicalO(MekanismChemicalStackData output) {setChemicalOutput(chemicalIndex(), output);}

    @Override
    public void load() {
        var data = getData();
        var kind = kind();
        if (kind.itemInputs() > 0) loadIngredientSlot(0, data.getItemInput());
        if (kind.itemInputs() > 1) loadIngredientSlot(1, data.getExtraItemInput());
        if (kind.itemOutputs() > 0) setVisualOutput(0, data.getItemOutput());
        if (kind.itemOutputs() > 1) setVisualOutput(1, data.getSecondaryItemOutput());
        if (kind.fluidInputs() > 0) setVisualFluidInput(0, data.getFluidInput());
        if (kind.fluidOutputs() > 0) setVisualFluidOutput(0, data.getFluidOutput());
        if (kind.chemicalInputs() > 0) setChemicalInput(0, data.getChemicalInput());
        if (kind.chemicalInputs() > 1) setChemicalInput(1, data.getExtraChemicalInput());
        if (kind.chemicalOutputs() > 0) setChemicalOutput(0, data.getChemicalOutput());
        if (kind.chemicalOutputs() > 1) setChemicalOutput(1, data.getSecondaryChemicalOutput());
    }

    @Override
    public void save() {
        var data = getData();
        var kind = kind();
        if (kind.itemInputs() > 0) data.setItemInput(getVisualIngredient(0));
        if (kind.itemInputs() > 1) data.setExtraItemInput(getVisualIngredient(1));
        if (kind.itemOutputs() > 0) data.setItemOutput(getVisualOutput(0).getItem());
        if (kind.itemOutputs() > 1) data.setSecondaryItemOutput(getVisualOutput(1).getItem());
        if (kind.fluidInputs() > 0) data.setFluidInput(getVisualFluidInput(0));
        if (kind.fluidOutputs() > 0) data.setFluidOutput(getVisualFluidOutput(0));
        if (kind.chemicalInputs() > 0) data.setChemicalInput(getChemicalInput(0));
        if (kind.chemicalInputs() > 1) data.setExtraChemicalInput(getChemicalInput(1));
        if (kind.chemicalOutputs() > 0) data.setChemicalOutput(getChemicalOutput(0));
        if (kind.chemicalOutputs() > 1) data.setSecondaryChemicalOutput(getChemicalOutput(1));
    }

    @Override
    public UIElement createCanvas() {
        var itemInputs = new IngredientDisplaySlot[]{
                createIngredientSlot(0, JEI_SLOT_SIZE), createIngredientSlot(1, JEI_SLOT_SIZE)
        };
        var itemOutputs = new ItemSlot[]{
                createOutputSlot(0, JEI_SLOT_SIZE), createOutputSlot(1, JEI_SLOT_SIZE)
        };
        FluidDisplaySlot fluidInput = MekanismCanvasFactory.createFluidInputGauge();
        fluidInputSlots[0] = fluidInput;
        configureFluidInputSlot(0);
        FluidSlot fluidOutput = MekanismCanvasFactory.createFluidOutputGauge();
        fluidOutputSlots[0] = fluidOutput;
        configureFluidOutputSlot(0);
        return MekanismCanvasFactory.createCanvas(this, itemInputs, itemOutputs, fluidInput, fluidOutput);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData(); var kind = kind();
        content.addChild(sectionTitle("viscript_recipe.editor.properties.mekanism"));
        // 1.20.1 不支持 perTickUsage
//        if (kind.hasPerTickUsage()) content.addChild(switchField("viscript_recipe.config.mekanism.per_tick_usage",
//                data.isPerTickUsage(), data::setPerTickUsage));
        if (kind.hasDuration()) content.addChild(intField("viscript_recipe.config.mekanism.duration",
                data.getDuration(), 1, Integer.MAX_VALUE, data::setDuration));
        if (kind.hasEnergyRequired()) content.addChild(longField("viscript_recipe.config.mekanism.energy_required",
                data.getEnergyRequired(), 0, Long.MAX_VALUE, data::setEnergyRequired));
        if (kind.hasEnergyMultiplier()) content.addChild(longField("viscript_recipe.config.mekanism.energy_multiplier",
                data.getEnergyMultiplier(), 1, Long.MAX_VALUE, data::setEnergyMultiplier));
        if (kind == MekanismRecipeKind.ENERGY_CONVERSION) content.addChild(longField("viscript_recipe.config.mekanism.energy_output", data.getEnergyOutput(), 1, Long.MAX_VALUE, data::setEnergyOutput));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        if (isChemicalSlot()) {
            buildChemicalIngredientProperties(content, selectedSlotIndex() == 3 ?
                    "viscript_recipe.config.mekanism.extra_chemical_input" : "viscript_recipe.config.mekanism.chemical_input");
            return;
        }
        super.buildIngredientProperties(content);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        var data = getData();
        int index = selectedSlotIndex();
        if (isChemicalSlot()) {
            var chemical = selectedChemicalO();
            if (kind() == MekanismRecipeKind.REACTION && index == 2) {
                content.addChild(switchField("viscript_recipe.config.mekanism.reaction.has_chemical_output",
                        !chemical.isEmpty(), value -> {
                            if (value) setSelectedChemicalO(chemical.setAmount(1));
                            else setSelectedChemicalO(chemical.setAmount(0));
                            reloadProperties();
                        }));
                if (chemical.isEmpty()) return;
            }
            buildChemicalOutputProperties(content, chemical,
                    index == 3 ? "viscript_recipe.config.mekanism.secondary_chemical_output"
                            : "viscript_recipe.config.mekanism.chemical_output");
            return;
        }
        if (index == 1 && kind().itemOutputs() > 1) {
            content.addChildren(sectionTitle("viscript_recipe.editor.properties.mekanism"),
                    PropertiesView.createItemStackConfigurator("viscript_recipe.config.mekanism.secondary_item_output",
                            () -> getSelectedOutput().getItem(), this::setSelectedOutput)
            );
            if (kind().hasSecondaryChance()) {
                content.addChild(floatField("viscript_recipe.config.mekanism.secondary_chance",
                        data.getSecondaryChance(), 0, 1, data::setSecondaryChance));
            }
            return;
        }
        if (kind() == MekanismRecipeKind.REACTION && index == 0) {
            content.addChild(switchField("viscript_recipe.config.mekanism.reaction.has_item_output",
                    !getSelectedOutput().isEmpty(), value -> {
                        setSelectedOutput(value ? new ItemStack(Items.IRON_INGOT) : ItemStack.EMPTY);
                        var chemical = selectedChemicalO();
                        if (!value && chemical.isEmpty()) setSelectedChemicalO(chemical.setAmount(1));
                        reloadProperties();
                    }));
            if (getSelectedOutput().isEmpty()) return;
        }
        super.buildResultProperties(content);
    }

    private void buildChemicalIngredientProperties(UIElement content, String titleKey) {
        var data = selectedChemicalI();
        var kind = data.getKind();
        content.addChildren(sectionTitle(titleKey),
                selector("viscript_recipe.config.mekanism.chemical_ingredient.kind",
                        List.of(MekanismChemicalIngredientKind.values()), kind,
                        MekanismChemicalIngredientKind::displayName, value -> {
                    setSelectedChemicalI(data.setKind(value)); reloadProperties();
                })
        );
        if (kind == MekanismChemicalIngredientKind.TAG) {
            content.addChild(MekanismSearchComponents.chemicalTag("viscript_recipe.config.mekanism.chemical_ingredient.tag",
                    data.getChemicalType(), data::getTag, tag -> setSelectedChemicalI(data.setTag(tag)), Runnables.doNothing()));
        } else content.addChild(MekanismSearchComponents.chemical("viscript_recipe.config.mekanism.chemical_ingredient.chemical", data.getChemicalType(), data::getChemical, id -> setSelectedChemicalI(data.setChemical(id)), Runnables.doNothing()));
        content.addChild(longField("viscript_recipe.config.mekanism.chemical_ingredient.amount", data.getAmount(),
                1, Long.MAX_VALUE, l -> setSelectedChemicalI(data.setAmount(l))));
    }

    private void buildChemicalOutputProperties(UIElement content, MekanismChemicalStackData data, String titleKey) {
        if (kind() == MekanismRecipeKind.DISSOLUTION) content.addChild(selector("Chemical Type",
                List.of(ChemicalType.values()), data.getChemicalType(),
                c -> Component.nullToEmpty(c.getSerializedName()), value -> {
                    setSelectedChemicalO(data.setChemicalType(value)); reloadProperties();
                }));
        content.addChildren(
                MekanismSearchComponents.chemical(titleKey, data.getChemicalType(), data::getChemical, id -> {
                    if (data.getAmount() <= 0) data.setAmount(1);
                    setSelectedChemicalO(data.setChemical(id));
                }, Runnables.doNothing()),
                longField(titleKey + ".amount", data.getAmount(), 1, Long.MAX_VALUE,
                        l -> setSelectedChemicalO(data.setAmount(l)))
        );
    }

    void bindChemicalDisplay(int index, ChemicalDisplay display) {chemicals[index] = display;}

    void selectChemicalSlot(int index) {
        if (index == 0 || index == 1) selectSlot(SlotSelection.ingredient(index + 2));
        else selectSlot(SlotSelection.result(index));
    }

    static UIElement longField(String key, long value, long min, long max, Consumer<Long> setter) {
        return RecipeEditorUi.fieldGroup(key, RecipeEditorUi.longField(value, min, max, setter));
    }
}
