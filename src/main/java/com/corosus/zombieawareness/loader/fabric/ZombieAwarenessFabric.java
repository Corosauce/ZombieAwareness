package com.corosus.zombieawareness.loader.fabric;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.config.MobListsConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;

public class ZombieAwarenessFabric extends ZombieAwareness implements ModInitializer {

	public static MinecraftServer minecraftServer = null;

	public ZombieAwarenessFabric() {
		super();
		new WatutNetworkingFabric();

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "MobLists.toml");
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
			ZombieAwarenessFabric.minecraftServer = minecraftServer;
		});
		ServerPlayNetworking.registerGlobalReceiver(WatutNetworkingFabric.NBT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			CompoundTag nbt = buf.readNbt();
			server.execute(() -> {
				if (player != null) {
					ZombieAwareness.getPlayerStatusManagerServer().receiveAny(player, nbt);
				}
			});
		});
	}

	@Override
	public PlayerList getPlayerList() {
		return minecraftServer.getPlayerList();
	}

	@Override
	public boolean isModInstalled(String modID) {
		return FabricLoader.getInstance().isModLoaded(modID);
	}
}