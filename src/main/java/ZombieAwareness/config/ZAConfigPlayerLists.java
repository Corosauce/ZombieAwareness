package ZombieAwareness.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ZAConfigPlayerLists implements IConfigCategory {

	//Whitelists and blacklists
	@ConfigComment("Uses list of people to have omniscient targetting effect")
	public static boolean whiteListUsedOmniscient = false;
	@ConfigComment("Uses list of people to have senses spawned for")
	public static boolean whiteListUsedSenses = false;
	/*@ConfigComment("Uses list of mobs to prevent enhanced AI on")
	public static boolean blacklistUsedAITick = true;
	@ConfigComment("swaps blacklistUsedAITick/blacklistAITick into a whitelist")
	public static boolean forceListUsedAITickAsWhitelist = false;*/
	@ConfigComment("Uses list of people to have spawning of zombies for")
	public static boolean whiteListUsedExtraSpawning = false;
	@ConfigComment("List of people to have omniscient targetting effect")
	public static String whitelistOmniscientTargettedPlayers = "Corosus, SomeDude";
	@ConfigComment("List of people to have senses spawned for")
	public static String whitelistSenses = "Corosus, SomeDude";
	/*@ConfigComment("List of mobs to prevent enhanced AI on")
	public static String blacklistAITick = "Creeper, Enderman, Wolf";*/
	@ConfigComment("List of people to have spawning of zombies for")
	public static String whitelistExtraSpawning = "Corosus";

	@Override
	public String getName() {
		return "PlayerRulesAndLists";
	}

	@Override
	public String getRegistryName() {
		return "zaconfigplayerlists";
	}

	@Override
	public String getConfigFileName() {
		return "ZombieAwareness" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Player Rules & Lists";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
