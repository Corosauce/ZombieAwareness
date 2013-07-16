package ZombieAwareness.config;

import java.io.File;

import modconfig.IConfigCategory;

public class ZAConfigPlayerLists implements IConfigCategory {

	//Whitelists and blacklists
	public static boolean whiteListUsedOmnipotent = false;
	public static boolean whiteListUsedSenses = false;
	public static boolean blacklistUsedAITick = true;
	public static String whitelistOmnipotentTargettedPlayers = "Corosus, SomeDude";
	public static String whitelistSenses = "Corosus, SomeDude";
	public static String blacklistAITick = "Creeper, Enderman, Wolf";
	public static String whitelistExtraSpawning = "Corosus";

	@Override
	public String getConfigFileName() {
		// TODO Auto-generated method stub
		return "ZombieAwareness" + File.separator + "PlayerRulesAndLists";
	}

	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return "Zombie Awareness: Player Rules & Lists";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
