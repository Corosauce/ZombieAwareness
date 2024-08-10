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

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ZombieAwareness.MODID);

    public ZombieAwarenessForgeClient() {
        super();
    }

    @Override
    public void init() {
        super.init();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        SOUND_EVENTS.register(modEventBus);
    }

    @Override
    public SoundEvent register(String name) {
        SoundEvent soundEvent = super.register(name);
        SOUND_EVENTS.register(name, () -> soundEvent);
        return soundEvent;
    }
}
