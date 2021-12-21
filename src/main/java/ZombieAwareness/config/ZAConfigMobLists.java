package ZombieAwareness.config;

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
		return "ZombieAwareness" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return "Zombie Awareness: Mob Lists";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
