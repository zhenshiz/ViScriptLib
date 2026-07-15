package com.viscript_recipe.data;

import com.viscript_recipe.compat.RecipeCompatModules;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauRecipeEditorTypes;
import com.viscript_recipe.data.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.data.cataclysm.CataclysmRecipeEditorTypes;
import com.viscript_recipe.data.create.CreateRecipeEditorTypes;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingRecipeEditorTypes;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.data.goety.GoetyRecipeEditorTypes;
import com.viscript_recipe.data.iceandfire.IceAndFireRecipeEditorTypes;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeCookeryRecipeEditorTypes;
import com.viscript_recipe.data.spore.SporeRecipeEditorTypes;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidRecipeEditorTypes;
import com.viscript_recipe.data.vanilla.VanillaRecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public final class RecipeEditorTypes {
    public static final ResourceLocation CRAFTING_TABLE = VanillaRecipeEditorTypes.CRAFTING_TABLE;
    public static final ResourceLocation FURNACE = VanillaRecipeEditorTypes.FURNACE;
    public static final ResourceLocation BLAST_FURNACE = VanillaRecipeEditorTypes.BLAST_FURNACE;
    public static final ResourceLocation SMOKER = VanillaRecipeEditorTypes.SMOKER;
    public static final ResourceLocation CAMPFIRE = VanillaRecipeEditorTypes.CAMPFIRE;
    public static final ResourceLocation STONECUTTER = VanillaRecipeEditorTypes.STONECUTTER;
    public static final ResourceLocation SMITHING_TABLE = VanillaRecipeEditorTypes.SMITHING_TABLE;
    public static final ResourceLocation CRAFTING_SHAPED = VanillaRecipeEditorTypes.CRAFTING_SHAPED;
    public static final ResourceLocation CRAFTING_SHAPELESS = VanillaRecipeEditorTypes.CRAFTING_SHAPELESS;
    public static final ResourceLocation SMELTING = VanillaRecipeEditorTypes.SMELTING;
    public static final ResourceLocation BLASTING = VanillaRecipeEditorTypes.BLASTING;
    public static final ResourceLocation SMOKING = VanillaRecipeEditorTypes.SMOKING;
    public static final ResourceLocation CAMPFIRE_COOKING = VanillaRecipeEditorTypes.CAMPFIRE_COOKING;
    public static final ResourceLocation STONECUTTING = VanillaRecipeEditorTypes.STONECUTTING;
    public static final ResourceLocation SMITHING_TRANSFORM = VanillaRecipeEditorTypes.SMITHING_TRANSFORM;
    public static final ResourceLocation ICEANDFIRE_DRAGON_FORGE = IceAndFireRecipeEditorTypes.DRAGON_FORGE;
    public static final ResourceLocation ICEANDFIRE_DRAGONFORGE = IceAndFireRecipeEditorTypes.DRAGONFORGE;
    public static final ResourceLocation FARMERSDELIGHT_COOKING_POT = FarmersDelightRecipeEditorTypes.COOKING_POT;
    public static final ResourceLocation FARMERSDELIGHT_CUTTING_BOARD = FarmersDelightRecipeEditorTypes.CUTTING_BOARD;
    public static final ResourceLocation FARMERSDELIGHT_COOKING = FarmersDelightRecipeEditorTypes.COOKING;
    public static final ResourceLocation FARMERSDELIGHT_CUTTING = FarmersDelightRecipeEditorTypes.CUTTING;
    public static final ResourceLocation ARS_NOUVEAU_ENCHANTING_APPARATUS = ArsNouveauRecipeEditorTypes.APPARATUS;
    public static final ResourceLocation ARS_NOUVEAU_ARMOR_UPGRADE = ArsNouveauRecipeEditorTypes.ARMOR_UPGRADE;
    public static final ResourceLocation ARS_NOUVEAU_ENCHANTMENT = ArsNouveauRecipeEditorTypes.ENCHANTMENT;
    public static final ResourceLocation ARS_NOUVEAU_IMBUEMENT = ArsNouveauRecipeEditorTypes.IMBUEMENT;
    public static final ResourceLocation ARS_NOUVEAU_GLYPH = ArsNouveauRecipeEditorTypes.GLYPH;
    public static final ResourceLocation ARS_NOUVEAU_CRUSH = ArsNouveauRecipeEditorTypes.CRUSH;
    public static final ResourceLocation ARS_NOUVEAU_REACTIVE_ENCHANTMENT = ArsNouveauRecipeEditorTypes.REACTIVE_ENCHANTMENT;
    public static final ResourceLocation ARS_NOUVEAU_SPELL_WRITE = ArsNouveauRecipeEditorTypes.SPELL_WRITE;
    public static final ResourceLocation ARS_NOUVEAU_PRESTIDIGITATION = ArsNouveauRecipeEditorTypes.PRESTIDIGITATION;
    public static final ResourceLocation KALEIDOSCOPE_COOKERY_POT = KaleidoscopeCookeryRecipeEditorTypes.POT;
    public static final ResourceLocation KALEIDOSCOPE_COOKERY_STOCKPOT = KaleidoscopeCookeryRecipeEditorTypes.STOCKPOT;
    public static final ResourceLocation KALEIDOSCOPE_COOKERY_MILLSTONE = KaleidoscopeCookeryRecipeEditorTypes.MILLSTONE;
    public static final ResourceLocation KALEIDOSCOPE_COOKERY_CHOPPING_BOARD = KaleidoscopeCookeryRecipeEditorTypes.CHOPPING_BOARD;
    public static final ResourceLocation KALEIDOSCOPE_COOKERY_STEAMER = KaleidoscopeCookeryRecipeEditorTypes.STEAMER;
    public static final ResourceLocation KALEIDOSCOPE_COOKERY_TEAPOT = KaleidoscopeCookeryRecipeEditorTypes.TEAPOT;
    public static final ResourceLocation CREATE_CRUSHING = CreateRecipeEditorTypes.create("crushing");
    public static final ResourceLocation CREATE_MIXING = CreateRecipeEditorTypes.create("mixing");
    public static final ResourceLocation CREATE_MECHANICAL_CRAFTING = CreateRecipeEditorTypes.MECHANICAL_CRAFTING;
    public static final ResourceLocation CREATE_SEQUENCED_ASSEMBLY = CreateRecipeEditorTypes.SEQUENCED_ASSEMBLY;
    public static final ResourceLocation EXTENDEDCRAFTING_CRAFTING_CORE = ExtendedCraftingRecipeEditorTypes.CRAFTING_CORE;
    public static final ResourceLocation EXTENDEDCRAFTING_CRAFTING_TABLE = ExtendedCraftingRecipeEditorTypes.CRAFTING_TABLE;
    public static final ResourceLocation EXTENDEDCRAFTING_BASIC_TABLE = ExtendedCraftingRecipeEditorTypes.BASIC_TABLE;
    public static final ResourceLocation EXTENDEDCRAFTING_ADVANCED_TABLE = ExtendedCraftingRecipeEditorTypes.ADVANCED_TABLE;
    public static final ResourceLocation EXTENDEDCRAFTING_ELITE_TABLE = ExtendedCraftingRecipeEditorTypes.ELITE_TABLE;
    public static final ResourceLocation EXTENDEDCRAFTING_ULTIMATE_TABLE = ExtendedCraftingRecipeEditorTypes.ULTIMATE_TABLE;
    public static final ResourceLocation EXTENDEDCRAFTING_COMPRESSOR = ExtendedCraftingRecipeEditorTypes.COMPRESSOR;
    public static final ResourceLocation EXTENDEDCRAFTING_ENDER_CRAFTER = ExtendedCraftingRecipeEditorTypes.ENDER_CRAFTER;
    public static final ResourceLocation EXTENDEDCRAFTING_FLUX_CRAFTER = ExtendedCraftingRecipeEditorTypes.FLUX_CRAFTER;
    public static final ResourceLocation AVARITIA_CRAFTING_TABLE = AvaritiaRecipeEditorTypes.CRAFTING_TABLE;
    public static final ResourceLocation AVARITIA_SCULK_CRAFTING_TABLE = AvaritiaRecipeEditorTypes.SCULK_CRAFTING_TABLE;
    public static final ResourceLocation AVARITIA_NETHER_CRAFTING_TABLE = AvaritiaRecipeEditorTypes.NETHER_CRAFTING_TABLE;
    public static final ResourceLocation AVARITIA_END_CRAFTING_TABLE = AvaritiaRecipeEditorTypes.END_CRAFTING_TABLE;
    public static final ResourceLocation AVARITIA_EXTREME_CRAFTING_TABLE = AvaritiaRecipeEditorTypes.EXTREME_CRAFTING_TABLE;
    public static final ResourceLocation AVARITIA_NEUTRON_COMPRESSOR = AvaritiaRecipeEditorTypes.NEUTRON_COMPRESSOR;
    public static final ResourceLocation AVARITIA_EXTREME_SMITHING_TABLE = AvaritiaRecipeEditorTypes.EXTREME_SMITHING_TABLE;
    public static final ResourceLocation SPORE_SURGERY = SporeRecipeEditorTypes.SURGERY;
    public static final ResourceLocation SPORE_GRAFTING = SporeRecipeEditorTypes.GRAFTING;
    public static final ResourceLocation CATACLYSM_MECHANICAL_FUSION_ANVIL = CataclysmRecipeEditorTypes.MECHANICAL_FUSION_ANVIL;
    public static final ResourceLocation CATACLYSM_ALTAR_OF_AMETHYST = CataclysmRecipeEditorTypes.ALTAR_OF_AMETHYST;
    public static final ResourceLocation CATACLYSM_WEAPON_FUSION = CataclysmRecipeEditorTypes.WEAPON_FUSION;
    public static final ResourceLocation CATACLYSM_AMETHYST_BLESS = CataclysmRecipeEditorTypes.AMETHYST_BLESS;
    public static final ResourceLocation TOUHOU_LITTLE_MAID_ALTAR = TouhouLittleMaidRecipeEditorTypes.ALTAR_RECIPE;
    public static final ResourceLocation GOETY_CURSED_INFUSER = GoetyRecipeEditorTypes.CURSED_INFUSER_RECIPE;
    public static final ResourceLocation GOETY_RITUAL = GoetyRecipeEditorTypes.RITUAL;
    public static final ResourceLocation GOETY_BRAZIER = GoetyRecipeEditorTypes.BRAZIER;
    public static final ResourceLocation GOETY_PULVERIZE = GoetyRecipeEditorTypes.PULVERIZE;
    public static final ResourceLocation GOETY_BREWING = GoetyRecipeEditorTypes.BREWING;

    private static final LinkedHashMap<ResourceLocation, RecipeEditorCategory> CATEGORIES = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceLocation, RecipeEditorType> TYPES = new LinkedHashMap<>();

    static {
        RecipeCompatModules.registerEditorTypes();
    }

    private RecipeEditorTypes() {
    }

    public static void registerCategory(RecipeEditorCategory category) {
        CATEGORIES.put(category.id(), category);
    }

    public static void register(RecipeEditorType type) {
        TYPES.put(type.id(), type);
    }

    public static Collection<RecipeEditorCategory> allCategories() {
        return List.copyOf(CATEGORIES.values());
    }

    public static Collection<RecipeEditorType> all() {
        return List.copyOf(TYPES.values());
    }

    public static Optional<RecipeEditorCategory> getCategory(@Nullable ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : CATEGORIES.get(id));
    }

    public static Optional<RecipeEditorType> get(@Nullable ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : TYPES.get(normalizeTypeAlias(id)));
    }

    private static ResourceLocation normalizeTypeAlias(ResourceLocation id) {
        return AvaritiaRecipeEditorTypes.normalizeAlias(ExtendedCraftingRecipeEditorTypes.normalizeAlias(id));
    }

    public static RecipeEditorCategory requireCategory(ResourceLocation id) {
        return getCategory(id).orElseThrow(() -> new IllegalArgumentException("Unknown recipe editor category: " + id));
    }

    public static RecipeEditorType require(ResourceLocation id) {
        return get(id).orElseThrow(() -> new IllegalArgumentException("Unknown recipe editor type: " + id));
    }

    public static List<RecipeEditorCategory> availableCategories() {
        return CATEGORIES.values().stream()
                .filter(RecipeEditorCategory::isAvailable)
                .filter(category -> !availableInCategory(category.id()).isEmpty())
                .toList();
    }

    public static List<RecipeEditorType> availableInCategory(ResourceLocation category) {
        return TYPES.values().stream()
                .filter(type -> type.category().equals(category))
                .filter(RecipeEditorType::isAvailable)
                .toList();
    }

    public static ResourceLocation defaultTypeForCategory(ResourceLocation category) {
        var configuredDefault = getCategory(category).map(RecipeEditorCategory::defaultType).orElse(CRAFTING_SHAPED);
        if (get(configuredDefault).filter(RecipeEditorType::isAvailable).isPresent()) {
            return configuredDefault;
        }
        return availableInCategory(category).stream()
                .findFirst()
                .map(RecipeEditorType::id)
                .orElse(CRAFTING_SHAPED);
    }

    public static boolean isInCategory(@Nullable ResourceLocation id, ResourceLocation category) {
        return get(id)
                .map(type -> type.category().equals(category))
                .orElse(false);
    }

    public static RecipeEditorLayout layoutForType(@Nullable ResourceLocation id) {
        return get(id)
                .flatMap(type -> getCategory(type.category()))
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID);
    }
}
