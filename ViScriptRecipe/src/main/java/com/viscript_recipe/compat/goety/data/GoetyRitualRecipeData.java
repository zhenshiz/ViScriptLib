package com.viscript_recipe.compat.goety.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeEditorTypes;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class GoetyRitualRecipeData implements IVSRecipeData {
    public static final int MAX_PEDESTAL_INGREDIENTS = 12;

    @Persisted
    private RecipeIngredient activationItem = RecipeIngredient.item(Items.BOOK);
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
    @Persisted
    private GoetyRitualCraftType craftType = GoetyRitualCraftType.MAGIC;
    @Persisted
    private ResourceLocation ritualType = new ResourceLocation("goety", "craft");
    @Persisted
    private int soulCost;
    @Persisted
    private int duration = 30;
    @Persisted
    private int summonLife = -1;
    @Persisted
    private boolean hasSacrifice;
    @Persisted
    private ResourceLocation entityToSacrifice = new ResourceLocation("zombies");
    @Persisted
    private String entityToSacrificeDisplayName = "entity.minecraft.zombie";
    @Persisted
    private boolean hasSummon;
    @Persisted
    private ResourceLocation entityToSummon = new ResourceLocation("zombie");
    @Persisted
    private boolean hasConversion;
    @Persisted
    private ResourceLocation entityToConvert = new ResourceLocation("zombies");
    @Persisted
    private String entityToConvertDisplayName = "entity.minecraft.zombie";
    @Persisted
    private ResourceLocation entityToConvertInto = new ResourceLocation("zombie_villager");
    @Persisted
    private boolean hasStructure;
    @Persisted
    private ResourceLocation structureToLocate = new ResourceLocation("village");
    @Persisted
    private String structureDisplayName = "filled_map.village";
    @Persisted
    private boolean hasEnchantment;
    @Persisted
    private ResourceLocation enchantment = new ResourceLocation("sharpness");
    @Persisted
    private int xpLevelCost;
    @Persisted
    private String research = "";

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileRitual(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setActivationItem(RecipeIngredient.item(itemFromRegistry("goety:dark_gem", Items.BOOK)))
                .setIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(Items.IRON_INGOT),
                        RecipeIngredient.item(Items.COAL),
                        RecipeIngredient.item(itemFromRegistry("goety:cursed_ingot", Items.GOLD_INGOT))
                )))
                .setResult(new ItemStack(itemFromRegistry("goety:dark_ingot", Items.NETHERITE_INGOT)))
                .setCraftType(GoetyRitualCraftType.FORGE)
                .setRitualType(GoetyRecipeEditorTypes.goety("craft"))
                .setSoulCost(5);
    }
}
