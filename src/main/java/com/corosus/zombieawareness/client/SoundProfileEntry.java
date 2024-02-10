package com.corosus.zombieawareness.client;

import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Accounts for all things we need deal with for sounds, range, amp, chance, matching, etc
 * 
 * @author Corosus
 *
 */
public class SoundProfileEntry {

	//for both partial matches and SoundEvents converted to resource string
	private String soundName;
	private List<Integer> listSoundEventTypes = new ArrayList<>();
	private boolean isSoundType;
	//when used, the amount it amplifies the strength, and strength translates to distance heard from
	private double multiplier;
	private double maxDistToSpawnFromPlayer = 4D;
	private boolean partialMatchOnly = false;
	//0 = always use, random chance of actually spawning a sound sense when the sound happens, helps for sounds that play a lot, preventing sense spam
	private int oddsTo1ToUse = 0;
	
	public static String getSoundEventName(SoundEvent soundEvent) {
		return soundEvent.getLocation().toString();
	}

	public SoundProfileEntry(SoundEvent soundEvent, double multiplier/*, double distanceMax*/) {
		this(getSoundEventName(soundEvent), multiplier/*, distanceMax*/, false, 0);
	}
	
	public SoundProfileEntry(SoundEvent soundEvent, double multiplier/*, double distanceMax*/, int oddsTo1ToUse) {
		this(getSoundEventName(soundEvent), multiplier/*, distanceMax*/, false, oddsTo1ToUse);
	}
	
	public SoundProfileEntry(String soundName, double multiplier) {
		this(soundName, multiplier, true, 0);
	}
	
	public SoundProfileEntry(String soundName, double multiplier/*, double distanceMax*/, boolean partialMatchOnly, int oddsTo1ToUse) {
		this.soundName = soundName;
		this.multiplier = multiplier;
		//this.distanceMax = distanceMax;
		this.partialMatchOnly = partialMatchOnly;
		this.oddsTo1ToUse = oddsTo1ToUse;
	}
	
	public String getSoundName() {
		return soundName;
	}

	public void setSoundName(String soundName) {
		this.soundName = soundName;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getMaxDistToSpawnFromPlayer() {
		return maxDistToSpawnFromPlayer;
	}

	public SoundProfileEntry setMaxDistToSpawnFromPlayer(double maxDistToSpawnFromPlayer) {
		this.maxDistToSpawnFromPlayer = maxDistToSpawnFromPlayer;
		return this;
	}

	public boolean isPartialMatchOnly() {
		return partialMatchOnly;
	}

	public void setPartialMatchOnly(boolean partialMatchOnly) {
		this.partialMatchOnly = partialMatchOnly;
	}

	public int getOddsTo1ToUse() {
		return oddsTo1ToUse;
	}

	public void setOddsTo1ToUse(int oddsTo1ToUse) {
		this.oddsTo1ToUse = oddsTo1ToUse;
	}

	public boolean containsSoundType(int soundType) {
		return listSoundEventTypes.contains(soundType);
	}

	public List<Integer> getListSoundEventTypes() {
		return listSoundEventTypes;
	}

	public SoundProfileEntry setListSoundEventTypes(List<Integer> listSoundEventTypes) {
		this.listSoundEventTypes = listSoundEventTypes;
		this.isSoundType = true;
		return this;
	}

	public boolean isSoundType() {
		return isSoundType;
	}

	public void setSoundType(boolean soundType) {
		isSoundType = soundType;
	}
}
