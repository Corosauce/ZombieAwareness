package ZombieAwareness;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ZAEventHandler {
	
	public static World lastWorld = null;
	
	@SubscribeEvent
	public void soundEvent(PlaySoundAtEntityEvent event) {
		
		try {
			/*if (event.getSound().getSoundName().toString().contains("piston")) {
				System.out.println(event.getSound().getSoundName().toString());
			}*/
			
			if (event.getEntity() != null) {
				//moved to world event listener for getting coords
				//ZAUtil.soundHook(event.getSound().getSoundName().toString(), event.getEntity().worldObj, (float)event.getEntity().posX, (float)event.getEntity().posY, (float)event.getEntity().posZ, event.getVolume(), event.getPitch());
			}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	@SubscribeEvent
	public void setAttackTarget(LivingSetAttackTargetEvent event) {
		ZAUtil.hookSetAttackTarget(event);
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		ZAUtil.hookBlockEvent(event, 20);
	}
	
	@SubscribeEvent
	public void harvest(HarvestCheck event) {
		ZAUtil.hookBlockEvent(event, 3);
	}
	
	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (!event.getEntityLiving().worldObj.isRemote) {
			ZAUtil.hookBlockEvent(event, 3);
			
			
		}
	}
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {
			
			if (lastWorld != DimensionManager.getWorld(0)) {
	    		lastWorld = DimensionManager.getWorld(0);
	    		
	    		World worlds[] = DimensionManager.getWorlds();
	    		for (int i = 0; i < worlds.length; i++) {
	    			World world = worlds[i];
	    			world.addEventListener(new WorldEventListener(world.provider.getDimension()));
	    		}
	    	}
			
			//System.out.println("tick ZA");
			ZombieAwareness.instance.onTick(FMLCommonHandler.instance().getMinecraftServerInstance());
		}
	}
}
