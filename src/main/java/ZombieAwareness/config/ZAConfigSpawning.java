package ZombieAwareness.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ZAConfigSpawning implements IConfigCategory {

	@ConfigComment("The max count of zombies active in loaded chunks for duplicating zombie at a location")
	public static int maxZombiesNight = 50;
	@ConfigComment("Odds to 1 of duplicating extra zombies")
	public static int maxZombiesNightBaseRarity = 100;
	@ConfigComment("Delay rate of spawning extra zombies")
	public static int zombieSpawnTickDelay = 5;
	@ConfigComment("Automatically target closest player on spawn for mods extra spawning features")
	public static boolean extraSpawningAutoTarget = false;
	@ConfigComment("The max count of zombies active in loaded chunks for extra random surface spawning, or extra cave spawning")
	public static int extraSpawningMaxCount = 50;
	@ConfigComment("Odds to 1 of spawning extra random zombies")
	public static int extraSpawningRandomPool = 100;
	@ConfigComment("Min distance required from closest player to spawn extra random position zombie")
	public static int extraSpawningDistMin = 50;
	@ConfigComment("Max distance required from closest player to spawn extra random position zombie")
	public static int extraSpawningDistMax = 100;

	@Override
	public String getConfigFileName() {
		return "ZombieAwareness" + File.separator + "Spawning";
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Spawning";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
