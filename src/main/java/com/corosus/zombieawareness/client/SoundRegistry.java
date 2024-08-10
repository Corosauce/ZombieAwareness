package com.corosus.zombieawareness.client;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.ZombieAwarenessClient;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;

public class SoundRegistry {

	/*private HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<>();

	public void init() {
		register("alert");
		register("target");
		register("investigate");

	}

	public void register(String name) {

		SoundEvent event = SoundEvent.createVariableRangeEvent(new ResourceLocation(ZombieAwareness.MODID, name));
		lookupStringToEvent.put(name, event);
		Registry.register(BuiltInRegistries.SOUND_EVENT, new ResourceLocation(ZombieAwareness.MODID, name), event);
	}*/

	public static SoundEvent get(String soundPath) {
		return ZombieAwarenessClient.instance().getSound(soundPath);//lookupStringToEvent.get(soundPath);
	}

}
