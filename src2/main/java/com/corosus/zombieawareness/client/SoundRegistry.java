package com.corosus.zombieawareness.client;

import java.util.HashMap;

import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundRegistry {

	private static HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<>();

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void onSoundsRegistry(final RegistryEvent.Register<SoundEvent> event) {
			SoundRegistry.init();
		}
	}

	public static void init() {
		register("alert");
		register("target");
		register("investigate");
	}

	public static void register(String soundPath) {
		ResourceLocation resLoc = new ResourceLocation(ZombieAwareness.MODID, soundPath);
		SoundEvent event = new SoundEvent(resLoc).setRegistryName(resLoc);
		if (lookupStringToEvent.containsKey(soundPath)) {
			System.out.println("ZA SOUNDS WARNING: duplicate sound registration for " + soundPath);
		} else {
			ForgeRegistries.SOUND_EVENTS.register(event);
			lookupStringToEvent.put(soundPath, event);
		}
	}

	public static SoundEvent get(String soundPath) {
		return lookupStringToEvent.get(soundPath);
	}

}
