package com.viscript_lib.uitest;

import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ServerContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.util.item.ItemOutputTargets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.List;

/** 在真实服务端验证 VSL 发货的入包、脚下掉落和自定义溢出回调。 */
@LDLRegisterClient(name = "vsl_item_output", group = ViScriptLib.MOD_ID, registry = UIScenario.REGISTRY,
        environment = RegistrationEnvironment.DEV_ONLY)
public final class ItemOutputScenario implements UIScenario {
    @Override
    public void define(ScenarioBuilder s) {
        s.server("full inventory drops complete output at player feet", sc -> {
            prepare(sc, true);
            var template = diamonds(7);
            long remaining = ItemOutputTargets.giveItem(sc.player(), ItemOutputTargets.PLAYER_INVENTORY, template, 130);
            sc.check("all delivered", remaining == 0);
            sc.check("caller template preserved", template.getCount() == 7);
            checkDelivery(sc, Items.DIAMOND, 0, 130);
            sc.check("components preserved", drops(sc.player()).stream()
                    .allMatch(entity -> ItemStack.isSameItemSameComponents(entity.getItem(), template)));
            checkFeet(sc);
        }).server("free inventory and pick up dropped output", sc -> {
            sc.player().getInventory().clearContent();
            for (var entity : drops(sc.player())) {
                entity.setNoPickUpDelay();
                entity.playerTouch(sc.player());
            }
            checkDelivery(sc, Items.DIAMOND, 130, 0);
        }).server("partial capacity drops only rejected amount", sc -> {
            prepare(sc, true);
            sc.player().getInventory().setItem(0, diamonds(60));
            sc.check("all delivered", ItemOutputTargets.giveItem(sc.player(), "inventory", diamonds(1), 130) == 0);
            checkDelivery(sc, Items.DIAMOND, 64, 126);
        }).server("empty inventory receives all without drops", sc -> {
            prepare(sc, false);
            sc.check("all delivered", ItemOutputTargets.giveItem(sc.player(), "inventory", diamonds(130)) == 0);
            checkDelivery(sc, Items.DIAMOND, 130, 0);
        }).server("non-stackable items are separate drops", sc -> {
            prepare(sc, true);
            sc.check("all swords delivered", ItemOutputTargets.giveItem(sc.player(), "inventory", new ItemStack(Items.DIAMOND_SWORD), 3) == 0);
            checkDelivery(sc, Items.DIAMOND_SWORD, 0, 3);
            sc.check("three entities", drops(sc.player()).size() == 3);
        }).server("unknown output falls back to inventory", sc -> {
            prepare(sc, false);
            sc.check("all delivered", ItemOutputTargets.giveItem(sc.player(), "missing_target", diamonds(1), 5) == 0);
            checkDelivery(sc, Items.DIAMOND, 5, 0);
        }).server("custom overflow callback owns exactly the remainder", sc -> {
            prepare(sc, true);
            sc.player().getInventory().setItem(0, diamonds(60));
            int[] calls = {0};
            long[] rejected = {0};
            var template = diamonds(7);
            long remaining = ItemOutputTargets.giveItem(sc.player(), "inventory", template, 10, (player, item, count) -> {
                calls[0]++;
                rejected[0] = count;
                sc.check("callback uses receiving player", player == sc.player());
                sc.check("callback receives component preserving unit copy", item.getCount() == 1
                        && ItemStack.isSameItemSameComponents(item, template));
                item.setCount(99);
                return 2;
            });
            sc.check("callback once for six rejected items", calls[0] == 1 && rejected[0] == 6);
            sc.check("unhandled amount returned", remaining == 2);
            sc.check("callback cannot mutate caller template", template.getCount() == 7);
            checkDelivery(sc, Items.DIAMOND, 64, 0);
        }).server("callback skipped without overflow", sc -> {
            prepare(sc, false);
            int[] calls = {0};
            ItemOutputTargets.OverflowHandler handler = (player, item, count) -> { calls[0]++; return count; };
            sc.check("all inserted", ItemOutputTargets.giveItem(sc.player(), "inventory", diamonds(1), 3, handler) == 0);
            sc.check("empty template leaves requested amount", ItemOutputTargets.giveItem(sc.player(), "inventory", ItemStack.EMPTY, 8, handler) == 8);
            sc.check("negative count ignored", ItemOutputTargets.giveItem(sc.player(), "inventory", diamonds(1), -5, handler) == 0);
            sc.check("callback not invoked", calls[0] == 0);
        }).teardownServer("clear test output", sc -> prepare(sc, false));
    }

    static void prepare(ServerContext sc, boolean full) {
        sc.player().setGameMode(GameType.SURVIVAL);
        drops(sc.player()).forEach(ItemEntity::discard);
        sc.player().getInventory().clearContent();
        if (full) {
            for (int i = 0; i < sc.player().getInventory().items.size(); i++) {
                sc.player().getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
            }
        }
    }

    static List<ItemEntity> drops(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(16), ItemEntity::isAlive);
    }

    static void checkDelivery(ServerContext sc, Item item, long inventoryExpected, long droppedExpected) {
        long inventory = sc.player().getInventory().items.stream().filter(stack -> stack.is(item)).mapToLong(ItemStack::getCount).sum();
        long dropped = drops(sc.player()).stream().map(ItemEntity::getItem).filter(stack -> stack.is(item)).mapToLong(ItemStack::getCount).sum();
        sc.check(item + " inventory count", inventory == inventoryExpected, inventoryExpected, inventory);
        sc.check(item + " dropped count", dropped == droppedExpected, droppedExpected, dropped);
        sc.check("drops respect stack limits", drops(sc.player()).stream()
                .allMatch(entity -> entity.getItem().getCount() <= entity.getItem().getMaxStackSize()));
    }

    static void checkFeet(ServerContext sc) {
        sc.check("drops spawn at feet without throwing velocity", drops(sc.player()).stream().allMatch(entity ->
                entity.getX() == sc.player().getX() && entity.getZ() == sc.player().getZ()
                        && Math.abs(entity.getY() - sc.player().getY() - 0.1) < 0.00001
                        && entity.getDeltaMovement().lengthSqr() == 0));
        sc.check("pickup belongs to recipient", drops(sc.player()).stream()
                .allMatch(entity -> sc.player().getUUID().equals(entity.getTarget())));
    }

    static ItemStack diamonds(int count) {
        var stack = new ItemStack(Items.DIAMOND, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Output test diamond"));
        return stack;
    }
}
