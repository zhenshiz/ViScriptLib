package com.viscript_team.event;

import com.viscript_team.ai.FactionTargetGoal;
import com.viscript_team.network.FactionNameTagSync;
import com.viscript_team.util.FactionApi;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@UtilityClass
public class FactionEvents {
    private static final Set<Mob> FACTION_GOAL_MOBS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int FACTION_TARGET_GOAL_PRIORITY = 4;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob) || !FACTION_GOAL_MOBS.add(mob)) {
            return;
        }
        // 低优先级注入，只在原版/其他模组没有更高优先级目标时补上阵营索敌。
        mob.targetSelector.addGoal(FACTION_TARGET_GOAL_PRIORITY, new FactionTargetGoal(mob));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FactionNameTagSync.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FactionNameTagSync.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FactionNameTagSync.sync(player);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingDamageEvent event) {
        // 伤害事件是最终兜底，避免自定义 AI 绕过友伤规则。
        if (!FactionApi.canHurtFromSource(event.getSource(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() == null) {
            return;
        }
        // 这里主要拦原版/NeoForge 目标切换，自定义 AI 可以直接调用 FactionApi.canTarget。
        if (!FactionApi.canTarget(event.getEntity(), event.getNewTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target != null && !FactionApi.canTarget(mob, target)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            mob.setAggressive(false);
        }
    }
}
