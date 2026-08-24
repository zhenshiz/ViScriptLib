package com.viscript_recipe.mixin;

import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.EnumAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(value = EnumAccessor.class, remap = false)
public abstract class EnumAccessorMixin {

    @Shadow @Final
    private static WeakHashMap<Class<? extends Enum<?>>, Map<String, Enum<?>>> enumNameCache;

    @Inject(method = "getEnum(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", at = @At(value = "HEAD"))
    private static void getEnum(Class<Enum<?>> type, String name, CallbackInfoReturnable<Enum<?>> cir) {
        var map = enumNameCache.get(type);
        if (map != null && map.isEmpty()) {
            enumNameCache.remove(type);
            var newMap = new HashMap<String, Enum<?>>();
            for (Enum<?> value : type.getEnumConstants()) {
                String enumName = EnumAccessor.getEnumName(value);
                newMap.put(enumName, value);
            }
            enumNameCache.put(type, newMap);
        }
    }
}
