package ZombieAwareness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPath;
import ZombieAwareness.config.ZAConfig;
import ZombieAwareness.config.ZAConfigFeatures;
import ZombieAwareness.config.ZAConfigPlayerLists;
import ZombieAwareness.config.ZAConfigSpawning;

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
	 * TODO: for release
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
	
	//these 2 are only used for each player, but i cant exactly backtrack to the player that makes the sound so its shared, shouldnt be too horrible...
	//moving to system that looks for previously made sense, and adds to its current strength, requires no player reference
    //public static long lastSoundTime;
    //public static float lastMultiply = 1.0F;
    
    public static Random rand = new Random();
    
    public static HashMap<String, Integer> lastHealths = new HashMap();
    public static HashMap<String, Long> lastBleedTimes = new HashMap();
    //public static HashMap<String, Long> lastSoundTimes;
    //public static HashMap<String, Integer> lastMultiplies;
    
    //amplified/debuffed sounds, also entries in here are exempt from the vague blacklist
    @Deprecated
    public static HashMap<SoundEvent, Double> lookupSoundToStrengthMultiplier = new HashMap<SoundEvent, Double>();
    public static List<SoundProfileEntry> listSoundProfiles = new ArrayList<SoundProfileEntry>();
    
    @Deprecated
    public static List<SoundEvent> listSoundBlacklistExact = new ArrayList<SoundEvent>();
    @Deprecated
    public static List<String> listSoundBlacklistVague = new ArrayList<String>();
    
    public static WeakHashMap<Entity, Long> lookupLastAlertTime = new WeakHashMap<Entity, Long>();
    public static long alertDelay = 60*20;
    
    public static WeakHashMap<Entity, Long> lookupLastInvestigateTime = new WeakHashMap<Entity, Long>();
    public static long investigateDelay = 60*20;
    
    public static boolean debug = false;
    
    static {
    	
    	//TODO: consider whitelists instead, too many things to blacklist
    	
    	listSoundBlacklistExact.add(SoundEvents.ENTITY_ARROW_SHOOT);
    	listSoundBlacklistExact.add(SoundRegistry.get("alert"));
    	
    	//walking
    	listSoundBlacklistVague.add(".step");
    	//picking up exp
    	listSoundBlacklistVague.add(".touch");
    	//picking up items
    	listSoundBlacklistVague.add(".pickup");
    	//weird eating based sound
    	listSoundBlacklistVague.add(".equip");
    	//listSoundBlacklistVague.add(".attack.crit");
    	listSoundBlacklistVague.add("player.attack");
    	listSoundBlacklistVague.add(".hurt");
    	listSoundBlacklistVague.add(".death");
    	
    	listSoundBlacklistVague.add(".ambient");
    	
    	listSoundBlacklistVague.add("generic.eat");
    	
    	
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_GENERIC_EXPLODE, 3D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.BLOCK_PISTON_EXTEND, 2D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1.1D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_ARROW_HIT, 1.1D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 0.8D);
    	
    	//make distant triggers
    	lookupSoundToStrengthMultiplier.put(SoundEvents.BLOCK_CHEST_CLOSE, 1.3D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, 1.3D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.BLOCK_IRON_DOOR_CLOSE, 1.3D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1.3D);
    	
    	int noisyInteractRange = 30;
    	double noisyInteractBuff = 1.3D;
    	
    	//short dist ones
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1.1D));
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ENTITY_ARROW_HIT, 1.1D));
    	
    	
    	
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.BLOCK_CHEST_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.BLOCK_IRON_DOOR_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, noisyInteractBuff).setDistanceMax(noisyInteractRange));
    	
    	listSoundProfiles.add(new SoundProfileEntry(".place", noisyInteractBuff));
    	listSoundProfiles.add(new SoundProfileEntry("player.burp", 1.1D));
    	
    	//covers all note block sounds
    	listSoundProfiles.add(new SoundProfileEntry("block.note", noisyInteractBuff).setDistanceMax(64));
    	
    	//TODO: buttons levers
    	
    	//long dist ones
    	if (ZAConfigFeatures.noisyZombies) listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 0.8D, 8*20).setDistanceMax(48));
    	if (ZAConfigFeatures.noisyPistons) listSoundProfiles.add(new SoundProfileEntry(SoundEvents.BLOCK_PISTON_EXTEND, 2D, 20).setDistanceMax(128));
    	
    	listSoundProfiles.add(new SoundProfileEntry(SoundEvents.ENTITY_GENERIC_EXPLODE, 3D).setDistanceMax(128));
    	
    	
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
	
	public static void tickPlayer(EntityPlayer player) {
    	
		if (ZAConfigFeatures.wanderingHordes) {
			if (rand.nextInt(100) == 0) {
				spawnWaypoint(player);
			}
		}
		
		if (!ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(player))) {
			if (ZAConfigFeatures.extraSpawningSurface) {
				if (!player.worldObj.isDaytime()) {
					if (ZombieAwareness.lastMobsCountSurface < ZAConfigSpawning.extraSpawningSurfaceMaxCount) {
						if (ZAConfigSpawning.extraSpawningSurfaceRandomPool <= 0 || rand.nextInt(ZAConfigSpawning.extraSpawningSurfaceRandomPool) == 0) {
							spawnNewMobSurface(player);
						}
					}
				}
			}
			
			if (ZAConfigFeatures.extraSpawningCave) {
				//we need existing zombies for this, caves always have something, for now depend on existing ones, do better cave spawning with hostile worlds tech
				//code in ZombieAwareness class
				
				if (ZombieAwareness.lastMobsCountCaves < ZAConfigSpawning.extraSpawningCavesMaxCount) {
					if (ZAConfigSpawning.extraSpawningCavesRandomPool <= 0 || rand.nextInt(ZAConfigSpawning.extraSpawningCavesRandomPool) == 0) {
						spawnNewMobCave(player);
					}
				}
			}
		}
		

        if (ZAConfigFeatures.awareness_Scent) {
        	int lastHealth = lastHealths.containsKey(CoroUtilEntity.getName(player)) ? lastHealths.get(CoroUtilEntity.getName(player)) : 0;
    		Long lastBleedTime = lastBleedTimes.containsKey(CoroUtilEntity.getName(player)) ? lastBleedTimes.get(CoroUtilEntity.getName(player)) : 0;
    		
    		Vec3d pos = new Vec3d(player.posX, player.posY, player.posZ);
    		
            if((int)player.getHealth() != lastHealth) {
                if(player.getHealth() < lastHealth) {
                	EntityScent scent = spawnOrBuffSenseAtPos(player.worldObj, pos, EnumSenseType.SCENT_BLOOD, ZAConfig.scentStrength);
                	ZombieAwareness.dbg("spawned or buffed scent sense from damage: " + scent.getStrengthPeak());
                }

                lastHealth = (int) player.getHealth();
            }
            
            lastHealths.put(CoroUtilEntity.getName(player), lastHealth);

            if(player.getHealth() / player.getMaxHealth() < 0.6F && lastBleedTime < System.currentTimeMillis()) {
                lastBleedTime = System.currentTimeMillis() + 30000L;
                lastBleedTimes.put(CoroUtilEntity.getName(player), lastBleedTime);
                EntityScent scent = spawnOrBuffSenseAtPos(player.worldObj, pos, EnumSenseType.SCENT_BLOOD, ZAConfig.scentStrength);
                ZombieAwareness.dbg("spawned or buffed scent sense from bleeding: " + scent.getStrengthPeak());
            }
        }
    }
	
	public static void giveRandomSpeedBoost(EntityLiving ent) {
		
		if (ZAConfig.zombieRandSpeedBoost > 0) {
			double randBoost = ent.worldObj.rand.nextDouble() * ZAConfig.zombieRandSpeedBoost;
			AttributeModifier speedBoostModifier = new AttributeModifier(UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836"), "ZA speed boost", randBoost, 1);
			ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(speedBoostModifier);
		}
		
	}
    
    public static void huntTarget(EntityLivingBase ent, EntityLivingBase targ, int pri) {
    	CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)ent, targ, 1);
		if (ent instanceof EntityLiving) ((EntityLiving)ent).setAttackTarget(targ);
	}
	
	public static void huntTarget(EntityLivingBase ent, EntityLivingBase targ) {
		huntTarget(ent, targ, 0);
	}
    
	public static boolean isEnemy(Entity ent, Entity targ) {
		return isEnemy(ent, targ, false);
	}
	
    public static boolean isEnemy(Entity ent, Entity targ, boolean omniTarget) {
    	if (targ instanceof EntityLivingBase) {
			if (targ instanceof EntityPlayer) {
				if (!((EntityPlayer) targ).capabilities.isCreativeMode) {
					if (!omniTarget) {
						return true;
					} else if (ZAConfigPlayerLists.whiteListUsedOmniscient) {
						if (ZAConfigPlayerLists.whitelistOmniscientTargettedPlayers.contains(CoroUtilEntity.getName(((EntityPlayer) targ)))) {
							if (ZAConfig.debugConsoleOmniscient) ZombieAwareness.dbg(CoroUtilEntity.getName((EntityPlayer) targ) + " targetting omnisciently by " + ent);
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
    
    public static void tickAI(EntityLiving ent) {
    	
    	if (ZAConfig.debugConsoleSuperDetailed) ZombieAwareness.dbg("ZA DBG: Ticking: " + ent);
    	
    	//A more performance friendly omniscient, only runs it when no target, but still allows for smaller ranged retargetting
    	if (ent.worldObj.getTotalWorldTime() % 40 == 0) {
	    	if (ZAConfig.omniscient && ent.getAttackTarget() == null) {
	    		ai_FindTarget(ent, true);
	    	} else {
	    		ai_FindTarget(ent, false);
	    	}
    	}
    	
		if (PFQueue.instance == null) {
    		new PFQueue(ent.worldObj);
    	}
		long time = 0;
		try {
			if (PFQueue.pfDelays.containsKey(ent)) {
				time = (Long)PFQueue.pfDelays.get(ent);
			}
		} catch (Exception ex) {
			
		}
		
		EntityScent senseTracked = null;
		
		if (ent.getAttackTarget() == null && (/*ent.worldObj.rand.nextInt(5) == 0 && */time < System.currentTimeMillis() && ent.getNavigator().noPath())) {
			//Find player made senses
			if (!ZAConfig.awareness_Light_OnlyZombies || (ent instanceof EntityZombie)) {
				if (!ZAConfigFeatures.awareness_Light || !ai_FindLightSource(ent)) {
    				//TEMP CODE, THREAD ME SO I CAN SCAN FOR BLOCKS FASTER!!! - ended up designing with minimal scanning, this works ok for now
					if (ent.worldObj.rand.nextInt(3) == 0) {
						senseTracked = ai_FindSense(ent, true);
					}
	    			
    			}
    		} else {
    			senseTracked = ai_FindSense(ent);
    		}
    	}
		
		if (senseTracked != null && ent.getNavigator().getPath() != null) {
			PathPoint pathTo = ent.getNavigator().getPath().getFinalPathPoint();
			if (pathTo != null) {
				EntityPlayer player = getClosestPlayer(ent.worldObj, pathTo.xCoord, pathTo.yCoord, pathTo.zCoord, 6D);
				if (player != null) {
					//tryPlayInvestigateSound(ent, new Vec3d(ent.posX, ent.posY, ent.posZ));
					tryPlayInvestigateSound(ent, new Vec3d(pathTo.xCoord, pathTo.yCoord, pathTo.zCoord));
				}
				
			}
			
		}
	    	
	    tickCustomMob(ent);
    }
    
    public static void tickCustomMob(EntityLivingBase ent) {
    	if (ZAConfigFeatures.wanderingHordes) {
	    	if (ent instanceof EntitySpider) {
	    		if (ent.getPassengers().size() > 0 && ent.getPassengers().get(0) instanceof EntitySkeleton) {
	    			if (ent.worldObj.rand.nextInt(100) == 0) {
	    				spawnWaypoint(ent);
	    			}
	    		}
	    	}
    	}
    }
    
    public static boolean ai_FindLightSource(EntityLivingBase ent) {
    	
    	if (ent.worldObj.isDaytime()) return false;
    	
    	if (ent.worldObj.rand.nextInt(1) == 0) {
    		
    		Random rand = new Random();
    		
    		int size = 32;
    		
    		for (int i = 0; i < 4; i++) {
    			EntityPlayer entP = getClosestPlayerToEntity(ent.worldObj, ent, 999);
        		if (entP != null) {
        			
        			size = 32 * (i+1);
        			
		    		int rX = (int)entP.posX + (rand.nextInt(size) - (size/2));
		    		int rY = (int)entP.posY + (rand.nextInt(size/2) - (size/4));
		    		int rZ = (int)entP.posZ + (rand.nextInt(size) - (size/2));
		    		
		    		int lightValue = entP.worldObj.getLightFromNeighbors(new BlockPos(rX, rY, rZ));
		    		
		    		if (lightValue > 4) {
		    			if ((ent.getDistanceToEntity(entP) > 64 && (ent.worldObj.rand.nextInt(20) == 0) || 
		    					ent.worldObj.rayTraceBlocks(new Vec3d(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), new Vec3d(rX, rY, rZ)) == null)) {
		    				if (CoroUtilPath.tryMoveToXYZLongDist((EntityCreature)ent, rX, rY, rZ, 1)) {
		    					//ZombieAwareness.dbg("pathing to lightsource at " + rX + ", " + rY + ", " + rZ + " - " + ent);
			    			}
			    			return true;
	    				}
		    		}
        		}
    		}
    	}
    	
    	return false;
    }
    
    public static EntityScent ai_FindSense(EntityLivingBase ent) {
    	return ai_FindSense(ent, true);
    }
    
    public static EntityScent ai_FindSense(EntityLivingBase ent, boolean includeWaypoints) {
    	
    	EntityScent var3 = getSenseNearEntity(ent);

        if(var3 != null) {
        	if (includeWaypoints || var3.type != 2) {
        		if (CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)ent, var3, 1)) {
        			//ZombieAwareness.dbg("ai_FindSense call, type: " + ((EntityScent)var3).type + " - " + ent.getName() + " -> " + var3.getPosition());
        			return var3;
        		}
        	}
        }
        
        return null;
    }
    
    public static boolean ai_FindTarget(EntityLiving ent, boolean omniscient) {
    	long huntRange = ZAConfig.sightRange;
    	
    	if (omniscient) huntRange = 512;
    	
    	if (/*ent.getHealth() > ent.getMaxHealth() * 0.90F && */(ent.getAttackTarget() == null || ent.worldObj.rand.nextInt(100) == 0)) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.getEntityBoundingBox().expand(huntRange, huntRange/2, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(isEnemy(ent, entity1, omniscient))
	            {
	            	if (omniscient || (ZAConfig.seeThroughWalls || ((EntityLivingBase) entity1).canEntityBeSeen(ent))) {
	            		if (sanityCheck(ent, entity1)/* && entity1 instanceof EntityPlayer*/) {
	            			float dist = ent.getDistanceToEntity(entity1);
	            			if (dist < closest) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
	            		}
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	huntTarget(ent, (EntityLivingBase)clEnt);
	        	return true;
	        }
		} else {
			
			//if (ent.entityToAttack != null) {
				//if (!ent.hasPath() && ent.getDistanceToEntity(ent.entityToAttack) > 5F) {
					//PFQueue.getPath(ent, ent.entityToAttack, ent.maxPFRange);
				//}
			//}
			
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
        List<Entity> listEnts = entSource.worldObj.getEntitiesWithinAABBExcludingEntity(entSource, entSource.getEntityBoundingBox().expand((double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense));
        
        EntityScent entBest = null;
        //double distBest = 999999;

        for(int i = 0; i < listEnts.size(); ++i) {
        	Entity entCheck = listEnts.get(i);

            if (entCheck instanceof EntityScent) {
            	
            	double dist = entSource.getDistanceToEntity(entCheck);
            	
            	//if (dist < distBest) {
		            if (dist < ((EntityScent)entCheck).getRange() && dist > 5.0F && entSource.worldObj.rand.nextInt(20) == 0) {
		                entBest = (EntityScent) entCheck;
		                return entBest;
		            }
            	//}
            }
        }

        return entBest;
    }

    public static void hookSoundEvent(SoundEvent sound, World world, double x, double y, double z, float volume, float pitch) {
        
    	if (!ZAConfigFeatures.awareness_Sound) {
            return;
        }
    	
    	if (world.isRemote || sound == null) return;
    	
    	if (world.provider.getDimension() != 0 && world.provider.getDimension() != -127) return;
    	
        if (!canSpawnTrace(world, x, y, z)) {
            return;
        }
        
        //EntityPlayer closePlayer = getClosestPlayer(world, x, y, z, 3D);
        
        EntityPlayer closestPlayer = getClosestPlayer(world, x, y, z, 128);
        
        //int soundVolumeAdjusted = (int)(20.0F * volume);
        
        /*if (listSoundBlacklistExact.contains(sound)) {
        	return;
        }*/
        
        //required to prevent all the .step types without having to maintain a huge list of exact sound events
        //we will also see if we buff a specific sound so we can exempt it from vague blacklist
        //eg: player.hurt exempt but all mobs hurt blacklisted
        String soundName = SoundProfileEntry.getSoundEventName(sound);
        /*if (!lookupSoundToStrengthMultiplier.containsKey(sound)) {
	        for (String soundFilter : listSoundBlacklistVague) {
	        	if (soundName.contains(soundFilter)) {
	        		return;
	        	}
	        }
        }*/
        
        double strength = ZAConfig.soundStrength;
    	
    	//specific buff for sound
    	/*if (lookupSoundToStrengthMultiplier.containsKey(sound)) {
    		strength *= lookupSoundToStrengthMultiplier.get(sound);
    	}*/
    	
    	Vec3d pos = new Vec3d(x, y, z);
        
    	if (closestPlayer != null) {
    		double distToPlayer = closestPlayer.getDistance(x, y, z);
        	
        	//custom cases for sounds we want to trigger that can be further away
        	
        	//new
        	
        	SoundProfileEntry entry = getFirstEntry(soundName);
        	
        	if (entry != null) {
        		if (distToPlayer <= entry.getDistanceMax()) {
            		if (entry.getOddsTo1ToUse() <= 0 || rand.nextInt(entry.getOddsTo1ToUse()) == 0) {
            			strength *= entry.getMultiplier();
            			
            			EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);
                		
                		ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent, sound: " + soundName + ", str: " + scent.getStrengthPeak() + ", vol: " + volume);
            		}
        		} else {
        			//ZombieAwareness.dbg("too far: " + soundName + " - " + distToPlayer);
        		}
        	} else {
        		//debug what we could add
        		if (distToPlayer <= 3) {
        			//ZombieAwareness.dbg("didnt spawn for: " + soundName);
        		}
        	}
    	}
    	
    	
        /*if((closePlayer != null)) {
        	
    		EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);
    		
    		ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent, sound: " + soundName + ", str: " + scent.getStrengthPeak() + ", vol: " + volume);
        	
        } else {
        	//previously used 128 range, lowering for sake of mob despawn concerns
        	//EntityPlayer farPlayer = getClosestPlayer(world, x, y, z, 128);
        	
            
            if (farPlayer != null) {
            	
            	
            	
            	//old
            	
            	if (ZAConfigFeatures.noisyZombies && sound == SoundEvents.ENTITY_ZOMBIE_AMBIENT && distToPlayer <= 48) {
            		if (rand.nextInt(1 + (ZombieAwareness.lastZombieCount * 8)) == 0) {
            			
            			EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength, false);
            			
            			ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent for noisyZombies, sound: " + soundName + ", str: " + scent.getStrengthPeak());
                	}
            	} else if (ZAConfigFeatures.noisyPistons && sound == SoundEvents.BLOCK_PISTON_EXTEND) {
        			if (rand.nextInt(20) == 0) {
        				
        				EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);
        				
        				ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent for noisyPistons, sound: " + soundName + ", str: " + scent.getStrengthPeak());
        			}
            	} else if (sound == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            		
            		EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, (int)strength);
    				
    				ZombieAwareness.dbg("spawned or buffed sound sense from soundEvent for distant sounds, sound: " + soundName + ", str: " + scent.getStrengthPeak());
            	}
            	
            }
        }*/
    }
    
    public static void hookBlockEvent(PlayerEvent event, int chance) {
    	
    	if (!ZAConfigFeatures.awareness_Sound) return;
    	
    	if (event.getEntity().worldObj.provider.getDimension() != 0 && event.getEntity().worldObj.provider.getDimension() != -127) return;
    	
    	if (event.getEntityPlayer() == null || (ZAConfigPlayerLists.whiteListUsedSenses && !ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(event.getEntityPlayer())))) return;
    	
	    	if (!event.getEntity().worldObj.isRemote && event.getEntity().worldObj.rand.nextInt(chance) == 0) {
		    	
	    		int strength = ZAConfig.soundStrength;
	    		Vec3d pos = new Vec3d(event.getEntityPlayer().posX, event.getEntityPlayer().posY, event.getEntityPlayer().posZ);
	    		
	    		EntityScent scent = spawnOrBuffSenseAtPos(event.getEntity().worldObj, pos, EnumSenseType.SOUND, strength);
	    		
	    		ZombieAwareness.dbg("spawned or buffed sound sense from blockEvent: " + scent.getStrengthPeak());
	    	}
    }
    
    public static void hookSetAttackTarget(LivingSetAttackTargetEvent event) {
    	
    	//ZombieAwareness.dbg(event.getEntityLiving().getEntityId() + " targetting " + event.getTarget());
    	
    	if (event.getEntityLiving() instanceof EntityLiving) {
    		if (event.getTarget() instanceof EntityPlayer) {
	    		//tryPlayAlertSound((EntityLiving)event.getEntityLiving(), new Vec3d(event.getTarget().posX, event.getTarget().posY, event.getTarget().posZ));
	    		tryPlayTargetSound((EntityLiving)event.getEntityLiving(), (EntityLivingBase)event.getTarget(), new Vec3d(event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ));
    		} else if (event.getTarget() == null) {
    			//dont use, AI stupidly detargets when resetting tasks despite still chasing player, causing double alert noise if this code is used
    			/*if (lookupLastAlertTime.containsKey(event.getEntityLiving())) {
    				lookupLastAlertTime.remove(event.getEntityLiving());
        			System.out.println("detarget");
    			}*/
    		}
    	}
    	
    }

	public static void hookPlayEvent(World world, int type,
			BlockPos blockPosIn, int data) {
		//if event type is for playing a record
		if (type == 1010) {
			//if putting in a record and not taking out
			if (Item.getItemById(data) instanceof ItemRecord) {
				Vec3d pos = new Vec3d(blockPosIn.getX(), blockPosIn.getY(), blockPosIn.getZ());
				EntityScent scent = spawnOrBuffSenseAtPos(world, pos, EnumSenseType.SOUND, 300);
				ZombieAwareness.dbg("spawned or buffed sound sense from playEvent: " + scent.getStrengthPeak());
			}
		}
	}
    
    public static void spawnNewMobSurface(EntityPlayer player) {
        
        
        int minDist = ZAConfigSpawning.extraSpawningDistMin;
        int maxDist = ZAConfigSpawning.extraSpawningDistMax;
        int range = maxDist * 2;
        
        for (int tries = 0; tries < 5; tries++) {
	        int tryX = (int)player.posX - (range/2) + (rand.nextInt(range));
	        int tryZ = (int)player.posZ - (range/2) + (rand.nextInt(range));
	        int tryY = player.worldObj.getHeight(new BlockPos(tryX, 0, tryZ)).getY();
	
	        if (player.getDistance(tryX, tryY, tryZ) < minDist || player.getDistance(tryX, tryY, tryZ) > maxDist || !canSpawnMob(player.worldObj, tryX, tryY, tryZ) || player.worldObj.getLightFromNeighbors(new BlockPos(tryX, tryY, tryZ)) >= 6) {
	            continue;
	        }
	
	        /*EntityZombie entZ = new EntityZombie(player.worldObj);
			entZ.setPosition(tryX, tryY, tryZ);
			entZ.onSpawnWithEgg((IEntityLivingData)null);
			player.worldObj.spawnEntityInWorld(entZ);*/

			int randSize = player.worldObj.rand.nextInt(ZAConfigSpawning.extraSpawningSurfaceMaxGroupSize) + 1;
			WorldServer world = (WorldServer) player.worldObj;
	        for (int i = 0; i < randSize; i++) {
	        	spawnMobsAllowed(player, world, tryX, tryY, tryZ);
	        }
			
			//if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(player);
			
	        if (ZAConfig.debugConsoleSpawns) ZombieAwareness.dbg("spawnNewMobSurface: " + tryX + ", " + tryY + ", " + tryZ);
	        
	        return;
        }
    }
    
    public static void spawnNewMobCave(EntityPlayer player) {
        
        int minDist = ZAConfigSpawning.extraSpawningCavesDistMin;
        int maxDist = ZAConfigSpawning.extraSpawningCavesDistMax;
        int range = maxDist * 2;
        
        //System.out.println("try");
        
        for (int tries = 0; tries < ZAConfigSpawning.extraSpawningCavesTryCount; tries++) {
	        int tryX = (int)player.posX - (range/2) + (rand.nextInt(range));
	        int tryY = (int)player.posY - (range/2) + (rand.nextInt(range));
	        int tryZ = (int)player.posZ - (range/2) + (rand.nextInt(range));
	        
	        if (player.getDistance(tryX, tryY, tryZ) < minDist || player.getDistance(tryX, tryY, tryZ) > maxDist
	        		|| !isInDarkCave(player.worldObj, tryX, tryY, tryZ, true)) {
	            continue;
	        }
	
	        int randSize = player.worldObj.rand.nextInt(ZAConfigSpawning.extraSpawningCavesMaxGroupSize) + 1;
	        WorldServer world = (WorldServer) player.worldObj;
	        for (int i = 0; i < randSize; i++) {
	        	spawnMobsAllowed(player, world, tryX, tryY, tryZ);
	        }
			
			
			
	        return;
        }
    }
    
    public static void spawnMobsAllowed(EntityPlayer player, WorldServer world, int tryX, int tryY, int tryZ) {
    	if (!ZAConfigSpawning.extraSpawningUseNaturalSpawnList) {
	        EntityZombie entZ = new EntityZombie(world);
			entZ.setPosition(tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F);
			entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (IEntityLivingData)null);
			giveRandomSpeedBoost(entZ);
			world.spawnEntityInWorld(entZ);
			
			if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(player);
			
			if (ZAConfig.debugConsoleSpawns) {
	        	ZombieAwareness.dbg("spawnNewMob: " + tryX + ", " + tryY + ", " + tryZ);
	        }
    	} else {
    		//WorldServer world = (WorldServer) player.worldObj;
    		SpawnListEntry spawnlistentry = world.getSpawnListEntryForTypeAt(EnumCreatureType.MONSTER, new BlockPos(tryX, tryY, tryZ));
    		
    		EntityLiving entityliving;

            try
            {
                entityliving = (EntityLiving)spawnlistentry.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {world});
                entityliving.setLocationAndAngles(tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F, world.rand.nextFloat() * 360.0F, 0.0F);

                Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, world, tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F);
                if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && entityliving.getCanSpawnHere()))
                {
                    world.spawnEntityInWorld(entityliving);
                    if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, tryX + 0.5F, tryY + 1.1F, tryZ + 0.5F))
                    {
                        entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityliving)), (IEntityLivingData) null);
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
    }
    
    /**
     * Method is far from perfect, but should work well enough without intensive processing to verify
     * coords fed in should be solid block with air above it (2 blocks of vertical space, 1 width of size)
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean isInDarkCave(World world, int x, int y, int z, boolean checkSpaceToSpawn) {
    	IBlockState state = world.getBlockState(new BlockPos(x, y, z));
    	Block block = state.getBlock();
    	if (!world.canSeeSky(new BlockPos(x, y, z)) && world.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {
    		if (!CoroUtilBlock.isAir(block) && state.getMaterial() == Material.ROCK/*(block != Blocks.grass || block.getMaterial() != Material.grass)*/) {
    		
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
    }
    
    public static boolean canSpawnMob(World world, int x, int y, int z) {
    	IBlockState state = world.getBlockState(new BlockPos(x-1,y,z));
        Block id = state.getBlock();//Block.pressurePlatePlanks.blockID;

        /*if (id == Block.grass.blockID || id == Block.stone.blockID || id == Block.tallGrass.blockID || id == Block.grass.blockID || id == Block.sand.blockID) {
            return true;
        }*/
        if (!CoroUtilBlock.isAir(id) && state.getMaterial() == Material.LEAVES) {
        	return false;
        }
        return true;
    }
    
    public static void spawnWaypoint(Entity entSource) {
        
        int range = 256;
        
        double tryX = (int)entSource.posX - (range/2) + (rand.nextInt(range));
        double tryZ = (int)entSource.posZ - (range/2) + (rand.nextInt(range));
        double tryY =  entSource.worldObj.getHeight(new BlockPos(tryX, 0, tryZ)).getY();

        if (!canSpawnTrace(entSource.worldObj, tryX, tryY, tryZ)) {
            return;
        }

        double height = entSource.posY/* - (double)var0.yOffset*/ + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        
        EntityScent var1 = getSenseNodeAtPos(entSource.worldObj, new Vec3d(tryX, tryY, tryZ), EnumSenseType.WAYPOINT);
        
        boolean newNode = false;
    	
    	if (var1 == null) {
    		var1 = new EntityScent(entSource.worldObj);
    		newNode = true;
    	}

        var1.setStrengthPeak(60);
        
        if (newNode) {
	        var1.setPosition(tryX, tryY, tryZ);
	        var1.type = 2;
	        
	        entSource.worldObj.spawnEntityInWorld(var1);
        }
        
        if (debug) System.out.println("WP: " + entSource + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
    }

    public static boolean canSpawnTrace(World world, double x, double y, double z) {
    	IBlockState state = world.getBlockState(new BlockPos(x,y,z));
        if (state.getMaterial() == Material.CIRCUITS) {
            return false;
        }
        return true;
    }
    
    public static EntityPlayer getClosestPlayerToEntity(World world, Entity par1Entity, double par2)
    {
        return getClosestPlayer(world, par1Entity.posX, par1Entity.posY, par1Entity.posZ, par2);
    }

    /**
     * Gets the closest player to the point within the specified distance (distance can be set to less than 0 to not
     * limit the distance). Args: x, y, z, dist
     */
    public static EntityPlayer getClosestPlayer(World world, double par1, double par3, double par5, double par7)
    {
        double d4 = -1.0D;
        EntityPlayer entityplayer = null;

        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            EntityPlayer entityplayer1 = (EntityPlayer)world.playerEntities.get(i);
            if (!ZAConfigPlayerLists.whiteListUsedSenses || ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(entityplayer1))) {
            	double d5 = entityplayer1.getDistanceSq(par1, par3, par5);

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
    public static EntityScent getSenseNodeAtPos(World parWorld, Vec3d parPos, EnumSenseType type) {
    	
    	if (ZAConfig.extraScentCutoffRange == -1) return null;
    	
    	AxisAlignedBB aabb = new AxisAlignedBB(parPos.xCoord, parPos.yCoord, parPos.zCoord, parPos.xCoord + 1, parPos.yCoord + 1, parPos.zCoord + 1);
    	aabb = aabb.expand(ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange);
    	
    	List list = parWorld.getEntitiesWithinAABB(EntityScent.class, aabb);
    	
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
    
    public static EntityScent spawnOrBuffSenseAtPos(World world, Vec3d parPos, EnumSenseType type, int strength) {
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
    public static EntityScent spawnOrBuffSenseAtPos(World world, Vec3d parPos, EnumSenseType type, int strength, boolean frequentSoundMultiply) {
		
    	EntityScent sense = getSenseNodeAtPos(world, parPos, type);
    	
    	if (sense == null) {
    		sense = new EntityScent(world);
    		sense.type = type.ordinal();
	        sense.setPosition(parPos.xCoord, parPos.yCoord, parPos.zCoord);
    		sense.setStrengthPeak(strength);
	        world.spawnEntityInWorld(sense);
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
    
    public static void tryPlayTargetSound(EntityLiving entAlerted, EntityLivingBase entTargetted, Vec3d pos) {
    	if (!lookupLastAlertTime.containsKey(entAlerted) || lookupLastAlertTime.get(entAlerted) + alertDelay < entAlerted.worldObj.getTotalWorldTime()) {
    		if (entAlerted.canEntityBeSeen(entTargetted)) {
				//entAlerted.worldObj.playSound(null, pos.xCoord, pos.yCoord, pos.zCoord, SoundRegistry.get("target"), SoundCategory.HOSTILE, 3F, 0.8F + (entAlerted.worldObj.rand.nextFloat() * 0.2F));
	    		entAlerted.worldObj.playSound(null, entTargetted.posX, entTargetted.posY, entTargetted.posZ, ZAConfigFeatures.soundUseAlternateAlertNoise ? SoundRegistry.get("alert") : SoundRegistry.get("target"), SoundCategory.HOSTILE, (float)ZAConfigFeatures.soundVolumeAlertTarget, ZAConfigFeatures.soundUseAlternateAlertNoise ? 1F : 0.8F + (entAlerted.worldObj.rand.nextFloat() * 0.2F));
				lookupLastAlertTime.put(entAlerted, entAlerted.worldObj.getTotalWorldTime());
				//ZombieAwareness.dbg("!!! alert play for ent: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastAlertTime.size());
    		} else {
    			//likely due to new call for help routine in vanilla, so treat it like investigating until line of sight is made
    			tryPlayInvestigateSound(entAlerted, pos);
    			//ZombieAwareness.dbg("??? tried play alert for no LOS entity: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastAlertTime.size());
    		}
		} else {
			//ZombieAwareness.dbg("already played alert for ent: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastAlertTime.size());
		}
    }
    
    public static void tryPlayInvestigateSound(EntityLiving entAlerted, Vec3d pos) {
    	if (!lookupLastInvestigateTime.containsKey(entAlerted) || lookupLastInvestigateTime.get(entAlerted) + investigateDelay < entAlerted.worldObj.getTotalWorldTime()) {
			entAlerted.worldObj.playSound(null, pos.xCoord, pos.yCoord, pos.zCoord, SoundRegistry.get("investigate"), SoundCategory.HOSTILE, (float)ZAConfigFeatures.soundVolumeInvestigate, 0.7F + (entAlerted.worldObj.rand.nextFloat() * 0.3F));
			lookupLastInvestigateTime.put(entAlerted, entAlerted.worldObj.getTotalWorldTime());
			//ZombieAwareness.dbg("!!! investigate play for ent: " + entAlerted.getEntityId() + ", lookupSize: " + lookupLastInvestigateTime.size());
		}
    }

	public static void tickWorld(World world) {
		/*if (world.getTotalWorldTime() % 40 == 0) {
			for (Entity ent : lookupLastAlertTime)
		}*/
	}
}
