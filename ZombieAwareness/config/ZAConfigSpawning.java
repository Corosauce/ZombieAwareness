package ZombieAwareness.config;

import java.io.File;

import modconfig.IConfigCategory;

public class ZAConfigSpawning implements IConfigCategory {

	public static int maxZombiesNight = 50;
	public static int maxZombiesNightBaseRarity = 100;
	public static int zombieSpawnTickDelay = 5;
	public static boolean extraSpawningAutoTarget = false;
	public static int extraSpawningMaxCount = 50;
	public static int extraSpawningRandomPool = 100;
	public static int extraSpawningDistMin = 50;
	public static int extraSpawningDistMax = 100;

	@Override
	public String getConfigFileName() {
		// TODO Auto-generated method stub
		return "ZombieAwareness" + File.separator + "Spawning";
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return "Zombie Awareness: Spawning";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
