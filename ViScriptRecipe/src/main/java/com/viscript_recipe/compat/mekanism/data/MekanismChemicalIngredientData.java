package com.viscript_recipe.compat.mekanism.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mekanism.api.chemical.ChemicalType;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class MekanismChemicalIngredientData implements ISkipDefaultedSerialize {
    @Persisted
    private ChemicalType chemicalType = ChemicalType.GAS;
    @Persisted
    private MekanismChemicalIngredientKind kind = MekanismChemicalIngredientKind.CHEMICAL;
    @Persisted
    private ResourceLocation chemical = new ResourceLocation("mekanism", "oxygen");
    @Persisted
    private ResourceLocation tag = new ResourceLocation("mekanism", "water_vapor");
    @Persisted
    private long amount = 1;

    public boolean isEmpty() {return amount <= 0;}

    public static MekanismChemicalIngredientData empty() {return new MekanismChemicalIngredientData().setAmount(0);}
}
