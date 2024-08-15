package com.corosus.zombieawareness.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(StartAttacking.class)
public abstract class MixinTargetting2 {

    @Inject(method = "lambda$create$1",
            at = @At(value = "HEAD"), cancellable = true)
    private static void hook(Predicate pCanAttack, Function pTargetFinder, MemoryAccessor p_258778_, MemoryAccessor p_258779_, ServerLevel p_258773_, Mob p_258774_, long p_258775_, CallbackInfoReturnable<Boolean> cir) {

    }
}
