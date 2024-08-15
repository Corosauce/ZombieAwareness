package com.corosus.zombieawareness;

import com.corosus.zombieawareness.client.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;

public abstract class ZombieAwarenessClient {

    private static ZombieAwarenessClient instance;

    public static ZombieAwarenessClient instance() {
        return instance;
    }

    public ZombieAwarenessClient() {
        instance = this;
    }
}
