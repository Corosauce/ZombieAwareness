package com.corosus.zombieawareness.loader.forge;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.ZombieAwarenessClient;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ZombieAwarenessForgeClient extends ZombieAwarenessClient {

    public ZombieAwarenessForgeClient() {
        super();
    }
}
