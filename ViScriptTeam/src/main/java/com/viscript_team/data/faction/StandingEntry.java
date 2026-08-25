package com.viscript_team.data.faction;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class StandingEntry implements IPersistedSerializable {
    @Persisted
    private String factionId = "";
    @Persisted
    private int points;

    public StandingEntry(String factionId, int points) {
        this.factionId = Faction.normalizeId(factionId);
        this.points = points;
    }

    public StandingEntry setFactionId(String factionId) {
        this.factionId = Faction.normalizeId(factionId);
        return this;
    }

    boolean hasValidFaction() {
        return factionId != null && !factionId.isBlank();
    }

    void normalizeStoredData() {
        if (hasValidFaction()) {
            factionId = Faction.normalizeId(factionId);
        }
    }
}
