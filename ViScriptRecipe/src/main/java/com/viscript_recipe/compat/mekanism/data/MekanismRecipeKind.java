package com.viscript_recipe.compat.mekanism.data;

import com.viscript_recipe.compat.mekanism.MekanismRecipeEditorTypes;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public enum MekanismRecipeKind {
    CRUSHING("crushing", "crusher", 1, 0, 0, 1, 0, 0),
    ENRICHING("enriching", "enrichment_chamber", 1, 0, 0, 1, 0, 0),
    SMELTING("smelting", "energized_smelter", 1, 0, 0, 1, 0, 0),
    CHEMICAL_INFUSING("chemical_infusing", "chemical_infuser", 0, 0, 2, 0, 0, 1),
    COMBINING("combining", "combiner", 2, 0, 0, 1, 0, 0),
    SEPARATING("separating", "electrolytic_separator", 0, 1, 0, 0, 0, 2),
    WASHING("washing", "chemical_washer", 0, 1, 1, 0, 0, 1),
    EVAPORATING("evaporating", "thermal_evaporation_controller", 0, 1, 0, 0, 1, 0),
    ACTIVATING("activating", "solar_neutron_activator", 0, 0, 1, 0, 0, 1),
    CENTRIFUGING("centrifuging", "isotopic_centrifuge", 0, 0, 1, 0, 0, 1),
    CRYSTALLIZING("crystallizing", "chemical_crystallizer", 0, 0, 1, 1, 0, 0),
    DISSOLUTION("dissolution", "chemical_dissolution_chamber", 1, 0, 1, 0, 0, 1),
    COMPRESSING("compressing", "osmium_compressor", 1, 0, 1, 1, 0, 0),
    PURIFYING("purifying", "purification_chamber", 1, 0, 1, 1, 0, 0),
    INJECTING("injecting", "chemical_injection_chamber", 1, 0, 1, 1, 0, 0),
    NUCLEOSYNTHESIZING("nucleosynthesizing", "antiprotonic_nucleosynthesizer", 1, 0, 1, 1, 0, 0),
    ENERGY_CONVERSION("energy_conversion", "basic_energy_cube", 1, 0, 0, 0, 0, 0),
    GAS_CONVERSION("gas_conversion", "purification_chamber", 1, 0, 0, 0, 0, 1), // 1.20.1
    //CHEMICAL_CONVERSION("chemical_conversion", "purification_chamber", 1, 0, 0, 0, 0, 1),
    OXIDIZING("oxidizing", "chemical_oxidizer", 1, 0, 0, 0, 0, 1),
    INFUSION_CONVERSION("infusion_conversion", "metallurgic_infuser", 1, 0, 0, 0, 0, 1), // 1.20.1
    PIGMENT_EXTRACTING("pigment_extracting", "pigment_extractor", 1, 0, 0, 0, 0, 1),
    PIGMENT_MIXING("pigment_mixing", "pigment_mixer", 0, 0, 2, 0, 0, 1),
    METALLURGIC_INFUSING("metallurgic_infusing", "metallurgic_infuser", 1, 0, 1, 1, 0, 0),
    PAINTING("painting", "painting_machine", 1, 0, 1, 1, 0, 0),
    REACTION("reaction", "pressurized_reaction_chamber", 1, 1, 1, 1, 0, 1),
    CONDENSENTRATING("condensentrating", "rotary_condensentrator", 0, 0, 1, 0, 1, 0),
    DECONDENSENTRATING("decondensentrating", "rotary_condensentrator", 0, 1, 0, 0, 0, 1),
    SAWING("sawing", "precision_sawmill", 1, 0, 0, 2, 0, 0);

    private final ResourceLocation typeId;
    private final ResourceLocation workstationId;
    private final int itemInputs;
    private final int fluidInputs;
    private final int chemicalInputs;
    private final int itemOutputs;
    private final int fluidOutputs;
    private final int chemicalOutputs;

    MekanismRecipeKind(String path, String workstationPath, int itemInputs, int fluidInputs, int chemicalInputs,
                       int itemOutputs, int fluidOutputs, int chemicalOutputs) {
        typeId = new ResourceLocation(MekanismRecipeEditorTypes.MOD_ID, path);
        workstationId = new ResourceLocation(MekanismRecipeEditorTypes.MOD_ID, workstationPath);
        this.itemInputs = itemInputs;
        this.fluidInputs = fluidInputs;
        this.chemicalInputs = chemicalInputs;
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.chemicalOutputs = chemicalOutputs;
    }

    public static Optional<MekanismRecipeKind> byType(ResourceLocation type) {
        return Arrays.stream(values()).filter(kind -> kind.typeId.equals(type)).findFirst();
    }

/*    public boolean hasPerTickUsage() {
        return switch (this) {
            case DISSOLUTION, COMPRESSING, PURIFYING, INJECTING, NUCLEOSYNTHESIZING, METALLURGIC_INFUSING, PAINTING -> true;
            default -> false;
        };
    }*/

    public boolean hasDuration() {
        return this == NUCLEOSYNTHESIZING || this == REACTION;
    }

    public boolean hasEnergyRequired() { return this == REACTION; }
    public boolean hasEnergyMultiplier() { return this == SEPARATING; }
    public boolean hasSecondaryChance() { return this == SAWING; }

    public boolean gasInput() {
        return switch (this) {
            case ACTIVATING, CENTRIFUGING, DISSOLUTION, CHEMICAL_INFUSING, COMPRESSING, INJECTING, NUCLEOSYNTHESIZING, REACTION, PURIFYING, CONDENSENTRATING -> true;
            default -> false;
        };
    }

    public boolean gasOutput() {
        return switch (this) {
            case DISSOLUTION, // 这个比较特殊，可以输出任意类型
                    ACTIVATING, CENTRIFUGING, CHEMICAL_INFUSING, OXIDIZING, SEPARATING, GAS_CONVERSION, REACTION, DECONDENSENTRATING -> true;
            default -> false;
        };
    }

    public boolean infuseTypeInput() {
        return this == METALLURGIC_INFUSING;
    }

    public boolean infuseTypeOutput() {
        return this == INFUSION_CONVERSION;
    }

    public boolean pigmentInput() {
        return this == PAINTING || this == PIGMENT_MIXING;
    }

    public boolean pigmentOutput() {
        return this == PIGMENT_EXTRACTING || this == PIGMENT_MIXING;
    }

    public boolean slurryInput() {
        return this == WASHING;
    }

    public boolean slurryOutput() {
        return this == WASHING;
    }
}
