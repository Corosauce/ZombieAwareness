package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.players.PlayerList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(ServerLevel.class)
public class MixinLevelEvent {

    @Redirect(method = "levelEvent",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcast(Lnet/minecraft/world/entity/player/Player;DDDDLnet/minecraft/resources/ResourceKey;Lnet/minecraft/network/protocol/Packet;)V"))
    public void broadcast(PlayerList playerList, @Nullable Player pExcept, double pX, double pY, double pZ, double pRadius, ResourceKey<Level> pDimension, Packet<?> pPacket) {
        playerList.broadcast(pExcept, pX, pY, pZ, pRadius, pDimension, pPacket);

        ClientboundLevelEventPacket packet = (ClientboundLevelEventPacket) pPacket;
        int type = packet.type;
        Level world = playerList.getServer().getLevel(pDimension);
        if (world != null) {
            ZAUtil.hookPlayEvent(type, world, pX, pY, pZ, 0);
        }
    }


}
