package com.corosus.zombieawareness.loader.fabric;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.ZombieAwarenessClient;
import com.corosus.zombieawareness.client.RenderScent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ZombieAwarenessFabricClient extends ZombieAwarenessClient implements ClientModInitializer {

	public ZombieAwarenessFabricClient() {
		super();
	}

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ZombieAwarenessFabric.SENSE, (context) -> new RenderScent(context));
	}

	@Override
	public SoundEvent register(String name) {
		SoundEvent soundEvent = super.register(name);
		Registry.register(BuiltInRegistries.SOUND_EVENT, new ResourceLocation(ZombieAwareness.MODID, name), soundEvent);
		return soundEvent;
	}

}