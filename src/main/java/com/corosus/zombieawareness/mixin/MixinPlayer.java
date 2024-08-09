package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {

    @Inject(method = "getDigSpeed",
            at = @At(value = "HEAD"), cancellable = true, remap=false)
    public void hook(BlockState p_36282_, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Player ent = ((Player)(Object)this);
        if (!ent.level().isClientSide) {
            if (!ZAUtil.isZombieAwarenessActive(ent.level())) return;
            if (!ZAConfigGeneral.blockHittingEvent_Active) return;
            //ZombieAwareness.dbg("BreakSpeed event");
            ZAUtil.hookBlockEvent(ent, ZAConfigGeneral.blockHittingEvent_OddsTo1);
        }
    }

    @Inject(method = "tick",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook2(CallbackInfo ci) {
        Player ent = ((Player)(Object)this);
        if (ent.level().isClientSide) return;

        if (ent.level().getGameTime() % ZAConfigGeneral.tickRatePlayerLoop == 0) {

            ZAUtil.tickPlayer(ent);
        }
    }
}
