package modconfig;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.forge.CULog;
import com.corosus.coroutil.util.CULog;
import com.corosus.coroutil.util.OldUtil;
import modconfig.forge.CommandModConfig;
import modconfig.forge.EventHandlerPacket;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.OldUtil;

@Mod(modid = "configmod", name="Extended Mod Config", version="v1.0", useMetadata=false)
public class ConfigMod {

	@Mod.Instance( value = "configmod" )
	public static ConfigMod instance;
	
	//public static Class configFieldClass = ModConfigFields.class;
	//public static ModConfigFields configFieldInstance = new ModConfigFields(); //upgrade to class.getConstructor later 
	
	public static List<ModConfigData> configs = new ArrayList<ModConfigData>();
	public static List<ModConfigData> liveEditConfigs = new ArrayList<ModConfigData>();
	public static HashMap<String, ModConfigData> configLookup = new HashMap<String, ModConfigData>();
	
	public static String eventChannelName = "modconfig";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
	
    public ConfigMod() {
    	//instance = this;
    }
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandModConfig());
    }
    
    @Mod.EventHandler
    private static void preInit(FMLPreInitializationEvent event) {
    	eventChannel.register(new EventHandlerPacket());
    	//new ConfigMod();
    	//instance.saveFilePath = event.getSuggestedConfigurationFile();
    	//instance.initData();
    	//instance.writeConfigFiles(false);
    }
    
    @Mod.EventHandler
    private static void init(FMLInitializationEvent event) {
    	
    }
    
    public static void populateData(String modid) {

        try {
            configLookup.get(modid).configData.clear();

            ModConfigData data = configLookup.get(modid);

            if (data != null) {
                //int pos = 0;

                processHashMap(modid, data.valsInteger);
                processHashMap(modid, data.valsDouble);
                processHashMap(modid, data.valsBoolean);
                processHashMap(modid, data.valsString);
            } else {
                System.out.println("error: cant find config data for gui");
            }

            //sort it here!
            Collections.sort(configLookup.get(modid).configData, new ConfigComparatorName());
            //configLookup.get(modid).configData.
        } catch (Exception ex) {
            if (ConfigCoroUtil.useLoggingDebug) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void processHashMap(String modid, Map map) {
    	Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String name = (String)pairs.getKey();
	        Object val = pairs.getValue();
	        String comment = getComment(modid, name);
	        ConfigEntryInfo info = new ConfigEntryInfo(configLookup.get(modid).configData.size(), name, val, comment);
	        configLookup.get(modid).configData.add(info);
	    }
    }
    
    public void initData() {
    	
    }
    
    public void writeConfigFiles(Boolean resetData) {
    	
    }
    
    public static String getSaveFolderPath() {
    	if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
    
    @SideOnly(Side.CLIENT)
	public static String getClientSidePath() {
		return FMLClientHandler.instance().getClient().mcDataDir.getPath();
	}
    
    public static void dbg(Object obj) {
		if (true) {
			System.out.println(obj);
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
		}
	}
    
    /* Main Usage Methods Start */
    
    /* Main Inits */

    public static void addConfigFile(IConfigCategory configCat) {
    	addConfigFile(configCat.getRegistryName(), configCat, true);
    }
    
    public static void addConfigFile(String modID, IConfigCategory configCat, boolean liveEdit) {
    	//if (instance == null) init(event);

        //prevent adding twice
        if (configLookup.containsKey(configCat.getRegistryName())) {
            return;
        }
    	
    	ModConfigData configData = new ModConfigData(configCat.getConfigFileName()/*new File(getSaveFolderPath() + "config" + File.separator + configCat.getConfigFileName() + ".cfg")*/, modID, configCat.getClass(), configCat);
    	
    	configs.add(configData);
    	if (liveEdit) liveEditConfigs.add(configData);
    	configLookup.put(modID, configData);
    	
    	configData.initData();
    	configData.writeConfigFile(false);
    }
    
    /* Get Inner Field value */
    public static Object getField(String configID, String name) {
    	try { return OldUtil.getPrivateValue(configLookup.get(configID).configClass, instance, name);
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
            Field field = configLookup.get(configID).configClass.getDeclaredField(name);
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
    	if (configLookup.get(configID).setFieldBasedOnType(name, obj)) {
        	//writeHashMapsToFile();
    		configLookup.get(configID).writeConfigFile(true);
        	return true;
    	}
    	return false;
    }

    public static void forceSaveAllFilesFromRuntimeSettings() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : configLookup.values()) {
            data.writeConfigFile(true);
        }
    }

    public static void forceLoadRuntimeSettingsFromFile() {
        CULog.dbg("forceSaveAllFilesFromRuntimeSettings invoked");
        for (ModConfigData data : configLookup.values()) {
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
