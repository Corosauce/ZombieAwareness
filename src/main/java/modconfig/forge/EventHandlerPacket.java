package modconfig.forge;

import java.util.Collections;
import java.util.Iterator;

import CoroUtil.forge.CULog;
import modconfig.ConfigComparatorName;
import modconfig.ConfigEntryInfo;
import modconfig.ConfigMod;
import modconfig.gui.GuiConfigEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.packet.PacketHelper;

public class EventHandlerPacket {

	@SubscribeEvent
    @SideOnly(Side.CLIENT)
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {

		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());
			String command = nbt.getString("command");
			
			//System.out.println("command: " + command);

			Minecraft.getMinecraft().addScheduledTask(() -> {
				if (command.equals("setData")) {
					String modID = nbt.getString("modID");
					NBTTagCompound nbtEntries = nbt.getCompoundTag("entries");
					//int entryCount = nbt.getInteger("entryCount");
					int pos = 0;
					//ConfigMod.dbg("modconfig packet, size: " + "derp");
					if (!GuiConfigEditor.clientMode || ConfigMod.configLookup.get(modID).configData.size() == 0) {
						ConfigMod.configLookup.get(modID).configData.clear();
						//Iterator it = nbtEntries.getTagList(p_150295_1_, p_150295_2_)
						Iterator it = nbtEntries.getKeySet().iterator();
						while (it.hasNext()) {
							String tagName = (String) it.next();
							NBTTagCompound entry = nbtEntries.getCompoundTag(tagName);
							String str1 = entry.getString("name");
							String str2 = entry.getString("value");
							String str3 = "";//dis.readUTF();
							ConfigMod.configLookup.get(modID).configData.add(new ConfigEntryInfo(pos++, str1, str2, str3));
						}
						try {
							Collections.sort(ConfigMod.configLookup.get(modID).configData, new ConfigComparatorName());
						} catch (Exception ex) {
							CULog.err("coroutil configmod exception sorting config values");
							//shh
						}
					}
				} else if (command.equals("openGUI")) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiConfigEditor());
				}
			});

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP entP = ((NetHandlerPlayServer)event.getHandler()).player;
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());
			String command = nbt.getString("command");
			
			//System.out.println("command: " + command);

			entP.mcServer.addScheduledTask(() -> {
				if (command.equals("setData")) {
					String data = nbt.getString("data");

					if (entP instanceof EntityPlayerMP) {
						CommandModConfig.parseSetCommand((EntityPlayerMP) entP, data.split(" "));
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
