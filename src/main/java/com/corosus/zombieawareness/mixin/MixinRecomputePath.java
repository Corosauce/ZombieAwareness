package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.config.ZAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * I noticed when mobs were pathfollowing to a sense, they would stop randomly, I discovered if I disabled path recomputing, it fixed it.
 * So I am preventing it from running during the cooldown I use for other things, might prevent
 */
@Mixin(PathNavigation.class)
public abstract class MixinRecomputePath {

    @Shadow
    protected Mob mob;

    @Inject(method = "shouldRecomputePath",
            at = @At(value = "HEAD"), cancellable = true)
    public void shouldRecomputePath(BlockPos p_200904_, CallbackInfoReturnable<Boolean> cir) {
        long lastActionTime = mob.getPersistentData().getLong(ZAUtil.ZA_LAST_ACTION);
        if (lastActionTime > 0 && mob.level.getGameTime() - ZAConfig.tickCooldownBetweenPathfinds < lastActionTime) {
            //System.out.println("cancelling random wander");
            cir.setReturnValue(false);
        }
    }
}
