package modconfig.forge;

import java.util.ArrayList;
import java.util.List;

import modconfig.ConfigMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import CoroUtil.util.CoroUtilMisc;

public class CommandModConfig extends CommandBase {

	@Override
	public String getName() {
		return "config";
	}

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "Magic dev method!";
	}
	
	@Override
	public List getTabCompletions(MinecraftServer server, ICommandSender par1ICommandSender, String[] par2ArrayOfStr, BlockPos pos)
    {
		List<String> list = new ArrayList<String>(ConfigMod.configLookup.get(getName()).valsBoolean.keySet());
		list.addAll(ConfigMod.configLookup.get(getName()).valsInteger.keySet());
		list.addAll(ConfigMod.configLookup.get(getName()).valsString.keySet());
        return list;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {
		
		EntityPlayer player = null;
		if (var1 instanceof EntityPlayer) {
			player = (EntityPlayer) var1;
		}
		World world = var1.getEntityWorld();
		int dimension = world.provider.getDimension();
		BlockPos posBlock = var1.getPosition();
		Vec3d posVec = var1.getPositionVector();
		
		try {
			/*if(var1 instanceof EntityPlayerMP)
			{*/
				int cmd = 0;
				int modid = 1;
				int field = 2;
				int vall = 3;
				//EntityPlayer player = getCommandSenderAsPlayer(var1);
				//EntityPlayerMP playerMP = (EntityPlayerMP) player;
				if (var2.length > 0) {
					if (var2[cmd].equalsIgnoreCase("get")) {
						if (var2.length > 2) {
							Object obj = ConfigMod.getField(var2[modid], var2[field]);
							if (obj != null) {
								var1.sendMessage(new TextComponentString(var2[field] + " = " + obj));
							} else {
								CoroUtilMisc.sendCommandSenderMsg(var1, "failed to get " + var2[field]);
							}
						} else {
							CoroUtilMisc.sendCommandSenderMsg(var1, "get requires 3 parameters");
						}
					} else if (var2[cmd].equalsIgnoreCase("set")) {
						if (var2.length > 2) {
							
							parseSetCommand((EntityPlayerMP) var1, var2);
							
							/*String val = "";
							for (int i = vall; i < var2.length; i++) val += var2[i] + (i != var2.length-1 ? " " : "");
							if (ConfigMod.updateField(var2[modid], var2[field], val)) {
								CoroUtil.sendPlayerMsg(playerMP, "set " + var2[field] + " to " + val);
								
								List blah = new ArrayList();
								
								blah.add((String)var2[field]);
								blah.add((String)val);
								
								ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacket(var2[modid]), (EntityPlayerMP)player);
								//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(PacketHelper.getModConfigPacket(var2[modid]));
							} else {
								CoroUtil.sendPlayerMsg(playerMP, "failed to set " + var2[field]);
							}*/
						} else {
							CoroUtilMisc.sendCommandSenderMsg(var1, "set requires 3+ parameters");
						}
					} else if (var2[cmd].equalsIgnoreCase("reload") && player != null) {
						ConfigMod.forceLoadRuntimeSettingsFromFile();
						player.sendMessage(new TextComponentString("Reloaded all runtime configurations from file"));
					} else if (var2[cmd].equalsIgnoreCase("update") && player != null) {
						ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacket(var2[modid]), (EntityPlayerMP)player);
						//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(PacketHelper.getModConfigPacket(var2[modid]));
					} else if ((var2[cmd].equalsIgnoreCase("menu") || var2[cmd].equalsIgnoreCase("gui")) && player != null) {
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setString("command", "openGUI");
						ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacketMenu(), (EntityPlayerMP)player);
					}
					
				} else if (player != null) {
					//((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(PacketHelper.getModConfigPacketMenu());
					ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacketMenu(), (EntityPlayerMP)player);
				}
			/*}*/
		} catch (Exception ex) {
			System.out.println("Exception handling Config Mod command");
			ex.printStackTrace();
		}
		
	}
	
	public static void parseSetCommand(EntityPlayerMP playerMP, String[] var2) {
		int cmd = 0;
		int modid = 1;
		int field = 2;
		int vall = 3;
		
		String val = "";
		for (int i = vall; i < var2.length; i++) val += var2[i] + (i != var2.length-1 ? " " : "");
		if (ConfigMod.updateField(var2[modid], var2[field], val)) {
			CoroUtilMisc.sendCommandSenderMsg(playerMP, "set " + var2[field] + " to " + val);
			
			List blah = new ArrayList();
			
			blah.add((String)var2[field]);
			blah.add((String)val);
			
			ConfigMod.eventChannel.sendTo(PacketHelper.getModConfigPacket(var2[modid]), playerMP);
			//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(PacketHelper.getModConfigPacket(var2[modid]));
		} else {
			CoroUtilMisc.sendCommandSenderMsg(playerMP, "failed to set " + var2[field]);
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender)
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

}
