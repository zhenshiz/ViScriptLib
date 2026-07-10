package com.viscriptshop;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec CONFIG_SPEC;

    //是否打开FTB Library的按钮来允许打开商店
    public static ForgeConfigSpec.BooleanValue showFtbLibraryButton = null;

    //是否启用旧版本数据迁移（.shop文件）
    //当确认所有.shop文件都是最新版本后，可以关闭此选项以提升性能
    public static ForgeConfigSpec.BooleanValue enableLegacyDataMigration;

    // 商店UI单次购买最多给予玩家多少个物品
    // -1 表示不限制
    public static ForgeConfigSpec.IntValue maxShopUiGiveItemsPerPurchase;

    // 是否使用玩家独立库存
    // false 表示所有玩家共享库存，true 表示每个玩家单独消耗库存
    public static ForgeConfigSpec.BooleanValue isPersonalStock;

    static {
        ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
        CONFIG_BUILDER.push("config");
        if (ViscriptShop.isFtbLibraryLoaded()) {
            showFtbLibraryButton = CONFIG_BUILDER.define("showFtbLibraryButton", false);
        }
        enableLegacyDataMigration = CONFIG_BUILDER.define("enableLegacyDataMigration", true);
        maxShopUiGiveItemsPerPurchase = CONFIG_BUILDER.defineInRange("maxShopUiGiveItemsPerPurchase", -1, -1, Integer.MAX_VALUE);
        isPersonalStock = CONFIG_BUILDER.define("isPersonalStock", false);
        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }
}
