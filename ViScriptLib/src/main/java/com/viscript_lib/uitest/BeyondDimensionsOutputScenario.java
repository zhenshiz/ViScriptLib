package com.viscript_lib.uitest;

import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.util.item.ItemOutputTargets;
import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 使用真实维度网络验证数量上限、种类上限和溢出回调。 */
@LDLRegisterClient(name = "vsl_dimension_output", group = ViScriptLib.MOD_ID, registry = UIScenario.REGISTRY,
        modID = "beyonddimensions", environment = RegistrationEnvironment.DEV_ONLY)
public final class BeyondDimensionsOutputScenario implements UIScenario {
    @Override
    public void define(ScenarioBuilder s) {
        s.server("unavailable network falls back to inventory", sc -> {
            ItemOutputScenario.prepare(sc, false);
            sc.check("isolated player has no network", !DimensionsNet.hasPrimaryNet(sc.player()));
            sc.check("fallback delivers", ItemOutputTargets.giveItem(sc.player(), ItemOutputTargets.BEYOND_DIMENSIONS,
                    ItemOutputScenario.diamonds(1), 5) == 0);
            ItemOutputScenario.checkDelivery(sc, Items.DIAMOND, 5, 0);
        }).server("network quantity capacity drops overflow despite empty inventory", sc -> {
            ItemOutputScenario.prepare(sc, false);
            var network = DimensionsNet.createNewNetForPlayer(sc.player(), 64, 1);
            sc.put("network", network);
            DimensionsNet.setPrimaryNetForPlayer(sc.player(), network);
            sc.check("network available", ItemOutputTargets.resolve(ItemOutputTargets.BEYOND_DIMENSIONS).isItemOutputAvailable(sc.player()));
            sc.check("all delivered", ItemOutputTargets.giveItem(sc.player(), ItemOutputTargets.BEYOND_DIMENSIONS,
                    ItemOutputScenario.diamonds(1), 70) == 0);
            sc.check("network stores its capacity", ItemOutputTargets.resolve(ItemOutputTargets.BEYOND_DIMENSIONS)
                    .getItemStackCount(sc.player(), ItemOutputScenario.diamonds(1)) == 64);
            ItemOutputScenario.checkDelivery(sc, Items.DIAMOND, 0, 6);
            ItemOutputScenario.checkFeet(sc);
        }).server("network type limit rejects new type", sc -> {
            ItemOutputScenario.prepare(sc, true);
            sc.check("all delivered", ItemOutputTargets.giveItem(sc.player(), ItemOutputTargets.BEYOND_DIMENSIONS,
                    new ItemStack(Items.GOLD_INGOT), 9) == 0);
            sc.check("network rejects second resource type", ItemOutputTargets.resolve(ItemOutputTargets.BEYOND_DIMENSIONS)
                    .getItemStackCount(sc.player(), new ItemStack(Items.GOLD_INGOT)) == 0);
            ItemOutputScenario.checkDelivery(sc, Items.GOLD_INGOT, 0, 9);
            ItemOutputScenario.checkFeet(sc);
        }).server("network overflow uses custom callback", sc -> {
            ItemOutputScenario.prepare(sc, false);
            long[] rejected = {0};
            long remaining = ItemOutputTargets.giveItem(sc.player(), ItemOutputTargets.BEYOND_DIMENSIONS,
                    ItemOutputScenario.diamonds(1), 7, (player, item, count) -> { rejected[0] = count; return count; });
            sc.check("callback gets full remainder", rejected[0] == 7 && remaining == 7);
            ItemOutputScenario.checkDelivery(sc, Items.DIAMOND, 0, 0);
        }).server("available network stores long quantities without drops", sc -> {
            var network = (DimensionsNet) sc.get("network");
            network.getUnifiedStorage().clearStorage();
            network.getUnifiedStorage().setSlotCapacity(Long.MAX_VALUE);
            long count = (long) Integer.MAX_VALUE + 5;
            sc.check("long amount delivered", ItemOutputTargets.giveItem(sc.player(), ItemOutputTargets.BEYOND_DIMENSIONS,
                    ItemOutputScenario.diamonds(1), count) == 0);
            sc.check("long amount preserved", ItemOutputTargets.resolve(ItemOutputTargets.BEYOND_DIMENSIONS)
                    .getItemStackCount(sc.player(), ItemOutputScenario.diamonds(1)) == count);
            ItemOutputScenario.checkDelivery(sc, Items.DIAMOND, 0, 0);
        }).teardownServer("clear isolated dimension network", sc -> {
            ItemOutputScenario.prepare(sc, false);
            DimensionsNet network = sc.get("network");
            if (network != null) network.destroySelf();
        });
    }
}
