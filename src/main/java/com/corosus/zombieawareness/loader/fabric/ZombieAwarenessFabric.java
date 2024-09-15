package com.corosus.zombieawareness.loader.fabric;

import com.corosus.zombieawareness.EntityScent;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.config.MobListsConfig;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;
import java.util.function.Supplier;

public class ZombieAwarenessFabric extends ZombieAwareness implements ModInitializer {

	public static MinecraftServer minecraftServer = null;

	public static Supplier<EntityType<EntityScent>> SENSE_SUP;

	static {
		SENSE = EntityType.Builder.
				of(EntityScent::new, MobCategory.MISC)
				.updateInterval(20)
				.clientTrackingRange(128)
				.sized(0f, 0f).build(SENSE_NAME.toString());

		SENSE_SUP = registerImpl(BuiltInRegistries.ENTITY_TYPE, SENSE_NAME, () -> SENSE);
	}

	static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ZombieAwareness.MODID, name);
		return registerImpl(BuiltInRegistries.ENTITY_TYPE, id, () -> builder.build(id.toString()));
	}

	public static <T> Supplier<T> registerImpl(Registry<? super T> registry, ResourceLocation id, Supplier<T> obj) {
		T register = Registry.register(registry, id, obj.get());
		return () -> register;
	}

	public ZombieAwarenessFabric() {
		super();

		ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, MobListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "MobLists.toml");

		//ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, SoundsListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "SoundLists.toml");

		//FabricDefaultAttributeRegistry.register(SENSE, EntityScent.createMobAttributes());

		init();
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
			ZombieAwarenessFabric.minecraftServer = minecraftServer;
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

	@Override
	public SoundEvent register(String name) {
		SoundEvent soundEvent = super.register(name);
		Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(ZombieAwareness.MODID, name), soundEvent);
		return soundEvent;
	}
}