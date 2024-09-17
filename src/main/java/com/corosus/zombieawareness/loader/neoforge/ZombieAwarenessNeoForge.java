package com.corosus.zombieawareness.loader.neoforge;


import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.loader.neoforge.client.ClientRegistry;
import com.corosus.zombieawareness.loader.neoforge.config.MobListsConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.List;

@Mod(ZombieAwareness.MODID)
public class ZombieAwarenessNeoForge extends ZombieAwareness {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ZombieAwareness.MODID);

    public static ModContainer modContainer;

    public ZombieAwarenessNeoForge(ModContainer container) {
        super();

        modContainer = container;

        container.getEventBus().addListener(this::setup);
        /*NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);*/

        //load class so it registers
        EntityRegistry.init(container);

        if (FMLEnvironment.dist.isClient()) {
            ClientEvents clientEvents = new ClientEvents();
            container.getEventBus().addListener(ClientRegistry::registerModels);

        }

        container.registerConfig(ModConfig.Type.COMMON, MobListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "MobLists.toml");

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

    /*public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            WatutMod.getPlayerStatusManagerClient().tickPlayer(event.getEntity());
        } else {
            WatutMod.getPlayerStatusManagerServer().tickPlayer(event.getEntity());
        }
    }

    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        WatutMod.getPlayerStatusManagerServer().playerLoggedIn(event.getEntity());
    }*/

    @Override
    public void init() {
        super.init();
        SOUND_EVENTS.register(modContainer.getEventBus());
    }

    public SoundEvent register(String name) {

        SOUND_EVENTS.register(
                name, // must match the resource location on the next line
                () -> {
                    SoundEvent event = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ZombieAwareness.MODID, name));
                    lookupStringToEvent.put(name, event);
                    return event;
                });

        return null;
        /*SoundEvent event = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ZombieAwareness.MODID, name));
        lookupStringToEvent.put(name, event);
        return event;*/
    }

    @Override
    public List<? extends String> getEnhancedMobs() {
        return MobListsConfig.GENERAL.enhancedMobs.get();
    }
}
