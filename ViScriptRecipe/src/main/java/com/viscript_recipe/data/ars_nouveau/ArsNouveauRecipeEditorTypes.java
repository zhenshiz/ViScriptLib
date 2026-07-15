package com.viscript_recipe.data.ars_nouveau;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ArsNouveauRecipeEditorTypes {
    public static final String MOD_ID = "ars_nouveau";

    public static final ResourceLocation ENCHANTING_APPARATUS = ars("enchanting_apparatus");
    public static final ResourceLocation IMBUEMENT_CHAMBER = ars("imbuement_chamber");
    public static final ResourceLocation SCRIBES_TABLE = ars("scribes_table");
    public static final ResourceLocation CRUSHING = ars("crushing");
    public static final ResourceLocation APPARATUS = ars("enchanting_apparatus");
    public static final ResourceLocation ARMOR_UPGRADE = ars("armor_upgrade");
    public static final ResourceLocation ENCHANTMENT = ars("enchantment");
    public static final ResourceLocation IMBUEMENT = ars("imbuement");
    public static final ResourceLocation GLYPH = ars("glyph");
    public static final ResourceLocation CRUSH = ars("crush");
    public static final ResourceLocation REACTIVE_ENCHANTMENT = ars("reactive_enchantment");
    public static final ResourceLocation SPELL_WRITE = ars("spell_write");
    public static final ResourceLocation PRESTIDIGITATION = ars("prestidigitation");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private ArsNouveauRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ENCHANTING_APPARATUS,
                "viscript_recipe.editor.category.ars_nouveau.enchanting_apparatus",
                MOD_ID,
                REQUIRED_MODS,
                APPARATUS,
                RecipeEditorLayout.ARS_NOUVEAU_APPARATUS,
                ENCHANTING_APPARATUS
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                IMBUEMENT_CHAMBER,
                "viscript_recipe.editor.category.ars_nouveau.imbuement_chamber",
                MOD_ID,
                REQUIRED_MODS,
                IMBUEMENT,
                RecipeEditorLayout.ARS_NOUVEAU_IMBUEMENT,
                IMBUEMENT_CHAMBER
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SCRIBES_TABLE,
                "viscript_recipe.editor.category.ars_nouveau.scribes_table",
                MOD_ID,
                REQUIRED_MODS,
                GLYPH,
                RecipeEditorLayout.ARS_NOUVEAU_GLYPH,
                SCRIBES_TABLE
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CRUSHING,
                "viscript_recipe.editor.category.ars_nouveau.crush",
                MOD_ID,
                REQUIRED_MODS,
                CRUSH,
                RecipeEditorLayout.ARS_NOUVEAU_CRUSH,
                ars("glyph_crush")
        ));
    }

    private static void registerTypes() {
/*        RecipeEditorTypes.register(new RecipeEditorType(
                APPARATUS,
                ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.enchanting_apparatus",
                REQUIRED_MODS,
                false,
                entry -> entry.getArsNouveauApparatus().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getArsNouveauApparatus().getResult(),
                (entry, stack) -> entry.getArsNouveauApparatus().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                ARMOR_UPGRADE,
                ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.armor_upgrade",
                REQUIRED_MODS,
                false,
                entry -> entry.getArsNouveauArmorUpgrade().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> new ItemStack(itemFromRegistry("ars_nouveau:arcanist_robes", Items.LEATHER_CHESTPLATE)),
                (entry, stack) -> {
                }
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                ENCHANTMENT,
                ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.enchantment",
                REQUIRED_MODS,
                false,
                entry -> entry.getArsNouveauEnchantment().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> new ItemStack(Items.ENCHANTED_BOOK),
                (entry, stack) -> {
                }
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                IMBUEMENT,
                IMBUEMENT_CHAMBER,
                "viscript_recipe.editor.type.ars_nouveau.imbuement",
                REQUIRED_MODS,
                false,
                entry -> entry.getArsNouveauImbuement().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getArsNouveauImbuement().getResult(),
                (entry, stack) -> entry.getArsNouveauImbuement().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                GLYPH,
                SCRIBES_TABLE,
                "viscript_recipe.editor.type.ars_nouveau.glyph",
                REQUIRED_MODS,
                false,
                entry -> entry.getArsNouveauGlyph().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> entry.getArsNouveauGlyph().getResult(),
                (entry, stack) -> entry.getArsNouveauGlyph().setResult(copy(stack))
        ));
        RecipeEditorTypes.register(new RecipeEditorType(
                CRUSH,
                CRUSHING,
                "viscript_recipe.editor.type.ars_nouveau.crush",
                REQUIRED_MODS,
                false,
                entry -> entry.getArsNouveauCrush().compile(),
                entry -> false,
                (entry, value) -> {
                },
                entry -> firstCrushOutput(entry.getArsNouveauCrush()),
                (entry, stack) -> setFirstCrushOutput(entry.getArsNouveauCrush(), stack)
        ));*/
    }

    private static ItemStack firstCrushOutput(ArsNouveauCrushRecipeData data) {
        if (data.getOutputs() == null || data.getOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var output = data.getOutputs().get(0);
        return output == null || output.getItem() == null ? ItemStack.EMPTY : output.getItem();
    }

    private static void setFirstCrushOutput(ArsNouveauCrushRecipeData data, ItemStack stack) {
        if (data.getOutputs() == null) {
            data.setOutputs(new java.util.ArrayList<>());
        }
        if (data.getOutputs().isEmpty()) {
            data.getOutputs().add(new ArsNouveauCrushOutputData());
        }
        data.getOutputs().get(0).setItem(copy(stack));
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static Item itemFromRegistry(String id, Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return fallback;
        }
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }

    public static ResourceLocation ars(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
