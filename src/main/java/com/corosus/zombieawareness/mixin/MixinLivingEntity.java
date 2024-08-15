package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "tick",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(CallbackInfo ci) {
        ZombieAwareness.unitTest("7");
        LivingEntity ent = ((LivingEntity)(Object)this);
        if (ent.level().isClientSide) return;

        //ZombieAwarenessOld.tickEntity(ent);
        if ((ent.level().getGameTime() + ent.getId()) % Math.max(1, ZAConfigGeneral.tickRateAILoop) == 0) {
            if (ZombieAwareness.canProcessEntity(ent) && ent instanceof Mob) {
                ZAUtil.tickAI((Mob) ent);
            }
        }
    }
}
