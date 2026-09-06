package com.viscript_lib.accessor;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_lib.util.item.ViScriptItemStack;

/**
 * VSL 自己需要在 LDLib2 RPC 扫描前注册的访问器。
 */
public final class ViScriptLibAccessors {
    private ViScriptLibAccessors() {
    }

    @ViScriptRegisterAccessors
    public static void register(RegisterAccessorEvent event) {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(ViScriptItemStack.class)
                .codec(ViScriptItemStack.CODEC)
                .streamCodec(ViScriptItemStack.STREAM_CODEC)
                .codecMark()
                .build(), 0);
    }
}
