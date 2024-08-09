package com.corosus.zombieawareness.loader.forge;

import com.corosus.zombieawareness.EventRegistry;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.config.MobListsConfig;
import com.corosus.zombieawareness.loader.forge.client.ClientRegistry;
import com.corosus.zombieawareness.loader.forge.client.SoundRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;

@Mod(ZombieAwarenessForge.MODID)
public class ZombieAwarenessForge extends ZombieAwareness {
	
    public ZombieAwarenessForge() {
        super();
        new WatutNetworkingForge();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //modEventBus.addListener(this::setup);
        //MinecraftForge.EVENT_BUS.register(this);
        /*MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(EventHandlerForge::getRegisteredParticles);
        }*/

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.safeRunForDist(() -> ClientRegistry::new, () -> EventRegistry::new);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityRegistry());
        MinecraftForge.EVENT_BUS.register(new ZAEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);

        EntityRegistry.init();
        SoundRegistry.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "MobLists.toml");

        modBus.addListener(this::onLoad);

    }

    private void setup(final FMLCommonSetupEvent event) {
        WatutNetworkingForge.register();
    }

    @Override
    public PlayerList getPlayerList() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList();
    }

    @Override
    public boolean isModInstalled(String modID) {
        return ModList.get().isLoaded(modID);
    }
}
