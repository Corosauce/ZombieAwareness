package com.corosus.zombieawareness.config;

import com.corosus.modconfig.IConfigCategory;
import com.corosus.zombieawareness.ZombieAwareness;

import java.io.File;

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
		return ZombieAwareness.MODID + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Mob Lists";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
