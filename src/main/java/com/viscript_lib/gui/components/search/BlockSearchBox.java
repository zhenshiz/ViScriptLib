package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

/**
 * 只选择方块类型本身的自动补全框。
 */
public class BlockSearchBox extends RegistrySearchBox<Block> {

    public BlockSearchBox() {
        this(Blocks.STONE);
    }

    public BlockSearchBox(Block defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.BLOCK,
                BuiltInRegistries.BLOCK::getKey,
                block -> idString(BuiltInRegistries.BLOCK.getKey(block)),
                BlockSearchBox::searchBlocks,
                UIElementProvider.optionalIconText(
                        BlockSearchBox::createBlockIcon,
                        block -> Component.translatable(block.getDescriptionId())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedBlockId() {
        return getSelectedId();
    }

    public String getSelectedBlockIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static ResourceLocation getBlockId(@Nullable Block block) {
        return block == null ? null : BuiltInRegistries.BLOCK.getKey(block);
    }

    public static String getBlockIdString(@Nullable Block block) {
        var id = getBlockId(block);
        return id == null ? "" : id.toString();
    }

    private static void searchBlocks(String word, IResultHandler<Block> searchHandler) {
        searchRegistry(
                BuiltInRegistries.BLOCK,
                word,
                searchHandler,
                block -> LocalizationUtils.format(block.getDescriptionId())
        );
    }

    private static IGuiTexture createBlockIcon(Block block) {
        var item = block.asItem();
        if (item == Items.AIR) {
            return IGuiTexture.EMPTY;
        }
        return new ItemStackTexture(item);
    }
}
