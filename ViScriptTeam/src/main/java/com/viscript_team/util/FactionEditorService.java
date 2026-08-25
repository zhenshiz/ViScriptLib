package com.viscript_team.util;

import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.network.FactionNameTagSync;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@UtilityClass
public class FactionEditorService {
    private static final String RESULT_PREFIX = ViScriptTeam.MOD_ID + ".faction_editor.result.";
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]{1,64}");
    private static final int MAX_NAME_LENGTH = 64;

    public static Result createFaction(ServerPlayer player, String requestedId, String requestedName) {
        if (!canEdit(player)) {
            return failure("no_permission");
        }
        String id = requestedId == null ? "" : requestedId.trim().toLowerCase(Locale.ROOT);
        if (!VALID_ID.matcher(id).matches()) {
            return failure("invalid_id");
        }
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            return failure("invalid_name");
        }

        FactionSavedData data = FactionSavedData.get(player.serverLevel());
        if (data.getFaction(id).isPresent()) {
            return failure("already_exists");
        }
        Faction faction = data.getOrCreateFaction(id).setName(name);
        data.saveFaction(faction);
        FactionNameTagSync.syncAll(player.server);
        return success("created");
    }

    public static Result updateFaction(ServerPlayer player, String factionId, CompoundTag settings) {
        if (!canEdit(player)) {
            return failure("no_permission");
        }
        FactionSavedData data = FactionSavedData.get(player.serverLevel());
        Faction faction;
        try {
            faction = data.getFaction(factionId).orElse(null);
        } catch (IllegalArgumentException ignored) {
            faction = null;
        }
        if (faction == null) {
            return failure("not_found");
        }

        String name = settings.getString("name").trim();
        if (name.length() > MAX_NAME_LENGTH) {
            return failure("invalid_name");
        }
        if (!settings.contains("color", Tag.TAG_INT)) {
            return failure("invalid_color");
        }
        int color = settings.getInt("color") & 0xFFFFFF;

        Set<String> desiredEnemies = new HashSet<>();
        var enemyTags = settings.getList("enemyFactions", Tag.TAG_STRING);
        for (int i = 0; i < enemyTags.size(); i++) {
            String enemyId = enemyTags.getString(i);
            try {
                String normalizedEnemyId = Faction.normalizeId(enemyId);
                if (normalizedEnemyId.equals(faction.getId())
                        || data.getFaction(normalizedEnemyId).isEmpty()
                        || !desiredEnemies.add(normalizedEnemyId)) {
                    return failure("invalid_enemy");
                }
            } catch (IllegalArgumentException ignored) {
                return failure("invalid_enemy");
            }
        }

        faction.setName(name)
                .setColor(color)
                .setFriendlyFire(settings.getBoolean("friendlyFire"))
                .setAttackEnemyFactions(settings.getBoolean("attackEnemyFactions"));

        String id = faction.getId();
        Set<String> currentEnemies = new HashSet<>(faction.getEnemyFactions());
        currentEnemies.stream()
                .filter(enemyId -> !desiredEnemies.contains(enemyId))
                .forEach(enemyId -> data.removeEnemyFaction(id, enemyId));
        desiredEnemies.stream()
                .filter(enemyId -> !currentEnemies.contains(enemyId))
                .forEach(enemyId -> data.addEnemyFaction(id, enemyId));

        data.saveFaction(faction);
        FactionNameTagSync.syncAll(player.server);
        return success("updated");
    }

    public static Result deleteFaction(ServerPlayer player, String factionId) {
        if (!canEdit(player)) {
            return failure("no_permission");
        }
        try {
            if (!ViScriptTeamServerUtil.deleteFaction(player.serverLevel(), factionId)) {
                return failure("not_found");
            }
        } catch (IllegalArgumentException ignored) {
            return failure("not_found");
        }
        return success("deleted");
    }

    public static boolean canEdit(ServerPlayer player) {
        return player.createCommandSourceStack().hasPermission(2);
    }

    private static Result success(String key) {
        return new Result(true, RESULT_PREFIX + key);
    }

    private static Result failure(String key) {
        return new Result(false, RESULT_PREFIX + "error." + key);
    }

    public record Result(boolean success, String messageKey) {
    }
}
