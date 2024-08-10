package com.corosus.zombieawareness;

import com.corosus.zombieawareness.client.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;

public abstract class ZombieAwarenessClient {

    private static ZombieAwarenessClient instance;

    private HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<>();

    public static ZombieAwarenessClient instance() {
        return instance;
    }

    public ZombieAwarenessClient() {
        instance = this;

        init();
    }

    public void init() {
        register("alert");
        register("target");
        register("investigate");
    }

    public SoundEvent register(String name) {
        SoundEvent event = SoundEvent.createVariableRangeEvent(new ResourceLocation(ZombieAwareness.MODID, name));
        lookupStringToEvent.put(name, event);
        return event;
    }

    public SoundEvent getSound(String soundPath) {
        return lookupStringToEvent.get(soundPath);
    }
}
