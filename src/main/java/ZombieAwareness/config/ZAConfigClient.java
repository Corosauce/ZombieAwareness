package ZombieAwareness.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ZAConfigClient implements IConfigCategory {

	
	//Client only fields
	public static boolean client_renderBlood = true;
	public static boolean client_debugSensesVisual = false;
	
	@Override
	public String getConfigFileName() {
		return "ZombieAwareness" + File.separator + "ClientConfig";
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Client";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
