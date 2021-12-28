package com.corosus.zombieawareness.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ZAConfigFeatures implements IConfigCategory {

	@ConfigComment("Spawn sound sense sources for monsters to track")
	public static boolean awareness_Sound = true;
	@ConfigComment("Spawn scent/blood sense sources for monsters to track")
	public static boolean awareness_Scent = true;
	@ConfigComment("Monsters see light near player and move towards it")
	public static boolean awareness_Light = true;
	@ConfigComment("Growling zombies attracts other zombies")
	public static boolean noisyZombies = true;
	@ConfigComment("Pistons cause sound sense sources attracting monsters")
	public static boolean noisyPistons = true;
	@ConfigComment("Causes monsters to horde up and wander the surface together to random points")
	public static boolean wanderingHordes = false;
	@ConfigComment("Spawn extra zombies randomly on the surface at night if dark and away from player until Spawning.extraSpawningMaxCount is reached")
	public static boolean extraSpawningSurface = false;
	@ConfigComment("Spawn extra zombies in caves where other zombies already are if dark and away from player until Spawning.extraSpawningMaxCount is reached")
	public static boolean extraSpawningCave = false;
	@ConfigComment("MC/Forge default is 0.1, this overrides that default, set to -1 to cause no override then restore forges default in their config")
	public static double zombieSummonHelpBaseChance = 0D;
	@ConfigComment("MC/Forge default is 0.05, this overrides that default, set to -1 to cause no override then restore forges default in their config")
	public static double zombieBabyChance = 0.0F;
	
	@ConfigComment("How loud sounds should be when you are alerted that a mob is coming to investigate an area near you")
	public static double soundVolumeInvestigate = 0.5D;
	
	@ConfigComment("How loud sounds should be for a mob targetting you")
	public static double soundVolumeAlertTarget = 0.5D;

	public static boolean soundAlerts = true;

	public static boolean soundInvestigates = true;

	@ConfigComment("Uses a different kind of mob alert noise, might break immersion a bit ;)")
	public static boolean soundUseAlternateAlertNoise = false;

	@ConfigComment("Only spawn sound sense entities in Overworld")
	public static boolean awareness_Sound_OverworldOnly = false;

	@Override
	public String getName() {
		return "Features";
	}

	@Override
	public String getRegistryName() {
		return "zaconfigfeatures";
	}

	@Override
	public String getConfigFileName() {
		return "zombieawareness" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: " + getName();
	}

	@Override
	public void hookUpdatedValues() {
		/*if (zombieBabyChance != -1) ForgeModContainer.zombieBabyChance = (float) zombieBabyChance;
		if (zombieSummonHelpBaseChance != -1) ForgeModContainer.zombieSummonBaseChance = zombieSummonHelpBaseChance;*/
	}

}
