package com.corosus.zombieawareness.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import com.corosus.zombieawareness.ZombieAwareness;

import java.io.File;

public class ZAConfigPlayerLists implements IConfigCategory {

	//Whitelists and blacklists
	@ConfigComment("Uses list of people to have omniscient targetting effect")
	public static boolean whiteListUsedOmniscient = false;
	@ConfigComment("Uses list of people to have senses spawned for")
	public static boolean whiteListUsedSenses = true;
	//public static boolean whiteListUsedSenses = false;
	@ConfigComment("If using the whitelist, nothing will happen if a non whitelisted player is nearby")
	public static boolean notNearNonWhitelistedPlayers = true;
	//public static boolean notNearNonWhitelistedPlayers = false;
	public static int dist = 32;
	@ConfigComment("List of people to have omniscient targetting effect")
	public static String whitelistOmniscientTargettedPlayers = "Corosus, SomeDude";
	@ConfigComment("List of people to have senses spawned for")
	public static String whitelistSenses = "Corosus, SomeDude";

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
		return ZombieAwareness.MODID + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Player Rules & Lists";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
