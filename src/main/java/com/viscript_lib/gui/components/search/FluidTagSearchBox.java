package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * 流体标签自动补全框，值类型为 {@code TagKey<Fluid>}。
 */
public class FluidTagSearchBox extends RegistrySearchBox<TagKey<Fluid>> {

    public FluidTagSearchBox() {
        this(FluidTags.WATER);
    }

    public FluidTagSearchBox(TagKey<Fluid> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.FLUID,
                TagKey::location,
                tag -> tag.location().toString(),
                FluidTagSearchBox::searchFluidTags,
                UIElementProvider.optionalIconText(
                        FluidTagSearchBox::createFluidTagIcon,
                        tag -> Component.literal(tag.location().toString())
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedFluidTagId() {
        return getSelectedId();
    }

    public String getSelectedFluidTagIdString() {
        return getSelectedIdString();
    }

    public String getSelectedFluidTagReferenceString() {
        return getFluidTagReferenceString(getValue());
    }

    @Nullable
    public static ResourceLocation getFluidTagId(@Nullable TagKey<Fluid> tag) {
        return tag == null ? null : tag.location();
    }

    public static String getFluidTagIdString(@Nullable TagKey<Fluid> tag) {
        var id = getFluidTagId(tag);
        return id == null ? "" : id.toString();
    }

    public static String getFluidTagReferenceString(@Nullable TagKey<Fluid> tag) {
        var id = getFluidTagIdString(tag);
        return id.isEmpty() ? "" : "#" + id;
    }

    private static void searchFluidTags(String word, IResultHandler<TagKey<Fluid>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.FLUID.getTagNames()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                .filter(tag -> matches(lowerWord, tag.location().toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static IGuiTexture createFluidTagIcon(TagKey<Fluid> tag) {
        var fluids = new ArrayList<Fluid>();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(tag)) {
            var fluid = holder.value();
            if (fluid != Fluids.EMPTY) {
                fluids.add(fluid);
            }
            if (fluids.size() >= 64) {
                break;
            }
        }
        if (fluids.isEmpty()) {
            return IGuiTexture.EMPTY;
        }
        return new FluidSearchBox.FluidIconTexture(fluids);
    }
}
