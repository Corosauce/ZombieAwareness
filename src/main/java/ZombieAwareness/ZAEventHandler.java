package ZombieAwareness;

import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ZAEventHandler {
	
	@SubscribeEvent
	public void soundEvent(PlaySoundAtEntityEvent event) {
		
		try {
			if (event.entity != null) {
				ZAUtil.soundHook(event.name, event.entity.worldObj, (float)event.entity.posX, (float)event.entity.posY, (float)event.entity.posZ, event.volume, event.pitch);
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
		if (!event.entityLiving.worldObj.isRemote) {
			ZAUtil.blockEvent(event, 3);
			
			
		}
	}
}
