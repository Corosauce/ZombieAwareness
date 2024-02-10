package com.corosus.zombieawareness.mixin.client;

import com.corosus.zombieawareness.ZAUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinPlaySoundClient {

    @Inject(method = "playSound",
            at = @At(value = "HEAD"))
    private void injectBroadcast(double pX, double pY, double pZ, SoundEvent sound, SoundSource soundSource, float volume, float pitch, boolean p_233610_, long p_233611_, CallbackInfo ci) {
        ZAUtil.hookSoundEventClient(sound, ((ClientLevel)(Object)this), pX, pY, pZ, volume, pitch);
    }
}
