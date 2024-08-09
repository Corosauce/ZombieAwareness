package com.corosus.zombieawareness.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StartAttacking.class)
public abstract class MixinTargetting2 {

    @Inject(method = "create(Ljava/util/function/Predicate;Ljava/util/function/Function;)Lnet/minecraft/world/entity/ai/behavior/BehaviorControl;",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(LivingEntity pTarget, CallbackInfo ci) {

    }
}
