package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import org.jetbrains.annotations.Nullable;

/**
 * 声音事件自动补全框，值类型为 {@code SoundEvent}。
 */
public class SoundEventSearchBox extends RegistrySearchBox<SoundEvent> {

    public SoundEventSearchBox() {
        this(SoundEvents.ITEM_PICKUP);
    }

    public SoundEventSearchBox(SoundEvent defaultValue) {
        super(
                defaultValue,
                () -> BuiltInRegistries.SOUND_EVENT,
                BuiltInRegistries.SOUND_EVENT::getKey,
                soundEvent -> idString(BuiltInRegistries.SOUND_EVENT.getKey(soundEvent)),
                SoundEventSearchBox::searchSoundEvents,
                UIElementProvider.text(soundEvent -> Component.literal(getSoundEventIdString(soundEvent)))
        );
    }

    @Nullable
    public ResourceLocation getSelectedSoundEventId() {
        return getSelectedId();
    }

    public String getSelectedSoundEventIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public ResourceLocation getSelectedSoundLocation() {
        return getSoundLocation(getValue());
    }

    public String getSelectedSoundLocationString() {
        return getSoundLocationString(getValue());
    }

    @Nullable
    public static ResourceLocation getSoundEventId(@Nullable SoundEvent soundEvent) {
        return soundEvent == null ? null : BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
    }

    public static String getSoundEventIdString(@Nullable SoundEvent soundEvent) {
        var id = getSoundEventId(soundEvent);
        return id == null ? "" : id.toString();
    }

    @Nullable
    public static ResourceLocation getSoundLocation(@Nullable SoundEvent soundEvent) {
        return soundEvent == null ? null : soundEvent.getLocation();
    }

    public static String getSoundLocationString(@Nullable SoundEvent soundEvent) {
        var location = getSoundLocation(soundEvent);
        return location == null ? "" : location.toString();
    }

    private static void searchSoundEvents(String word, IResultHandler<SoundEvent> searchHandler) {
        searchRegistry(
                BuiltInRegistries.SOUND_EVENT,
                word,
                searchHandler,
                soundEvent -> soundEvent.getLocation().toString()
        );
    }
}
