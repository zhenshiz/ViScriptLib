package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.ars_nouveau.*;
import com.viscript_recipe.data.avaritia.*;
import com.viscript_recipe.data.cataclysm.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.data.cataclysm.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.create.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.data.create.CreateProcessingRecipeData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.data.extendedcrafting.*;
import com.viscript_recipe.data.farmersdelight.FarmerCookingPotRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
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
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeEntry implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.entry.enabled")
    private boolean enabled = true;

    @Configurable(name = "viscript_recipe.config.entry.operation")
    @ConfigSelector(candidate = {"add", "replace", "remove"})
    private RecipeOperation operation = RecipeOperation.REPLACE;

    @Configurable(name = "viscript_recipe.config.entry.recipe_id")
    private ResourceLocation recipeId = new ResourceLocation("viscript_recipe", "example");

    @Configurable(name = "viscript_recipe.config.entry.type")
    private ResourceLocation type = RecipeEditorTypes.CRAFTING_SHAPED;

    @Configurable(name = "viscript_recipe.config.entry.shaped", subConfigurable = true)
    private ShapedCraftingRecipeData shaped = new ShapedCraftingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.shapeless", subConfigurable = true)
    private ShapelessCraftingRecipeData shapeless = new ShapelessCraftingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.cooking", subConfigurable = true)
    private CookingRecipeData cooking = new CookingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.stonecutting", subConfigurable = true)
    private StonecuttingRecipeData stonecutting = new StonecuttingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.smithing_transform", subConfigurable = true)
    private SmithingTransformRecipeData smithingTransform = new SmithingTransformRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.irons_spellbooks.alchemist_cauldron", subConfigurable = true)
    private IronAlchemistCauldronRecipeData ironAlchemistCauldron = new IronAlchemistCauldronRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.irons_spellbooks.arcane_anvil", subConfigurable = true)
    private IronArcaneAnvilRecipeData ironArcaneAnvil = new IronArcaneAnvilRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.irons_spellbooks.smithing_no_addition", subConfigurable = true)
    private IronNoAdditionSmithingRecipeData ironNoAdditionSmithing = new IronNoAdditionSmithingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.iceandfire.dragon_forge", subConfigurable = true)
    private DragonForgeRecipeData iceAndFireDragonForge = new DragonForgeRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.spore.surgery", subConfigurable = true)
    private SporeSurgeryRecipeData sporeSurgery = new SporeSurgeryRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.spore.grafting", subConfigurable = true)
    private SporeGraftingRecipeData sporeGrafting = new SporeGraftingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.farmersdelight.cooking_pot", subConfigurable = true)
    private FarmerCookingPotRecipeData farmerCookingPot = new FarmerCookingPotRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.farmersdelight.cutting_board", subConfigurable = true)
    private FarmerCuttingRecipeData farmerCuttingBoard = new FarmerCuttingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.create.processing", subConfigurable = true)
    private CreateProcessingRecipeData createProcessing = new CreateProcessingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.create.mechanical_crafting", subConfigurable = true)
    private CreateMechanicalCraftingRecipeData createMechanicalCrafting = new CreateMechanicalCraftingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.create.sequenced_assembly", subConfigurable = true)
    private CreateSequencedAssemblyRecipeData createSequencedAssembly = new CreateSequencedAssemblyRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.extendedcrafting.table", subConfigurable = true)
    private ExtendedCraftingTableRecipeData extendedCraftingTable = new ExtendedCraftingTableRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.extendedcrafting.ender_crafter", subConfigurable = true)
    private ExtendedCraftingEnderCrafterRecipeData extendedCraftingEnderCrafter = new ExtendedCraftingEnderCrafterRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.extendedcrafting.flux_crafter", subConfigurable = true)
    private ExtendedCraftingFluxCrafterRecipeData extendedCraftingFluxCrafter = new ExtendedCraftingFluxCrafterRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.extendedcrafting.combination", subConfigurable = true)
    private ExtendedCraftingCombinationRecipeData extendedCraftingCombination = new ExtendedCraftingCombinationRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.extendedcrafting.compressor", subConfigurable = true)
    private ExtendedCraftingCompressorRecipeData extendedCraftingCompressor = new ExtendedCraftingCompressorRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.extendedcrafting.ultimate_singularity", subConfigurable = true)
    private ExtendedCraftingUltimateSingularityRecipeData extendedCraftingUltimateSingularity = new ExtendedCraftingUltimateSingularityRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.apparatus", subConfigurable = true)
    private ArsNouveauApparatusRecipeData arsNouveauApparatus = new ArsNouveauApparatusRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.armor_upgrade", subConfigurable = true)
    private ArsNouveauArmorUpgradeRecipeData arsNouveauArmorUpgrade = new ArsNouveauArmorUpgradeRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.enchantment", subConfigurable = true)
    private ArsNouveauEnchantmentRecipeData arsNouveauEnchantment = new ArsNouveauEnchantmentRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.imbuement", subConfigurable = true)
    private ArsNouveauImbuementRecipeData arsNouveauImbuement = new ArsNouveauImbuementRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.glyph", subConfigurable = true)
    private ArsNouveauGlyphRecipeData arsNouveauGlyph = new ArsNouveauGlyphRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.crush", subConfigurable = true)
    private ArsNouveauCrushRecipeData arsNouveauCrush = new ArsNouveauCrushRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.ars_nouveau.pedestal_only", subConfigurable = true)
    private ArsNouveauPedestalOnlyRecipeData arsNouveauPedestalOnly = new ArsNouveauPedestalOnlyRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.kaleidoscope_cookery.pot", subConfigurable = true)
    private KaleidoscopePotRecipeData kaleidoscopePot = new KaleidoscopePotRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.kaleidoscope_cookery.stockpot", subConfigurable = true)
    private KaleidoscopeStockpotRecipeData kaleidoscopeStockpot = new KaleidoscopeStockpotRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.kaleidoscope_cookery.millstone", subConfigurable = true)
    private KaleidoscopeMillstoneRecipeData kaleidoscopeMillstone = new KaleidoscopeMillstoneRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.kaleidoscope_cookery.chopping_board", subConfigurable = true)
    private KaleidoscopeChoppingBoardRecipeData kaleidoscopeChoppingBoard = new KaleidoscopeChoppingBoardRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.kaleidoscope_cookery.steamer", subConfigurable = true)
    private KaleidoscopeSteamerRecipeData kaleidoscopeSteamer = new KaleidoscopeSteamerRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.kaleidoscope_cookery.teapot", subConfigurable = true)
    private KaleidoscopeTeapotRecipeData kaleidoscopeTeapot = new KaleidoscopeTeapotRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.avaritia.table", subConfigurable = true)
    private AvaritiaTableRecipeData avaritiaTable = new AvaritiaTableRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.avaritia.compressor", subConfigurable = true)
    private AvaritiaCompressorRecipeData avaritiaCompressor = new AvaritiaCompressorRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.avaritia.extreme_smithing", subConfigurable = true)
    private AvaritiaExtremeSmithingRecipeData avaritiaExtremeSmithing = new AvaritiaExtremeSmithingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.avaritia.infinity_catalyst", subConfigurable = true)
    private AvaritiaInfinityCatalystRecipeData avaritiaInfinityCatalyst = new AvaritiaInfinityCatalystRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.avaritia.eternal_singularity", subConfigurable = true)
    private AvaritiaEternalSingularityRecipeData avaritiaEternalSingularity = new AvaritiaEternalSingularityRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.avaritia.full_matter_cluster", subConfigurable = true)
    private AvaritiaFullMatterClusterRecipeData avaritiaFullMatterCluster = new AvaritiaFullMatterClusterRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.cataclysm.weapon_fusion", subConfigurable = true)
    private CataclysmWeaponFusionRecipeData cataclysmWeaponFusion = new CataclysmWeaponFusionRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.cataclysm.amethyst_bless", subConfigurable = true)
    private CataclysmAmethystBlessRecipeData cataclysmAmethystBless = new CataclysmAmethystBlessRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.touhou_little_maid.altar", subConfigurable = true)
    private TouhouLittleMaidAltarRecipeData touhouLittleMaidAltar = new TouhouLittleMaidAltarRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.goety.cursed_infuser", subConfigurable = true)
    private GoetyCursedInfuserRecipeData goetyCursedInfuser = new GoetyCursedInfuserRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.goety.ritual", subConfigurable = true)
    private GoetyRitualRecipeData goetyRitual = new GoetyRitualRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.goety.brazier", subConfigurable = true)
    private GoetyBrazierRecipeData goetyBrazier = new GoetyBrazierRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.goety.pulverize", subConfigurable = true)
    private GoetyPulverizeRecipeData goetyPulverize = new GoetyPulverizeRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.goety.brewing", subConfigurable = true)
    private GoetyBrewingRecipeData goetyBrewing = new GoetyBrewingRecipeData();

    public Recipe<?> compile() {
        return RecipeEditorTypes.require(getType()).compile(this);
    }

    public ResourceLocation getType() {
        return type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
    }

    public RecipeEntry setType(ResourceLocation type) {
        this.type = type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
        return this;
    }

    public boolean isType(ResourceLocation type) {
        return getType().equals(type);
    }
}
