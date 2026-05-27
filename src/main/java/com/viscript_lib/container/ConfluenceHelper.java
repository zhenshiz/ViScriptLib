package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.ItemUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.attachment.ExtraInventory;
import org.confluence.mod.common.attachment.PlayerPiggyBankContainer;

/**
 * 汇流来世 兼容硬币槽，弹药槽以及猪猪存钱罐
 */
@LDLRegister(name = Confluence.MODID, registry = IContainerHelper.CONTAINER_HELPER_ID, modID = Confluence.MODID)
public class ConfluenceHelper implements IContainerHelper {

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        int count = 0;

        //额外物品栏（硬币槽、弹药槽等）
        count += ItemUtil.getItemCountByContainer(ExtraInventory.of(player), item);

        // 存钱罐
        count += ItemUtil.getItemCountByContainer(PlayerPiggyBankContainer.of(player), item);

        return count;
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {

        //额外物品栏（硬币槽、弹药槽等）
        count = ItemUtil.removeItemByContainer(ExtraInventory.of(player), item, count);

        // 存钱罐
        count = ItemUtil.removeItemByContainer(PlayerPiggyBankContainer.of(player), item, count);

        return count;
    }
}
