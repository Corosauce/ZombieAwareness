package ZombieAwareness.config;

import java.io.File;

import modconfig.IConfigCategory;

public class ZAConfigFeatures implements IConfigCategory {

	public static boolean awareness_Sound = true;
	public static boolean awareness_Scent = true;
	public static boolean awareness_Light = true;
	public static boolean noisyZombies = true;
	public static boolean noisyPistons = true;
	public static boolean wanderingHordes = true;
	public static boolean extraSpawningSurface = false;
	public static boolean extraSpawningCave = false;

	@Override
	public String getConfigFileName() {
		// TODO Auto-generated method stub
		return "ZombieAwareness" + File.separator + "Features";
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return "Zombie Awareness: Features";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
