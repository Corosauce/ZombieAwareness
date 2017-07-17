package ZombieAwareness;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;

public class SoundRegistry {

	private static HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<String, SoundEvent>();

	public static void init() {
		register("alert");
		register("target");
		register("investigate");



	}

	public static void register(String soundPath) {
		ResourceLocation resLoc = new ResourceLocation(ZombieAwareness.modID, soundPath);
		SoundEvent event = new SoundEvent(resLoc).setRegistryName(resLoc);
		ForgeRegistries.SOUND_EVENTS.register(event);
		if (lookupStringToEvent.containsKey(soundPath)) {
			System.out.println("ZA SOUNDS WARNING: duplicate sound registration for " + soundPath);
		}
		lookupStringToEvent.put(soundPath, event);
	}

	public static SoundEvent get(String soundPath) {
		return lookupStringToEvent.get(soundPath);
	}

}
