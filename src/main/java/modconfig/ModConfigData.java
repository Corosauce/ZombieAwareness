package modconfig;

import com.corosus.coroutil.util.CULog;
import com.corosus.coroutil.util.OldUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModConfigData {
	public String configID;
	public Class configClass;
	public IConfigCategory configInstance;
	
	public HashMap<String, String> valsString = new HashMap<String, String>();
	public HashMap<String, Integer> valsInteger = new HashMap<String, Integer>();
	public HashMap<String, Double> valsDouble = new HashMap<String, Double>();
	public HashMap<String, Boolean> valsBoolean = new HashMap<String, Boolean>();

	public HashMap<String, ForgeConfigSpec.ConfigValue<String>> valsStringConfig = new HashMap<>();
	public HashMap<String, ForgeConfigSpec.ConfigValue<Integer>> valsIntegerConfig = new HashMap<>();
	public HashMap<String, ForgeConfigSpec.ConfigValue<Double>> valsDoubleConfig = new HashMap<>();
	public HashMap<String, ForgeConfigSpec.ConfigValue<Boolean>> valsBooleanConfig = new HashMap<>();

	//Client data
	public List<ConfigEntryInfo> configData = new ArrayList<ConfigEntryInfo>();	
	
    //public Configuration preInitConfig;
	//public static final CategoryGeneral GENERAL = new CategoryGeneral();
    //public File saveFilePath;
    public String saveFilePath;

	public ModConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
		configID = parStr;
		configClass = parClass;
		configInstance = parConfig;
		saveFilePath = savePath;
	}
	
	public void updateHashMaps() {
    	Field[] fields = configClass.getDeclaredFields();
    	
    	for (int i = 0; i < fields.length; i++) {
    		Field field = fields[i];
    		String name = field.getName();
    		processField(name);
    	}
    }

	public void updateConfigFieldValues() {
		Field[] fields = configClass.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String name = field.getName();
			processFieldFromForgeConfig(name);
		}
	}

	private void processFieldFromForgeConfig(String fieldName) {
		try {
			Object obj = ConfigMod.getField(configID, fieldName);
			if (obj instanceof String) {
				valsString.put(fieldName, (String)obj);
				String val = valsStringConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, val);
			} else if (obj instanceof Integer) {
				valsInteger.put(fieldName, (Integer)obj);
				int what = valsIntegerConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, what);
				//OldUtil.setPrivateValue();
			} else if (obj instanceof Double) {
				valsDouble.put(fieldName, (Double)obj);
				Double val = valsDoubleConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, val);
			} else if (obj instanceof Boolean) {
				valsBoolean.put(fieldName, (Boolean)obj);
				Boolean val = valsBooleanConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, val);
			} else {
				//dbg("unhandled datatype, update initField");
			}
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public void initData() {
    	valsString.clear();
    	valsInteger.clear();
    	valsDouble.clear();
    	valsBoolean.clear();
    	
    	updateHashMaps();
    }
	
	public boolean updateField(String name, Object obj) {
    	if (setFieldBasedOnType(name, obj)) {
        	//writeHashMapsToFile();
    		writeConfigFile(true);
        	return true;
    	}
    	return false;
    }
    
    public boolean setFieldBasedOnType(String name, Object obj) {
    	try {
    		if (valsString.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, (String)obj);
    			inputField(name, (String)obj);
    		} else if (valsInteger.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, Integer.valueOf(obj.toString()));
    			inputField(name, Integer.valueOf(obj.toString()));
    		} else if (valsDouble.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, Double.valueOf(obj.toString()));
    			inputField(name, Double.valueOf(obj.toString()));
    		} else if (valsBoolean.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, Boolean.valueOf(obj.toString()));
    			inputField(name, Boolean.valueOf(obj.toString()));
    		} else {
    			return false;
    		}
    		
    		configInstance.hookUpdatedValues();
    		
    		return true;
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	return false;
    }
    
    /*public void writeHashMapsToFile() {
    	Iterator it = valsString.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String name = (String)pairs.getKey();
	        Object val = pairs.getValue();
	    }
    }*/
    
    private void processField(String fieldName) {
    	try {
	    	Object obj = ConfigMod.getField(configID, fieldName);
	    	if (obj instanceof String) {
	    		valsString.put(fieldName, (String)obj);
	    	} else if (obj instanceof Integer) {
	    		valsInteger.put(fieldName, (Integer)obj);
	    	} else if (obj instanceof Double) {
	    		valsDouble.put(fieldName, (Double)obj);
	    	} else if (obj instanceof Boolean) {
	    		valsBoolean.put(fieldName, (Boolean)obj);
	    	} else {
	    		//dbg("unhandled datatype, update initField");
	    	}
    	} catch (Exception ex) { ex.printStackTrace(); }
    }
    
    private void inputField(String fieldName, Object obj) {
    	if (obj instanceof String) {
    		valsString.put(fieldName, (String)obj);
    	} else if (obj instanceof Integer) {
    		valsInteger.put(fieldName, (Integer)obj);
    	} else if (obj instanceof Double) {
    		valsDouble.put(fieldName, (Double)obj);
    	} else if (obj instanceof Boolean) {
    		valsBoolean.put(fieldName, (Boolean)obj);
    	} else {
    		
    	}
    }
    
    public void writeConfigFile(boolean resetConfig) {

		//TODO: see if we need support for resetting config
        //if (resetConfig) if (saveFilePath.exists()) saveFilePath.delete();
    	//preInitConfig = new Configuration(saveFilePath);
    	//preInitConfig.load();
		ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
		BUILDER.comment("General mod settings").push("general");
    	
    	Field[] fields = configClass.getDeclaredFields();
    	
    	for (int i = 0; i < fields.length; i++) {
    		Field field = fields[i];
    		String name = field.getName();

    		addToConfig(BUILDER, field, name);
    	}

		CULog.dbg("writeConfigFile invoked for " + this.configID + ", resetConfig: " + resetConfig);
    	//preInitConfig.save();
		BUILDER.pop();
		ForgeConfigSpec CONFIG = BUILDER.build();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG, saveFilePath + ".toml");
    }
    
    /**
     * Perform the actual adding of values to the config file
     * @param name Name of the variable
     * @param field Field in the file the variable is
     */
    private void addToConfig(ForgeConfigSpec.Builder builder, Field field, String name) {

        // Comment from the annotation on the value of the actual variable that 'name' is retrieved from
        String comment = "";

        ConfigComment anno_comment = field.getAnnotation(ConfigComment.class);
        if (anno_comment != null) {
            comment = anno_comment.value()[0];
        }

        //System.out.println("registering config field: " + name);

        Object obj = ConfigMod.getField(configID, name);
        if (obj instanceof String) {
            //obj = preInitConfig.get(configInstance.getCategory(), name, (String)obj, comment).getString();
			//obj = builder.define(name, (String)obj).get();
			builder.comment(comment).define(name, (String)obj);
			valsStringConfig.put(name, builder.define(name, (String)obj));
        } else if (obj instanceof Integer) {
            //obj = preInitConfig.get(configInstance.getCategory(), name, (Integer)obj, comment).getInt((Integer)obj);
			//obj = builder.defineInRange(name, (Integer)obj, Integer.MIN_VALUE, Integer.MAX_VALUE).get();
			valsIntegerConfig.put(name, builder.comment(comment).defineInRange(name, (Integer)obj, Integer.MIN_VALUE, Integer.MAX_VALUE));
        } else if (obj instanceof Double) {
            //obj = preInitConfig.get(configInstance.getCategory(), name, (Double)obj, comment).getDouble((Double)obj);
			//obj = builder.defineInRange(name, (Double) obj, Double.MIN_VALUE, Double.MAX_VALUE).get();
			//builder.defineInRange(name, (Double) obj, Double.MIN_VALUE, Double.MAX_VALUE);
			valsDoubleConfig.put(name, builder.comment(comment).defineInRange(name, (Double)obj, Double.MIN_VALUE, Double.MAX_VALUE));
        } else if (obj instanceof Boolean) {
            //obj = preInitConfig.get(configInstance.getCategory(), name, (Boolean)obj, comment).getBoolean((Boolean)obj);
			//obj = builder.define(name, (Boolean)obj).get();
			//builder.define(name, (Boolean)obj);
			valsBooleanConfig.put(name, builder.comment(comment).define(name, (Boolean)obj));
        } else {
            //dbg("unhandled datatype, update initField");
        }
        setFieldBasedOnType(name, obj);
    }
}
