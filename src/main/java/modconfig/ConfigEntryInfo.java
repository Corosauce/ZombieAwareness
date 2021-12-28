package modconfig;

import modconfig.gui.GuiBetterTextField;
import net.minecraft.client.Minecraft;

public class ConfigEntryInfo {
	public int index;
	public String name;
	public Object value;
	
	/** Comment/description associated with value */
	public String comment;
	
	public boolean markForUpdate = false;
	
	public ConfigEntryInfo(int parIndex, String parName, Object parVal, String parComment) {
		index = parIndex;
		name = parName;
		value = parVal;
		comment = parComment;
	}
}
