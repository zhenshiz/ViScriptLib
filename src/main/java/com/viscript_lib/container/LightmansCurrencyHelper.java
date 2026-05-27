package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscriptshop.util.ItemUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.data.WalletDataWrapper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Lightmans Currency兼容 玩家穿戴的钱袋
 */
@LDLRegister(name = LightmansCurrency.MODID, registry = IContainerHelper.CONTAINER_HELPER_ID, modID = LightmansCurrency.MODID)
public class LightmansCurrencyHelper implements IContainerHelper {
    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        if (!CoinAPI.getApi().IsCoin(item, false)) {
            return 0;
        }

        WalletHandler walletHandler = WalletHandler.get(player);
        ItemStack wallet = walletHandler.getWallet();
        if (!WalletItem.isWallet(wallet)) {
            return 0;
        }

        return ItemUtil.getItemCountByContainer(WalletItem.getDataWrapper(wallet).getContents(), item);
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {
        if (!CoinAPI.getApi().IsCoin(item, false)) {
            return 0;
        }

        WalletHandler walletHandler = WalletHandler.get(player);
        ItemStack wallet = walletHandler.getWallet();
        if (!WalletItem.isWallet(wallet)) {
            return 0;
        }

        WalletDataWrapper wrapper = WalletItem.getDataWrapper(wallet);
        Container contents = wrapper.getContents();
        count = ItemUtil.removeItemByContainer(contents, item, count);

        wrapper.setContents(contents, player);

        return count;
    }
}
