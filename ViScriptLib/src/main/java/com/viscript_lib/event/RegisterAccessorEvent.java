package com.viscript_lib.event;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.RegistryAccessor;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;

import java.util.function.Supplier;

/**
 * VSL 早期访问器注册上下文。
 *
 * <p>这个类不会再通过 NeoForge 事件总线分发。需要注册访问器的附属模组应提供
 * {@code @ViScriptRegisterAccessors} 静态方法，并在方法参数里接收本对象。
 */
public class RegisterAccessorEvent {

    /**
     * 注册普通可持久化数据类的直接访问器。
     */
    public <T> void register(Class<T> type, Supplier<T> factory) {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(type)
                .codec(PersistedParser.createCodec(factory))
                .streamCodec(PersistedParser.createStreamCodec(factory))
                .codecMark()
                .build(), 0);
    }

    /**
     * 注册基于 Minecraft 注册表 id 序列化的访问器。
     */
    public void register(RegistryAccessor<?> accessor) {
        AccessorRegistries.registerAccessor(accessor, 100);
    }
}
