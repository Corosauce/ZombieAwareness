package com.corosus.zombieawareness.client;

import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry {

	public static SoundEvent get(String soundPath) {
		return ZombieAwareness.instance().getSound(soundPath);
	}

}
