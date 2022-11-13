package com.corosus.zombieawareness;

import com.corosus.coroutil.util.CoroUtilAttributes;
import com.corosus.coroutil.util.CoroUtilEntity;
import com.corosus.coroutil.util.CoroUtilPath;
import com.corosus.coroutil.util.CoroUtilWorldTime;
import com.corosus.zombieawareness.client.SoundProfileEntry;
import com.corosus.zombieawareness.client.SoundRegistry;
import com.corosus.zombieawareness.config.ZAConfig;
import com.corosus.zombieawareness.config.ZAConfigFeatures;
import com.corosus.zombieawareness.config.ZAConfigPlayerLists;
import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.*;

public class ZAUtil {
    
    public static Random rand = new Random();
    
    public static HashMap<String, Integer> lastHealths = new HashMap();
    public static HashMap<String, Long> lastBleedTimes = new HashMap();

    public static List<SoundProfileEntry> listSoundProfiles = new ArrayList<>();

	//runtime cache lookups, populates as sounds are made, useful for partial matches that require iterating entire sound profile list each time
	public static HashMap<String, SoundProfileEntry> lookupSoundNameToProfileCache = new HashMap<>();
	public static HashMap<Integer, SoundProfileEntry> lookupSoundIntToProfile = new HashMap<>();

    public static WeakHashMap<Entity, Long> lookupLastAlertTime = new WeakHashMap<>();
    public static long alertDelay = 60*20;
    
    public static WeakHashMap<Entity, Long> lookupLastInvestigateTime = new WeakHashMap<>();
    public static long investigateDelay = 60*20;

	public static HashMap<String, Long> lookupLastWaypointTime = new HashMap<>();

	public static HashMap<EntityType, Boolean> lookupTickableEntitiesCache = new HashMap<>();

	public static String SPEED_BOOST_TAG = "za_speedbosted";
	public static String ZA_LAST_ACTION = "za_last_action";

    public static boolean debug = false;
    
    static {
		addSoundHooks();
    }

	public static void addSoundHooks() {
		int noisyInteractRange = 30;
		double noisyInteractBuff = 1.3D;

		listSoundProfiles.clear();
		lookupSoundIntToProfile.clear();
		lookupSoundNameToProfileCache.clear();

		//short dist ones
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ARROW_HIT_PLAYER, 1.1D));
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ARROW_HIT, 1.1D));
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.CROSSBOW_HIT, 1.1D));

		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.CHEST_CLOSE, noisyInteractBuff).setMaxDistToSpawnFromPlayer(noisyInteractRange));

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
		addSoundIntegerEntry(new SoundProfileEntry("debug-doors", noisyInteractBuff).setListSoundEventTypes(listDoorSoundEvents));

		//records
		List<Integer> list = new ArrayList<>();
		list.add(1010);
		addSoundIntegerEntry(new SoundProfileEntry("debug-records", 300).setListSoundEventTypes(list));

		listSoundProfiles.add(new SoundProfileEntry(".place", noisyInteractBuff));
		//listSoundProfiles.add(new SoundProfileEntry("generic.eat", 0.3D));
		listSoundProfiles.add(new SoundProfileEntry("player.burp", 1.1D));

		//covers all note block sounds
		listSoundProfiles.add(new SoundProfileEntry("block.note", noisyInteractBuff).setMaxDistToSpawnFromPlayer(64));

		listSoundProfiles.add(new SoundProfileEntry("lever.click", noisyInteractBuff).setMaxDistToSpawnFromPlayer(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry("pressure_plate", noisyInteractBuff).setMaxDistToSpawnFromPlayer(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry("button.click", noisyInteractBuff).setMaxDistToSpawnFromPlayer(noisyInteractRange));

		listSoundProfiles.add(new SoundProfileEntry("tripwire", noisyInteractBuff).setMaxDistToSpawnFromPlayer(noisyInteractRange));
		listSoundProfiles.add(new SoundProfileEntry("block.barrel", noisyInteractBuff).setMaxDistToSpawnFromPlayer(noisyInteractRange));

		//long dist ones
		if (ZAConfigFeatures.noisyZombies) listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ZOMBIE_AMBIENT, 0.8D, ZAConfig.noisyZombiesReinforceOddsTo1).setMaxDistToSpawnFromPlayer(18));
		if (ZAConfigFeatures.noisyPistons) listSoundProfiles.add(new SoundProfileEntry(SoundEvents.PISTON_EXTEND, 2D, 20).setMaxDistToSpawnFromPlayer(24));

		//not used traditionally, looked up directly
		listSoundProfiles.add(new SoundProfileEntry(SoundEvents.GENERIC_EXPLODE, 3D).setMaxDistToSpawnFromPlayer(24));
	}

	public static void addSoundIntegerEntry(SoundProfileEntry entry) {
		for (int soundID : entry.getListSoundEventTypes()) {
			lookupSoundIntToProfile.put(soundID, entry);
		}
		//redundant now?
		listSoundProfiles.add(entry);
	}

	public static SoundProfileEntry getSoundIDEntry(int soundType) {
		return lookupSoundIntToProfile.get(soundType);
	}
    
    public static SoundProfileEntry getSoundIDEntry(String sound) {
		if (lookupSoundNameToProfileCache.containsKey(sound)) {
			return lookupSoundNameToProfileCache.get(sound);
		}
    	for (SoundProfileEntry entry : listSoundProfiles) {

    		if (entry.getSoundName().equals(sound)) {
				lookupSoundNameToProfileCache.put(sound, entry);
    			return entry;
    		} else if (entry.isPartialMatchOnly() && sound.contains(entry.getSoundName())) {
				lookupSoundNameToProfileCache.put(sound, entry);
    			return entry;
    		}
    	}
    	return null;
    }
	
	public static void tickPlayer(Player player) {
    	
		if (ZAConfigFeatures.wanderingHordes) {

			long lastWaypoint = lookupLastWaypointTime.containsKey(CoroUtilEntity.getName(player)) ? lookupLastWaypointTime.get(CoroUtilEntity.getName(player)) : 0L;
			if (lastWaypoint < System.currentTimeMillis()) {
				//System.out.println("lastWaypoint: " + lastWaypoint);
				lastWaypoint = System.currentTimeMillis() + ZAConfigFeatures.frequencyOfWanderingHordesPerPlayer * 1000L;
				lookupLastWaypointTime.put(CoroUtilEntity.getName(player), lastWaypoint);
				//if (rand.nextInt(25) == 0) {
				spawnWaypoint(player);
				//}
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

	public static void processMobSpawn(LivingSpawnEvent.SpecialSpawn event) {
		if (ZAConfig.zombieRandSpeedBoost > 0) {
			LivingEntity ent = event.getEntityLiving();

			if (ent instanceof Zombie) {
				if (!isMobSpeedBosted((Mob) ent)) {
					giveRandomSpeedBoost((Mob) ent);
				}
			}
		}
	}

	public static boolean isMobSpeedBosted(Mob ent) {
		return ent.getPersistentData().getBoolean(SPEED_BOOST_TAG);
	}
	
	public static void giveRandomSpeedBoost(Mob ent) {
		if (ZAConfig.zombieRandSpeedBoost > 0) {
			double randBoost = ent.level.random.nextDouble() * ZAConfig.zombieRandSpeedBoost;
			if (ent.isBaby()) {
				randBoost *= -1;
			}
			AttributeModifier speedBoostModifier = new AttributeModifier(CoroUtilAttributes.SPEED_BOOST_UUID, "ZA speed boost", randBoost, AttributeModifier.Operation.MULTIPLY_BASE);
            if (!ent.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(speedBoostModifier)) {
				ZombieAwareness.dbg("boosting zombie speed to " + randBoost);
                ent.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(speedBoostModifier);
				ent.getPersistentData().putBoolean(SPEED_BOOST_TAG, true);
            }
		}
	}
    
    public static void huntTarget(Mob ent, LivingEntity targ, int pri) {
    	CoroUtilPath.tryMoveToEntityLivingLongDist(ent, targ, 1);
		if (ent instanceof Mob) (ent).setTarget(targ);
	}
	
	public static void huntTarget(Mob ent, LivingEntity targ) {
		huntTarget(ent, targ, 0);
	}
    
	public static boolean isEnemy(Entity ent, Entity targ) {
		return isEnemy(ent, targ, false);
	}
	
    public static boolean isEnemy(Entity ent, Entity targ, boolean omniTarget) {
    	if (targ instanceof LivingEntity) {
			if (targ instanceof Player) {
				if (!((Player) targ).isCreative() && ((Player) targ).getEffect(MobEffects.INVISIBILITY) == null) {
					if (!omniTarget) {
						return true;
					} else if (ZAConfigPlayerLists.whiteListUsedOmniscient) {
						if (ZAConfigPlayerLists.whitelistOmniscientTargettedPlayers.contains(CoroUtilEntity.getName(((Player) targ)))) {
							if (ZAConfig.debugConsoleOmniscient) ZombieAwareness.dbg(CoroUtilEntity.getName((Player) targ) + " targetting omnisciently by " + ent);
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

	public static void markPerformedPathing(Mob ent) {
		ent.setNoActionTime(0);
		ent.getPersistentData().putLong(ZA_LAST_ACTION, ent.level.getGameTime());
	}
    
    public static void tickAI(Mob ent) {
		//if (true) return;
		//ZAConfig.debugConsoleSuperDetailed = false;
    	if (ZAConfig.debugConsoleSuperDetailed) ZombieAwareness.dbg("ZA DBG: Ticking: " + ent);

		long lastActionTime = ent.getPersistentData().getLong(ZA_LAST_ACTION);
		if (lastActionTime > 0 && ent.level.getGameTime() - ZAConfig.tickCooldownBetweenPathfinds < lastActionTime) return;

		//if (ent.getNoActionTime() <= 40) return;
    	
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
			if (!ZAConfig.awareness_Light_OnlyZombies || (ent instanceof Zombie)) {
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
			Node pathTo = ent.getNavigation().getPath().getEndNode();
			if (pathTo != null) {
				Player player = getClosestPlayer(ent.level, pathTo.x, pathTo.y, pathTo.z, 6D);
				if (player != null) {
					//tryPlayInvestigateSound(ent, new Vec3d(ent.getX(), ent.getY(), ent.getZ()));
					tryPlayInvestigateSound(ent, new Vector3d(pathTo.x, pathTo.y, pathTo.z));
				}
				
			}
			
		}
	    	
	    tickCustomMob(ent);
    }
    
    public static void tickCustomMob(Mob ent) {
    	if (ZAConfigFeatures.wanderingHordes) {
	    	if (ent instanceof Spider) {
	    		if (ent.getPassengers().size() > 0 && ent.getPassengers().get(0) instanceof Skeleton) {
	    			if (ent.level.random.nextInt(100) == 0) {
	    				spawnWaypoint(ent);
	    			}
	    		}
	    	}
    	}
    }
    
    public static boolean ai_FindLightSource(Mob ent) {
    	
    	if (ent.level.isDay()) return false;

		boolean forceOn = false;
    	
    	if (forceOn || ent.level.random.nextInt(3) == 0) {

			float lightValueAtEntity = ent.level.getBrightness(ent.blockPosition());

    		Random rand = new Random();
    		
    		int size;
    		
    		for (int i = 0; i < 4; i++) {
    			Player entP = getClosestPlayerToEntity(ent.level, ent, 999);
        		if (entP != null) {

					//adjusted from 32 for 1.16 to get better results, vanilla might have changed something
        			size = 32 * (i+1);
        			//size = 12 * (i+1);
        			//size = 4;

		    		int rX = (int) Math.floor(entP.getX() + (rand.nextInt(size) - (size/2)));
		    		int rY = (int) Math.floor(entP.getY() + (rand.nextInt(size/2) - (size/4)));
		    		int rZ = (int) Math.floor(entP.getZ() + (rand.nextInt(size) - (size/2)));

		    		BlockPos pos = new BlockPos(rX, rY, rZ);

					if (!ent.level.isLoaded(pos)) continue;

		    		float lightValue = entP.level.getBrightness(pos);

					//as of 1.16 its float values for brightness, 0.2 seems like a good minimum brightness
					//moon phases doesnt seem to affect this value

		    		//if bright enough and also as bright or brighter than where they are currently
		    		if (forceOn || lightValue > 0.2F/* && lightValue >= lightValueAtEntity*/) {
						//adjusted to 32 from 64
		    			if ((forceOn || ent.level.random.nextInt(5) == 0) && ent.distanceTo(entP) > 16) {
							boolean canSeePos = CoroUtilEntity.canSee(ent, new BlockPos(rX, rY, rZ));
							if (canSeePos) {
								ZombieAwareness.dbg("try path to light source - " + rX + ", " + rY + ", " + rZ);
								if (ent.getNavigation().moveTo(rX, rY, rZ, 1)) {
								//if (CoroUtilPath.tryMoveToXYZLongDist(ent, rX, rY, rZ, 1)) {
									ZombieAwareness.dbg("node count: " + ent.getNavigation().getPath().getNodeCount());

									ZombieAwareness.dbg("pathing to lightsource at " + rX + ", " + rY + ", " + rZ + " - " + ent);
									markPerformedPathing(ent);
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
    
    public static EntityScent ai_FindSense(Mob ent) {
    	return ai_FindSense(ent, true);
    }
    
    public static EntityScent ai_FindSense(Mob ent, boolean includeWaypoints) {
    	
    	EntityScent var3 = getSenseNearEntity(ent);

        if(var3 != null) {
        	if (includeWaypoints || var3.type != 2) {
        		if (CoroUtilPath.tryMoveToEntityLivingLongDist(ent, var3, 1)) {
					markPerformedPathing(ent);
        			ZombieAwareness.dbg("ai_FindSense call, type: " + ((EntityScent)var3).type + " - " + ent.getName() + " -> " + var3.position());
        			return var3;
        		}
        	}
        }
        
        return null;
    }
    
    public static boolean ai_FindTarget(Mob ent, boolean omniscient) {
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
	            	if (omniscient || (ZAConfig.seeThroughWalls || ((LivingEntity) entity1).hasLineOfSight(ent))) {
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
				markPerformedPathing(ent);
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
        List<Entity> listEnts = entSource.level.getEntities(entSource, entSource.getBoundingBox().inflate(ZAConfig.maxPFRangeSense, ZAConfig.maxPFRangeSense, ZAConfig.maxPFRangeSense));
        
        EntityScent entBest = null;

        for(int i = 0; i < listEnts.size(); ++i) {
        	Entity entCheck = listEnts.get(i);

            if (entCheck instanceof EntityScent) {
            	
            	double dist = entSource.distanceTo(entCheck);

				float rangeToClose = 5F;
				int randChance = 20;
				if (((EntityScent)entCheck).type == 2) {
					rangeToClose = 10F;
					randChance = 2;
				}

				if (dist < ((EntityScent)entCheck).getRange() && dist > rangeToClose && entSource.level.random.nextInt(randChance) == 0) {
					entBest = (EntityScent) entCheck;
					return entBest;
				}
            }
        }

        return entBest;
    }

	public static void hookPlayEvent(int type, Level world, double pX, double pY, double pZ, int data) {

		addSoundHooks();

		//if event type is for playing a record
		if (world.isClientSide() || !canSpawnTraceQuickCheck(world)) return;

		SoundProfileEntry entry = getSoundIDEntry(type);
		if (entry != null) {
			Player closestPlayer = getClosestPlayer(world, pX, pY, pZ, entry.getMaxDistToSpawnFromPlayer());
			if (closestPlayer != null) {

				Vector3d pos = new Vector3d(pX, pY, pZ);
				if (!canSpawnTrace(world, pX, pY, pZ)) {
					return;
				}

				handleSoundProfileEvent(world, entry, pos, closestPlayer);
			}
		}
	}

    public static void hookSoundEvent(SoundEvent sound, Level world, double x, double y, double z, float volume, float pitch) {

		addSoundHooks();
    	
    	if (world.isClientSide() || sound == null || !canSpawnTraceQuickCheck(world)) return;

		String soundName = SoundProfileEntry.getSoundEventName(sound);
		SoundProfileEntry entry = getSoundIDEntry(soundName);

		if (entry != null) {
			Player closestPlayer = getClosestPlayer(world, x, y, z, entry.getMaxDistToSpawnFromPlayer());
			if (closestPlayer != null) {
				Vector3d pos = new Vector3d(x, y, z);
				if (!canSpawnTrace(world, x, y, z)) {
					//spawn 1 higher up, added for tripwire detection, cant spawn sense on tripwire
					y += 1;
					if (!canSpawnTrace(world, x, y, z)) {
						return;
					}
				}
				handleSoundProfileEvent(world, entry, pos, closestPlayer);
			}
		}
    }

	public static void handleSoundProfileEvent(Level world, SoundProfileEntry entry, Vector3d pos, Player closestPlayer) {
		double distToPlayer = Math.sqrt(closestPlayer.distanceToSqr(pos.x, pos.y, pos.z));
		double strength = ZAConfig.soundStrength;
		if (distToPlayer <= entry.getMaxDistToSpawnFromPlayer()) {
			if (entry.getOddsTo1ToUse() <= 0 || rand.nextInt(entry.getOddsTo1ToUse()) == 0) {
				strength *= entry.getMultiplier();

				EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);

				ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent, sound: " + entry.getSoundName() + ", str: " + scent.getStrengthPeak()/* + ", vol: " + volume*/);
			}
		}
	}
    
    public static void hookBlockEvent(PlayerEvent event, int chance) {
		if (event.getEntity() != null && !canSpawnTraceQuickCheck(event.getEntity().level)) return;
    	
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
	public static void handleBlockBasedEvent(Player player, Level world, BlockPos pos, int chance) {
		if (player == null && ZAConfig.blockBreakEvent_PlayersOnly) {
			return;
		}

		if (!canSpawnTraceQuickCheck(world)) return;

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
    	
    	if (event.getEntityLiving() instanceof Mob) {
    		if (event.getTarget() instanceof Player) {
	    		//tryPlayAlertSound((EntityLiving)event.getEntityLiving(), new Vec3d(event.getTarget().getX(), event.getTarget().getY(), event.getTarget().getZ()));
	    		tryPlayTargetSound((Mob)event.getEntityLiving(), (LivingEntity)event.getTarget(), new Vector3d(event.getEntityLiving().getX(), event.getEntityLiving().getY(), event.getEntityLiving().getZ()));
    		} else if (event.getTarget() == null) {
    			//dont use, AI stupidly detargets when resetting tasks despite still chasing player, causing double alert noise if this code is used
    			/*if (lookupLastAlertTime.containsKey(event.getEntityLiving())) {
    				lookupLastAlertTime.remove(event.getEntityLiving());
        			System.out.println("detarget");
    			}*/
    		}
    	}
    	
    }
    
    public static void spawnWaypoint(Entity entSource) {
        int range = 128;
        
        double tryX = (int)entSource.getX() - (range/2) + (rand.nextInt(range));
        double tryZ = (int)entSource.getZ() - (range/2) + (rand.nextInt(range));
        double tryY = entSource.level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)Math.floor(tryX), (int)Math.floor(tryZ));

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

        var1.setStrengthPeak(150);
        
        if (newNode) {
	        var1.setPos(tryX, tryY, tryZ);
	        var1.type = 2;
	        
	        entSource.level.addFreshEntity(var1);
        }

		//System.out.println("spawning new waypoint for ");
        if (debug) System.out.println("WP: " + entSource + " - range: " + var1.getRange());
    }

	public static boolean canSpawnTraceQuickCheck(Level world) {
		if (!ZAConfigFeatures.awareness_Sound) {
			return false;
		}
		if (ZAConfigFeatures.awareness_Sound_OverworldOnly) {
			if (world.dimension() != Level.OVERWORLD) return false;
		}
		return true;
	}

    public static boolean canSpawnTrace(Level world, double x, double y, double z) {
    	BlockPos pos = new BlockPos(x,y,z);
    	if (!world.isLoaded(pos)) return false;
    	BlockState state = world.getBlockState(pos);
    	//iirc circuits check was to prevent senses spawning on pressure plates and triggering them, but there should be better ways to stop that...
		//might be redundant since AABB and canTriggerWalking and canBeCollidedWith fix
        if (state.getMaterial() == Material.DECORATION && (!(state.getBlock() instanceof ButtonBlock) && !(state.getBlock() instanceof LeverBlock))) {
            return false;
        }
        return true;
    }
    
    public static Player getClosestPlayerToEntity(Level world, Entity par1Entity, double par2)
    {
        return getClosestPlayer(world, par1Entity.getX(), par1Entity.getY(), par1Entity.getZ(), par2);
    }

    /**
     * Gets the closest player to the point within the specified distance (distance can be set to less than 0 to not
     * limit the distance). Args: x, y, z, dist
     */
    public static Player getClosestPlayer(Level world, double x, double y, double z, double maxDistance)
    {
        double closestDist = -1.0D;
        Player closestPlayer = null;

        for (int i = 0; i < world.players().size(); ++i)
        {
            Player entityplayer1 = world.players().get(i);
            if (!ZAConfigPlayerLists.whiteListUsedSenses || ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(entityplayer1))) {
            	double d5 = entityplayer1.distanceToSqr(x, y, z);

                if ((maxDistance < 0.0D || d5 < maxDistance * maxDistance) && (closestDist == -1.0D || d5 < closestDist))
                {
                    closestDist = d5;
                    closestPlayer = entityplayer1;
                }
            }
        }

        return closestPlayer;
    }

    /**
     * Checks if a scent of the same type is already at this location
     * 
     * @param parWorld
     * @param parPos
     * @param type
     * @return
     */
    public static EntityScent getSenseNodeAtPos(Level parWorld, Vector3d parPos, EnumSenseType type) {
    	
    	if (ZAConfig.extraScentCutoffRange == -1) return null;
    	
    	AABB aabb = new AABB(parPos.x, parPos.y, parPos.z, parPos.x + 1, parPos.y + 1, parPos.z + 1);
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
    
    public static EntityScent spawnOrBuffSenseAtPos(Level world, Vector3d parPos, EnumSenseType type, int strength) {
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
    public static EntityScent spawnOrBuffSenseAtPos(Level world, Vector3d parPos, EnumSenseType type, int strength, boolean frequentSoundMultiply) {
		
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
    
    public static void tryPlayTargetSound(Mob entAlerted, LivingEntity entTargetted, Vector3d pos) {

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
				if (entAlerted.hasLineOfSight(entTargetted)) {
					//entAlerted.level.playSound(null, pos.x, pos.y, pos.z, SoundRegistry.get("target"), SoundCategory.HOSTILE, 3F, 0.8F + (entAlerted.level.random.nextFloat() * 0.2F));
					entAlerted.level.playSound(null, entTargetted.getX(), entTargetted.getY(), entTargetted.getZ(), ZAConfigFeatures.soundUseAlternateAlertNoise ? SoundRegistry.get("alert") : SoundRegistry.get("target"), SoundSource.HOSTILE, (float) ZAConfigFeatures.soundVolumeAlertTarget, ZAConfigFeatures.soundUseAlternateAlertNoise ? 1F : 0.8F + (entAlerted.level.random.nextFloat() * 0.2F));
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
    
    public static void tryPlayInvestigateSound(Mob entAlerted, Vector3d pos) {

		if (!ZAConfigFeatures.soundInvestigates) return;

		if (!ZombieAwareness.canProcessEntity(entAlerted)) return;

    	if (!lookupLastInvestigateTime.containsKey(entAlerted) || lookupLastInvestigateTime.get(entAlerted) + investigateDelay < entAlerted.level.getGameTime()) {
			entAlerted.level.playSound(null, pos.x, pos.y, pos.z, SoundRegistry.get("investigate"), SoundSource.HOSTILE, (float)ZAConfigFeatures.soundVolumeInvestigate, 0.7F + (entAlerted.level.random.nextFloat() * 0.3F));
			lookupLastInvestigateTime.put(entAlerted, entAlerted.level.getGameTime());
			ZombieAwareness.dbg("!!! investigate play for ent: " + entAlerted.getId() + ", lookupSize: " + lookupLastInvestigateTime.size());
		}
    }

	public static boolean isZombieAwarenessActive(Level world) {
		if (world == null) return false;
		if (ZAConfig.daysBeforeFeaturesActivate <= 0) return true;
		if (((double)world.getGameTime() / CoroUtilWorldTime.getDayLength()) >= ZAConfig.daysBeforeFeaturesActivate) {
			return true;
		} else {
			return false;
		}
	}
}
