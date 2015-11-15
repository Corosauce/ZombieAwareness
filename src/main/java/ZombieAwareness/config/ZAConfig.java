package ZombieAwareness.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ZAConfig implements IConfigCategory {

	@ConfigComment("Max pathfind range for zombies [performance sensitive]")
	public static int maxPFRange = 64; //also max awareness range
	@ConfigComment("Max awareness range for zombies")
	public static int maxPFRangeSense = 64; //also max awareness range
	@ConfigComment("Prevent non zombies from being attracted to light")
	public static boolean awareness_Light_OnlyZombies = true;
	//Sights
	public static int sightRange = 16;
	@ConfigComment("Always target closest player [performance sensitive]")
	public static boolean omnipotent = false;
	@ConfigComment("skips line of sight check on targetting [performance sensitive]")
	public static boolean seeThroughWalls = false;
	@ConfigComment("Effects how far monsters will sense scents from")
	public static int scentStrength = 60;
	@ConfigComment("Effects how far monsters will sense sound sources from")
	public static int soundStrength = 60;
	@ConfigComment("max rate of spawning sound sources in milliseconds")
	public static int frequentSoundThreshold = 1000;
	@ConfigComment("currently unused")
	public static int zombieRandSpeedBoost = 5;
	//public static int tickRateMainLoop = 1;
	@ConfigComment("how frequently the mod iterates all entities, effects rates, less is more frequent [performance sensitive]")
	public static int tickRateAILoop = 5;
	@ConfigComment("how frequently the mod iterates all players, effects rates, less is more frequent")
	public static int tickRatePlayerLoop = 1;
	
	public static boolean debugConsole = false;
	public static boolean debugConsoleSpawns = false;
	public static boolean debugConsoleOmnipotent = false;
	public static boolean debugConsoleSuperDetailed = false;
	
	@ConfigComment("Minimum distance required between active sense sources, prevents spamming sources [performance sensitive]")
	public static double extraScentCutoffRange = 1;
	@ConfigComment("Random X Z variation of sound source placement")
	public static int soundScentSpawnPosRandom = 1;
	
	//Client only fields
	public static boolean client_renderBlood = true;
	public static boolean client_debugRenderSounds = false;
	
	@Override
	public String getConfigFileName() {
		return "ZombieAwareness" + File.separator + "General";
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: General";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
