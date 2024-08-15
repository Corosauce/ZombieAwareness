package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(StartAttacking.class)
public abstract class MixinTargettingFabric {

    @Inject(method = "method_47123",
            at = @At(value = "HEAD"))
    private static void hook(Predicate canAttack, Function targetFinder, MemoryAccessor memoryAccessor, MemoryAccessor memoryAccessor2, ServerLevel serverLevel, Mob mob, long l, CallbackInfoReturnable<Boolean> cir) {
        Optional<? extends LivingEntity> optional = (Optional)targetFinder.apply(mob);
        if (!optional.isEmpty()) {
            LivingEntity livingEntity = optional.get();
            ZAUtil.test(mob, livingEntity);
            if (mob.canAttack(livingEntity)) {
                ZAUtil.hookSetAttackTarget(mob, livingEntity);
            }
        }
    }
}
