package com.viscript_lib.mixin;

import com.lowdragmc.lowdraglib2.CommonProxy;
import com.viscript_lib.accessor.PreRpcAccessorBootstrap;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommonProxy.class, remap = false)
public class LDLibCommonProxyMixin {

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/lowdragmc/lowdraglib2/networking/rpc/RPCPacketDistributor;init()V"
            )
    )
    private static void viscript_lib$registerAccessorsBeforeRpc(IEventBus eventBus, CallbackInfo ci) {
        PreRpcAccessorBootstrap.bootstrap();
    }
}
