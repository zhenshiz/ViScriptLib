package com.viscript_lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Field;

@Mixin(PersistedParser.class)
public class PersistedParserMixin {

    @WrapOperation(method = "serializeInternal", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Modifier;isStatic(I)Z"))
    private static boolean serializeInternal(int mod, Operation<Boolean> original, @Local(argsOnly = true) Object object, @Local(name = "field") Field field) {
        // 注入原方法的字段是否静态判断，搭便车以跳过字段的序列化
        boolean called = original.call(mod);
        if (called) return true;
        // 如果存储的默认值里面没有该类，则不跳过字段序列化
        var map = ISkipDefaultedSerialize.defaultValues.get(object.getClass().getName());
        if (map == null) return false;

        Object defaultValue = map.get(field.getName());
        if (defaultValue == null) return false;
        // 如果字段值与缓存的默认值相同，则让isStatic()返回true，跳过字段序列化
        Object value;
        field.setAccessible(true);
        try {
            value = field.get(object);
        } catch (Exception e) {
            return false;
        }
        // 针对ItemStack优化一手
        if (value instanceof ItemStack s1 && defaultValue instanceof ItemStack s2) return ItemStack.matches(s1, s2);
        return value != null && value.equals(defaultValue);
    }
}
