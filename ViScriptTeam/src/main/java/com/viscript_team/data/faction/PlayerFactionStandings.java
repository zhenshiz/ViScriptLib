package com.viscript_team.data.faction;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class PlayerFactionStandings implements IPersistedSerializable {
    @Persisted
    private UUID playerId = new UUID(0L, 0L);
    @Persisted
    private final List<StandingEntry> standings = new ArrayList<>();

    public PlayerFactionStandings(UUID playerId) {
        this.playerId = playerId;
    }

    public Optional<StandingEntry> findStanding(String factionId) {
        String id = Faction.normalizeId(factionId);
        return standings.stream().filter(standing -> standing.getFactionId().equals(id)).findFirst();
    }

    public boolean isEmpty() {
        return standings.isEmpty();
    }

    boolean hasValidPlayer() {
        return playerId != null;
    }

    void normalizeStoredData() {
        standings.forEach(StandingEntry::normalizeStoredData);
        standings.removeIf(standing -> !standing.hasValidFaction());
    }
}
