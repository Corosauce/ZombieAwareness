package com.corosus.zombieawareness.loader.forge;

import com.corosus.zombieawareness.EventRegistry;
import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.ZombieAwarenessClient;
import com.corosus.zombieawareness.config.MobListsConfig;
import com.corosus.zombieawareness.config.SoundsListsConfig;
import com.corosus.zombieawareness.loader.forge.client.ClientRegistry;
import com.corosus.zombieawareness.client.SoundRegistry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;

@Mod(ZombieAwarenessForge.MODID)
public class ZombieAwarenessForge extends ZombieAwareness {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ZombieAwareness.MODID);
	
    public ZombieAwarenessForge() {
        super();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //modEventBus.addListener(this::setup);
        //MinecraftForge.EVENT_BUS.register(this);
        /*MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(EventHandlerForge::getRegisteredParticles);
        }*/

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.safeRunForDist(() -> ClientRegistry::new, () -> EventRegistry::new);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            new ZombieAwarenessForgeClient();
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityRegistry());
        MinecraftForge.EVENT_BUS.register(new ZAEventHandler());
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SoundsListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "SoundLists.toml");
        //MinecraftForge.EVENT_BUS.addListener(this::serverStart);

        EntityRegistry.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "MobLists.toml");

        //modBus.addListener(this::onLoad);

        init();

    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    @Override
    public PlayerList getPlayerList() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList();
    }

    @Override
    public boolean isModInstalled(String modID) {
        return ModList.get().isLoaded(modID);
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
