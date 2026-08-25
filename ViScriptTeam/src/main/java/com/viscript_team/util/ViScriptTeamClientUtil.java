package com.viscript_team.util;

import com.lowdragmc.lowdraglib2.integration.kjs.KJSBindings;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.client.ClientFactionNameTagCache;
import com.viscript_team.data.faction.FactionAttitude;
import dev.latvian.mods.kubejs.typings.Info;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

@KJSBindings(value = "ViScriptTeamUtil", modId = ViScriptTeam.MOD_ID, clientOnly = true)
@UtilityClass
public class ViScriptTeamClientUtil {
    @Nullable
    @Info("客户端获取实体相对当前玩家的阵营态度缓存，不存在时返回 null")
    public static FactionAttitude getEntityAttitude(Entity entity) {
        return getEntityAttitude(entity.getUUID());
    }

    @Nullable
    @Info("客户端通过实体 UUID 获取相对当前玩家的阵营态度缓存，不存在时返回 null")
    public static FactionAttitude getEntityAttitude(UUID entityId) {
        return ClientFactionNameTagCache.getAttitude(entityId);
    }

    @Info("客户端清空阵营名牌缓存")
    public static void clearFactionNameTagCache() {
        ClientFactionNameTagCache.clear();
    }
}
