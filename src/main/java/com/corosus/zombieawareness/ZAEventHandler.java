package com.corosus.zombieawareness;

import com.corosus.zombieawareness.client.SoundProfileEntry;
import modconfig.ConfigMod;
import modconfig.ModConfigData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import com.corosus.zombieawareness.config.ZAConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = ZombieAwareness.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZAEventHandler {

	@SubscribeEvent
	public void noteBlockEvent(NoteBlockEvent.Play event) {
		if (event.getWorld() instanceof World) {
			ZAUtil.hookSoundEvent(SoundEvents.NOTE_BLOCK_BASS, (World) event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 1, 1);
		}
	}
	
	@SubscribeEvent
	public void soundEvent(PlaySoundAtEntityEvent event) {
		
		try {

			/*if (event.getSound().getSoundName().toString().contains("piston")) {
				System.out.println(event.getSound().getSoundName().toString());
			}*/
			/*String str = event.getSound().getRegistryName().toString();
			if (str.contains("tripwire")) {
				System.out.println(str);
			}*/
			
			if (event.getEntity() != null && !event.getEntity().level.isClientSide()) {
				ZAUtil.hookSoundEvent(event.getSound(), event.getEntity().level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getVolume(), event.getPitch());
				//moved to world event listener for getting coords
				//ZAUtil.soundHook(event.getSound().getSoundName().toString(), event.getEntity().world, (float)event.getEntity().posX, (float)event.getEntity().posY, (float)event.getEntity().posZ, event.getVolume(), event.getPitch());
			}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	@SubscribeEvent
	public void setAttackTarget(LivingSetAttackTargetEvent event) {
		if (!event.getEntityLiving().level.isClientSide) {
			if (!ZAUtil.isZombieAwarenessActive(event.getEntityLiving().level)) return;
			ZAUtil.hookSetAttackTarget(event);
		}
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		if (!event.getEntityLiving().level.isClientSide) {
			if (!ZAUtil.isZombieAwarenessActive(event.getEntityLiving().level)) return;
			if (!ZAConfig.blockHittingEvent_Active) return;
			//ZombieAwareness.dbg("BreakSpeed event");
			ZAUtil.hookBlockEvent(event, ZAConfig.blockHittingEvent_OddsTo1);
		}
	}

	@SubscribeEvent
	public void harvest(HarvestCheck event) {
		if (!event.getEntityLiving().level.isClientSide) {
			/*if (!ZAUtil.isZombieAwarenessActive(event.getEntityLiving().level)) return;
			ZombieAwareness.dbg("HarvestCheck event");
			ZAUtil.hookBlockEvent(event, 3);*/
		}
	}

	/*@SubscribeEvent
	public void breakBlock(BlockEvent.HarvestDropsEvent event) {
		if (!event.getWorld().isClientSide) {
			if (!ZAUtil.isZombieAwarenessActive(event.getWorld())) return;
			if (!ZAConfig.blockBreakEvent_Active) return;
			ZombieAwarenessOld.dbg("HarvestDrops event");
			ZAUtil.handleBlockBasedEvent(event.getHarvester(), event.getWorld(), event.getPos(), 3);
		}
	}*/

	@SubscribeEvent
	public void breakBlock(BlockEvent.BreakEvent event) {
		if (!event.getWorld().isClientSide() && event.getWorld() instanceof World) {
			if (!ZAUtil.isZombieAwarenessActive((World)event.getWorld())) return;
			if (!ZAConfig.blockBreakEvent_Active) return;
			ZombieAwareness.dbg("HarvestDrops event");
			ZAUtil.handleBlockBasedEvent(event.getPlayer(), (World)event.getWorld(), event.getPos(), 3);
		}
	}
	
	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (!event.getEntityLiving().level.isClientSide) {
			if (event.getHand() == Hand.MAIN_HAND) {
				/**
				 * event is way too spammy, since i have much greater sound play access now I am going to try and avoid using this event entirely
				 */
				//ZombieAwareness.dbg("interact event");
				//ZAUtil.hookBlockEvent(event, 3);
			}
		}
	}
	
	/*@SubscribeEvent
	public void tickServer(TickEvent.ServerTickEvent event) {
		
		if (event.phase == TickEvent.Phase.START) {

			if (!ZAUtil.isZombieAwarenessActive(DimensionManager.getWorld(0))) return;

			ZombieAwarenessOld.instance.onTick();
		}
	}*/

	/*@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		int dimID = event.getWorld().provider.getDimension();
		CULog.dbg("adding ZA world listener for dimID: " + dimID + ", remote?: " + event.getWorld().isClientSide);
		event.getWorld().addEventListener(new WorldEventListener(dimID));
	}*/

	@SubscribeEvent
	public void tickEntity(LivingEvent.LivingUpdateEvent event) {
		LivingEntity ent = event.getEntityLiving();
		if (ent.level.isClientSide) return;

		//ZombieAwarenessOld.tickEntity(ent);
		if ((ent.level.getGameTime() + ent.getId()) % ZAConfig.tickRateAILoop == 0) {
			if (ZombieAwareness.canProcessEntity(ent) && ent instanceof MobEntity) {
				ZAUtil.tickAI((MobEntity) ent);
			}
		}
	}

	@SubscribeEvent
	public void tickPlayer(TickEvent.PlayerTickEvent event) {
		if (event.player.level.isClientSide || event.phase == TickEvent.Phase.END) return;

		if (event.player.level.getGameTime() % ZAConfig.tickRatePlayerLoop == 0) {

			ZAUtil.tickPlayer(event.player);
		}

	}

	/*@SubscribeEvent
	public void tickWorld(TickEvent.WorldTickEvent event) {
		if (event.world.isRemote || event.phase == TickEvent.Phase.END) return;

		ZombieAwarenessOld.tickWorld(event.world);
	}*/

	@SubscribeEvent
	public void explosion(ExplosionEvent.Detonate event) {
		SoundProfileEntry entry = ZAUtil.getFirstEntry(SoundEvents.GENERIC_EXPLODE.location.toString());
		if (entry != null) {
			Vector3d pos = event.getExplosion().getPosition();
			PlayerEntity closestPlayer = ZAUtil.getClosestPlayer(event.getWorld(), pos.x, pos.y, pos.z, 128);
			if (closestPlayer != null) {
				ZAUtil.handleSoundProfileEvent(event.getWorld(), entry, pos, closestPlayer);
			}
		}
	}
}
