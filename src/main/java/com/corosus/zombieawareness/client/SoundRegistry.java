package com.corosus.zombieawareness.client;

import java.util.HashMap;

import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundRegistry {

	private static HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<>();

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ZombieAwareness.MODID);

	//public static final RegistryObject<SoundEvent> TESLA_ARMOR_EFFECT = SOUND_EVENTS.register("tesla_armor_effect", () -> new SoundEvent(new ResourceLocation(ZombieAwareness.MODID, "tesla_armor_effect")));

	public static void init() {
		register("alert");
		register("target");
		register("investigate");
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		SOUND_EVENTS.register(modEventBus);
	}

	public static void register(String name) {

		SoundEvent event = new SoundEvent(new ResourceLocation(ZombieAwareness.MODID, name));
		lookupStringToEvent.put(name, event);
		SOUND_EVENTS.register(name, () -> event);
	}

	public static SoundEvent get(String soundPath) {
		return lookupStringToEvent.get(soundPath);
	}

}
