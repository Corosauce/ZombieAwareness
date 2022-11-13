package com.corosus.zombieawareness.config;

import java.io.File;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;

public class ZAConfigSpawning implements IConfigCategory {

	@ConfigComment("The max count of zombies active in loaded chunks for duplicating zombie at a location, for 1 player, vanilla count is about 25, so this adds 10 extra")
	public static int maxZombiesNight = 35;
	@ConfigComment("Odds to 1 of duplicating extra zombies")
	public static int maxZombiesNightBaseRarity = 100;
	@ConfigComment("Delay rate of spawning extra zombies")
	public static int zombieSpawnTickDelay = 5;
	@ConfigComment("Automatically target closest player on spawn for mods extra spawning features")
	public static boolean extraSpawningAutoTarget = false;
	//@ConfigComment("The max count of zombies active in loaded chunks for extra random surface spawning, or extra cave spawning")
	//public static int extraSpawningMaxCount = 50;
	
	//surface spawns
	@ConfigComment("The max count of zombies active in loaded chunks for extra surface spawning")
	public static int extraSpawningSurfaceMaxCount = 50;
	@ConfigComment("Odds to 1 of spawning extra random zombies")
	public static int extraSpawningSurfaceRandomPool = 100;
	@ConfigComment("Min distance required from closest player to spawn extra random position zombie")
	public static int extraSpawningDistMin = 50;
	@ConfigComment("Max distance required from closest player to spawn extra random position zombie")
	public static int extraSpawningDistMax = 100;
	
	@ConfigComment("Max amount of zombies that can spawn together on surface")
	public static int extraSpawningSurfaceMaxGroupSize = 3;
	
	//cave spawns configs
	@ConfigComment("The max count of zombies active in loaded chunks for extra cave spawning")
	public static int extraSpawningCavesMaxCount = 50;
	@ConfigComment("Odds to 1 of spawning extra random zombies")
	public static int extraSpawningCavesRandomPool = 1;
	@ConfigComment("Min distance required from closest player to spawn extra random position zombie")
	public static int extraSpawningCavesDistMin = 50;
	@ConfigComment("Max distance required from closest player to spawn extra random position zombie")
	public static int extraSpawningCavesDistMax = 100;
	
	@ConfigComment("Max amount of zombies that can spawn together in a cave")
	public static int extraSpawningCavesMaxGroupSize = 5;
	
	@ConfigComment("How intensively it tries to spawn mobs in caves")
	public static int extraSpawningCavesTryCount = 15;
	
	@ConfigComment("Instead of just zombies, use the list of mobs minecraft tries to normally spawn with, could include monsters from mods")
	public static boolean extraSpawningUseNaturalSpawnList = false;

	@Override
	public String getName() {
		return "Spawning";
	}

	@Override
	public String getRegistryName() {
		return "zaconfigspawning";
	}

	@Override
	public String getConfigFileName() {
		return ZombieAwareness.MODID + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: " + getName();
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
