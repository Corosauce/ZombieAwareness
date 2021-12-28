package com.corosus.zombieawareness;

import modconfig.ConfigMod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import com.corosus.zombieawareness.client.ClientRegistry;
import com.corosus.zombieawareness.config.MobListsConfig;
import com.corosus.zombieawareness.config.ZAConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ZombieAwareness.MODID)
public class ZombieAwareness
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "zombieawareness";

    public ZombieAwareness() {
        // Register the setup method for modloading
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);
        DistExecutor.safeRunForDist(() -> ClientRegistry::new, () -> EventRegistry::new);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityRegistry());
        MinecraftForge.EVENT_BUS.register(new ZAEventHandler());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobListsConfig.CONFIG);

        ConfigMod.addConfigFile(new ZAConfig());
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void clientSetup(FMLClientSetupEvent event) {
        //WeatherUtilSound.init();

    }

    /*private void addReloadListenersLate(AddReloadListenerEvent event) {
        event.addListener((IResourceManagerReloadListener) resourceManager -> CookingRegistry.initFoodRegistry(event.getDataPackRegistries().getRecipeManager()));
    }*/

    public static void dbg(Object obj) {
        if (ZAConfig.debugConsole || true) {
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
        if (ZAUtil.lookupTickableEntities.containsKey(ent))
        {
            return ZAUtil.lookupTickableEntities.get(ent);
        }

        //TODO: fix config

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
            ZAUtil.lookupTickableEntities.put(ent, result);
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
        return ent.getCategory() == EntityClassification.MONSTER;
    }

    public static boolean getDefaultForEntity(EntityType ent) {
        if (canConfigEntity(ent)) {
            if (MobListsConfig.enhancedMobsDefaults.contains(ent.getRegistryName().toString())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
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
        //TODO: fix config
        //config.load();

        for(Map.Entry<RegistryKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            boolean tickEnt = canProcessEntity(entry.getValue(), true);
            /*if (tickEnt) {
                MobListsConfig.enhancedMobsDefaults.add(entry.getValue().getRegistryName().toString());
            }*/
        }
        //config.save();
    }
}
