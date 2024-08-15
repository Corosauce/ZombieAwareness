package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.client.SoundProfileEntry;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Explosion.class)
public abstract class MixinExplosion {

    @Shadow
    public Level level;

    @Shadow
    public double x;

    @Shadow
    public double y;

    @Shadow
    public double z;

    @Inject(method = "explode",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(CallbackInfo ci) {
        ZombieAwareness.unitTest("4");
        Explosion explosion = ((Explosion)(Object)this);
        SoundProfileEntry entry = ZAUtil.getSoundIDEntry(SoundEvents.GENERIC_EXPLODE.getLocation().toString());
        if (entry != null) {
            Vec3 pos = new Vec3(x, y, z);
            Player closestPlayer = ZAUtil.getClosestPlayer(level, pos.x, pos.y, pos.z, 128);
            if (closestPlayer != null) {
                ZAUtil.handleSoundProfileEvent(level, entry, new Vector3d(pos.x, pos.y, pos.z), closestPlayer);
            }
        }
    }
}
