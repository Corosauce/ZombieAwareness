package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinPlaySound {

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"))
    public void injectBroadcast(Player pExcept, double pX, double pY, double pZ, Holder<SoundEvent> sound, SoundSource soundSource, float volume, float pitch, long seed, CallbackInfo ci) {
        ZAUtil.hookSoundEvent(sound.get(), ((ServerLevel)(Object)this), pX, pY, pZ, volume, pitch);
    }
}
