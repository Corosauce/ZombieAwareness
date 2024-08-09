package com.corosus.zombieawareness.loader.forge;

import com.corosus.coroutil.config.ConfigCoroUtil;
import com.corosus.coroutil.util.CULog;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.WatutNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketNBTFromServer {
    private final CompoundTag nbt;

    public PacketNBTFromServer(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static void encode(PacketNBTFromServer msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.nbt);
    }

    public static PacketNBTFromServer decode(FriendlyByteBuf buffer) {
        return new PacketNBTFromServer(buffer.readNbt());
    }

    public static class Handler {
        public static void handle(final PacketNBTFromServer msg, Supplier<NetworkEvent.Context> ctx) {

            ctx.get().enqueueWork(() -> {
                try {
                    CompoundTag nbt = msg.nbt;
                    UUID uuid = UUID.fromString(nbt.getString(WatutNetworking.NBTDataPlayerUUID));
                    //ZombieAwareness.getPlayerStatusManagerClient().receiveAny(uuid, nbt);
                } catch (Exception ex) {
                    CULog.dbg("WATUT ERROR: packet with invalid uuid sent from server");
                    CULog.dbg("full nbt data: " + msg.nbt);
                    if (ConfigCoroUtil.useLoggingDebug) {
                        ex.printStackTrace();
                    }
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
