package com.viscript_lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.viscript_lib.util.item.MissingItemStackRecovery;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CustomDirectAccessor.class, remap = false)
public abstract class CustomDirectAccessorMixin {
    @WrapOperation(
            method = "writeDirectVar",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private DataResult<?> viscript_lib$decodeItemStackInRecoveryScope(Codec<?> codec,
                                                                      DynamicOps<?> ops,
                                                                      Object payload,
                                                                      Operation<DataResult<?>> original) {
        var accessor = (CustomDirectAccessor<?>)(Object) this;
        if (accessor.getType() != ItemStack.class || !MissingItemStackRecovery.isRecoveryActive()) {
            return original.call(codec, ops, payload);
        }

        var result = original.call(codec, ops, payload);
        if (result.get().left().isPresent()) {
            return result;
        }

        var recovered = MissingItemStackRecovery.recoverSerializedStack((DynamicOps) ops, payload);
        return recovered.isPresent() ? DataResult.success(recovered.get()) : result;
    }
}
