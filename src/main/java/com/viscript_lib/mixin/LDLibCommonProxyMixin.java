package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.CommonProxy;
import com.viscript_lib.accessor.PreRpcAccessorBootstrap;
import net.neoforged.bus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonProxy.class)
public class LDLibCommonProxyMixin {

    @Inject(
            method = "init(Lnet/neoforged/bus/api/IEventBus;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/lowdragmc/lowdraglib2/networking/rpc/RPCPacketDistributor;init()V",
                    shift = At.Shift.AFTER

            )
    )
    private static void viscript_lib$registerAccessorsBeforeRpc(IEventBus eventBus, CallbackInfo ci) {
        PreRpcAccessorBootstrap.bootstrap();
    }
}
