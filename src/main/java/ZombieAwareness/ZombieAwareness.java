package ZombieAwareness;

import ZombieAwareness.config.ZAConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
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

        MinecraftForge.EVENT_BUS.addListener(this::serverStop);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void clientSetup(FMLClientSetupEvent event) {
        //WeatherUtilSound.init();

    }

    @SubscribeEvent
    public void serverStop(FMLServerStoppedEvent event) {

    }

    /*private void addReloadListenersLate(AddReloadListenerEvent event) {
        event.addListener((IResourceManagerReloadListener) resourceManager -> CookingRegistry.initFoodRegistry(event.getDataPackRegistries().getRecipeManager()));
    }*/

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

        boolean result = false;
        if (canConfigEntity(ent)) {

            /*if (ZombifiedPiglinEntity.class.isAssignableFrom(ent.getClass())) return false;

            if (ZombieEntity.class.isAssignableFrom(ent.getClass()) ||
                    AbstractSkeletonEntity.class.isAssignableFrom(ent.getClass()) ||
                    WitchEntity.class.isAssignableFrom(ent.getClass()) ||
                    SpiderEntity.class.isAssignableFrom(ent.getClass())) {
                result = true;
            } else {
                result = false;
            }*/

            //cant lookup entity class anymore, lets just make it work for everything
            if (ent != EntityType.ENDERMAN) {
                result = true;
            } else {
                result = false;
            }
        }
        return result;
    }

    /**
     * 1.10.2: only name available
     * 1.11.2: 'name' field in EntityEntry, new registered name is the snake case key for ForgeRegistries.ENTITIES.getEntries() as a resource location
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
        }
        //config.save();
    }
}
