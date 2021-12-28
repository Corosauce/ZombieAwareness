package com.corosus.zombieawareness.config;

import java.io.File;

import modconfig.ConfigComment;
import modconfig.IConfigCategory;

public class ZAConfigMobLists implements IConfigCategory {



	@Override
	public String getName() {
		return "MobLists";
	}

	@Override
	public String getRegistryName() {
		return "zaconfigmoblists";
	}

	@Override
	public String getConfigFileName() {
		return "zombieawareness" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Mob Lists";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
