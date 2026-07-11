package com.viscript_lib.container;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_lib.register.IContainerHelper;
import com.viscript_lib.util.item.ItemStackCompareMode;
import com.viscript_lib.util.item.ItemUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Lightmans Currency兼容 玩家穿戴的钱袋
 */
@LDLRegister(name = LightmansCurrency.MODID, registry = IContainerHelper.CONTAINER_HELPER_ID, modID = LightmansCurrency.MODID)
public class LightmansCurrencyHelper implements IContainerHelper {
    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item) {
        return getItemStackCount(player, item, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int getItemStackCount(ServerPlayer player, ItemStack item,
                                 ItemStackCompareMode compareMode,
                                 List<String> components) {
        if (!CoinAPI.getApi().IsCoin(item, false)) {
            return 0;
        }

        WalletHandler walletHandler = new WalletHandler(player);
        ItemStack wallet = walletHandler.getWallet();
        if (!WalletItem.isWallet(wallet)) {
            return 0;
        }

        return ItemUtil.getItemCountByContainer(WalletItem.getWalletInventory(wallet), item, compareMode, components);
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count) {
        return removeItemStackByCount(player, item, count, ItemStackCompareMode.ALL_COMPONENTS, List.of());
    }

    @Override
    public int removeItemStackByCount(ServerPlayer player, ItemStack item, int count,
                                      ItemStackCompareMode compareMode,
                                      List<String> components) {
        if (!CoinAPI.getApi().IsCoin(item, false)) {
            return count;
        }

        WalletHandler walletHandler = new WalletHandler(player);
        ItemStack wallet = walletHandler.getWallet();
        if (!WalletItem.isWallet(wallet)) {
            return count;
        }

        Container contents = WalletItem.getWalletInventory(wallet);
        count = ItemUtil.removeItemByContainer(contents, item, count, compareMode, components);

        return count;
    }
}
