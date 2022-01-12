package com.corosus.zombieawareness.config;

import java.io.File;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;

public class ZAConfigPlayerLists implements IConfigCategory {

	//Whitelists and blacklists
	@ConfigComment("Uses list of people to have omniscient targetting effect")
	public static boolean whiteListUsedOmniscient = false;
	@ConfigComment("Uses list of people to have senses spawned for")
	public static boolean whiteListUsedSenses = false;
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
