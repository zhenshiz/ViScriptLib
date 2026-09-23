package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * 方块标签自动补全框，值类型为 {@code TagKey<Block>}。
 */
public class BlockTagSearchBox extends RegistrySearchBox<TagKey<Block>> {

    public BlockTagSearchBox() {
        this(BlockTags.PLANKS);
    }

    public BlockTagSearchBox(TagKey<Block> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.BLOCK,
                TagKey::location,
                tag -> tag.location().toString(),
                BlockTagSearchBox::searchBlockTags,
                UIElementProvider.optionalIconText(
                        BlockTagSearchBox::createBlockTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedBlockTagId() {
        return getSelectedId();
    }

    public String getSelectedBlockTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedBlockTagReferenceString() {
        return getBlockTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getBlockTagId(@Nullable TagKey<Block> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getBlockTagIdString(@Nullable TagKey<Block> tag) {
        var id = getBlockTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getBlockTagReferenceString(@Nullable TagKey<Block> tag) {
        var id = getBlockTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchBlockTags(String word, IResultHandler<TagKey<Block>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.BLOCK.getTagNames()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                .filter(tag -> matches(lowerWord, tag.location().toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static IGuiTexture createBlockTagIcon(TagKey<Block> tag) {
        var items = new ArrayList<Item>();
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            var item = holder.value().asItem();
            if (item != Items.AIR) {
                items.add(item);
            }
            if (items.size() >= 64) {
                break;
            }
        }
        if (items.isEmpty()) {
            return IGuiTexture.EMPTY;
        }
        return new ItemStackTexture(items.toArray(Item[]::new));
    }
}
