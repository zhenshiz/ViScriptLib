package com.vss_market;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final ForgeConfigSpec.DoubleValue MIN_PRICE;
    public static final ForgeConfigSpec.DoubleValue MAX_PRICE;
    public static final ForgeConfigSpec.IntValue MAX_STACKS_PER_OPERATION;
    public static final ForgeConfigSpec.IntValue MAX_LISTINGS_PER_SHOP;
    public static final ForgeConfigSpec.IntValue MAX_SHOP_NAME_LENGTH;

    static {
        ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
        CONFIG_BUILDER.push("market");

        // 商品允许设置的最低单价。
        MIN_PRICE = CONFIG_BUILDER.defineInRange("minPrice", 1D, Double.MIN_VALUE, Double.MAX_VALUE);

        // 商品允许设置的最高单价。
        MAX_PRICE = CONFIG_BUILDER.defineInRange("maxPrice", 1_000_000_000D, Double.MIN_VALUE, Double.MAX_VALUE);

        // 单次上架、购买、补货允许处理的最大数量。
        MAX_STACKS_PER_OPERATION = CONFIG_BUILDER.defineInRange("maxStacksPerOperation", 100_000, 1, Integer.MAX_VALUE);

        // 每个玩家店铺允许拥有的最大商品条目数。
        MAX_LISTINGS_PER_SHOP = CONFIG_BUILDER.defineInRange("maxListingsPerShop", 200, 1, 10_000);

        // 玩家店铺名称允许的最大长度。
        MAX_SHOP_NAME_LENGTH = CONFIG_BUILDER.defineInRange("maxShopNameLength", 32, 1, 128);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }
}
