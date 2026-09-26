package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 流体自动补全框，值类型为 {@code Fluid}。
 */
@SuppressWarnings("deprecation")
public class FluidSearchBox extends RegistrySearchBox<Fluid> {

    public FluidSearchBox() {
        this(Fluids.WATER);
    }

    public FluidSearchBox(Fluid defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.FLUID,
                BuiltInRegistries.FLUID::getKey,
                fluid -> idString(BuiltInRegistries.FLUID.getKey(fluid)),
                FluidSearchBox::searchFluids,
                UIElementProvider.optionalIconText(
                        FluidSearchBox::createFluidIcon,
                        fluid -> fluid.getFluidType().getDescription()
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedFluidId() {
        return getSelectedId();
    }

    public String getSelectedFluidIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static ResourceLocation getFluidId(@Nullable Fluid fluid) {
        return fluid == null ? null : BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static String getFluidIdString(@Nullable Fluid fluid) {
        var id = getFluidId(fluid);
        return id == null ? "" : id.toString();
    }

    private static void searchFluids(String word, IResultHandler<Fluid> searchHandler) {
        searchRegistry(
                BuiltInRegistries.FLUID,
                word,
                searchHandler,
                fluid -> LocalizationUtils.format(fluid.getFluidType().getDescriptionId())
        );
    }

    static IGuiTexture createFluidIcon(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return IGuiTexture.EMPTY;
        }
        return new FluidIconTexture(List.of(fluid));
    }

    static final class FluidIconTexture implements IGuiTexture {
        private final List<Fluid> fluids;
        private int index;
        private int ticks;
        private long lastTick;

        FluidIconTexture(List<Fluid> fluids) {
            this.fluids = List.copyOf(fluids);
        }

        @Override
        public IGuiTexture copy() {
            return new FluidIconTexture(fluids);
        }

        @Override
        public void draw(GuiGraphics graphics, float mouseX, float mouseY, float x, float y, float width, float height, float partialTicks) {
            if (fluids.isEmpty() || width <= 0 || height <= 0) {
                return;
            }
            updateTick();
            if (index >= fluids.size()) {
                index = 0;
            }

            var fluid = fluids.get(index);
            var extensions = IClientFluidTypeExtensions.of(fluid);
            var stillTexture = extensions.getStillTexture();
            if (stillTexture == null) {
                return;
            }
            var sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(stillTexture);
            var color = extensions.getTintColor();

            graphics.flush();
            RenderSystem.enableBlend();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(
                    Math.round(x),
                    Math.round(y),
                    0,
                    Math.max(1, Math.round(width)),
                    Math.max(1, Math.round(height)),
                    sprite,
                    red(color),
                    green(color),
                    blue(color),
                    alpha(color)
            );
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        private void updateTick() {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            var tick = level.getGameTime();
            if (tick == lastTick) {
                return;
            }
            lastTick = tick;
            if (fluids.size() > 1 && ++ticks % 20 == 0) {
                if (++index == fluids.size()) {
                    index = 0;
                }
            }
        }

        private static float alpha(int color) {
            var alpha = color >>> 24;
            return alpha == 0 ? 1.0F : alpha / 255.0F;
        }

        private static float red(int color) {
            return ((color >> 16) & 0xFF) / 255.0F;
        }

        private static float green(int color) {
            return ((color >> 8) & 0xFF) / 255.0F;
        }

        private static float blue(int color) {
            return (color & 0xFF) / 255.0F;
        }
    }
}
