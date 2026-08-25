package com.viscript_team;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec CONFIG_SPEC;
    // 玩家对新建阵营的默认声望点数。
    public static final ForgeConfigSpec.IntValue DEFAULT_FACTION_POINTS;
    // 玩家声望低于该值时，阵营会将玩家视为敌对；达到该值但低于友好阈值时视为中立。
    public static final ForgeConfigSpec.IntValue DEFAULT_NEUTRAL_POINTS;
    // 玩家声望达到或超过该值时，阵营会将玩家视为友好。
    public static final ForgeConfigSpec.IntValue DEFAULT_FRIENDLY_POINTS;
    // 新建阵营是否默认允许同阵营成员或友好玩家受到友伤。
    public static final ForgeConfigSpec.BooleanValue DEFAULT_FACTION_FRIENDLY_FIRE;
    // 新建阵营是否默认主动锁定敌对阵营列表中的目标。
    public static final ForgeConfigSpec.BooleanValue DEFAULT_ATTACK_ENEMY_FACTIONS;

    static {
        ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
        CONFIG_BUILDER.push("faction");
        DEFAULT_FACTION_POINTS = CONFIG_BUILDER.defineInRange("defaultFactionPoints", 50, Integer.MIN_VALUE, Integer.MAX_VALUE);
        DEFAULT_NEUTRAL_POINTS = CONFIG_BUILDER.defineInRange("neutralFactionPoints", 50, Integer.MIN_VALUE, Integer.MAX_VALUE);
        DEFAULT_FRIENDLY_POINTS = CONFIG_BUILDER.defineInRange("friendlyFactionPoints", 100, Integer.MIN_VALUE, Integer.MAX_VALUE);
        DEFAULT_FACTION_FRIENDLY_FIRE = CONFIG_BUILDER.define("defaultFactionFriendlyFire", false);
        DEFAULT_ATTACK_ENEMY_FACTIONS = CONFIG_BUILDER.define("defaultAttackEnemyFactions", true);
        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }
}
