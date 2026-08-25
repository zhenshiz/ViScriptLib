package com.viscript_team.client;

import com.viscript_team.data.faction.FactionAttitude;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public class ClientFactionNameTagCache {
    private static final Map<UUID, FactionAttitude> ENTITY_ATTITUDES = new HashMap<>();

    public static void apply(CompoundTag tag) {
        ENTITY_ATTITUDES.clear();
        ListTag entries = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.hasUUID("entityId")) {
                ENTITY_ATTITUDES.put(entry.getUUID("entityId"), parseAttitude(entry.getString("attitude")));
            }
        }
    }

    public static void clear() {
        ENTITY_ATTITUDES.clear();
    }

    @Nullable
    public static FactionAttitude getAttitude(UUID entityId) {
        return ENTITY_ATTITUDES.get(entityId);
    }

    private static FactionAttitude parseAttitude(String name) {
        try {
            return FactionAttitude.valueOf(name);
        } catch (IllegalArgumentException e) {
            return FactionAttitude.NEUTRAL;
        }
    }
}
