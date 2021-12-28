package modconfig;

/* To be used for future stuff like generic tile entity configuration, as well as item configuration (which writes out to the itemstack nbt)
 * Unlike IConfigCategory, this data will be handled in mc internal nbt systems and not forge config files, unless a special case comes up where outside editing in a txt editor is recommended */
public interface IConfigInstance {

	public void readData();
	public void writeData();
	
}
