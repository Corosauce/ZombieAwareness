package ZombieAwareness;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ZAEventHandler {
	
	@ForgeSubscribe
	public void soundEvent(PlaySoundAtEntityEvent event) {
		
		try {
			ZAUtil.soundHook(event.name, event.entity.worldObj, (float)event.entity.posX, (float)event.entity.posY, (float)event.entity.posZ, event.volume, event.pitch);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	@ForgeSubscribe
	public void breakSpeed(BreakSpeed event) {
		ZAUtil.blockEvent(event, 20);
	}
	
	@ForgeSubscribe
	public void harvest(HarvestCheck event) {
		ZAUtil.blockEvent(event, 3);
	}
	
	@ForgeSubscribe
	public void interact(PlayerInteractEvent event) {
		if (!event.entityLiving.worldObj.isRemote) {
			ZAUtil.blockEvent(event, 3);
			
			
		}
	}
}
