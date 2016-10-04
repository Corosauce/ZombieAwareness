package ZombieAwareness;

import net.minecraft.util.SoundEvent;

/**
 * Accounts for all things we need deal with for sounds, range, amp, chance, matching, etc
 * 
 * @author Corosus
 *
 */
public class SoundProfileEntry {

	//for both partial matches and SoundEvents converted to resource string
	private String soundName;
	private double multiplier;
	private double distanceMax = 3D;
	private boolean partialMatchOnly = false;
	//0 = always use
	private int oddsTo1ToUse = 0;

	public SoundProfileEntry(SoundEvent soundEvent, double multiplier/*, double distanceMax*/) {
		this(soundEvent.getSoundName().toString(), multiplier/*, distanceMax*/, false, 0);
	}
	
	public SoundProfileEntry(SoundEvent soundEvent, double multiplier/*, double distanceMax*/, int oddsTo1ToUse) {
		this(soundEvent.getSoundName().toString(), multiplier/*, distanceMax*/, false, oddsTo1ToUse);
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

	public double getDistanceMax() {
		return distanceMax;
	}

	public SoundProfileEntry setDistanceMax(double distanceMax) {
		this.distanceMax = distanceMax;
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
	
}
