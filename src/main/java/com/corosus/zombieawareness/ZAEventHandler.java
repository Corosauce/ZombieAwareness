package com.corosus.zombieawareness;

import com.corosus.zombieawareness.client.SoundProfileEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.PlayLevelSoundEvent;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;

@Mod.EventBusSubscriber(modid = ZombieAwareness.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZAEventHandler {

	@SubscribeEvent
	public void noteBlockEvent(NoteBlockEvent.Play event) {
		if (event.getLevel() instanceof Level) {
			ZAUtil.hookSoundEvent(SoundEvents.NOTE_BLOCK_BASS.get(), (Level) event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 1, 1);
		}
	}
	
	@SubscribeEvent
	public void soundEvent(PlayLevelSoundEvent.AtEntity event) {
		
		try {

			/*if (event.getSound().getSoundName().toString().contains("piston")) {
				System.out.println(event.getSound().getSoundName().toString());
			}*/
			/*String str = event.getSound().getRegistryName().toString();
			if (str.contains("tripwire")) {
				System.out.println(str);
			}*/
			
			if (event.getEntity() != null && !event.getEntity().level().isClientSide()) {
				ZAUtil.hookSoundEvent(event.getSound().get(), event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getNewVolume(), event.getNewPitch());
				//moved to world event listener for getting coords
				//ZAUtil.soundHook(event.getSound().getSoundName().toString(), event.getEntity().world, (float)event.getEntity().posX, (float)event.getEntity().posY, (float)event.getEntity().posZ, event.getVolume(), event.getPitch());
			}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	@SubscribeEvent
	public void setAttackTarget(LivingChangeTargetEvent event) {
		if (!event.getEntity().level().isClientSide) {
			if (!ZAUtil.isZombieAwarenessActive(event.getEntity().level())) return;
			ZAUtil.hookSetAttackTarget(event);
		}
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		if (!event.getEntity().level().isClientSide) {
			if (!ZAUtil.isZombieAwarenessActive(event.getEntity().level())) return;
			if (!ZAConfigGeneral.blockHittingEvent_Active) return;
			//ZombieAwareness.dbg("BreakSpeed event");
			ZAUtil.hookBlockEvent(event, ZAConfigGeneral.blockHittingEvent_OddsTo1);
		}
	}

	@SubscribeEvent
	public void harvest(HarvestCheck event) {
		if (!event.getEntity().level().isClientSide) {
			/*if (!ZAUtil.isZombieAwarenessActive(event.getEntity().level())) return;
			ZombieAwareness.dbg("HarvestCheck event");
			ZAUtil.hookBlockEvent(event, 3);*/
		}
	}

	/*@SubscribeEvent
	public void breakBlock(BlockEvent.HarvestDropsEvent event) {
		if (!event.getlevel()().isClientSide) {
			if (!ZAUtil.isZombieAwarenessActive(event.getlevel()())) return;
			if (!ZAConfig.blockBreakEvent_Active) return;
			ZombieAwarenessOld.dbg("HarvestDrops event");
			ZAUtil.handleBlockBasedEvent(event.getHarvester(), event.getlevel()(), event.getPos(), 3);
		}
	}*/

	@SubscribeEvent
	public void breakBlock(BlockEvent.BreakEvent event) {
		if (!event.getLevel().isClientSide() && event.getLevel() instanceof Level) {
			if (!ZAUtil.isZombieAwarenessActive((Level)event.getLevel())) return;
			if (!ZAConfigGeneral.blockBreakEvent_Active) return;
			ZombieAwareness.dbg("HarvestDrops event");
			ZAUtil.handleBlockBasedEvent(event.getPlayer(), (Level)event.getLevel(), event.getPos(), 3);
		}
	}
	
	@SubscribeEvent
	public void interact(PlayerInteractEvent event) {
		if (!event.getEntity().level().isClientSide) {
			if (event.getHand() == InteractionHand.MAIN_HAND) {
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

			if (!ZAUtil.isZombieAwarenessActive(DimensionManager.getLevel(0))) return;

			ZombieAwarenessOld.instance.onTick();
		}
	}*/

	/*@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		int dimID = event.getLevel().provider.getDimension();
		CULog.dbg("adding ZA world listener for dimID: " + dimID + ", remote?: " + event.getLevel().isClientSide);
		event.getLevel().addEventListener(new WorldEventListener(dimID));
	}*/

	@SubscribeEvent
	public void tickEntity(LivingEvent.LivingTickEvent event) {
		LivingEntity ent = event.getEntity();
		if (ent.level().isClientSide) return;

		//ZombieAwarenessOld.tickEntity(ent);
		if ((ent.level().getGameTime() + ent.getId()) % Math.max(1, ZAConfigGeneral.tickRateAILoop) == 0) {
			if (ZombieAwareness.canProcessEntity(ent) && ent instanceof Mob) {
				ZAUtil.tickAI((Mob) ent);
			}
		}
	}

	@SubscribeEvent
	public void spawnEntity(MobSpawnEvent.FinalizeSpawn event) {
		LivingEntity ent = event.getEntity();
		if (ent.level().isClientSide) return;

		ZAUtil.processMobSpawn(event);
	}

	@SubscribeEvent
	public void tickPlayer(TickEvent.PlayerTickEvent event) {
		if (event.player.level().isClientSide || event.phase == TickEvent.Phase.END) return;

		if (event.player.level().getGameTime() % ZAConfigGeneral.tickRatePlayerLoop == 0) {

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
		SoundProfileEntry entry = ZAUtil.getSoundIDEntry(SoundEvents.GENERIC_EXPLODE.getLocation().toString());
		if (entry != null) {
			Vec3 pos = event.getExplosion().getPosition();
			Player closestPlayer = ZAUtil.getClosestPlayer(event.getLevel(), pos.x, pos.y, pos.z, 128);
			if (closestPlayer != null) {
				ZAUtil.handleSoundProfileEvent(event.getLevel(), entry, new Vector3d(pos.x, pos.y, pos.z), closestPlayer);
			}
		}
	}
}
