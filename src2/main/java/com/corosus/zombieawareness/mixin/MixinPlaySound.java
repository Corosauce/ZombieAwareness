package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(ServerWorld.class)
public class MixinPlaySound {

    @Redirect(method = "playSound",
    at = @At(value = "INVOKE",
    target = "Lnet/minecraft/server/management/PlayerList;broadcast(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/RegistryKey;Lnet/minecraft/network/IPacket;)V"))
    public void broadcast(PlayerList playerList, @Nullable PlayerEntity pExcept, double pX, double pY, double pZ, double pRadius, RegistryKey<World> pDimension, IPacket<?> pPacket) {
        playerList.broadcast(pExcept, pX, pY, pZ, pRadius, pDimension, pPacket);

        SPlaySoundEffectPacket packet = (SPlaySoundEffectPacket) pPacket;
        SoundEvent sound = packet.sound;
        World world = playerList.getServer().getLevel(pDimension);
        if (world != null) {
            ZAUtil.hookSoundEvent(sound, world, pX, pY, pZ, 1, 1);
        }
    }


}
