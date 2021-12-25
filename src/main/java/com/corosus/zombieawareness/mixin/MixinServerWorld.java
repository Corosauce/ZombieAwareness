package com.corosus.zombieawareness.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;

@Mixin(ServerWorld.class)
public class MixinServerWorld {

    /**
     * @author
     */
    @Overwrite
    public void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch) {
        net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pSound, pCategory, pVolume, pPitch);
        if (event.isCanceled() || event.getSound() == null) return;
        pSound = event.getSound();
        pCategory = event.getCategory();
        pVolume = event.getVolume();
        //this.server.getPlayerList().broadcast(pPlayer, pX, pY, pZ, pVolume > 1.0F ? (double)(16.0F * pVolume) : 16.0D, this.dimension(), new SPlaySoundEffectPacket(pSound, pCategory, pX, pY, pZ, pVolume, pPitch));
    }

}
