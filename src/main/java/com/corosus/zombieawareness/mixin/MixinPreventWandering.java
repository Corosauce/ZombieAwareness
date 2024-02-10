package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomStrollGoal.class)
public abstract class MixinPreventWandering {

    @Shadow
    protected PathfinderMob mob;

    @Inject(method = "canUse",
            at = @At(value = "HEAD"), cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        long lastActionTime = mob.getPersistentData().getLong(ZAUtil.ZA_LAST_ACTION);
        if (lastActionTime > 0 && mob.level().getGameTime() - ZAConfigGeneral.tickCooldownBetweenPathfinds < lastActionTime) {
            //System.out.println("cancelling random wander");
            cir.setReturnValue(false);
        }
    }
}
