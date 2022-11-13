package com.corosus.zombieawareness;

import com.corosus.zombieawareness.client.ClientRegistry;
import com.corosus.zombieawareness.config.*;
import com.corosus.modconfig.ConfigMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(ZombieAwareness.MODID)
public class ZombieAwareness
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "zombieawareness";

    public ZombieAwareness() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.safeRunForDist(() -> ClientRegistry::new, () -> EventRegistry::new);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityRegistry());
        MinecraftForge.EVENT_BUS.register(new ZAEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);

        FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(MODID), MODID);
        ConfigMod.addConfigFile(MODID, new ZAConfig());
        ConfigMod.addConfigFile(MODID, new ZAConfigClient());
        ConfigMod.addConfigFile(MODID, new ZAConfigFeatures());
        //ConfigMod.addConfigFile(MODID, new ZAConfigMobLists());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobListsConfig.CONFIG, ZombieAwareness.MODID + File.separator + "MobLists.toml");
        ConfigMod.addConfigFile(MODID, new ZAConfigPlayerLists());
        //ConfigMod.addConfigFile(MODID, new ZAConfigSpawning());
        ZombieAwareness.generateEntityTickList();
        //required to make forge tell us when our mods reload, and we then tell ModConfig about it so it does its thing
        modBus.addListener(this::onReload);
    }

    @SubscribeEvent
    public void onReload(final ModConfigEvent.Reloading configEvent) {
        clearConfigCache();
        ConfigMod.onReload(configEvent);
    }

    @SubscribeEvent
    public void serverStart(ServerStartingEvent event) {
        clearConfigCache();
    }

    public static void clearConfigCache() {
        ZAUtil.lookupTickableEntitiesCache.clear();
    }

    public static void dbg(Object obj) {
        if (ZAConfig.debugConsole) {
            System.out.println(obj);
        }
    }

    public static boolean canProcessEntity(Entity ent) {
        if (!canEntityBeProcessedOverride(ent)) {
            return false;
        }
        return canProcessEntity(ent.getType(), false);
    }

    /**
     * Looks up and generates config info if entry missing
     *
     * @param ent
     * @param pregen
     * @return
     */
    public static boolean canProcessEntity(EntityType ent, boolean pregen) {

        String entName = getEntityRegisteredName(ent);
        if (ZAUtil.lookupTickableEntitiesCache.containsKey(ent))
        {
            return ZAUtil.lookupTickableEntitiesCache.get(ent);
        }

        boolean result = false;
        if (canConfigEntity(ent)) {
            //if (!pregen) config.load();
            boolean canProcess = getDefaultForEntity(ent);
            try {
                //prevent crash for case where mod entity can be null or blank
                if (entName != null && !entName.equals("")) {
                    //result = config.get(configCategory, entName, canProcess).getBoolean(canProcess);
                    result = canProcess;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //if (!pregen) config.save();
            ZAUtil.lookupTickableEntitiesCache.put(ent, result);
        }

        return result;
    }

    /**
     * Handles special cases for specific instances like if owned and has an owner. But I want that to never be processed anyways
     * Placeholder for now until more dynamic needs are found
     *
     * @param entity
     * @return false if you want to cancel processing, true lets it continue with other rules
     */
    public static boolean canEntityBeProcessedOverride(Entity entity) {
        return true;
		/*boolean result = false;
		if (entity instanceof IEntityOwnable) {
			if (entity.
		}*/
    }

    /**
     * Used to avoid adding entries to config that cant be used even if set to true
     *
     * @param ent
     * @return
     */
    public static boolean canConfigEntity(EntityType ent) {
        //return MonsterEntity.class.isAssignableFrom(ent.getClass()) || IMob.class.isAssignableFrom(ent.getClass());
        return ent.getCategory() == MobCategory.MONSTER;
    }

    public static boolean getDefaultForEntity(EntityType ent) {
        if (canConfigEntity(ent)) {
            if (MobListsConfig.GENERAL.enhancedMobs.get().contains(ent.getRegistryName().toString())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static List<String> getListFromCSV(String csv) {
        return Stream.of(csv.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 1.10.2: only name available
     * 1.11.2: 'name' field in EntityEntry, new registered name is the snake case key for ForgeRegistries.ENTITIES.getEntries() as a resource location
     * 1.16.5: rip class inheritance info
     *
     * @param ent
     * @return
     */
    public static String getEntityRegisteredName(EntityType ent) {
        try {

            return ent.getRegistryName().toString();
        } catch (Exception ex) {
            if (ZAConfig.debugConsole) {
                ex.printStackTrace();
            }
            return ent.getClass().getSimpleName();

        }
    }

    /**
     * Generates list of entities we can process, these are written to config they can modify every entities config after first run
     *
     */
    public static void generateEntityTickList() {
        for(Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            //calling canProcessEntity fills the lists
            boolean tickEnt = canProcessEntity(entry.getValue(), true);
            /*if (tickEnt) {
                MobListsConfig.enhancedMobsDefaults.add(entry.getValue().getRegistryName().toString());
            }*/
        }
    }
}
