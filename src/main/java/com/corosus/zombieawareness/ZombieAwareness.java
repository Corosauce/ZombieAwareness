package com.corosus.zombieawareness;

import com.corosus.modconfig.CoroConfigRegistry;
import com.corosus.zombieawareness.config.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ZombieAwareness
{

    // Define mod id in a common place for everything to reference
    public static final String MODID = "zombieawareness";

    private static ZombieAwareness instance;

    private HashMap<String, SoundEvent> lookupStringToEvent = new HashMap<>();

    public static ZombieAwareness instance() {
        return instance;
    }

    public static ResourceLocation SENSE_NAME = new ResourceLocation(ZombieAwareness.MODID, "scent");
    public static EntityType<EntityScent> SENSE;

    public static HashMap<UUID, CompoundTag> entityData = new HashMap<>();

    public static HashMap<String, Boolean> unitTest = new HashMap<>();

    public static void unitTest(String num) {
        /*if (!unitTest.containsKey(num)) {
            for (int i = 1; i <= 16; i++) {
                System.out.println(i + ": " + unitTest.containsKey(String.valueOf(i)));
            }
            System.out.println(num + " - count: " + unitTest.size() + " of 16");
        }
        unitTest.put(num, true);*/
    }

    public CompoundTag getPersistentData(Entity ent) {
        if (!entityData.containsKey(ent.getUUID())) entityData.put(ent.getUUID(), new CompoundTag());
        return entityData.get(ent.getUUID());
    }

    public CompoundTag setPersistentData(Entity ent, CompoundTag compoundTag) {
        return entityData.put(ent.getUUID(), compoundTag);
    }

    public ZombieAwareness() {
        instance = this;

        new File("./config/" + MODID).mkdirs();
        CoroConfigRegistry.instance().addConfigFile(MODID, new ZAConfigGeneral());
        CoroConfigRegistry.instance().addConfigFile(MODID, new ZAConfigClient());
        CoroConfigRegistry.instance().addConfigFile(MODID, new ZAConfigFeatures());
        //ConfigMod.addConfigFile(MODID, new ZAConfigMobLists());

        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SoundsListsConfig.CONFIG, ZombieAwarenessMod.MODID + File.separator + "SoundLists.toml");
        CoroConfigRegistry.instance().addConfigFile(MODID, new ZAConfigPlayerLists());
        //ConfigMod.addConfigFile(MODID, new ZAConfigSpawning());
        //ZombieAwarenessMod.generateEntityTickList();
        //required to make forge tell us when our mods reload, and we then tell ModConfig about it so it does its thing
        //modBus.addListener(this::onReload);
        generateEntityTickList();
        //generateSoundList();

    }

    public abstract PlayerList getPlayerList();

    public abstract boolean isModInstalled(String modID);



    /*@SubscribeEvent
    public void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getFileName().contains("MobLists.toml")) {
            //System.out.println("ZombieAwarenessMod.generateEntityTickList();");

        }

        if (configEvent.getConfig().getFileName().contains("SoundLists.toml")) {

        }
    }*/


    public static void serverStarting() {
        clearConfigCache();
    }

    public static void clearConfigCache() {
        ZAUtil.lookupTickableEntitiesCache.clear();
    }

    public static void dbg(Object obj) {
        if (ZAConfigGeneral.debugConsole) {
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
            if (MobListsConfig.GENERAL.enhancedMobs.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(ent).toString())) {
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

            return BuiltInRegistries.ENTITY_TYPE.getKey(ent).toString();
        } catch (Exception ex) {
            if (ZAConfigGeneral.debugConsole) {
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
        for(Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            //calling canProcessEntity fills the lists
            boolean tickEnt = canConfigEntity(entry.getValue());
            if (tickEnt) {
                MobListsConfig.enhanceableMobsList.add(entry.getKey().location().toString());
            }
        }
        //MobListsConfig.GENERAL.enhanceableMobs.set(MobListsConfig.enhanceableMobsList);
        //System.out.println(MobListsConfig.enhanceableMobsList);
    }

    public static void generateSoundList() {
        for(Map.Entry<ResourceKey<SoundEvent>, SoundEvent> entry : BuiltInRegistries.SOUND_EVENT.entrySet()) {
            //calling canProcessEntity fills the lists
            //boolean tickEnt = canConfigEntity(entry.getValue());
            if (true) {
                SoundsListsConfig.allSoundsInGameList.add(entry.getKey().location().toString());
            }
        }
        SoundsListsConfig.GENERAL.allSoundsInGame.set(SoundsListsConfig.allSoundsInGameList);
        //System.out.println(SoundsListsConfig.allSoundsInGameList);
        //System.out.println("asdasd");
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
