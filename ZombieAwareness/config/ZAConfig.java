package ZombieAwareness.config;

import java.io.File;

import modconfig.IConfigCategory;

public class ZAConfig implements IConfigCategory {

	public static int maxPFRange = 64; //also max awareness range
	public static int maxPFRangeSense = 64; //also max awareness range
	public static boolean awareness_Light_OnlyZombies = true;
	//Sights
	public static int sightRange = 16;
	public static boolean omnipotent = false;
	public static boolean seeThroughWalls = false;
	public static int scentStrength = 60;
	public static int soundStrength = 60;
	public static int frequentSoundThreshold = 1000;
	public static int zombieRandSpeedBoost = 5;
	//public static int tickRateMainLoop = 1;
	public static int tickRateAILoop = 5;
	public static int tickRatePlayerLoop = 1;
	
	public static boolean debugConsole = false;
	public static boolean debugConsoleSpawns = false;
	public static boolean debugConsoleOmnipotent = false;
	public static boolean debugConsoleSuperDetailed = false;
	
	//Client only fields
	public static boolean client_renderBlood = true;
	public static boolean client_debugRenderSounds = false;
	
	@Override
	public String getConfigFileName() {
		// TODO Auto-generated method stub
		return "ZombieAwareness" + File.separator + "General";
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return "Zombie Awareness: General";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
