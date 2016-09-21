package ZombieAwareness;

import net.minecraft.util.SoundEvent;

public class SoundProfileEntry {

	//for partial matches or SoundEvents converted to resource string
	private String soundName;
	private double multiplier;
	private double distanceMax;

	public SoundProfileEntry(SoundEvent soundEvent, double multiplier, double distanceMax) {
		this(soundEvent.getSoundName().toString(), multiplier, distanceMax);
	}
	
	public SoundProfileEntry(String soundName, double multiplier, double distanceMax) {
		this.soundName = soundName;
		this.multiplier = multiplier;
		this.distanceMax = distanceMax;
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

	public void setDistanceMax(double distanceMax) {
		this.distanceMax = distanceMax;
	}
	
}
