package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

/**
 * 粒子类型自动补全框，值类型为 {@code ParticleType<?>}。
 */
public class ParticleTypeSearchBox extends RegistrySearchBox<ParticleType<?>> {

    public ParticleTypeSearchBox() {
        this(ParticleTypes.FLAME);
    }

    public ParticleTypeSearchBox(ParticleType<?> defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.PARTICLE_TYPE,
                BuiltInRegistries.PARTICLE_TYPE::getKey,
                particleType -> idString(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType)),
                ParticleTypeSearchBox::searchParticleTypes,
                UIElementProvider.text(particleType -> Component.literal(getParticleTypeIdString(particleType)))
        );
    }

    @Nullable
    public ResourceLocation getSelectedParticleTypeId() {
        return getSelectedId();
    }

    public String getSelectedParticleTypeIdString() {
        return getSelectedIdString();
    }

    public boolean selectedParticleTypeOverridesLimiter() {
        return particleTypeOverridesLimiter(getValue());
    }

    @Nullable
    public static ResourceLocation getParticleTypeId(@Nullable ParticleType<?> particleType) {
        return particleType == null ? null : BuiltInRegistries.PARTICLE_TYPE.getKey(particleType);
    }

    public static String getParticleTypeIdString(@Nullable ParticleType<?> particleType) {
        var id = getParticleTypeId(particleType);
        return id == null ? "" : id.toString();
    }

    public static boolean particleTypeOverridesLimiter(@Nullable ParticleType<?> particleType) {
        return particleType != null && particleType.getOverrideLimiter();
    }

    private static void searchParticleTypes(String word, IResultHandler<ParticleType<?>> searchHandler) {
        searchRegistry(
                BuiltInRegistries.PARTICLE_TYPE,
                word,
                searchHandler,
                particleType -> idString(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType)).replace('_', ' ')
        );
    }
}
