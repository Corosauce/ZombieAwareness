package modconfig;

import com.corosus.coroutil.util.CULog;
import com.corosus.coroutil.util.OldUtil;
import com.corosus.zombieawareness.config.ZAConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Field;
import java.util.*;

@Mod(ConfigMod.MODID)
public class ConfigMod {

	public static ConfigMod instance;
	
	public static List<ModConfigData> configs = new ArrayList<>();
	public static List<ModConfigData> liveEditConfigs = new ArrayList<>();
	public static HashMap<String, ModConfigData> lookupRegistryNameToConfig = new HashMap<>();

    //for new forge config, routing reloaded config event to the class to update
	public static HashMap<String, ModConfigData> lookupFilePathToConfig = new HashMap<>();

    public static final String MODID = "modconfig";
	
    public ConfigMod() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        //modBus.addListener(this::onLoad);
        //modBus.addListener(this::onReload);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
    }



    @SubscribeEvent
    public void serverStart(FMLServerStartingEvent event) {
        //force a full update right before server starts because forge file watching is unreliable
        //itll randomly not invoke ModConfig.Reloading for configs and stick with old values
        dbg("Performing a full config mod force sync");
        updateAllConfigsFromForge();
    }

    public static void onLoad(final ModConfig.Loading configEvent, String modID) {
        System.out.println("filename1: " + configEvent.getConfig().getFileName());
        ModConfigData configData = ConfigMod.lookupRegistryNameToConfig.get("zaconfig");
        configData.updateConfigFieldValues();
        System.out.println("maxPFRangeSense1: " + ZAConfig.maxPFRangeSense);
    }


    public static void onReload(final ModConfig.Reloading configEvent/*, String modID*/) {


        //for new forge config, we set our simple configs field values based on what forge config loaded from file now that the file is fully loaded and ready
        //we cant do this on the fly per field like we used to, forge complains the config builder isnt done yet
        ModConfigData configData = ConfigMod.lookupFilePathToConfig.get(configEvent.getConfig().getFileName());
        if (configData != null) {
            dbg("Coro ConfigMod updating runtime values for file: " + configEvent.getConfig().getFileName());
            configData.updateConfigFieldValues();
            configData.configInstance.hookUpdatedValues();
        } else {
            dbg("ERROR, cannot find ModConfigData reference for filename: " + configEvent.getConfig().getFileName());
        }
    }

    public static void updateAllConfigsFromForge() {
        for (ModConfigData configData : ConfigMod.lookupFilePathToConfig.values()) {
            dbg("Coro ConfigMod updating runtime values for file: " + configData.saveFilePath);
            configData.updateConfigFieldValues();
            configData.configInstance.hookUpdatedValues();
        }
    }
    
    public static void processHashMap(String modid, Map map) {
    	Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String name = (String)pairs.getKey();
	        Object val = pairs.getValue();
	        String comment = getComment(modid, name);
	        ConfigEntryInfo info = new ConfigEntryInfo(lookupRegistryNameToConfig.get(modid).configData.size(), name, val, comment);
	        lookupRegistryNameToConfig.get(modid).configData.add(info);
	    }
    }
    
    public void initData() {
    	
    }
    
    public void writeConfigFiles(Boolean resetData) {
    	
    }
    
    public static void dbg(Object obj) {
		if (true) {
			System.out.println(obj);
		}
	}
    
    /* Main Usage Methods Start */
    
    /* Main Inits */

    public static void addConfigFile(String modID, IConfigCategory configCat) {
    	addConfigFile(modID, configCat.getRegistryName(), configCat, true);
    }
    
    public static void addConfigFile(String modID, String categoryName, IConfigCategory configCat, boolean liveEdit) {
    	//if (instance == null) init(event);

        //prevent adding twice
        if (lookupRegistryNameToConfig.containsKey(configCat.getRegistryName())) {
            return;
        }
    	
    	ModConfigData configData = new ModConfigData(configCat.getConfigFileName()/*new File(getSaveFolderPath() + "config" + File.separator + configCat.getConfigFileName() + ".cfg")*/, categoryName, configCat.getClass(), configCat);
    	
    	configs.add(configData);
    	if (liveEdit) liveEditConfigs.add(configData);
    	lookupRegistryNameToConfig.put(categoryName, configData);
        System.out.println("adding: " + configCat.getConfigFileName() + ".toml");
        lookupFilePathToConfig.put(configCat.getConfigFileName() + ".toml", configData);

    	configData.initData();
    	configData.writeConfigFile(false);

        //for new forge config, we set our simple configs field values based on what forge config loaded from file now that the file is fully loaded and ready
        //we cant do this on the fly per field like we used to, forge complains the config builder isnt done yet
        //no work here, values not updated yet
        //configData.updateConfigFieldValues();
    }
    
    /* Get Inner Field value */
    public static Object getField(String configID, String name) {
    	try { return OldUtil.getPrivateValue(lookupRegistryNameToConfig.get(configID).configClass, instance, name);
    	} catch (Exception ex) { ex.printStackTrace(); }
    	return null;
    }

    /**
     * Return the comment/description associated with a specific field
     * @param configID ID of the config file
     * @param name Name of the value to retrieve from
     * @return The comment associated with the value, null if there is not one or it is not found
     */
    public static String getComment(String configID, String name) {    	
        try {
            Field field = lookupRegistryNameToConfig.get(configID).configClass.getDeclaredField(name);
            ConfigComment anno_comment = field.getAnnotation(ConfigComment.class);
            return anno_comment == null ? null : anno_comment.value()[0];
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /* Update Config Field Entirely */
    public static boolean updateField(String configID, String name, Object obj) {
    	if (lookupRegistryNameToConfig.get(configID).setFieldBasedOnType(name, obj)) {
        	//writeHashMapsToFile();
    		lookupRegistryNameToConfig.get(configID).writeConfigFile(true);
        	return true;
    	}
    	return false;
    }

    public static void forceSaveAllFilesFromRuntimeSettings() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : lookupRegistryNameToConfig.values()) {
            data.writeConfigFile(true);
        }
    }

    public static void forceLoadRuntimeSettingsFromFile() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : lookupRegistryNameToConfig.values()) {
            //data.reloadRuntimeFromFile();
            data.writeConfigFile(false);
        }
    }
    
    /* Sync the HashMap data if an outside source modified one of the config fields */
    public static void updateHashMaps() {
    	/*Field[] fields = configLookup.get(configID).getDeclaredFields();
    	
    	for (int i = 0; i < fields.length; i++) {
    		Field field = fields[i];
    		String name = field.getName();
    		instance.processField(name);
    	}*/
    }
    
    /* Main Usage Methods End */
}
