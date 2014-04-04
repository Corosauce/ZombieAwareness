package ZombieAwareness;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.OldUtil;
import CoroUtil.pathfinding.PFQueue;

public class CommandZA extends CommandBase {

	@Override
	public String getCommandName() {
		return "za";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
			if (var2.length < 1)
	        {
				//exception throws dont seem to always get sent to player, do it manually
				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("Invalid usage, example: '/za set maxZombiesNight 100, /za get count <entityname>, /za kill <entityname>', see ZAMod.cfg for all possible set configurations"));
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
			        	
			        	var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(var2[1] + " now set to: " + OldUtil.getPrivateValueBoth(ZombieAwareness.class, ZombieAwareness.instance, var2[1], var2[1])));
	        		}
		        	
		        	
		        	//System.out.println("new setting for max zombies: " + ZombieAwareness.maxZombiesNight);
		        	
		        	//if (var2[0] == "maxZombiesNight") ZombieAwareness.maxZombiesNight = Integer.valueOf(var2[1]);
	        	} else if (var2[0].equalsIgnoreCase("get")) {
	        		if (var2[1].equalsIgnoreCase("count")) {
	        			boolean exact = false;
	        			if (var2.length > 3) exact = var2[3].equals("exact");
	        			var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(var2[2] + " count: " + getEntityCount(var2[2], false, exact, ((EntityPlayer)var1).dimension)));
	        		} else if (var2[1].equalsIgnoreCase("time")) {
	        			if (ZombieAwareness.lastSpawnSysTime > 0) {
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("last ZA spawn: " + (int)((System.currentTimeMillis() - ZombieAwareness.lastSpawnSysTime) / 1000F) + "s"));
	        			} else {
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("none yet"));
	        			}
	        		} else if (var2[1].equalsIgnoreCase("PFQueue")) {
	        			if (var2[2].equalsIgnoreCase("lastpf")) {
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("last PF Time: " + ((System.currentTimeMillis() - PFQueue.lastSuccessPFTime) / 1000F)));
	        				//var1.sendChatToPlayer(var2[2] + " set to: " + c_CoroAIUtil.getPrivateValueBoth(PFQueue.class, PFQueue.instance, var2[2], var2[2]));
	        			} if (var2[2].equalsIgnoreCase("stats")) {
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("PFQueue Stats"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("-------------"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(PFQueue.lastQueueSize + " - " + "PF queue size"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(PFQueue.lastChunkCacheCount + " - " + "Cached chunks"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(PFQueue.statsPerSecondPath + " - " + "Pathfinds / 10 sec"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(PFQueue.statsPerSecondPathSkipped + " - " + "Old PF Skips / 10 sec"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(PFQueue.statsPerSecondNodeMaxIter + " - " + "Big PF Skips / 10 sec"));
	        				var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(PFQueue.statsPerSecondNode + " - " + "Nodes ran / 10 sec"));
	        					        				
	        				
	        				
	        			} else {
	        				//var1.sendChatToPlayer("Last chunk cache count: " + PFQueue.lastChunkCacheCount);
		        			var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(var2[2] + " set to: " + OldUtil.getPrivateValueBoth(PFQueue.class, PFQueue.instance, var2[2], var2[2])));
	        			}
	        		} else {
	        			var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(var2[1] + " set to: " + OldUtil.getPrivateValueBoth(ZombieAwareness.class, ZombieAwareness.instance, var2[1], var2[1])));
	        		}
	        	} else if (var2[0].equalsIgnoreCase("kill")) {
	        		boolean exact = false;
	        		int dim = ((EntityPlayer)var1).dimension;
        			//if (var2.length > 2) exact = var2[2].equals("exact");
	        		if (var2.length > 2) dim = Integer.valueOf(var2[1]);
	        		var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(var2[1] + " count killed: " + getEntityCount(var2[1], true, exact, dim)));
	        	} else if (var2[0].equalsIgnoreCase("list")) {
	        		String param = null;
	        		int dim = ((EntityPlayer)var1).dimension;
	        		if (var2.length > 1) dim = Integer.valueOf(var2[1]);
	        		if (var2.length > 2) param = var2[2];
	        		HashMap<String, Integer> entNames = listEntities(param, dim);
	                
	        		var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("List for dimension id: " + dim));
	        		
	                Iterator it = entNames.entrySet().iterator();
	                while (it.hasNext()) {
	                    Map.Entry pairs = (Map.Entry)it.next();
	                    var1.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(pairs.getKey() + " = " + pairs.getValue()));
	                    //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	                    it.remove();
	                }
	        	}
	        	
	        }
		} catch (Exception ex) {
			System.out.println("Caught ZA command crash!!!");
			ex.printStackTrace();
		}
	}
	
	public HashMap<String, Integer> listEntities(String entName, int dim) {
		HashMap<String, Integer> entNames = new HashMap<String, Integer>();
        
        for (int var33 = 0; var33 < DimensionManager.getWorld(dim).loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)DimensionManager.getWorld(dim).loadedEntityList.get(var33);
            
            if (EntityList.getEntityString(ent) != null && (entName == null || EntityList.getEntityString(ent).toLowerCase().contains(entName.toLowerCase()))) {
	            int val = 1;
	            if (entNames.containsKey(EntityList.getEntityString(ent))) {
	            	val = entNames.get(EntityList.getEntityString(ent))+1;
	            }
	            entNames.put(EntityList.getEntityString(ent), val);
            }
            
        }
        return entNames;
	}
	
	public int getEntityCount(String entName, boolean killEntities, boolean exact, int dim) {
		int count = 0;
		
        for (int var33 = 0; var33 < DimensionManager.getWorld(dim).loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)DimensionManager.getWorld(dim).loadedEntityList.get(var33);
            
            if (EntityList.getEntityString(ent) != null && (EntityList.getEntityString(ent).equals(entName) || (!exact && EntityList.getEntityString(ent).toLowerCase().contains(entName.toLowerCase())))) {
            	count++;
            	if (killEntities) ent.setDead();
            }
        }
        
        return count;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

}
