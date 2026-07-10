/*
package com.viscript_lib.accessor;

import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.RegistryAccessor;
import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

*/
/**
 * VSL 自己需要在 LDLib2 RPC 扫描前注册的访问器。
 *//*

public final class ViScriptLibAccessors {
    private ViScriptLibAccessors() {
    }

    @ViScriptRegisterAccessors
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(RegisterAccessorEvent event) {
        event.register(RegistryAccessor.of(
                DataComponentType.class,
                (Registry) BuiltInRegistries.DATA_COMPONENT_TYPE
        ));
    }
}
*/
