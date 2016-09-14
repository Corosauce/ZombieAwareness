package ZombieAwareness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.SoundRegistry;
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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.event.ForgeEventFactory;
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
	 * - must have sound triggers:
	 * - block mine
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
    
    public static HashMap<SoundEvent, Double> lookupSoundToStrengthMultiplier = new HashMap<SoundEvent, Double>();
    
    public static List<SoundEvent> listSoundBlacklist = new ArrayList<SoundEvent>();
    
    public static boolean debug = false;
    
    static {
    	
    	listSoundBlacklist.add(SoundEvents.ENTITY_ARROW_SHOOT);
    	//listSoundBlacklist.add(SoundEvents.BLOCK);
    	
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_GENERIC_EXPLODE, 3D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.BLOCK_PISTON_EXTEND, 2D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1.1D);
    	lookupSoundToStrengthMultiplier.put(SoundEvents.ENTITY_ARROW_HIT, 1.1D);
    }
	
	public static void playerTick(EntityPlayer player) {
    	
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
		
		int lastHealth = lastHealths.containsKey(CoroUtilEntity.getName(player)) ? lastHealths.get(CoroUtilEntity.getName(player)) : 0;
		Long lastBleedTime = lastBleedTimes.containsKey(CoroUtilEntity.getName(player)) ? lastBleedTimes.get(CoroUtilEntity.getName(player)) : 0;
		
        if((int)player.getHealth() != lastHealth) {
            if(player.getHealth() < lastHealth) {
                spawnScent(player);
            }

            lastHealth = (int) player.getHealth();
        }
        
        lastHealths.put(CoroUtilEntity.getName(player), lastHealth);

        if(player.getHealth() / player.getMaxHealth() < 0.6F && lastBleedTime < System.currentTimeMillis()) {
            lastBleedTime = System.currentTimeMillis() + 30000L;
            lastBleedTimes.put(CoroUtilEntity.getName(player), lastBleedTime);
            spawnScent(player);
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
    
    public static void aiTick(EntityLiving ent) {
    	
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
		if (ent.getAttackTarget() == null && (/*ent.worldObj.rand.nextInt(5) == 0 && */time < System.currentTimeMillis() && ent.getNavigator().noPath())) {
			//Find player made senses
			if (!ZAConfig.awareness_Light_OnlyZombies || (ent instanceof EntityZombie)) {
				if (!ZAConfigFeatures.awareness_Light || !ai_FindLightSource(ent)) {
    				//TEMP CODE, THREAD ME SO I CAN SCAN FOR BLOCKS FASTER!!! - ended up designing with minimal scanning, this works ok for now
					if (ent.worldObj.rand.nextInt(3) == 0) {
						ai_FindSense(ent, true);
					}
	    			
    			}
    		} else {
    			if (ai_FindSense(ent)) {
    				
    			}
    		}
    	}
	    	
	    customMobTick(ent);
    }
    
    public static void customMobTick(EntityLivingBase ent) {
    	if (ent instanceof EntitySpider) {
    		if (ent.getPassengers().size() > 0 && ent.getPassengers().get(0) instanceof EntitySkeleton) {
    			if (ent.worldObj.rand.nextInt(100) == 0) {
    				spawnWaypoint(ent);
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
		    					ZombieAwareness.dbg("pathing to lightsource at " + rX + ", " + rY + ", " + rZ + " - " + ent);
			    			}
			    			return true;
	    				}
		    		}
        		}
    		}
    	}
    	
    	return false;
    }
    
    public static boolean ai_FindSense(EntityLivingBase ent) {
    	return ai_FindSense(ent, true);
    }
    
    public static boolean ai_FindSense(EntityLivingBase ent, boolean includeWaypoints) {
    	
        Entity var3 = getScent(ent);

        if(var3 != null) {
        	if (includeWaypoints || ((EntityScent)var3).type != 2) {
        		if (CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)ent, var3, 1)) {
        			ZombieAwareness.dbg("ai_FindSense call, type: " + ((EntityScent)var3).type + " - " + ent.getName() + " -> " + var3.getPosition());
        			return true;
        		}
        	}
        }
        
        return false;
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
    public static Entity getScent(Entity entSource) {
        List<Entity> listEnts = entSource.worldObj.getEntitiesWithinAABBExcludingEntity(entSource, entSource.getEntityBoundingBox().expand((double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense));
        
        Entity entBest = null;
        //double distBest = 999999;

        for(int i = 0; i < listEnts.size(); ++i) {
        	Entity entCheck = listEnts.get(i);

            if (entCheck instanceof EntityScent) {
            	
            	double dist = entSource.getDistanceToEntity(entCheck);
            	
            	//if (dist < distBest) {
		            if (dist < ((EntityScent)entCheck).getRange() && dist > 5.0F && entSource.worldObj.rand.nextInt(20) == 0) {
		                entBest = entCheck;
		                return entBest;
		            }
            	//}
            }
        }

        return entBest;
    }

    public static void soundHook(SoundEvent sound, World world, double x, double y, double z, float volume, float pitch) {
        
    	if (world.isRemote || sound == null) return;
    	
    	if (world.provider.getDimension() != 0 && world.provider.getDimension() != -127) return;
    	
    	String soundName = sound.getSoundName().toString();

    	//System.out.println("Derb: " + var0 + " - TC: " + traceCount);
    	
    	/*if (var0.contains("creeper") || var0.contains("explo")) {
    		System.out.println("Derb: " + var0);
		}*/
    	
        //TEEEEEMMMMMMMMPPPPPPPPP
        if (!ZAConfigFeatures.awareness_Sound/* || traceCount >= maxTraces*/) {
            return;
        }

        if (!canSpawnTrace(world, x, y, z)) {
            return;
        }

        
        
        EntityPlayer var6 = getClosestPlayer(world, x, y, z, 3D);
        int var7 = (int)(20.0F * volume);
        boolean var8 = false;
        /*if(var0.substring(7).equals("drr")) {
           var8 = true;
        }*/

        /*if(soundName.length() > 8+4 && (soundName.substring(7).equals("bow") || soundName.substring(7).equals("pop") || soundName.substring(7).equals("wood"))) {
            return;
        }*/
        
        if (listSoundBlacklist.contains(sound)) {
        	return;
        }

        if((var8 || var6 != null) && var7 > 15) {
            EntityScent var9 = getSenseNodeAtPos(world, new Vec3d(x, y, z), 1);
            
            if (var9 == null) {
            	var9 = new EntityScent(world);
            }
            
            if(var7 < 25) {
                var9.setStrength(ZAConfig.soundStrength);
            }

            var7 = var9.strength;

            if(soundName.substring(7).equals("drr")) {
                var7 += 10;
            }

            if(var9.lastBuffTime + (long)ZAConfig.frequentSoundThreshold > System.currentTimeMillis()) {
            	var9.lastMultiply += 0.1F;
                var7 = (int)((float)var7 * var9.lastMultiply);
            } else {
            	var9.lastMultiply = 1.0F;
            }
            //System.out.println("sound: " + var0 + );
            var9.lastBuffTime = System.currentTimeMillis();
            var9.setStrength(var7);
            var9.type = 1;
            var9.setPosition(x, y, z);
            world.spawnEntityInWorld(var9);
            
            ZombieAwareness.dbg("spawned sound sense from sound: " + soundName);
            
            //System.out.println("sound: " + var0 + " - range: " + var9.getRange());
            //System.out.println(var9.getRange());
        } else {
        	EntityPlayer farPlayer = getClosestPlayer(world, x, y, z, 128D);
            
            if (farPlayer != null) {
            	if(soundName.substring(7).equals("bow") || soundName.substring(7).equals("pop") || soundName.substring(7).equals("wood")) {
                    return;
                }
            	
            	if (ZAConfigFeatures.noisyZombies && soundName.contains("zombie.say")) {
            		if (rand.nextInt(1 + (ZombieAwareness.lastZombieCount * 8)) == 0) {
            			//if (traceCount < maxTraces / 4) {
            				EntityScent es = spawnSoundSense(world, x, y, z, 80);
            				ZombieAwareness.dbg("spawned sound sense from sound: " + soundName);
            			//}
            			//if (es != null) System.out.println("zombie: " + var0 + " - TC: " + traceCount + " - " + (es).getRange());
            		}
            	} else {
            		if(ZAConfigFeatures.noisyPistons && soundName.contains("piston")) {
            			if (rand.nextInt(20) == 0) {
	            			//if (traceCount < maxTraces / 4) {
		            			EntityScent es = spawnSoundSense(world, x, y, z, 400);
		            			if (es != null) {
		            				ZombieAwareness.dbg("spawned sound sense from sound: " + soundName + " - " + es.getRange());
		            			}
		            			//if (es != null) System.out.println("Derbbb: " + var0 + " - TC: " + traceCount + " - " + (es).getRange());
	            			//}
            			}
                    }
            	}
            }
        }
    }
    
    public static void blockEvent(PlayerEvent event, int chance) {
    	
    	if (!ZAConfigFeatures.awareness_Sound) return;
    	
    	if (event.getEntity().worldObj.provider.getDimension() != 0 && event.getEntity().worldObj.provider.getDimension() != -127) return;
    	
    	if (event.getEntityPlayer() == null || (ZAConfigPlayerLists.whiteListUsedSenses && !ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(event.getEntityPlayer())))) return;
    	
	    	if (!event.getEntity().worldObj.isRemote && event.getEntity().worldObj.rand.nextInt(chance) == 0) {
		    	
		    	EntityScent var9 = getSenseNodeAtPos(event.getEntity().worldObj, new Vec3d((double)event.getEntityPlayer().posX, (double)event.getEntityPlayer().posY, (double)event.getEntityPlayer().posZ), 1);
		    	
		    	boolean newNode = false;
		    	
		    	if (var9 == null) {
		    		var9 = new EntityScent(event.getEntity().worldObj);
		    		newNode = true;
		    	}
		    	
		    	int var7;
		
		        var9.setStrength(ZAConfig.soundStrength);
		        
		
		        var7 = var9.strength;
		
		
		        if(var9.lastBuffTime + (long)ZAConfig.frequentSoundThreshold > System.currentTimeMillis()) {
		        	var9.lastMultiply += 0.1F;
		            var7 = (int)((float)var7 * var9.lastMultiply);
		        } else {
		        	var9.lastMultiply = 1.0F;
		        }
		        
		        var9.lastBuffTime = System.currentTimeMillis();
		        var9.setStrength(var7);
		        var9.type = 1;
		        var9.setPosition((double)event.getEntityPlayer().posX, (double)event.getEntityPlayer().posY, (double)event.getEntityPlayer().posZ);
		        event.getEntity().worldObj.spawnEntityInWorld(var9);
		        
		        ZombieAwareness.dbg("spawned sound sense from blockEvent");
		        
		        //System.out.println("sound: mining: " + var9.getRange());
	    	}
    }
    
    public static EntityScent spawnSoundSense(World world, double x, double y, double z, int strength) {
		int size = ZAConfig.soundScentSpawnPosRandom;
		int randX = world.rand.nextInt(size);
		int randZ = world.rand.nextInt(size);
		
    	EntityScent var9 = getSenseNodeAtPos(world, new Vec3d(x + (-(size/2) + randX), y, z + (-(size/2) + randZ)), 1);
    	
    	boolean newNode = false;
    	
    	if (var9 == null) {
    		var9 = new EntityScent(world);
    		newNode = true;
    	}
    	
		var9.setStrength(strength);
		if (newNode) {
			var9.type = 1;
			
	        var9.setPosition(x + (-(size/2) + randX), y, z + (-(size/2) + randZ));
	        
	        world.spawnEntityInWorld(var9);
		}
        return var9;
    }

    public static void spawnScent(Entity var0) {
        if (!ZAConfigFeatures.awareness_Scent/* || traceCount > maxTraces*/) {
            return;
        }

        if (!canSpawnTrace(var0.worldObj, var0.posX, var0.posY, var0.posZ)) {
            return;
        }

        double height = var0.posY/* - (double)var0.yOffset*/ + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        
        
        EntityScent var1 = getSenseNodeAtPos(var0.worldObj, new Vec3d(var0.posX, height, var0.posZ), 0);
        
        boolean newNode = false;
    	
    	if (var1 == null) {
    		var1 = new EntityScent(var0.worldObj);
    		newNode = true;
    	}
        
    	var1.setStrength(ZAConfig.scentStrength);
    	
    	if (newNode) {
	        var1.setPosition(var0.posX, height, var0.posZ);
	        var1.type = 0;
	        
	        var0.worldObj.spawnEntityInWorld(var1);
    	}
        
        
        
        //System.out.println("scent: " + var0 + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
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
    
    public static void spawnWaypoint(Entity var0) {
        if (!ZAConfigFeatures.awareness_Scent/* || traceCount > maxTraces*/) {
            return;
        }
        
        int range = 256;
        
        double tryX = (int)var0.posX - (range/2) + (rand.nextInt(range));
        double tryZ = (int)var0.posZ - (range/2) + (rand.nextInt(range));
        double tryY =  var0.worldObj.getHeight(new BlockPos(tryX, 0, tryZ)).getY();

        if (!canSpawnTrace(var0.worldObj, tryX, tryY, tryZ)) {
            return;
        }

        double height = var0.posY/* - (double)var0.yOffset*/ + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        
        EntityScent var1 = getSenseNodeAtPos(var0.worldObj, new Vec3d(tryX, tryY, tryZ), 2);
        
        boolean newNode = false;
    	
    	if (var1 == null) {
    		var1 = new EntityScent(var0.worldObj);
    		newNode = true;
    	}

        var1.setStrength(60);
        
        if (newNode) {
	        var1.setPosition(tryX, tryY, tryZ);
	        var1.type = 2;
	        
	        var0.worldObj.spawnEntityInWorld(var1);
        }
        
        if (debug) System.out.println("WP: " + var0 + " - range: " + var1.getRange());
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
     * Checks if a scent of the same type is already right at this location
     * 
     * @param parWorld
     * @param parPos
     * @param type
     * @return
     */
    public static EntityScent getSenseNodeAtPos(World parWorld, Vec3d parPos, int type) {
    	
    	if (ZAConfig.extraScentCutoffRange == -1) return null;
    	
    	AxisAlignedBB aabb = new AxisAlignedBB(parPos.xCoord, parPos.yCoord, parPos.zCoord, parPos.xCoord + 1, parPos.yCoord + 1, parPos.zCoord + 1);
    	aabb = aabb.expand(ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange);
    	
    	List list = parWorld.getEntitiesWithinAABB(EntityScent.class, aabb);
    	
    	if (list.size() > 0) {
    		for(int j = 0; j < list.size(); j++)
            {
    			EntityScent node = (EntityScent)list.get(j);
    			if (node.type == type) {
    				return node;
    			}
            }
    	}
    	
    	return null;
    }
}
