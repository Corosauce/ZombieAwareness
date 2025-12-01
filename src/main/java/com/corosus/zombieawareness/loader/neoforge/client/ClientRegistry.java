package com.corosus.zombieawareness.loader.neoforge.client;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.client.RenderScent;
import com.corosus.zombieawareness.loader.neoforge.EntityRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(ZombieAwareness.MODID)
public class ClientRegistry {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerModels(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityRegistry.SCENT.get(), render -> new RenderScent(render));
    }

}
