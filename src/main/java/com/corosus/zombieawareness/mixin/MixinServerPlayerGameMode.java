package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Shadow
    public ServerLevel level;

    @Shadow
    public ServerPlayer player;

    @Inject(method = "destroyBlock",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        ZombieAwareness.unitTest("16");
        if (!level.isClientSide) {
            if (!ZAUtil.isZombieAwarenessActive(level)) return;
            if (!ZAConfigGeneral.blockHittingEvent_Active) return;
            ZombieAwareness.dbg("HarvestDrops event");
            ZAUtil.handleBlockBasedEvent(player, level, pPos, ZAConfigGeneral.blockHittingEvent_OddsTo1);
        }
    }
}
