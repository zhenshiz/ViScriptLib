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
public class MekanismChemicalStackData implements ISkipDefaultedSerialize {
    @Persisted
    private ChemicalType chemicalType = ChemicalType.GAS;
    @Persisted
    private ResourceLocation chemical = new ResourceLocation("mekanism", "hydrogen");
    @Persisted
    private long amount = 1;

    public boolean isEmpty() {return amount <= 0;}

    public static MekanismChemicalStackData empty() {return new MekanismChemicalStackData().setAmount(0);}
}
