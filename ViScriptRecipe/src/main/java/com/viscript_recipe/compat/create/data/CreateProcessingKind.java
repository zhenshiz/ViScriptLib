package com.viscript_recipe.compat.create.data;

import com.viscript_recipe.compat.create.CreateRecipeEditorTypes;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
public enum CreateProcessingKind {
    CRUSHING("crushing", "crushing_wheel", "crushing", 1, 0, 7, 0, true, false, false, "create:crushing_wheel", Items.COBBLESTONE, Items.GRAVEL),
    MILLING("milling", "millstone", "milling", 1, 0, 4, 0, true, false, false, "create:millstone", Items.WHEAT, Items.WHEAT),
    CUTTING("cutting", "mechanical_saw", "cutting", 1, 0, 4, 0, true, false, false, "create:mechanical_saw", Items.OAK_LOG, Items.OAK_PLANKS),
    BLOCK_CUTTING("block_cutting", "mechanical_saw", "block_cutting", 1, 0, 64, 0, false, false, false, "create:mechanical_saw", Items.STONE, Items.STONE_SLAB),
    AUTO_PACKING("auto_packing", "mechanical_press", "auto_packing", 9, 0, 1, 0, false, false, false, "create:mechanical_press", Items.BONE_MEAL, Items.BONE_BLOCK),
    COMPACTING("compacting", "mechanical_press", "compacting", 64, 2, 4, 2, true, true, false, "create:mechanical_press", Items.IRON_NUGGET, Items.IRON_INGOT),
    PRESSING("pressing", "mechanical_press", "pressing", 1, 0, 2, 0, false, false, false, "create:mechanical_press", Items.IRON_INGOT, Items.IRON_INGOT),
    SANDPAPER_POLISHING("sandpaper_polishing", "sandpaper", "sandpaper_polishing", 1, 0, 1, 0, false, false, false, "create:sand_paper", Items.QUARTZ, Items.QUARTZ),
    BLASTING("blasting", "encased_fan", "blasting", 1, 0, 1, 0, true, false, false, "create:encased_fan", Items.RAW_IRON, Items.IRON_INGOT),
    HAUNTING("haunting", "encased_fan", "haunting", 1, 0, 12, 0, false, false, false, "create:encased_fan", Items.SAND, Items.SOUL_SAND),
    SMOKING("smoking", "encased_fan", "smoking", 1, 0, 1, 0, true, false, false, "create:encased_fan", Items.BEEF, Items.COOKED_BEEF),
    SPLASHING("splashing", "encased_fan", "splashing", 1, 0, 12, 0, false, false, false, "create:encased_fan", Items.GRAVEL, Items.FLINT),
    FILLING("filling", "spout", "filling", 1, 1, 1, 0, false, false, false, "create:spout", Items.GLASS_BOTTLE, Items.POTION),
    EMPTYING("emptying", "item_drain", "emptying", 1, 0, 1, 1, false, false, false, "create:item_drain", Items.POTION, Items.GLASS_BOTTLE),
    MIXING("mixing", "mechanical_mixer", "mixing", 64, 2, 4, 2, true, true, false, "create:mechanical_mixer", Items.SUGAR, Items.SLIME_BALL),
    AUTOMATIC_SHAPELESS("automatic_shapeless", "mechanical_mixer", "automatic_shapeless", 9, 0, 1, 0, false, false, false, "create:mechanical_mixer", Items.SUGAR, Items.SLIME_BALL),
    AUTOMATIC_BREWING("automatic_brewing", "mechanical_mixer", "automatic_brewing", 1, 1, 0, 1, false, true, false, "create:mechanical_mixer", Items.NETHER_WART, Items.POTION),
    DEPLOYING("deploying", "deployer", "deploying", 2, 0, 4, 0, false, false, true, "create:deployer", Items.IRON_BLOCK, Items.IRON_BLOCK),
    ITEM_APPLICATION("item_application", "manual_item_application", "item_application", 2, 0, 4, 0, false, false, true, "create:deployer", Items.COPPER_BLOCK, Items.COPPER_BLOCK);

    private final ResourceLocation typeId;
    private final ResourceLocation categoryId;
    private final String translationPath;
    private final int maxItemInputs;
    private final int maxFluidInputs;
    private final int maxItemOutputs;
    private final int maxFluidOutputs;
    private final boolean durationAllowed;
    private final boolean heatAllowed;
    private final boolean keepHeldItemAllowed;
    private final String machineItemId;
    private final Item defaultInput;
    private final Item defaultOutput;

    CreateProcessingKind(String typePath, String categoryPath, String translationPath, int maxItemInputs, int maxFluidInputs,
                         int maxItemOutputs, int maxFluidOutputs, boolean durationAllowed, boolean heatAllowed,
                         boolean keepHeldItemAllowed, String machineItemId, Item defaultInput, Item defaultOutput) {
        this.typeId = create(typePath);
        this.categoryId = create(categoryPath);
        this.translationPath = translationPath;
        this.maxItemInputs = maxItemInputs;
        this.maxFluidInputs = maxFluidInputs;
        this.maxItemOutputs = maxItemOutputs;
        this.maxFluidOutputs = maxFluidOutputs;
        this.durationAllowed = durationAllowed;
        this.heatAllowed = heatAllowed;
        this.keepHeldItemAllowed = keepHeldItemAllowed;
        this.machineItemId = machineItemId;
        this.defaultInput = defaultInput;
        this.defaultOutput = defaultOutput;
    }

    public static Optional<CreateProcessingKind> byType(ResourceLocation type) {
        return Arrays.stream(values()).filter(kind -> kind.typeId.equals(type)).findFirst();
    }

    public ResourceLocation machineItemLocation() {
        return ResourceLocation.tryParse(machineItemId);
    }

    private static ResourceLocation create(String path) {
        return new ResourceLocation(CreateRecipeEditorTypes.MOD_ID, path);
    }
}
