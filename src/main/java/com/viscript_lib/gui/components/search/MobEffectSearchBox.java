package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 状态效果自动补全框，值类型为 {@code Holder<MobEffect>}。
 */
public class MobEffectSearchBox extends RegistrySearchBox<Holder<MobEffect>> {

    public MobEffectSearchBox() {
        this(MobEffects.MOVEMENT_SPEED);
    }

    public MobEffectSearchBox(Holder<MobEffect> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.MOB_EFFECT,
                MobEffectSearchBox::getMobEffectId,
                MobEffectSearchBox::getMobEffectIdString,
                MobEffectSearchBox::searchMobEffects,
                UIElementProvider.iconText(
                        MobEffectSearchBox::createMobEffectIcon,
                        mobEffect -> mobEffect.value().getDisplayName()
                )
        );
    }

    @Nullable
    public ResourceLocation getSelectedMobEffectId() {
        return getSelectedId();
    }

    public String getSelectedMobEffectIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static ResourceLocation getMobEffectId(@Nullable Holder<MobEffect> mobEffect) {
        return mobEffect == null ? null : mobEffect.unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
    }

    public static String getMobEffectIdString(@Nullable Holder<MobEffect> mobEffect) {
        var id = getMobEffectId(mobEffect);
        return id == null ? "" : id.toString();
    }

    private static void searchMobEffects(String word, IResultHandler<Holder<MobEffect>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        BuiltInRegistries.MOB_EFFECT.holders()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .takeWhile(holder -> !Thread.currentThread().isInterrupted())
                .filter(holder -> matches(lowerWord, holder.key().location().toString())
                        || matches(lowerWord, LocalizationUtils.format(holder.value().getDescriptionId())))
                .forEach(searchHandler::acceptResult);
    }

    private static IGuiTexture createMobEffectIcon(Holder<MobEffect> mobEffect) {
        return new MobEffectIconTexture(List.of(mobEffect));
    }

    private static final class MobEffectIconTexture implements IGuiTexture {
        private final List<Holder<MobEffect>> mobEffects;
        private int index;
        private int ticks;
        private long lastTick;

        private MobEffectIconTexture(List<Holder<MobEffect>> mobEffects) {
            this.mobEffects = List.copyOf(mobEffects);
        }

        @Override
        public IGuiTexture copy() {
            return new MobEffectIconTexture(mobEffects);
        }

        @Override
        public void draw(GuiGraphics graphics, float mouseX, float mouseY, float x, float y, float width, float height, float partialTicks) {
            if (mobEffects.isEmpty() || width <= 0 || height <= 0) {
                return;
            }
            updateTick();
            if (index >= mobEffects.size()) {
                index = 0;
            }

            var sprite = Minecraft.getInstance().getMobEffectTextures().get(mobEffects.get(index));
            graphics.flush();
            RenderSystem.enableBlend();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(Math.round(x), Math.round(y), 0, Math.max(1, Math.round(width)), Math.max(1, Math.round(height)), sprite);
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
            if (mobEffects.size() > 1 && ++ticks % 20 == 0) {
                if (++index == mobEffects.size()) {
                    index = 0;
                }
            }
        }
    }
}
