package com.corosus.zombieawareness.config;

public interface IConfigCategory {

	public String getConfigFileName();
	public String getCategory();
	public void hookUpdatedValues();
	public String getName();
	public String getRegistryName();
	
}
