package ZombieAwareness;

import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ZAEventHandler {
	
	@SubscribeEvent
	public void soundEvent(PlaySoundAtEntityEvent event) {
		
		try {
			if (event.getEntity() != null) {
				ZAUtil.soundHook(event.getSound().getSoundName().toString(), event.getEntity().worldObj, (float)event.getEntity().posX, (float)event.getEntity().posY, (float)event.getEntity().posZ, event.getVolume(), event.getPitch());
			}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		ZAUtil.blockEvent(event, 20);
	}
	
	@SubscribeEvent
	public void harvest(HarvestCheck event) {
		ZAUtil.blockEvent(event, 3);
	}
	
	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (!event.getEntityLiving().worldObj.isRemote) {
			ZAUtil.blockEvent(event, 3);
			
			
		}
	}
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {
			//System.out.println("tick ZA");
			ZombieAwareness.instance.onTick(FMLCommonHandler.instance().getMinecraftServerInstance());
		}
	}
}
