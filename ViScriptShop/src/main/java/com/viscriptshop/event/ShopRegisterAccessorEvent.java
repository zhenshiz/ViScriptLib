package com.viscriptshop.event;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscriptshop.ShopRegistries;
import com.viscriptshop.gui.data.*;

public final class ShopRegisterAccessorEvent {
    private ShopRegisterAccessorEvent() {
    }

    @ViScriptRegisterAccessors
    public static void onRegisterAccessor(RegisterAccessorEvent event) {
        event.register(ItemMatchRule.class, ItemMatchRule::new);
        event.register(MerchantFlagGroup.class, MerchantFlagGroup::new);
        event.register(MerchantInfo.class, MerchantInfo::new);
        event.register(CategoryInfo.class, CategoryInfo::new);
        event.register(AggregatedResources.PurchaseEntry.class, AggregatedResources.PurchaseEntry::new);
        event.register(AggregatedResources.ItemEntry.class, AggregatedResources.ItemEntry::new);
        event.register(AggregatedResources.class, AggregatedResources::new);
        event.register(ShopInfo.class, ShopInfo::new);

        event.register(ShopRegistries.Money.class, ShopRegistries.Money::new);
    }
}
