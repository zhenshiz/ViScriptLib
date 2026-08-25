package com.viscript_team.data.faction;

import com.viscript_team.Config;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Faction implements IPersistedSerializable {
    /**
     * 阵营数据：玩家态度由声望点数决定，NPC 阵营敌对只维护紧凑的敌对列表
     */
    @Persisted
    private String id = "";
    @Persisted
    private String name = "";
    @Persisted
    private int color = 0xFFFFFF;
    @Persisted
    private int defaultPoints = Config.DEFAULT_FACTION_POINTS.get();
    @Persisted
    private int neutralPoints = Config.DEFAULT_NEUTRAL_POINTS.get();
    @Persisted
    private int friendlyPoints = Config.DEFAULT_FRIENDLY_POINTS.get();
    @Persisted
    private boolean friendlyFire = Config.DEFAULT_FACTION_FRIENDLY_FIRE.get();
    @Persisted
    private boolean attackEnemyFactions = Config.DEFAULT_ATTACK_ENEMY_FACTIONS.get();
    @Persisted
    private final Set<String> enemyFactions = new HashSet<>();

    public Faction(String id) {
        this.id = normalizeId(id);
        this.name = this.id;
    }

    public static String normalizeId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Faction id cannot be empty");
        }
        return normalized;
    }

    public FactionAttitude attitudeForPoints(int points) {
        if (points < neutralPoints) {
            return FactionAttitude.HOSTILE;
        }
        if (points >= friendlyPoints) {
            return FactionAttitude.FRIENDLY;
        }
        return FactionAttitude.NEUTRAL;
    }

    public Faction setId(String id) {
        this.id = normalizeId(id);
        return this;
    }

    public Faction setName(String name) {
        this.name = name == null || name.isBlank() ? id : name;
        return this;
    }

    public boolean allowsFriendlyFire() {
        return friendlyFire;
    }

    public boolean attacksEnemyFactions() {
        return attackEnemyFactions;
    }

    public Set<String> getEnemyFactions() {
        return Collections.unmodifiableSet(enemyFactions);
    }

    boolean hasValidId() {
        return id != null && !id.isBlank();
    }

    void normalizeStoredData() {
        if (hasValidId()) {
            id = normalizeId(id);
        }
        setName(name);
        Set<String> normalizedEnemies = new HashSet<>();
        for (String enemyFaction : enemyFactions) {
            if (enemyFaction != null && !enemyFaction.isBlank()) {
                String normalized = normalizeId(enemyFaction);
                if (!normalized.equals(id)) {
                    normalizedEnemies.add(normalized);
                }
            }
        }
        enemyFactions.clear();
        enemyFactions.addAll(normalizedEnemies);
    }

    boolean addEnemyFaction(String factionId) {
        String enemyFaction = normalizeId(factionId);
        return !enemyFaction.equals(id) && enemyFactions.add(enemyFaction);
    }

    boolean removeEnemyFaction(String factionId) {
        return enemyFactions.remove(normalizeId(factionId));
    }

    boolean isEnemyFaction(String factionId) {
        return enemyFactions.contains(normalizeId(factionId));
    }
}
