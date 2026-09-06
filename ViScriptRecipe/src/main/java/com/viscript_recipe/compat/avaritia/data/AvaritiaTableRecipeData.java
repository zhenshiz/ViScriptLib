package com.viscript_recipe.compat.avaritia.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
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
public class AvaritiaTableRecipeData implements IVSRecipeData {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 9;

    @Persisted
    private int width = 3;
    @Persisted
    private int height = 3;
    @Persisted
    private int tier = 1;
    @Persisted
    private boolean compatible;
    @Persisted
    private List<String> pattern = new ArrayList<>(List.of("A"));
    @Persisted
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of('A', RecipeIngredient.item(Items.DIAMOND))
    ));
    @Persisted
    private List<RecipeIngredient> shapelessIngredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.DIAMOND)
    ));

    @Persisted
    private ItemStack result = new ItemStack(Items.NETHER_STAR);

    public AvaritiaTableRecipeData setWidth(int width) {
        this.width = clampSize(width);
        return this;
    }

    public AvaritiaTableRecipeData setHeight(int height) {
        this.height = clampSize(height);
        return this;
    }

    public AvaritiaTableRecipeData setTier(int tier) {
        this.tier = Clamp.clamp(tier, 0, 4);
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return AvaritiaRecipeFactory.compileTable(type, this);
    }

    private static int clampSize(int value) {
        return Clamp.clamp(value, MIN_SIZE, MAX_SIZE);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        var tier = AvaritiaRecipeEditorTypes.tableTierForType(typeId);
        var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier);
        setTier(tier).setWidth(gridSize).setHeight(gridSize)
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of('A', RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT)))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT))
                )))
                .setResult(new ItemStack(itemFromRegistry(
                        switch (tier) {
                            case 1 -> "avaritia:neutron_pile";
                            case 2 -> "avaritia:neutron_nugget";
                            case 3 -> "avaritia:neutron_ingot";
                            default -> "avaritia:infinity_catalyst";
                        }, Items.NETHER_STAR)));
    }
}
