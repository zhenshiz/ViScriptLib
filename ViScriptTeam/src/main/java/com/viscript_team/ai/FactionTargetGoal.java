package com.viscript_team.ai;

import com.viscript_team.util.FactionApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class FactionTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
    public FactionTargetGoal(Mob mob) {
        super(mob, LivingEntity.class, 10, true, false, target -> FactionApi.shouldActivelyTarget(mob, target));
    }

    @Override
    public boolean canUse() {
        // 没有阵营的实体不主动扫描，避免给普通原版 AI 增加额外负担。
        if (randomInterval > 0 && mob.getRandom().nextInt(randomInterval) != 0) {
            return false;
        }
        if (FactionApi.getEntityFactionId(mob).isEmpty()) {
            target = null;
            return false;
        }
        findTarget();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && FactionApi.shouldActivelyTarget(mob, target) && super.canContinueToUse();
    }
}
