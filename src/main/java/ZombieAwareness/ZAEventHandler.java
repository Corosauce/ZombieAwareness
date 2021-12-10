package ZombieAwareness;

import CoroUtil.forge.CULog;
import ZombieAwareness.config.ZAConfig;
import net.minecraft.util.Hand;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ZAEventHandler {
	
	@SubscribeEvent
	public void soundEvent(PlaySoundAtEntityEvent event) {
		
		try {
			/*if (event.getSound().getSoundName().toString().contains("piston")) {
				System.out.println(event.getSound().getSoundName().toString());
			}*/
			
			if (event.getEntity() != null) {
				//moved to world event listener for getting coords
				//ZAUtil.soundHook(event.getSound().getSoundName().toString(), event.getEntity().world, (float)event.getEntity().posX, (float)event.getEntity().posY, (float)event.getEntity().posZ, event.getVolume(), event.getPitch());
			}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	@SubscribeEvent
	public void setAttackTarget(LivingSetAttackTargetEvent event) {
		if (!event.getEntityLiving().world.isRemote) {
			if (!ZAUtil.isZombieAwarenessActive(event.getEntityLiving().world)) return;
			ZAUtil.hookSetAttackTarget(event);
		}
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		if (!event.getEntityLiving().world.isRemote) {
			if (!ZAUtil.isZombieAwarenessActive(event.getEntityLiving().world)) return;
			if (!ZAConfig.blockHittingEvent_Active) return;
			//ZombieAwareness.dbg("BreakSpeed event");
			ZAUtil.hookBlockEvent(event, ZAConfig.blockHittingEvent_OddsTo1);
		}
	}

	@SubscribeEvent
	public void harvest(HarvestCheck event) {
		if (!event.getEntityLiving().world.isRemote) {
			/*if (!ZAUtil.isZombieAwarenessActive(event.getEntityLiving().world)) return;
			ZombieAwareness.dbg("HarvestCheck event");
			ZAUtil.hookBlockEvent(event, 3);*/
		}
	}

	@SubscribeEvent
	public void breakBlock(BlockEvent.HarvestDropsEvent event) {
		if (!event.getWorld().isRemote) {
			if (!ZAUtil.isZombieAwarenessActive(event.getWorld())) return;
			if (!ZAConfig.blockBreakEvent_Active) return;
			ZombieAwareness.dbg("HarvestDrops event");
			ZAUtil.handleBlockBasedEvent(event.getHarvester(), event.getWorld(), event.getPos(), 3);
		}
	}
	
	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (!event.getEntityLiving().world.isRemote) {
			if (event.getHand() == Hand.MAIN_HAND) {
				/**
				 * event is way too spammy, since i have much greater sound play access now I am going to try and avoid using this event entirely
				 */
				//ZombieAwareness.dbg("interact event");
				//ZAUtil.hookBlockEvent(event, 3);
			}
		}
	}
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {

			if (!ZAUtil.isZombieAwarenessActive(DimensionManager.getWorld(0))) return;

			ZombieAwareness.instance.onTick();
		}
	}

	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		int dimID = event.getWorld().provider.getDimension();
		CULog.dbg("adding ZA world listener for dimID: " + dimID + ", remote?: " + event.getWorld().isRemote);
		event.getWorld().addEventListener(new WorldEventListener(dimID));
	}

	@SubscribeEvent
	public void tickEntity(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntityLiving().world.isRemote) return;

		ZombieAwareness.tickEntity(event.getEntityLiving());
	}

	@SubscribeEvent
	public void tickPlayer(TickEvent.PlayerTickEvent event) {
		if (event.player.world.isRemote || event.phase == Phase.END) return;

		if (event.player.world.getTotalWorldTime() % ZAConfig.tickRatePlayerLoop == 0) {

			if (event.player.world.provider.getDimension() == 0) {
				//ZAUtil.tickPlayerOverworldOnly(event.player);
			}

			ZAUtil.tickPlayer(event.player);
		}

	}

	@SubscribeEvent
	public void tickWorld(TickEvent.WorldTickEvent event) {
		if (event.world.isRemote || event.phase == Phase.END) return;

		ZombieAwareness.tickWorld(event.world);
	}
}
