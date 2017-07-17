package ZombieAwareness;

import CoroUtil.OldUtil;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.util.CoroUtilMisc;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandZA extends CommandBase {

	@Override
	public String getName() {
		return "za";
	}

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "Magic dev method!";
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
			if (var2.length < 1)
	        {
				//exception throws dont seem to always get sent to player, do it manually
				CoroUtilMisc.sendCommandSenderMsg(var1, "Invalid usage, example: '/za set maxZombiesNight 100, /za get count <entityname>, /za kill <entityname>', see ZAMod.cfg for all possible set configurations");
	            throw new WrongUsageException("Invalid usage");
	        }
	        else
	        {

	        	if (var2[0].equalsIgnoreCase("set")) {
	        		if (var2[1].equalsIgnoreCase("PFQueue")) {
	        			PFQueue.instance = null;
	        		} else {
	        			int intVal = 0;
			        	try {
			        		intVal = Integer.valueOf(var2[2]);
			        	} catch (Exception ex2) { } //silence!
			        	boolean boolVal = Boolean.valueOf(var2[2]);

			        	if (intVal > 0) {
			        		OldUtil.setPrivateValueBoth(ZombieAwareness.class, ZombieAwareness.instance, var2[1], var2[1], intVal);
			        	} else {
			        		OldUtil.setPrivateValueBoth(ZombieAwareness.class, ZombieAwareness.instance, var2[1], var2[1], boolVal);
			        	}

			        	CoroUtilMisc.sendCommandSenderMsg(var1, var2[1] + " now set to: " + OldUtil.getPrivateValueBoth(ZombieAwareness.class, ZombieAwareness.instance, var2[1], var2[1]));
	        		}


		        	//System.out.println("new setting for max zombies: " + ZombieAwareness.maxZombiesNight);

		        	//if (var2[0] == "maxZombiesNight") ZombieAwareness.maxZombiesNight = Integer.valueOf(var2[1]);
	        	} else if (var2[0].equalsIgnoreCase("get")) {
	        		if (var2[1].equalsIgnoreCase("time")) {
	        			if (ZombieAwareness.lastSpawnSysTime > 0) {
	        				CoroUtilMisc.sendCommandSenderMsg(var1, "last ZA spawn: " + (int)((System.currentTimeMillis() - ZombieAwareness.lastSpawnSysTime) / 1000F) + "s");
	        			} else {
	        				CoroUtilMisc.sendCommandSenderMsg(var1, "none yet");
	        			}
	        		} else if (var2[1].equalsIgnoreCase("counts")) {
	        			CoroUtilMisc.sendCommandSenderMsg(var1, "surface: " + ZombieAwareness.lastMobsCountSurface + ", caves: " + ZombieAwareness.lastMobsCountCaves);
	        		} else {
	        			CoroUtilMisc.sendCommandSenderMsg(var1, var2[1] + " set to: " + OldUtil.getPrivateValueBoth(ZombieAwareness.class, ZombieAwareness.instance, var2[1], var2[1]));
	        		}
				} else if (var2[0].equalsIgnoreCase("isActive")) {
					var1.sendMessage(new TextComponentString((ZAUtil.isZombieAwarenessActive(world) ? "true" : "false")));
				} else if (var2[0].equalsIgnoreCase("profile") && player != null) {
					ZAUtil.startProfile(player.getName());
					player.sendMessage(new TextComponentString("ZA Profile started"));
				}

	        }
		} catch (Exception ex) {
			System.out.println("Caught ZA command crash!!!");
			ex.printStackTrace();
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

}
