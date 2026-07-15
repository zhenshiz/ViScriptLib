package com.viscript_recipe.data;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.data.ars_nouveau.*;
import com.viscript_recipe.data.avaritia.*;
import com.viscript_recipe.data.cataclysm.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.data.cataclysm.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.create.*;
import com.viscript_recipe.data.extendedcrafting.*;
import com.viscript_recipe.data.farmersdelight.FarmerCookingPotRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingResultData;
import com.viscript_recipe.data.goety.*;
import com.viscript_recipe.data.iceandfire.DragonForgeRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronArcaneAnvilRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronNoAdditionSmithingRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.*;
import com.viscript_recipe.data.spore.SporeGraftingRecipeData;
import com.viscript_recipe.data.spore.SporeSurgeryRecipeData;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.vanilla.*;

public final class RecipeDataAccessors {

    @ViScriptRegisterAccessors
    public static void register(RegisterAccessorEvent event) {
        registerBase(event);
        registerVanilla(event);
        registerIronSpellbooks(event);
        registerIceAndFire(event);
        registerSpore(event);
        registerFarmersDelight(event);
        registerCreate(event);
        registerExtendedCrafting(event);
        registerArsNouveau(event);
        registerKaleidoscopeCookery(event);
        registerAvaritia(event);
        registerCataclysm(event);
        registerTouhouLittleMaid(event);
        registerGoety(event);
    }

    private static void registerBase(RegisterAccessorEvent event) {
        event.register(RecipeFile.class, RecipeFile::new);
        event.register(RecipeEntry.class, RecipeEntry::new);
        event.register(RecipeIngredient.class, RecipeIngredient::new);
        event.register(RecipeIngredientValue.class, RecipeIngredientValue::new);
    }

    private static void registerVanilla(RegisterAccessorEvent event) {
        event.register(CraftingRemainderRule.class, CraftingRemainderRule::new);
        event.register(ShapedKeyEntry.class, ShapedKeyEntry::new);
        event.register(ShapedCraftingRecipeData.class, ShapedCraftingRecipeData::new);
        event.register(ShapelessCraftingRecipeData.class, ShapelessCraftingRecipeData::new);
        event.register(CookingRecipeData.class, CookingRecipeData::new);
        event.register(StonecuttingRecipeData.class, StonecuttingRecipeData::new);
        event.register(SmithingTransformRecipeData.class, SmithingTransformRecipeData::new);
    }

    private static void registerIronSpellbooks(RegisterAccessorEvent event) {
        event.register(IronArcaneAnvilRecipeData.class, IronArcaneAnvilRecipeData::new);
        event.register(IronAlchemistCauldronRecipeData.class, IronAlchemistCauldronRecipeData::new);
        event.register(IronNoAdditionSmithingRecipeData.class, IronNoAdditionSmithingRecipeData::new);
    }

    private static void registerIceAndFire(RegisterAccessorEvent event) {
        event.register(DragonForgeRecipeData.class, DragonForgeRecipeData::new);
    }

    private static void registerSpore(RegisterAccessorEvent event) {
        event.register(SporeSurgeryRecipeData.class, SporeSurgeryRecipeData::new);
        event.register(SporeGraftingRecipeData.class, SporeGraftingRecipeData::new);
    }

    private static void registerFarmersDelight(RegisterAccessorEvent event) {
        event.register(FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new);
        event.register(FarmerCuttingRecipeData.class, FarmerCuttingRecipeData::new);
        event.register(FarmerCuttingResultData.class, FarmerCuttingResultData::new);
    }

    private static void registerCreate(RegisterAccessorEvent event) {
        event.register(CreateFluidIngredientData.class, CreateFluidIngredientData::new);
        event.register(CreateMechanicalCraftingRecipeData.class, CreateMechanicalCraftingRecipeData::new);
        event.register(CreateProcessingOutputData.class, CreateProcessingOutputData::new);
        event.register(CreateProcessingRecipeData.class, CreateProcessingRecipeData::new);
        event.register(CreateSequencedAssemblyRecipeData.class, CreateSequencedAssemblyRecipeData::new);
        event.register(CreateSequencedAssemblyStepData.class, CreateSequencedAssemblyStepData::new);
    }

    private static void registerExtendedCrafting(RegisterAccessorEvent event) {
        event.register(ExtendedCraftingCombinationRecipeData.class, ExtendedCraftingCombinationRecipeData::new);
        event.register(ExtendedCraftingCompressorRecipeData.class, ExtendedCraftingCompressorRecipeData::new);
        event.register(ExtendedCraftingCountedIngredientData.class, ExtendedCraftingCountedIngredientData::new);
        event.register(ExtendedCraftingEnderCrafterRecipeData.class, ExtendedCraftingEnderCrafterRecipeData::new);
        event.register(ExtendedCraftingFluxCrafterRecipeData.class, ExtendedCraftingFluxCrafterRecipeData::new);
        event.register(ExtendedCraftingTableRecipeData.class, ExtendedCraftingTableRecipeData::new);
        event.register(ExtendedCraftingUltimateSingularityRecipeData.class, ExtendedCraftingUltimateSingularityRecipeData::new);
    }

    private static void registerArsNouveau(RegisterAccessorEvent event) {
        event.register(ArsNouveauApparatusRecipeData.class, ArsNouveauApparatusRecipeData::new);
        event.register(ArsNouveauArmorUpgradeRecipeData.class, ArsNouveauArmorUpgradeRecipeData::new);
        event.register(ArsNouveauCrushOutputData.class, ArsNouveauCrushOutputData::new);
        event.register(ArsNouveauCrushRecipeData.class, ArsNouveauCrushRecipeData::new);
        event.register(ArsNouveauEnchantmentRecipeData.class, ArsNouveauEnchantmentRecipeData::new);
        event.register(ArsNouveauGlyphRecipeData.class, ArsNouveauGlyphRecipeData::new);
        event.register(ArsNouveauImbuementRecipeData.class, ArsNouveauImbuementRecipeData::new);
        event.register(ArsNouveauPedestalOnlyRecipeData.class, ArsNouveauPedestalOnlyRecipeData::new);
    }

    private static void registerKaleidoscopeCookery(RegisterAccessorEvent event) {
        event.register(KaleidoscopePotRecipeData.class, KaleidoscopePotRecipeData::new);
        event.register(KaleidoscopeStockpotRecipeData.class, KaleidoscopeStockpotRecipeData::new);
        event.register(KaleidoscopeMillstoneRecipeData.class, KaleidoscopeMillstoneRecipeData::new);
        event.register(KaleidoscopeChoppingBoardRecipeData.class, KaleidoscopeChoppingBoardRecipeData::new);
        event.register(KaleidoscopeSteamerRecipeData.class, KaleidoscopeSteamerRecipeData::new);
        event.register(KaleidoscopeTeapotRecipeData.class, KaleidoscopeTeapotRecipeData::new);
    }

    private static void registerAvaritia(RegisterAccessorEvent event) {
        event.register(AvaritiaCompressorRecipeData.class, AvaritiaCompressorRecipeData::new);
        event.register(AvaritiaEternalSingularityRecipeData.class, AvaritiaEternalSingularityRecipeData::new);
        event.register(AvaritiaExtremeSmithingRecipeData.class, AvaritiaExtremeSmithingRecipeData::new);
        event.register(AvaritiaFullMatterClusterRecipeData.class, AvaritiaFullMatterClusterRecipeData::new);
        event.register(AvaritiaInfinityCatalystRecipeData.class, AvaritiaInfinityCatalystRecipeData::new);
        event.register(AvaritiaTableRecipeData.class, AvaritiaTableRecipeData::new);
    }

    private static void registerCataclysm(RegisterAccessorEvent event) {
        event.register(CataclysmWeaponFusionRecipeData.class, CataclysmWeaponFusionRecipeData::new);
        event.register(CataclysmAmethystBlessRecipeData.class, CataclysmAmethystBlessRecipeData::new);
    }

    private static void registerTouhouLittleMaid(RegisterAccessorEvent event) {
        event.register(TouhouLittleMaidAltarRecipeData.class, TouhouLittleMaidAltarRecipeData::new);
    }

    private static void registerGoety(RegisterAccessorEvent event) {
        event.register(GoetyCursedInfuserRecipeData.class, GoetyCursedInfuserRecipeData::new);
        event.register(GoetyRitualRecipeData.class, GoetyRitualRecipeData::new);
        event.register(GoetyBrazierRecipeData.class, GoetyBrazierRecipeData::new);
        event.register(GoetyPulverizeRecipeData.class, GoetyPulverizeRecipeData::new);
        event.register(GoetyBrewingRecipeData.class, GoetyBrewingRecipeData::new);
    }
}
