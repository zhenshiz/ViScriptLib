package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.attachment.ExtraInventory;
import org.confluence.mod.common.attachment.PlayerPiggyBankContainer;

import java.util.List;

/**
 * 汇流来世 兼容硬币槽，弹药槽以及猪猪存钱罐
 */
@LDLRegister(name = Confluence.MODID, registry = IContainerHelper.CONTAINER_HELPER_ID, modID = Confluence.MODID)
public class ConfluenceHelper implements IContainerHelper {

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item,
                                 ItemStackCompareMode compareMode,
                                 List<DataComponentType<?>> components) {
        int count = 0;

        //额外物品栏（硬币槽、弹药槽等）
        count += ItemUtil.getItemCountByContainer(ExtraInventory.of(player), item, compareMode, components);

        // 存钱罐
        count += ItemUtil.getItemCountByContainer(PlayerPiggyBankContainer.of(player), item, compareMode, components);

        return count;
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count,
                                      ItemStackCompareMode compareMode,
                                      List<DataComponentType<?>> components) {

        //额外物品栏（硬币槽、弹药槽等）
        count = ItemUtil.removeItemByContainer(ExtraInventory.of(player), item, count, compareMode, components);

        // 存钱罐
        count = ItemUtil.removeItemByContainer(PlayerPiggyBankContainer.of(player), item, count, compareMode, components);

        return count;
    }
}
