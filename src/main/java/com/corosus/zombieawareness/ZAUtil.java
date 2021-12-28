package com.corosus.zombieawareness;

import com.corosus.coroutil.util.*;
import com.corosus.zombieawareness.client.SoundProfileEntry;
import com.corosus.zombieawareness.client.SoundRegistry;
import com.corosus.zombieawareness.config.ZAConfig;
import com.corosus.zombieawareness.config.ZAConfigFeatures;
import com.corosus.zombieawareness.config.ZAConfigPlayerLists;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.*;

public class ZAUtil {
	
	/**
	 * 
	 * Default settings: 
	 * 
	 * - sound trigger: start at 8 range sense, increase to 32 as more are done
	 * - scent trigger: start at 24, fade, reset fade if new blood
	 * 
	 * - sense strength effects:
	 * - range it can be sensed
	 * - chance of them sensing it?
	 * 
	 * - strength * buff
	 * - if new, use above
	 * - if exists, apply multiplier
	 * 
	 * - must have sound triggers:
	 * - block mine
	 * - block place eg minecraft:block.gravel.place
	 * - chest use
	 * - doors
	 * - buttons
	 * - levers
	 * 
	 * - large sound triggers:
	 * -- explosions
	 * -- falling blocks
	 * -- falling anvils
	 * -- 
	 * 
	 * for 1.10.2 release
	 * x whitelist instead of blacklist i think
	 * x sound sense profile for long distance ones, for explosion, piston, noisy zombie, has data:
	 * x- sound event or partial string match
	 * x- max distance spawnable
	 * x- multiplier
	 * x buff jukebox, notebox
	 * x fix interact spam
	 * 
	 * 
	 */
    
    public static Random rand = new Random();
    
    public static HashMap<String, Integer> lastHealths = new HashMap();
    public static HashMap<String, Long> lastBleedTimes = new HashMap();

    public static List<SoundProfileEntry> listSoundProfiles = new ArrayList<SoundProfileEntry>();
    
    public static WeakHashMap<Entity, Long> lookupLastAlertTime = new WeakHashMap<Entity, Long>();
    public static long alertDelay = 60*20;
    
    public static WeakHashMap<Entity, Long> lookupLastInvestigateTime = new WeakHashMap<Entity, Long>();
    public static long investigateDelay = 60*20;

	public static HashMap<EntityType, Boolean> lookupTickableEntities = new HashMap<>();

	//quick process testing
	public static HashMap<Class, Integer> lookupProcessedEntityCounts = new HashMap<>();
	public static boolean profileActive = false;
	public static long profileStartTime = 0;
	public static String profileForPlayer = "";

	public static HashMap<RegistryKey<World>, WorldData> lookupWorldData = new HashMap<>();
    
    public static boolean debug = false;
    
    static {
		addSoundHooks();
    }

	public static void addSoundHooks() {
		int noisyInteractRange = 30;
		double noisyInteractBuff = 1.3D;

		//short dist ones
		listSoundProfiles.clear();

		//a lot of these lack entity source, hrm

		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ARROW_HIT_PLAYER, 1.1D));
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ARROW_HIT, 1.1D));
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.CROSSBOW_HIT, 1.1D));

		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.CHEST_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));

		/*listSoundProfiles.add(new SoundProfileEntry(SoundEvents.WOODEN_DOOR_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.IRON_DOOR_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.WOODEN_TRAPDOOR_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));*/

		List<Integer> listDoorSoundEvents = new ArrayList<>();
		//trap doors opening and close, metal and wood
		listDoorSoundEvents.add(1037);
		listDoorSoundEvents.add(1007);
		listDoorSoundEvents.add(1036);
		listDoorSoundEvents.add(1013);
		//doors, opening and close, metal and wood
		listDoorSoundEvents.add(1011);
		listDoorSoundEvents.add(1012);
		listDoorSoundEvents.add(1005);
		listDoorSoundEvents.add(1006);
		//fence gates
		listDoorSoundEvents.add(1008);
		listDoorSoundEvents.add(1014);
		listSoundProfiles.add(new SoundProfileEntry("debug-doors", noisyInteractBuff).setListSoundEventTypes(listDoorSoundEvents));

		//records
		List<Integer> list = new ArrayList<>();
		list.add(1010);
		listSoundProfiles.add(new SoundProfileEntry("debug-records", 300).setListSoundEventTypes(list));

		listSoundProfiles.add(new SoundProfileEntry(".place", noisyInteractBuff));
		//listSoundProfiles.add(new SoundProfileEntry("generic.eat", 0.3D));
		listSoundProfiles.add(new SoundProfileEntry("player.burp", 1.1D));

		//covers all note block sounds
		listSoundProfiles.add(new SoundProfileEntry("block.note", noisyInteractBuff).setDistanceMax(64));

		listSoundProfiles.add(new SoundProfileEntry("lever.click", noisyInteractBuff).setDistanceMax(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry("pressure_plate", noisyInteractBuff).setDistanceMax(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry("button.click", noisyInteractBuff).setDistanceMax(noisyInteractRange));

		listSoundProfiles.add(new SoundProfileEntry("tripwire", noisyInteractBuff).setDistanceMax(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry("block.barrel", noisyInteractBuff).setDistanceMax(noisyInteractRange));

		//long dist ones
		if (ZAConfigFeatures.noisyZombies) listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ZOMBIE_AMBIENT, 0.8D, 8*20).setDistanceMax(48));
		if (ZAConfigFeatures.noisyPistons) listSoundProfiles.add(new SoundProfileEntry(SoundEvents.PISTON_EXTEND, 2D, 20).setDistanceMax(128));

		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.GENERIC_EXPLODE, 3D).setDistanceMax(128));
	}

	public static SoundProfileEntry getFirstEntry(int soundType) {
		for (SoundProfileEntry entry : listSoundProfiles) {
			if (entry.isSoundType()) {
				if (entry.containsSoundType(soundType)) {
					return entry;
				}
			}
		}
		return null;
	}
    
    public static SoundProfileEntry getFirstEntry(String sound) {
    	for (SoundProfileEntry entry : listSoundProfiles) {

    		if (entry.getSoundName().equals(sound)) {
    			return entry;
    		} else if (entry.isPartialMatchOnly() && sound.contains(entry.getSoundName())) {
    			return entry;
    		}
    	}
    	return null;
    }
	
	public static void tickPlayer(PlayerEntity player) {

		/*if (!ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(player))) {
			if (ZAConfigFeatures.extraSpawningSurface) {
				if (!player.level.isDay()) {
					if (getWorldData(player.level.dimension()).lastMobsCountSurface < ZAConfigSpawning.extraSpawningSurfaceMaxCount) {
						if (ZAConfigSpawning.extraSpawningSurfaceRandomPool <= 0 || rand.nextInt(ZAConfigSpawning.extraSpawningSurfaceRandomPool) == 0) {
							spawnNewMobSurface(player);
						}
					}
				}
			}

			if (ZAConfigFeatures.extraSpawningCave) {
				if (getWorldData(player.level.dimension()).lastMobsCountCaves < ZAConfigSpawning.extraSpawningCavesMaxCount) {
					if (ZAConfigSpawning.extraSpawningCavesRandomPool <= 0 || rand.nextInt(ZAConfigSpawning.extraSpawningCavesRandomPool) == 0) {
						spawnNewMobCave(player);
					}
				}
			}
		}*/
    	
		if (ZAConfigFeatures.wanderingHordes) {
			if (rand.nextInt(25) == 0) {
				spawnWaypoint(player);
			}
		}

        if (ZAConfigFeatures.awareness_Scent && !player.isCreative()) {
        	int lastHealth = lastHealths.containsKey(CoroUtilEntity.getName(player)) ? lastHealths.get(CoroUtilEntity.getName(player)) : 0;
    		Long lastBleedTime = lastBleedTimes.containsKey(CoroUtilEntity.getName(player)) ? lastBleedTimes.get(CoroUtilEntity.getName(player)) : 0L;
    		
    		Vector3d pos = new Vector3d(player.getX(), player.getY(), player.getZ());
    		
            if((int)player.getHealth() != lastHealth) {
                if(player.getHealth() < lastHealth) {
                	EntityScent scent = spawnOrBuffSenseAtPos(player.level, pos, EnumSenseType.SCENT_BLOOD, ZAConfig.scentStrength);
                	ZombieAwareness.dbg("spawned or buffed scent sense from damage: " + scent.getStrengthPeak());
                }

                lastHealth = (int) player.getHealth();
            }
            
            lastHealths.put(CoroUtilEntity.getName(player), lastHealth);

            if(player.getHealth() / player.getMaxHealth() < 0.6F && lastBleedTime < System.currentTimeMillis()) {
                lastBleedTime = System.currentTimeMillis() + 30000L;
                lastBleedTimes.put(CoroUtilEntity.getName(player), lastBleedTime);
                EntityScent scent = spawnOrBuffSenseAtPos(player.level, pos, EnumSenseType.SCENT_BLOOD, ZAConfig.scentStrength);
                ZombieAwareness.dbg("spawned or buffed scent sense from bleeding: " + scent.getStrengthPeak());
            }
        }
    }
	
	public static void giveRandomSpeedBoost(MobEntity ent) {
		
		if (ZAConfig.zombieRandSpeedBoost > 0) {
			double randBoost = ent.level.random.nextDouble() * ZAConfig.zombieRandSpeedBoost;
			AttributeModifier speedBoostModifier = new AttributeModifier(CoroUtilAttributes.SPEED_BOOST_UUID, "ZA speed boost", randBoost, AttributeModifier.Operation.MULTIPLY_BASE);
            if (!ent.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(speedBoostModifier)) {
                ent.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(speedBoostModifier);
            }
		}
		
	}
    
    public static void huntTarget(MobEntity ent, LivingEntity targ, int pri) {
    	CoroUtilPath.tryMoveToEntityLivingLongDist(ent, targ, 1);
		if (ent instanceof MobEntity) (ent).setTarget(targ);
	}
	
	public static void huntTarget(MobEntity ent, LivingEntity targ) {
		huntTarget(ent, targ, 0);
	}
    
	public static boolean isEnemy(Entity ent, Entity targ) {
		return isEnemy(ent, targ, false);
	}
	
    public static boolean isEnemy(Entity ent, Entity targ, boolean omniTarget) {
    	if (targ instanceof LivingEntity) {
			if (targ instanceof PlayerEntity) {
				if (!((PlayerEntity) targ).isCreative() && ((PlayerEntity) targ).getEffect(Effects.INVISIBILITY) == null) {
					if (!omniTarget) {
						return true;
					} else if (ZAConfigPlayerLists.whiteListUsedOmniscient) {
						if (ZAConfigPlayerLists.whitelistOmniscientTargettedPlayers.contains(CoroUtilEntity.getName(((PlayerEntity) targ)))) {
							if (ZAConfig.debugConsoleOmniscient) ZombieAwareness.dbg(CoroUtilEntity.getName((PlayerEntity) targ) + " targetting omnisciently by " + ent);
							return true;
						}
					} else {
						return true;
					}
				}
			}
			return false;
    	}
    	return false;
	}
    
    public static boolean sanityCheck(Entity ent, Entity entity1) {
		return true;
	}
    
    public static void tickAI(MobEntity ent) {

		if (profileActive) {
			int val = 0;
			if (lookupProcessedEntityCounts.containsKey(ent.getClass())) {
				val = lookupProcessedEntityCounts.get(ent.getClass());
			}
			lookupProcessedEntityCounts.put(ent.getClass(), val + 1);
		}
		//ZAConfig.debugConsoleSuperDetailed = false;
    	if (ZAConfig.debugConsoleSuperDetailed) ZombieAwareness.dbg("ZA DBG: Ticking: " + ent);
    	
    	//A more performance friendly omniscient, only runs it when no target, but still allows for smaller ranged retargetting
		//adding entity ID onto world time to stagger processing per entity more, should improve TPS
    	if ((ent.level.getGameTime() + ent.getId()) % 40 == 0) {
	    	if (ZAConfig.omniscient && ent.getTarget() == null) {
	    		ai_FindTarget(ent, true);
	    	} else {
	    		ai_FindTarget(ent, false);
	    	}
    	}
		
		EntityScent senseTracked = null;
		
		if (ent.getTarget() == null && (ent.getNavigation().isDone())) {
			//Find player made senses
			if (!ZAConfig.awareness_Light_OnlyZombies || (ent instanceof ZombieEntity)) {
				if (!ZAConfigFeatures.awareness_Light || !ai_FindLightSource(ent)) {
					if (ent.level.random.nextInt(3) == 0) {
						senseTracked = ai_FindSense(ent, true);
					}
	    			
    			}
    		} else {
    			senseTracked = ai_FindSense(ent);
    		}
    	}
		
		if (senseTracked != null && ent.getNavigation().getPath() != null) {
			PathPoint pathTo = ent.getNavigation().getPath().getEndNode();
			if (pathTo != null) {
				PlayerEntity player = getClosestPlayer(ent.level, pathTo.x, pathTo.y, pathTo.z, 6D);
				if (player != null) {
					//tryPlayInvestigateSound(ent, new Vec3d(ent.getX(), ent.getY(), ent.getZ()));
					tryPlayInvestigateSound(ent, new Vector3d(pathTo.x, pathTo.y, pathTo.z));
				}
				
			}
			
		}
	    	
	    tickCustomMob(ent);
    }
    
    public static void tickCustomMob(MobEntity ent) {
    	if (ZAConfigFeatures.wanderingHordes) {
	    	if (ent instanceof SpiderEntity) {
	    		if (ent.getPassengers().size() > 0 && ent.getPassengers().get(0) instanceof SkeletonEntity) {
	    			if (ent.level.random.nextInt(100) == 0) {
	    				spawnWaypoint(ent);
	    			}
	    		}
	    	}
    	}
    }
    
    public static boolean ai_FindLightSource(MobEntity ent) {
    	
    	if (ent.level.isDay()) return false;
    	
    	if (ent.level.random.nextInt(3) == 0) {

			float lightValueAtEntity = ent.level.getBrightness(ent.blockPosition());

    		Random rand = new Random();
    		
    		int size;
    		
    		for (int i = 0; i < 4; i++) {
    			PlayerEntity entP = getClosestPlayerToEntity(ent.level, ent, 999);
        		if (entP != null) {

					//adjusted from 32 for 1.16 to get better results, vanilla might have changed something
        			size = 32 * (i+1);
        			//size = 2;

		    		int rX = MathHelper.floor(entP.getX() + (rand.nextInt(size) - (size/2)));
		    		int rY = MathHelper.floor(entP.getY() + (rand.nextInt(size/2) - (size/4)));
		    		int rZ = MathHelper.floor(entP.getZ() + (rand.nextInt(size) - (size/2)));

		    		BlockPos pos = new BlockPos(rX, rY, rZ);

					if (!ent.level.isLoaded(pos)) continue;

		    		float lightValue = entP.level.getBrightness(pos);

					//as of 1.16 its float values for brightness, 0.2 seems like a good minimum brightness
					//moon phases doesnt seem to affect this value

		    		//if bright enough and also as bright or brighter than where they are currently
		    		if (lightValue > 0.2F && lightValue >= lightValueAtEntity) {
						//adjusted to 32 from 64
		    			if (((ent.level.random.nextInt(5) == 0 && ent.distanceTo(entP) > 64))) {
							boolean canSeePos = CoroUtilEntity.canSee(ent, new BlockPos(rX, rY, rZ));
							if (canSeePos) {
								//CULog.dbg("path to light source");
								if (CoroUtilPath.tryMoveToXYZLongDist(ent, rX, rY, rZ, 1)) {
									ZombieAwareness.dbg("pathing to lightsource at " + rX + ", " + rY + ", " + rZ + " - " + ent);
								}
								return true;
							}
	    				}
		    		}
        		}
    		}
    	}
    	
    	return false;
    }
    
    public static EntityScent ai_FindSense(MobEntity ent) {
    	return ai_FindSense(ent, true);
    }
    
    public static EntityScent ai_FindSense(MobEntity ent, boolean includeWaypoints) {
    	
    	EntityScent var3 = getSenseNearEntity(ent);

        if(var3 != null) {
        	if (includeWaypoints || var3.type != 2) {
        		if (CoroUtilPath.tryMoveToEntityLivingLongDist(ent, var3, 1)) {
        			//ZombieAwareness.dbg("ai_FindSense call, type: " + ((EntityScent)var3).type + " - " + ent.getName() + " -> " + var3.position());
        			return var3;
        		}
        	}
        }
        
        return null;
    }
    
    public static boolean ai_FindTarget(MobEntity ent, boolean omniscient) {
    	long huntRange = ZAConfig.sightRange;
    	
    	if (omniscient) huntRange = 512;
    	
    	if ((ent.getTarget() == null || ent.level.random.nextInt(100) == 0)) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.level.getEntities(ent, ent.getBoundingBox().inflate(huntRange, huntRange/2, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);

	            //for new calmed zombies, keep them from targetting/pathing towards player
				//just blacklisting all of them from this feature, not checking if calm currently
	            if (ent.getClass().getSimpleName().equals("EntityZombiePlayer")) {
	            	continue;
				}

	            if(isEnemy(ent, entity1, omniscient))
	            {
	            	if (omniscient || (ZAConfig.seeThroughWalls || ((LivingEntity) entity1).canSee(ent))) {
	            		if (sanityCheck(ent, entity1)) {
	            			float dist = (float) ent.distanceToSqr(entity1);
	            			if (dist < closest) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
	            		}
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	huntTarget(ent, (LivingEntity)clEnt);
				ZombieAwareness.dbg(" hunting target " + ent + " " + clEnt);
	        	return true;
	        }
		}
    	return false;
    }
    
    /**
     * Gets sense within range provided sense is strong enough, has random chance and doesnt always return closest sense
     * 
     * @param entSource
     * @return
     */
    public static EntityScent getSenseNearEntity(Entity entSource) {
        List<Entity> listEnts = entSource.level.getEntities(entSource, entSource.getBoundingBox().inflate((double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense));
        
        EntityScent entBest = null;

        for(int i = 0; i < listEnts.size(); ++i) {
        	Entity entCheck = listEnts.get(i);

            if (entCheck instanceof EntityScent) {
            	
            	double dist = entSource.distanceTo(entCheck);

				//System.out.println("dist: " + dist);

				if (dist < ((EntityScent)entCheck).getRange() && dist > 5.0F && entSource.level.random.nextInt(20) == 0) {
					entBest = (EntityScent) entCheck;
					return entBest;
				}
            }
        }

        return entBest;
    }

	public static void hookPlayEvent(int type, World world,
									 double pX, double pY, double pZ, int data) {

		addSoundHooks();

		//if event type is for playing a record
		if (world.isClientSide()) return;
		if (!canSpawnTrace(world, pX, pY, pZ)) {
			return;
		}
		SoundProfileEntry entry = getFirstEntry(type);
		if (entry != null) {
			PlayerEntity closestPlayer = getClosestPlayer(world, pX, pY, pZ, 128);
			if (closestPlayer != null) {
				Vector3d pos = new Vector3d(pX, pY, pZ);
				handleSoundProfileEvent(world, entry, pos, closestPlayer);
			}
		}
	}

    public static void hookSoundEvent(SoundEvent sound, World world, double x, double y, double z, float volume, float pitch) {

		addSoundHooks();
    	
    	if (world.isClientSide() || sound == null) return;

		if (!canSpawnTrace(world, x, y, z)) {
			y += 1;
			if (!canSpawnTrace(world, x, y, z)) {
				return;
			}
		}

		String soundName = SoundProfileEntry.getSoundEventName(sound);
		SoundProfileEntry entry = getFirstEntry(soundName);

        //double strength = ZAConfig.soundStrength;
        
    	if (entry != null) {
    		//double distToPlayer = Math.sqrt(closestPlayer.distanceToSqr(x, y, z));

			/*if (distToPlayer <= 5) {
				System.out.println(soundName);
			}*/

			PlayerEntity closestPlayer = getClosestPlayer(world, x, y, z, 128);
        	if (closestPlayer != null) {
				Vector3d pos = new Vector3d(x, y, z);
				handleSoundProfileEvent(world, entry, pos, closestPlayer);
        		/*if (distToPlayer <= entry.getDistanceMax()) {
            		if (entry.getOddsTo1ToUse() <= 0 || rand.nextInt(entry.getOddsTo1ToUse()) == 0) {
            			strength *= entry.getMultiplier();
            			
            			EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);
                		
                		ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent, sound: " + soundName + ", str: " + scent.getStrengthPeak() + ", vol: " + volume);
            		}
        		}*/
        	}
    	}
    }

	public static void handleSoundProfileEvent(World world, SoundProfileEntry entry, Vector3d pos, PlayerEntity closestPlayer) {
		double distToPlayer = Math.sqrt(closestPlayer.distanceToSqr(pos.x, pos.y, pos.z));
		double strength = ZAConfig.soundStrength;
		if (distToPlayer <= entry.getDistanceMax()) {
			if (entry.getOddsTo1ToUse() <= 0 || rand.nextInt(entry.getOddsTo1ToUse()) == 0) {
				strength *= entry.getMultiplier();

				EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);

				ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent, sound: " + entry.getSoundName() + ", str: " + scent.getStrengthPeak()/* + ", vol: " + volume*/);
			}
		}
	}
    
    public static void hookBlockEvent(PlayerEvent event, int chance) {
    	
    	if (!ZAConfigFeatures.awareness_Sound) return;

		if (ZAConfigFeatures.awareness_Sound_OverworldOnly) {
			if (event.getEntity().level.dimension() != World.OVERWORLD/* && world.provider.getDimension() != -127*/) return;
		}
    	
    	if (event.getPlayer() == null || (ZAConfigPlayerLists.whiteListUsedSenses && !ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(event.getPlayer())))) return;
    	
		if (!event.getEntity().level.isClientSide() && event.getEntity().level.random.nextInt(chance) == 0) {

			int strength = ZAConfig.soundStrength;
			Vector3d pos = new Vector3d(event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ());

			EntityScent scent = spawnOrBuffSenseAtPos(event.getEntity().level, pos, EnumSenseType.SOUND, strength);

			ZombieAwareness.dbg("spawned or buffed sound sense from PlayerEvent: " + scent.getStrengthPeak());
		}
    }

	/**
	 * Player can be null
	 *
	 * @param player
	 * @param chance
	 */
	public static void handleBlockBasedEvent(PlayerEntity player, World world, BlockPos pos, int chance) {
		if (player == null && ZAConfig.blockBreakEvent_PlayersOnly) {
			return;
		}

		if (!ZAConfigFeatures.awareness_Sound) return;

		if (ZAConfigFeatures.awareness_Sound_OverworldOnly) {
			if (world.dimension() != World.OVERWORLD/* && world.provider.getDimension() != -127*/) return;
		}

		if (player != null && ZAConfigPlayerLists.whiteListUsedSenses) {
			if (ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(player))) return;
		}

		if (!world.isClientSide() && world.random.nextInt(chance) == 0) {

			int strength = ZAConfig.soundStrength;
			Vector3d posVec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());

			EntityScent scent = spawnOrBuffSenseAtPos(world, posVec, EnumSenseType.SOUND, strength);

			ZombieAwareness.dbg("spawned or buffed sound sense from BlockBasedEvent: " + scent.getStrengthPeak());
		}
	}
    
    public static void hookSetAttackTarget(LivingSetAttackTargetEvent event) {
    	
    	//ZombieAwareness.dbg(event.getEntityLiving().getEntityId() + " targetting " + event.getTarget());
    	
    	if (event.getEntityLiving() instanceof MobEntity) {
    		if (event.getTarget() instanceof PlayerEntity) {
	    		//tryPlayAlertSound((EntityLiving)event.getEntityLiving(), new Vec3d(event.getTarget().getX(), event.getTarget().getY(), event.getTarget().getZ()));
	    		tryPlayTargetSound((MobEntity)event.getEntityLiving(), (LivingEntity)event.getTarget(), new Vector3d(event.getEntityLiving().getX(), event.getEntityLiving().getY(), event.getEntityLiving().getZ()));
    		} else if (event.getTarget() == null) {
    			//dont use, AI stupidly detargets when resetting tasks despite still chasing player, causing double alert noise if this code is used
    			/*if (lookupLastAlertTime.containsKey(event.getEntityLiving())) {
    				lookupLastAlertTime.remove(event.getEntityLiving());
        			System.out.println("detarget");
    			}*/
    		}
    	}
    	
    }

	/*public static void hookPlayEvent(World world, int type,
			BlockPos blockPosIn, int data) {
		//if event type is for playing a record
		if (world.isClientSide()) return;
		if (type == 1010) {
			//if putting in a record and not taking out
			if (Item.getItemById(data) instanceof MusicDiscItem) {
				Vector3d pos = new Vector3d(blockPosIn.getX(), blockPosIn.getY(), blockPosIn.getZ());
				EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, 300);
				ZombieAwareness.dbg("spawned or buffed sound sense from playEvent: " + scent.getStrengthPeak());
			}
		}
	}*/
    
    /*public static void spawnNewMobSurface(PlayerEntity player) {
        
        
        int minDist = ZAConfigSpawning.extraSpawningDistMin;
        int maxDist = ZAConfigSpawning.extraSpawningDistMax;
        int range = maxDist * 2;
        
        for (int tries = 0; tries < 5; tries++) {
	        int tryX = (int)Math.floor(player.getX() - (range/2) + (rand.nextInt(range)));
	        int tryZ = (int)Math.floor(player.getZ() - (range/2) + (rand.nextInt(range)));
			int tryY = player.level.getHeight(Heightmap.Type.MOTION_BLOCKING, (int)Math.floor(tryX), (int)Math.floor(tryZ));
	
	        if (Math.sqrt(player.distanceToSqr(tryX, tryY, tryZ)) < minDist ||
					Math.sqrt(player.distanceToSqr(tryX, tryY, tryZ)) > maxDist ||
					!canSpawnMobOnGround(player.level, tryX, tryY - 1, tryZ) ||
					player.level.getLightEmission(new BlockPos(tryX, tryY, tryZ)) >= 6) {
	            continue;
	        }

			int randSize = player.level.random.nextInt(ZAConfigSpawning.extraSpawningSurfaceMaxGroupSize) + 1;
			ServerWorld world = (ServerWorld) player.level;
	        for (int i = 0; i < randSize; i++) {
	        	//spawnMobsAllowed(player, world, tryX, tryY, tryZ);
	        }
			
			//if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(player);
			
	        if (ZAConfig.debugConsoleSpawns) ZombieAwareness.dbg("spawnNewMobSurface: " + tryX + ", " + tryY + ", " + tryZ);
	        
	        return;
        }
    }*/
    
    /*public static void spawnNewMobCave(PlayerEntity player) {
        
        int minDist = ZAConfigSpawning.extraSpawningCavesDistMin;
        int maxDist = ZAConfigSpawning.extraSpawningCavesDistMax;
        int range = maxDist * 2;
        
        for (int tries = 0; tries < ZAConfigSpawning.extraSpawningCavesTryCount; tries++) {
	        int tryX = (int)Math.floor(player.getX() - (range/2) + (rand.nextInt(range)));
	        int tryY = (int)Math.floor(player.getY() - (range/2) + (rand.nextInt(range)));
	        int tryZ = (int)Math.floor(player.getZ() - (range/2) + (rand.nextInt(range)));

			if (Math.sqrt(player.distanceToSqr(tryX, tryY, tryZ)) < minDist ||
					Math.sqrt(player.distanceToSqr(tryX, tryY, tryZ)) > maxDist ||
					!canSpawnMobOnGround(player.level, tryX, tryY - 1, tryZ) ||
					!isInDarkCave(player.level, tryX, tryY, tryZ, true)) {
	            continue;
	        }
	
	        int randSize = player.level.random.nextInt(ZAConfigSpawning.extraSpawningCavesMaxGroupSize) + 1;
	        ServerWorld world = (ServerWorld) player.level;
	        for (int i = 0; i < randSize; i++) {
	        	//spawnMobsAllowed(player, world, tryX, tryY, tryZ);
	        }
			
			
			
	        return;
        }
    }*/
    
    /*public static void spawnMobsAllowed(PlayerEntity player, ServerWorld world, int tryX, int tryY, int tryZ) {
    	if (!ZAConfigSpawning.extraSpawningUseNaturalSpawnList) {
	        ZombieEntity entZ = new ZombieEntity(world);
			entZ.setPosition(tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F);
			entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (ILivingEntityData)null);
			giveRandomSpeedBoost(entZ);
			world.spawnEntity(entZ);
			
			if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(player);
			
			if (ZAConfig.debugConsoleSpawns) {
	        	ZombieAwareness.dbg("spawnNewMob: " + tryX + ", " + tryY + ", " + tryZ);
	        }
    	} else {
    		SpawnListEntry spawnlistentry = world.getSpawnListEntryForTypeAt(EntityClassification.MONSTER, new BlockPos(tryX, tryY, tryZ));
    		
    		MobEntity entityliving;

            try
            {
                entityliving = (MobEntity)spawnlistentry.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {world});
                entityliving.setLocationAndAngles(tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F, world.random.nextFloat() * 360.0F, 0.0F);

                Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, world, tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F);
                if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && entityliving.getCanSpawnHere()))
                {
                    world.spawnEntity(entityliving);
                    if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F))
                    {
                        entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityliving)), (ILivingEntityData) null);
                    }
                    giveRandomSpeedBoost(entityliving);
                    if (ZAConfig.debugConsoleSpawns) {
    		        	ZombieAwareness.dbg("spawnNewMob: " + tryX + ", " + tryY + ", " + tryZ + ", name: " + entityliving.toString());
    		        }
                    
                    if (ZAConfigSpawning.extraSpawningAutoTarget) entityliving.setAttackTarget(player);
                    
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
    	}
    }*/
    
    /**
     * Method is far from perfect, but should work well enough without intensive processing to verify
     * coords fed in should be solid block with air above it (2 blocks of vertical space, 1 width of size)
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    /*public static boolean isInDarkCave(World world, int x, int y, int z, boolean checkSpaceToSpawn) {
		BlockPos pos = new BlockPos(x, y, z);
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (!world.canSeeSky(pos) && world.getLightEmission(pos) < 5) {
    		if (!CoroUtilBlock.isAir(block) && state.getMaterial() == Material.STONE*//*(block != Blocks.grass || block.getMaterial() != Material.grass)*//*) {
    		
    			if (!checkSpaceToSpawn) {
    				return true;
    			} else {
    				Block blockAir1 = world.getBlockState(new BlockPos(x, y+1, z)).getBlock();
    				if (CoroUtilBlock.isAir(blockAir1)) {
    					Block blockAir2 = world.getBlockState(new BlockPos(x, y+2, z)).getBlock();
    					if (CoroUtilBlock.isAir(blockAir2)) {
    						return true;
    					}
    				}
    				
    			}
    		}
    	}
    	return false;
    }*/

	/**
	 * x y z coords are expected to be the ground the mob is going to spawn on
	 *
	 * @return
	 */
    /*public static boolean canSpawnMobOnGround(World world, int x, int y, int z) {
    	BlockPos pos = new BlockPos(x, y, z);
    	BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();//Block.pressurePlatePlanks.blockID;

        *//*if (id == Block.grass.blockID || id == Block.stone.blockID || id == Block.tallGrass.blockID || id == Block.grass.blockID || id == Block.sand.blockID) {
            return true;
        }*//*
        if (CoroUtilBlock.isAir(block) || !block.canCreatureSpawn(state, world, pos, MobEntity.SpawnPlacementType.ON_GROUND)) {
        	return false;
        }
        return true;
    }*/
    
    public static void spawnWaypoint(Entity entSource) {
        
        int range = 256;
        
        double tryX = (int)entSource.getX() - (range/2) + (rand.nextInt(range));
        double tryZ = (int)entSource.getZ() - (range/2) + (rand.nextInt(range));
        double tryY = entSource.level.getHeight(Heightmap.Type.MOTION_BLOCKING, (int)Math.floor(tryX), (int)Math.floor(tryZ));

        if (!canSpawnTrace(entSource.level, tryX, tryY, tryZ)) {
            return;
        }

        double height = entSource.getY();
        
        EntityScent var1 = getSenseNodeAtPos(entSource.level, new Vector3d(tryX, tryY, tryZ), EnumSenseType.WAYPOINT);
        
        boolean newNode = false;
    	
    	if (var1 == null) {
    		var1 = new EntityScent(EntityRegistry.SCENT, entSource.level);
    		newNode = true;
    	}

        var1.setStrengthPeak(60);
        
        if (newNode) {
	        var1.setPos(tryX, tryY, tryZ);
	        var1.type = 2;
	        
	        entSource.level.addFreshEntity(var1);
        }
        
        if (debug) System.out.println("WP: " + entSource + " - range: " + var1.getRange());
    }

    public static boolean canSpawnTrace(World world, double x, double y, double z) {
		if (!ZAConfigFeatures.awareness_Sound) {
			return false;
		}
		if (ZAConfigFeatures.awareness_Sound_OverworldOnly) {
			if (world.dimension() != World.OVERWORLD/* && world.provider.getDimension() != -127*/) return false;
		}
    	BlockPos pos = new BlockPos(x,y,z);
    	if (!world.isLoaded(pos)) return false;
    	BlockState state = world.getBlockState(pos);
    	//iirc circuits check was to prevent senses spawning on pressure plates and triggering them, but there should be better ways to stop that...
		//might be redundant since AABB and canTriggerWalking and canBeCollidedWith fix
        if (state.getMaterial() == Material.DECORATION && (!(state.getBlock() instanceof AbstractButtonBlock) && !(state.getBlock() instanceof LeverBlock))) {
            return false;
        }
        return true;
    }
    
    public static PlayerEntity getClosestPlayerToEntity(World world, Entity par1Entity, double par2)
    {
        return getClosestPlayer(world, par1Entity.getX(), par1Entity.getY(), par1Entity.getZ(), par2);
    }

    /**
     * Gets the closest player to the point within the specified distance (distance can be set to less than 0 to not
     * limit the distance). Args: x, y, z, dist
     */
    public static PlayerEntity getClosestPlayer(World world, double par1, double par3, double par5, double par7)
    {
        double d4 = -1.0D;
        PlayerEntity entityplayer = null;

        for (int i = 0; i < world.players().size(); ++i)
        {
            PlayerEntity entityplayer1 = (PlayerEntity)world.players().get(i);
            if (!ZAConfigPlayerLists.whiteListUsedSenses || ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(entityplayer1))) {
            	double d5 = entityplayer1.distanceToSqr(par1, par3, par5);

                if ((par7 < 0.0D || d5 < par7 * par7) && (d4 == -1.0D || d5 < d4))
                {
                    d4 = d5;
                    entityplayer = entityplayer1;
                }
            }
        }

        return entityplayer;
    }

    /**
     * Checks if a scent of the same type is already at this location
     * 
     * @param parWorld
     * @param parPos
     * @param type
     * @return
     */
    public static EntityScent getSenseNodeAtPos(World parWorld, Vector3d parPos, EnumSenseType type) {
    	
    	if (ZAConfig.extraScentCutoffRange == -1) return null;
    	
    	AxisAlignedBB aabb = new AxisAlignedBB(parPos.x, parPos.y, parPos.z, parPos.x + 1, parPos.y + 1, parPos.z + 1);
    	aabb = aabb.inflate(ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange);
    	
    	List list = parWorld.getEntitiesOfClass(EntityScent.class, aabb);
    	
    	if (list.size() > 0) {
    		for(int j = 0; j < list.size(); j++)
            {
    			EntityScent node = (EntityScent)list.get(j);
    			if (node.type == type.ordinal()) {
    				return node;
    			}
            }
    	}
    	
    	return null;
    }
    
    public static EntityScent spawnOrBuffSenseAtPos(World world, Vector3d parPos, EnumSenseType type, int strength) {
    	return spawnOrBuffSenseAtPos(world, parPos, type, strength, true);
    }
    
    /**
     * Tries to spawn a new sense, if one is close enough, it will multiply that senses current strength by lastMultiply
     * 
     * @param world
     * @param parPos
     * @param type
     * @param strength
     * @return
     */
    public static EntityScent spawnOrBuffSenseAtPos(World world, Vector3d parPos, EnumSenseType type, int strength, boolean frequentSoundMultiply) {
		
    	EntityScent sense = getSenseNodeAtPos(world, parPos, type);
    	
    	if (sense == null) {
    		sense = new EntityScent(EntityRegistry.SCENT, world);
    		sense.type = type.ordinal();
	        sense.setPos(parPos.x, parPos.y, parPos.z);
    		sense.setStrengthPeak(strength);
	        world.addFreshEntity(sense);
    	} else if (frequentSoundMultiply) {
    		//instead of amplifying current strength, amp the base value, but only if current strength is weaker than param
    		float str = sense.getStrengthPeak();
    		if (str < strength) {
    			str = strength;
    		}
    		
	        if(sense.lastBuffTime + (long)ZAConfig.frequentSoundThreshold > System.currentTimeMillis()) {
	        	sense.lastMultiply += 0.1F;
	        	str *= sense.lastMultiply;
	        } else {
	        	sense.lastMultiply = 1.0F;
	        }
	        
	        sense.lastBuffTime = System.currentTimeMillis();
	        sense.setStrengthPeak((int)str);
    	}
    	
        return sense;
    }
    
    public static void tryPlayTargetSound(MobEntity entAlerted, LivingEntity entTargetted, Vector3d pos) {

		if (!ZAConfigFeatures.soundAlerts) return;

		if (!ZombieAwareness.canProcessEntity(entAlerted)) return;

		//prevent target spam sound from omniscient zombies from HW-inv
		/*if (entAlerted.getEntityData().hasKey(UtilEntityBuffs.dataEntityBuffed_AI_Omniscience)) {
			return;
		}*/

		//added max dist and blocks loaded check due to https://github.com/Corosauce/ZombieAwareness/issues/11
		double distMaxCancel = 75;
    	if (!lookupLastAlertTime.containsKey(entAlerted) || lookupLastAlertTime.get(entAlerted) + alertDelay < entAlerted.level.getGameTime()) {
			if (entAlerted.distanceTo(entTargetted) < distMaxCancel
                    && entAlerted.level.dimension() == entTargetted.level.dimension()
					&& entAlerted.level.isLoaded(entAlerted.blockPosition())
					&& entTargetted.level.isLoaded(entTargetted.blockPosition())) {
				if (entAlerted.canSee(entTargetted)) {
					//entAlerted.level.playSound(null, pos.x, pos.y, pos.z, SoundRegistry.get("target"), SoundCategory.HOSTILE, 3F, 0.8F + (entAlerted.level.random.nextFloat() * 0.2F));
					entAlerted.level.playSound(null, entTargetted.getX(), entTargetted.getY(), entTargetted.getZ(), ZAConfigFeatures.soundUseAlternateAlertNoise ? SoundRegistry.get("alert") : SoundRegistry.get("target"), SoundCategory.HOSTILE, (float) ZAConfigFeatures.soundVolumeAlertTarget, ZAConfigFeatures.soundUseAlternateAlertNoise ? 1F : 0.8F + (entAlerted.level.random.nextFloat() * 0.2F));
					lookupLastAlertTime.put(entAlerted, entAlerted.level.getGameTime());
					//ZombieAwareness.dbg("!!! alert play for ent: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastAlertTime.size());
				} else {
					//likely due to new call for help routine in vanilla, so treat it like investigating until line of sight is made
					tryPlayInvestigateSound(entAlerted, pos);
					//ZombieAwareness.dbg("??? tried play alert for no LOS entity: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastAlertTime.size());
				}
			}
		} else {
			//ZombieAwareness.dbg("already played alert for ent: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastAlertTime.size());
		}
    }
    
    public static void tryPlayInvestigateSound(MobEntity entAlerted, Vector3d pos) {

		if (!ZAConfigFeatures.soundInvestigates) return;

		if (!ZombieAwareness.canProcessEntity(entAlerted)) return;

    	if (!lookupLastInvestigateTime.containsKey(entAlerted) || lookupLastInvestigateTime.get(entAlerted) + investigateDelay < entAlerted.level.getGameTime()) {
			entAlerted.level.playSound(null, pos.x, pos.y, pos.z, SoundRegistry.get("investigate"), SoundCategory.HOSTILE, (float)ZAConfigFeatures.soundVolumeInvestigate, 0.7F + (entAlerted.level.random.nextFloat() * 0.3F));
			lookupLastInvestigateTime.put(entAlerted, entAlerted.level.getGameTime());
			ZombieAwareness.dbg("!!! investigate play for ent: " + entAlerted.getId() + ", lookupSize: " + lookupLastInvestigateTime.size());
		}
    }

	public static boolean isZombieAwarenessActive(World world) {
		if (world == null) return false;
		if (ZAConfig.daysBeforeFeaturesActivate <= 0) return true;
		if (((double)world.getGameTime() / CoroUtilWorldTime.getDayLength()) >= ZAConfig.daysBeforeFeaturesActivate) {
			return true;
		} else {
			return false;
		}
	}

	public static WorldData getWorldData(RegistryKey<World> dimID) {
    	if (!lookupWorldData.containsKey(dimID)) {
    		lookupWorldData.put(dimID, new WorldData());
		}
		return lookupWorldData.get(dimID);
	}
}
