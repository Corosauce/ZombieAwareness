package com.corosus.zombieawareness.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MixinTargetting1 {

    @Inject(method = "setTarget",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(LivingEntity pTarget, CallbackInfo ci) {

    }
}
