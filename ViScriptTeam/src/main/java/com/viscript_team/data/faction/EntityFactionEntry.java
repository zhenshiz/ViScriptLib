package com.viscript_team.data.faction;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class EntityFactionEntry implements IPersistedSerializable {
    @Persisted
    private UUID entityId = new UUID(0L, 0L);
    @Persisted
    private String factionId = "";

    public EntityFactionEntry(UUID entityId, String factionId) {
        this.entityId = entityId;
        this.factionId = Faction.normalizeId(factionId);
    }

    public EntityFactionEntry setFactionId(String factionId) {
        this.factionId = Faction.normalizeId(factionId);
        return this;
    }

    boolean hasValidData() {
        return entityId != null && factionId != null && !factionId.isBlank();
    }

    void normalizeStoredData() {
        if (factionId != null && !factionId.isBlank()) {
            factionId = Faction.normalizeId(factionId);
        }
    }
}
