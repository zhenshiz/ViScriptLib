package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * 实体类型标签自动补全框，值类型为 {@code TagKey<EntityType<?>>}。
 */
public class EntityTypeTagSearchBox extends RegistrySearchBox<TagKey<EntityType<?>>> {

    public EntityTypeTagSearchBox() {
        this(EntityTypeTags.UNDEAD);
    }

    public EntityTypeTagSearchBox(TagKey<EntityType<?>> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.ENTITY_TYPE,
                TagKey::location,
                tag -> tag.location().toString(),
                EntityTypeTagSearchBox::searchEntityTypeTags,
                UIElementProvider.optionalIconText(
                        EntityTypeTagSearchBox::createEntityTypeTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedEntityTypeTagId() {
        return getSelectedId();
    }

    public String getSelectedEntityTypeTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedEntityTypeTagReferenceString() {
        return getEntityTypeTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getEntityTypeTagId(@Nullable TagKey<EntityType<?>> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getEntityTypeTagIdString(@Nullable TagKey<EntityType<?>> tag) {
        var id = getEntityTypeTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getEntityTypeTagReferenceString(@Nullable TagKey<EntityType<?>> tag) {
        var id = getEntityTypeTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchEntityTypeTags(String word, IResultHandler<TagKey<EntityType<?>>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.ENTITY_TYPE.getTagNames()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                .filter(tag -> matches(lowerWord, tag.location().toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static IGuiTexture createEntityTypeTagIcon(TagKey<EntityType<?>> tag) {
        var items = new ArrayList<Item>();
        for (Holder<EntityType<?>> holder : BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(tag)) {
            var egg = SpawnEggItem.byId(holder.value());
            if (egg != null) {
                items.add(egg);
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
