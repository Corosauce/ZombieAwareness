package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer {

    @Inject(method = "initServer",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(CallbackInfoReturnable<Boolean> cir) {
        ZombieAwareness.unitTest("1");
        ZombieAwareness.serverStarting();
    }
}
