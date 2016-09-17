package ZombieAwareness;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumSenseType {
	
	SCENT_BLOOD, SOUND, WAYPOINT;
	
	private static final Map<Integer, EnumSenseType> lookup = new HashMap<Integer, EnumSenseType>();
    static { for(EnumSenseType e : EnumSet.allOf(EnumSenseType.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumSenseType get(int intValue) { return lookup.get(intValue); }
}
