package com.viscript_lib.util;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_lib.annotation.DontSkipPersisted;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * ldlib2会自动序列化被注解了{@link com.lowdragmc.lowdraglib2.configurator.annotation.Configurable}和{@link com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted}的字段，即使该字段的值可能没必要序列化（等于默认值）。
 * <p>ldlib2提供了{@link com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue}用于给任意字段添加自定义的跳过序列化的方法，它的自由度高，但是对于拥有大量字段的数据类，并且我只需要该类的字段值等于默认值的时候跳过序列化，全部使用该注解会特别麻烦。此接口就是用于解决这个麻烦的。
 * <p>只要你的数据类实现了这个接口，该类实例中所有等于默认值的应该被ldlib2序列化的字段都会被跳过。
 * <p>如果你的类实现了这个接口，但不希望默认值被跳过序列化，可以使用{@link DontSkipPersisted}。
 * <p>此接口功能的实现完全兼容{@link com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue}，你依然可以用该注解添加自定义的跳过序列化方法。相比于该注解，这个接口跳过序列化的优先级更高。
 *
 * @see com.viscript_lib.mixin.PersistedParserMixin 功能实现
 * @author Amotassic
 */
public interface ISkipDefaultedSerialize extends IPersistedSerializable {
    HashMap<String, HashMap<String, Object>> defaultValues = new HashMap<>();

    @Override
    default void beforeSerialize() {
        var clazz = getClass();
        if (clazz.isAnnotationPresent(DontSkipPersisted.class)) return;
        if (defaultValues.containsKey(clazz.getName())) return;

        IPersistedSerializable instance;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            return;
        }

        var map = new HashMap<String, Object>();
        for (Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) /*|| Modifier.isTransient(mod)*/ || field.isAnnotationPresent(DontSkipPersisted.class)) continue;
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(instance));
            } catch (Exception ignored) {}
        }
        defaultValues.put(clazz.getName(), map);
    }
}
