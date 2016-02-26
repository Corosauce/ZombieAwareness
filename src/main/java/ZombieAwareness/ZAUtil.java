package ZombieAwareness;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import CoroUtil.OldUtil;
//import CoroUtil.pathfinding.PFQueue;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilPath;
import ZombieAwareness.config.ZAConfig;
import ZombieAwareness.config.ZAConfigFeatures;
import ZombieAwareness.config.ZAConfigPlayerLists;
import ZombieAwareness.config.ZAConfigSpawning;

public class ZAUtil {
	
	public static long traceCount = 0;
    //public static int lastHealth;
    //public static long lastBleedTime;
	
	//these 2 are only used for each player, but i cant exactly backtrack to the player that makes the sound so its shared, shouldnt be too horrible...
    public static long lastSoundTime;
    public static float lastMultiply = 1.0F;
    
    
    public static Random rand = new Random();
    
    //public static int maxTraces = 400;
    
    public static HashMap<String, Integer> lastHealths = new HashMap();
    public static HashMap<String, Long> lastBleedTimes = new HashMap();
    //public static HashMap<String, Long> lastSoundTimes;
    //public static HashMap<String, Integer> lastMultiplies;
    
    public static boolean debug = false;
	
	public static void playerTick(EntityPlayer player) {
    	
		if (ZAConfigFeatures.wanderingHordes) {
			if (rand.nextInt(100) == 0) {
				spawnWaypoint(player);
			}
		}
		
		if (ZombieAwareness.lastZombieCount < ZAConfigSpawning.extraSpawningMaxCount && !ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(player))) {
			if (ZAConfigFeatures.extraSpawningSurface) {
				 if (!player.worldObj.isDaytime()) {
					 if (rand.nextInt(Math.max(1, ZAConfigSpawning.extraSpawningRandomPool)) == 0) {
						 spawnNewMobSurface(player);
					 }
				 }
			}
			
			if (ZAConfigFeatures.extraSpawningCave) {
				//we need existing zombies for this, caves always have something, for now depend on existing ones, do better cave spawning with hostile worlds tech
				//code in ZombieAwareness class
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
    
    public static void moveHelper(EntityLiving ent) {
    	
    	//TEMP!
    	//Float.valueOf(c_CoroAIUtil.getPrivateValueBoth(EntityLivingBase.class, (EntityLivingBase)ent, c_CoroAIUtil.refl_obf_Item_moveSpeed, c_CoroAIUtil.refl_mcp_Item_moveSpeed).toString());
    	
    	if (ZAConfig.zombieRandSpeedBoost > 0 && ent instanceof EntityZombie && !EntityList.getEntityString(ent).contains("BrainyZombie")) {
	    	float moveSpeed = OldUtil.getMoveSpeed(ent);
	    	
	    	if (moveSpeed == 0.23F) {
	    		float newSpeed = moveSpeed + (ent.worldObj.rand.nextInt(ZAConfig.zombieRandSpeedBoost) / 70F);
	    		
	    		//new attribute adding code needed here, checks for existing attribute, if it doesnt have it adds one with a random speed buff
	    		
	    		//ObfuscationReflectionHelper.setPrivateValue(EntityLivingBase.class, ent, c_CoroAIUtil.refl_obf_Item_moveSpeed, newSpeed);
	    		//c_CoroAIUtil.setMoveSpeed(ent, newSpeed);
	    		//c_CoroAIUtil.setPrivateValueBoth(EntityLivingBase.class, ent, c_CoroAIUtil.refl_obf_Item_moveSpeed, c_CoroAIUtil.refl_mcp_Item_moveSpeed, newSpeed);
	    		//c_CoroAIUtil.setPrivateValueBoth(EntityLivingBase.class, (EntityLivingBase)ent, ObfuscationReflectionHelper.remapFieldNames("EntityLivingBase", c_CoroAIUtil.refl_obf_Item_moveSpeed), c_CoroAIUtil.refl_mcp_Item_moveSpeed, newSpeed);
	    	}
	    	
	    	//force fixing ai speed sets
	    	ent.getNavigator().setSpeed(moveSpeed);
	
	    	//System.out.println(moveSpeed);
    	}
    	
    	
    	if (ent.isInWater()) {
			//if (ent.jumpDelay == 0) {
				//if (entityToAttack != null) {
					//faceEntity(entityToAttack,30F,30F);
					//this.getMoveHelper().setMoveTo(entityToAttack.posX, entityToAttack.posY, entityToAttack.posZ, this.getMoveHelper().getSpeed());
				//}
				//jump();
				//this.jumpDelay = 30;
				ent.motionY += 0.03D;
				
				//pathfollow fix
				if (true) {
					if (ent.getNavigator().getPath() != null) {
						PathEntity pEnt = ent.getNavigator().getPath();
						int index = pEnt.getCurrentPathIndex()+1;
						//index--;
						if (index < 0) index = 0;
						if (index > pEnt.getCurrentPathLength()) index = pEnt.getCurrentPathLength()-1;
						Vec3 var1 = null;
						try {
							//var1 = pEnt.getVectorFromIndex(this, index);
							//var1 = pEnt.getVectorFromIndex(this, pEnt.getCurrentPathLength()-1);
							//if (pEnt.getCurrentPathLength() > 2) {
								var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathIndex());
							//}
						} catch (Exception ex) {
							//System.out.println("c_EnhAI water pf err");
							//ex.printStackTrace();
							if (pEnt.getCurrentPathLength() > 0) {
								var1 = pEnt.getVectorFromIndex(ent, pEnt.getCurrentPathLength()-1);
							}
						}
	
		                if (var1 != null)
		                {
		                	ent.getMoveHelper().setMoveTo(var1.xCoord, var1.yCoord, var1.zCoord, 0.53F);
		                	double dist = ent.getDistance(var1.xCoord, var1.yCoord, var1.zCoord);
		                	if (dist < 8) {
		                		//System.out.println("dist to node: " + dist);
		                		
		                	}
		                	if (dist < 2) {
		                		ent.getNavigator().getPath().incrementPathIndex();
		                	}
		                    //
		                }
					}
				}
				
				
				
			//}
		}
    }
    
    public static void huntTarget(EntityLivingBase ent, EntityLivingBase targ, int pri) {
		//PFQueue.getPath(ent, targ, ZAConfig.maxPFRange, pri, ZombieAwareness.instance);
    	//1.8 change from pfqueue to longdistance mini pather
		if (ent instanceof EntityCreature) {
        	CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)ent, targ, 1);
        } else {
        	System.out.println("trying to pathfind a non entity creature, what?!");
        }
		//System.out.println("huntTarget call: " + ent + " -> " + targ);
		if (ent instanceof EntityLiving) ((EntityLiving)ent).setAttackTarget(targ);
		//setState(EnumActState.FIGHTING);
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
					} else if (ZAConfigPlayerLists.whiteListUsedOmnipotent) {
						if (ZAConfigPlayerLists.whitelistOmnipotentTargettedPlayers.contains(CoroUtilEntity.getName(((EntityPlayer) targ)))) {
							if (ZAConfig.debugConsoleOmnipotent) ZombieAwareness.dbg(CoroUtilEntity.getName((EntityPlayer) targ) + " targetting omnipotently by " + ent);
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
    	
    	//if (ent.rand.nextInt(10) == 0) {
    		//if (!(ent instanceof EntityCreeper)) {
    	
    	//A more performance friendly omnipotence, only runs it when no target, but still allows for smaller ranged retargetting
    	if (ent.worldObj.getTotalWorldTime() % 40 == 0) {
	    	if (ZAConfig.omnipotent && ent.getAttackTarget() == null) {
	    		ai_FindTarget(ent, true);
	    	} else {
	    		ai_FindTarget(ent, false);
	    	}
    	}
    	
    	//1.8: turned off for switch to other pathfinder
		/*if (PFQueue.instance == null) {
    		new PFQueue(ent.worldObj);
    	}
		long time = 0;
		try {
			if (PFQueue.pfDelays.containsKey(ent)) {
				time = (Long)PFQueue.pfDelays.get(ent);
			}
		} catch (Exception ex) {
			
		}*/
    	
    	//1.8: this code used to depend on pfdelays, lets just stagger it by 5 seconds now
		if (ent.getAttackTarget() == null && (ent.worldObj.getTotalWorldTime() % 20 == 0 && ent.getNavigator().noPath())) {
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
    	
	    //System.out.println("ZA MOVEHELPER OFF");
    	//moveHelper(ent);
    }
    
    public static void customMobTick(EntityLivingBase ent) {
    	if (ent instanceof EntitySpider) {
    		if (ent.riddenByEntity != null && ent.riddenByEntity instanceof EntitySkeleton) {
    			//setAge(ent, 0);
    			//setAge((EntityLivingBase)ent.riddenByEntity, 0);
    			
    			if (ent.worldObj.rand.nextInt(100) == 0) {
    				spawnWaypoint(ent);
    			}
    		}
    	}
    }
    
    public static void setAge(EntityLivingBase ent, int age) {
    	try {
    		OldUtil.setPrivateValueBoth(EntityLivingBase.class, ent, "field_70708_bq", "entityAge", age);
    	} catch (Exception ex) {
    		ex.printStackTrace();
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
        			
		    		int rX = MathHelper.floor_double(entP.posX + (rand.nextInt(size) - (size/2)));
		    		int rY = MathHelper.floor_double(entP.posY + (rand.nextInt(size/2) - (size/4)));
		    		int rZ = MathHelper.floor_double(entP.posZ + (rand.nextInt(size) - (size/2)));
		    		
		    		int lightValue = entP.worldObj.getLightFromNeighbors(new BlockPos(rX, rY, rZ));
		    		//Block id = ent.worldObj.getBlock(rX, rY, rZ);
		    		
		    		if (lightValue > 4) {
		    			if ((ent.getDistanceToEntity(entP) > 64 && ent.worldObj.rand.nextInt(20) == 0) || ent.worldObj.rayTraceBlocks(new Vec3(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), new Vec3(rX, rY, rZ)) == null) {
		    				/*if (PFQueue.getPath(ent, rX, rY, rZ, 128F, 0, ZombieAwareness.instance)) {
			    				if (debug) System.out.println("pathing to lightsource");
			    			}*/
		    				//1.8 change from pfqueue to longdistance mini pather
		    				if (ent instanceof EntityCreature) {
		                    	if (CoroUtilPath.tryMoveToXYZLongDist((EntityCreature)ent, rX, rY, rZ, 1)) {
		                    		if (debug) System.out.println("pathing to lightsource");
		                    	}
		                    } else {
		                    	System.out.println("trying to pathfind a non entity creature, what?!");
		                    }
			    			return true;
	    				}
		    		}
        		}
	    		
	    		//Vec3 lookBlock = findLitBlock(ent, 0, 5, false);
	    		
	    		//if (lookBlock != null) {
	    			//rX = (int)lookBlock.xCoord;
	    			//rY = (int)lookBlock.yCoord+1;
	    			//rZ = (int)lookBlock.zCoord;
	    			
	    			//int id = ent.worldObj.getBlockId(rX, rY, rZ);
	    			
	    			
	    			
	    			/*System.out.println("block id look: " + id);
	    			System.out.println("detected light value: " + lightValue);*/
		    		
		    		//
		    				//System.out.println("detected light value: " + lightValue);
		    			
		    			
		    		//}
	    		//}
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
        		int curAge = ent.getAge();
            	
        		//TEMP OFF
            	//setAge(ent, curAge/2);
            	//ent.entityAge = 0;
        		
                //PFQueue.getPath(ent, var3, 64F, 0, ZombieAwareness.instance/*ent.getDistanceToEntity(var3) + 32F*/);
        		//1.8 change from pfqueue to longdistance mini pather
                if (ent instanceof EntityCreature) {
                	CoroUtilPath.tryMoveToEntityLivingLongDist((EntityCreature)ent, var3, 1);
                } else {
                	System.out.println("trying to pathfind a non entity creature, what?!");
                }
                
                
                //PFQueue.getPath(ent, mc.thePlayer, maxPFRange);
                //if (debug) System.out.println("ai_FindSense call, type: " + ((EntityScent)var3).type + " - " + ent + " -> ");
                return true;
        	}
        }
        
        return false;
    }
    
    public static boolean ai_FindTarget(EntityLiving ent, boolean omnipotent) {
    	long huntRange = ZAConfig.sightRange;
    	//maxPFRange = 128;
    	
    	if (omnipotent) huntRange = 512;
    	
    	if (/*ent.getHealth() > ent.getMaxHealth() * 0.90F && */(ent.getAttackTarget() == null || ent.worldObj.rand.nextInt(100) == 0)) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.getEntityBoundingBox().expand(huntRange, huntRange/2, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(isEnemy(ent, entity1, omnipotent))
	            {
	            	if (omnipotent || (ZAConfig.seeThroughWalls || ((EntityLivingBase) entity1).canEntityBeSeen(ent))) {
	            		if (sanityCheck(ent, entity1)/* && entity1 instanceof EntityPlayer*/) {
	            			float dist = ent.getDistanceToEntity(entity1);
	            			if (dist < closest) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
		            		
		            		//found = true;
		            		//break;
	            		}
	            		//this.hasAttacked = true;
	            		//getPathOrWalkableBlock(entity1, 16F);
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	huntTarget(ent, (EntityLivingBase)clEnt);
	        	return true;
	        }
	        /*if (!found) {
	        	setState(EnumKoaActivity.IDLE);
	        }*/
		} else {
			
			//if (ent.entityToAttack != null) {
				//if (!ent.hasPath() && ent.getDistanceToEntity(ent.entityToAttack) > 5F) {
					//PFQueue.getPath(ent, ent.entityToAttack, ent.maxPFRange);
				//}
			//}
			
		}
    	return false;
    }
    
    public static Entity getScent(Entity var0) {
        List var1 = var0.worldObj.getEntitiesWithinAABBExcludingEntity(var0, var0.getEntityBoundingBox().expand((double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense, (double)ZAConfig.maxPFRangeSense));
        Entity var2 = null;
        Entity var3 = null;
        Object var4 = null;
        float var5 = 90000.0F;
        float var6 = 90000.0F;
        boolean var7 = false;

        for(int var8 = 0; var8 < var1.size(); ++var8) {
            var2 = (Entity)var1.get(var8);

            if (var2 instanceof EntityScent) {
            	
	            if(var0.getDistanceToEntity(var2) < ((EntityScent)var2).getRange() && var0.getDistanceToEntity(var2) > 5.0F && var0.worldObj.rand.nextInt(20) == 0) {
	                var3 = var2;
	                //if (((EntityScent) var2).type == 0) {
	                	//if (debug) System.out.println("scent found by ent: " + var0 + " | " + var0.posX + " | " + var0.getDistanceToEntity(var2) + " | " + ((EntityScent)var2).getRange());
	                //}
	            }
            }
        }

        return var3;
    }

    public static void soundHook(String var0, World world, float var1, float var2, float var3, float var4, float var5) {
        
    	
    	
    	if (world.isRemote || var0 == null) return;
    	
    	if (world.provider.getDimensionId() != 0 && world.provider.getDimensionId() != -127) return;

    	//System.out.println("Derb: " + var0 + " - TC: " + traceCount);
    	
    	if (var0.contains("pop")) {
    		//System.out.println("Derb: " + var0 + " - TC: " + traceCount);
		}
    	
        //TEEEEEMMMMMMMMPPPPPPPPP
        if (!ZAConfigFeatures.awareness_Sound/* || traceCount >= maxTraces*/) {
            return;
        }

        if (!canSpawnTrace(world, MathHelper.floor_double(var1), MathHelper.floor_double(var2), MathHelper.floor_double(var3))) {
            return;
        }

        
        
        EntityPlayer var6 = getClosestPlayer(world, var1, var2, var3, 3D);//ModLoader.getMinecraftInstance().thePlayer;
        int var7 = (int)(20.0F * var4);
        boolean var8 = false;
        /*if(var0.substring(7).equals("drr")) {
           var8 = true;
        }*/

        try {
	        if(var0.substring(7).equals("bow") || var0.substring(7).equals("pop") || var0.substring(7).equals("wood")) {
	            return;
	        }
        } catch (Exception ex) {
        	return; // bad string length
        }

        if((var8 || var6 != null) && var7 > 15) {
            EntityScent var9 = new EntityScent(world);

            if (!canSpawnScentHere(world, new Vec3((double)var1, (double)var2, (double)var3))) {
	    		return;
	    	}
            
            if(var7 < 25) {
                var9.setStrength(ZAConfig.soundStrength);
            }

            var7 = var9.strength;

            if(var0.substring(7).equals("drr")) {
                var7 += 10;
            }

            if(lastSoundTime + (long)ZAConfig.frequentSoundThreshold > System.currentTimeMillis()) {
                lastMultiply += 0.1F;
                var7 = (int)((float)var7 * lastMultiply);
            } else {
                lastMultiply = 1.0F;
            }
            //System.out.println("sound: " + var0 + );
            lastSoundTime = System.currentTimeMillis();
            var9.setStrength(var7);
            var9.type = 1;
            var9.setPosition((double)var1, (double)var2, (double)var3);
            world.spawnEntityInWorld(var9);
            
            //System.out.println("sound: " + var0 + " - range: " + var9.getRange());
            //System.out.println(var9.getRange());
        } else {
        	EntityPlayer farPlayer = getClosestPlayer(world, var1, var2, var3, 128D);
            
            if (farPlayer != null) {
            	if(var0.substring(7).equals("bow") || var0.substring(7).equals("pop") || var0.substring(7).equals("wood")) {
                    return;
                }
            	
            	if (ZAConfigFeatures.noisyZombies && var0.contains("zombie.say")) {
            		if (rand.nextInt(1 + (ZombieAwareness.lastZombieCount * 8)) == 0) {
            			//if (traceCount < maxTraces / 4) {
            				EntityScent es = spawnSoundSense(world, var1, var2, var3, 80);
            			//}
            			//if (es != null) System.out.println("zombie: " + var0 + " - TC: " + traceCount + " - " + (es).getRange());
            		}
            	} else {
            		if(ZAConfigFeatures.noisyPistons && var0.contains("piston")) {
            			if (rand.nextInt(40) == 0) {
	            			//if (traceCount < maxTraces / 4) {
		            			EntityScent es = spawnSoundSense(world, var1, var2, var3, 100);
		            			//if (es != null) System.out.println("Derbbb: " + var0 + " - TC: " + traceCount + " - " + (es).getRange());
	            			//}
            			}
                    }
            	}
            }
        }
    }
    
    public static void blockEvent(PlayerEvent event, int chance) {
    	
    	if (event.entity.worldObj.provider.getDimensionId() != 0 && event.entity.worldObj.provider.getDimensionId() != -127) return;
    	
    	if (event.entityPlayer == null || (ZAConfigPlayerLists.whiteListUsedSenses && !ZAConfigPlayerLists.whitelistSenses.contains(CoroUtilEntity.getName(event.entityPlayer)))) return;
    	
    	//if (traceCount < maxTraces * 0.75) {
	    	if (!event.entity.worldObj.isRemote && event.entity.worldObj.rand.nextInt(chance) == 0) {
		    	EntityScent var9 = new EntityScent(event.entity.worldObj);
		    	
		    	if (!canSpawnScentHere(event.entity.worldObj, new Vec3((double)event.entityPlayer.posX, (double)event.entityPlayer.posY, (double)event.entityPlayer.posZ))) {
		    		return;
		    	}
		    	
		    	int var7;
		
		        var9.setStrength(ZAConfig.soundStrength);
		        
		
		        var7 = var9.strength;
		
		
		        if(lastSoundTime + (long)ZAConfig.frequentSoundThreshold > System.currentTimeMillis()) {
		            lastMultiply += 0.1F;
		            var7 = (int)((float)var7 * lastMultiply);
		        } else {
		            lastMultiply = 1.0F;
		        }
		        
		        lastSoundTime = System.currentTimeMillis();
		        var9.setStrength(var7);
		        var9.type = 1;
		        var9.setPosition((double)event.entityPlayer.posX, (double)event.entityPlayer.posY, (double)event.entityPlayer.posZ);
		        event.entity.worldObj.spawnEntityInWorld(var9);
		        
		        //System.out.println("sound: mining: " + var9.getRange());
	    	}
    	//}
    }
    
    public static EntityScent spawnSoundSense(World world, float x, float y, float z, int strength) {
    	//if (traceCount < maxTraces * 0.75) { //safety so it doesnt hog
			EntityScent var9 = new EntityScent(world);
			var9.setStrength(strength);
			var9.type = 1;
			
			int size = ZAConfig.soundScentSpawnPosRandom;
			int randX = var9.worldObj.rand.nextInt(size);
			int randZ = var9.worldObj.rand.nextInt(size);
			
            var9.setPosition((double)x + (-(size/2) + randX), (double)y, (double)z + (-(size/2) + randZ));
            
            if (!canSpawnScentHere(world, new Vec3((double)x + (-(size/2) + randX), (double)y, (double)z + (-(size/2) + randZ)))) {
	    		return null;
	    	}
            
            world.spawnEntityInWorld(var9);
            return var9;
		//}
    	//return null;
    }

    public static void spawnScent(Entity var0) {
        if (!ZAConfigFeatures.awareness_Scent/* || traceCount > maxTraces*/) {
            return;
        }

        if (!canSpawnTrace(var0.worldObj, MathHelper.floor_double(var0.posX), MathHelper.floor_double(var0.posY), MathHelper.floor_double(var0.posZ))) {
            return;
        }

        double height = var0.posY/* - (double)var0.yOffset*/ + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        EntityScent var1 = new EntityScent(var0.worldObj);
        
        if (!canSpawnScentHere(var0.worldObj, new Vec3(var0.posX, height, var0.posZ))) {
    		return;
    	}
        
        var1.setPosition(var0.posX, height, var0.posZ);
        var1.setStrength(ZAConfig.scentStrength);
        var1.type = 0;
        
        var0.worldObj.spawnEntityInWorld(var1);
        
        //System.out.println("scent: " + var0 + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
    }
    
    public static void spawnNewMobSurface(EntityLivingBase var0) {
        
        int range = 256;
        int minDist = ZAConfigSpawning.extraSpawningDistMin;
        int maxDist = ZAConfigSpawning.extraSpawningDistMax;
        
        for (int tries = 0; tries < 5; tries++) {
	        int tryX = MathHelper.floor_double(var0.posX) - (range/2) + (rand.nextInt(range));
	        int tryZ = MathHelper.floor_double(var0.posZ) - (range/2) + (rand.nextInt(range));
	        int tryY = var0.worldObj.getHeight(new BlockPos(tryX, 0, tryZ)).getY();
	
	        if (var0.getDistance(tryX, tryY, tryZ) < minDist || var0.getDistance(tryX, tryY, tryZ) > maxDist || !canSpawnMob(var0.worldObj, tryX, tryY, tryZ) || var0.worldObj.getLightFromNeighbors(new BlockPos(tryX, tryY, tryZ)) >= 6) {
	            continue;
	        }
	
	        EntityZombie entZ = new EntityZombie(var0.worldObj);
			entZ.setPosition(tryX, tryY, tryZ);
			var0.worldObj.spawnEntityInWorld(entZ);
			
			if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(var0);
			
	        if (ZAConfig.debugConsoleSpawns) ZombieAwareness.dbg("spawnNewMobSurface: " + tryX + ", " + tryY + ", " + tryZ);
	        
	        return;
        }
    }
    
    public static void spawnNewMobCave(Entity var0) {
        
        int range = 256;
        
        for (int tries = 0; tries < 5; tries++) {
	        int tryX = MathHelper.floor_double(var0.posX) - (range/2) + (rand.nextInt(range));
	        int tryZ = MathHelper.floor_double(var0.posZ) - (range/2) + (rand.nextInt(range));
	        int tryY = var0.worldObj.getHeight(new BlockPos(tryX, 0, tryZ)).getY();
	
	        if (!canSpawnTrace(var0.worldObj, tryX, tryY, tryZ)) {
	            continue;
	        }
	
	        EntityZombie entZ = new EntityZombie(var0.worldObj);
			entZ.setPosition(var0.posX, var0.posY, var0.posZ);
			var0.worldObj.spawnEntityInWorld(entZ);
			
	        if (ZAConfig.debugConsoleSpawns) ZombieAwareness.dbg("spawnNewMobCave: " + var0.posX + ", " + var0.posY + ", " + var0.posZ);
	        
	        return;
        }
    }
    
    public static boolean canSpawnMob(World world, int x, int y, int z) {
        Block id = world.getBlockState(new BlockPos(x-1,y,z)).getBlock();//Block.pressurePlatePlanks.blockID;

        /*if (id == Block.grass.blockID || id == Block.stone.blockID || id == Block.tallGrass.blockID || id == Block.grass.blockID || id == Block.sand.blockID) {
            return true;
        }*/
        if (!CoroUtilBlock.isAir(id) && id.getMaterial() == Material.leaves) {
        	return false;
        }
        return true;
    }
    
    public static void spawnWaypoint(Entity var0) {
        if (!ZAConfigFeatures.awareness_Scent/* || traceCount > maxTraces*/) {
            return;
        }
        
        int range = 256;
        
        int tryX = MathHelper.floor_double(var0.posX) - (range/2) + (rand.nextInt(range));
        int tryZ = MathHelper.floor_double(var0.posZ) - (range/2) + (rand.nextInt(range));
        int tryY = var0.worldObj.getHeight(new BlockPos(tryX, 0, tryZ)).getY();

        if (!canSpawnTrace(var0.worldObj, tryX, tryY, tryZ)) {
            return;
        }

        double height = var0.posY - /*(double)var0.yOffset*/ + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        EntityScent var1 = new EntityScent(var0.worldObj);
        
        if (!canSpawnScentHere(var0.worldObj, new Vec3(tryX, tryY, tryZ))) {
    		return;
    	}
        
        var1.setPosition(tryX, tryY, tryZ);
        var1.setStrength(60);
        var1.type = 2;
        
        var0.worldObj.spawnEntityInWorld(var1);
        if (debug) System.out.println("WP: " + var0 + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
    }

    public static boolean canSpawnTrace(World world, int x, int y, int z) {

        if (world.getBlockState(new BlockPos(x-1,y,z)).getBlock().getMaterial() == Material.circuits) {
            return false;
        }
        return true;
    }
    
    public static Vec3 findLitBlock(EntityLivingBase ent, int yOffset, float factor, boolean noYaw) {
    	
    	//temp test override
    	//ent = FMLClientHandler.instance().getClient().thePlayer;
    	
    	try {
	    	EntityLivingBase entityliving = ent;
	    	boolean foundLit = false;
	    	Vec3 foundVec = null;
	    	int tryPhase = 1;
	    	while (tryPhase < 5 && !foundLit) {
	    		float f = factor * tryPhase;
	    		float lookdist = factor * tryPhase;
	    		int randY = ent.worldObj.rand.nextInt(10)-5;
		        float f1 = entityliving.prevRotationPitch + (entityliving.rotationPitch - entityliving.prevRotationPitch) * lookdist;
		    	float f3 = entityliving.prevRotationYaw + (entityliving.rotationYaw - entityliving.prevRotationYaw) * lookdist;
		    	if (noYaw) f3 = 0.00001F;
		        //int i = (int)Math.floor((double)(f3 / 90F) + 0.5D);
		        //f3 = (float)i * 90F;
		        double d = entityliving.prevPosX + (entityliving.posX - entityliving.prevPosX) * (double)f;
		        double d1 = ((entityliving.prevPosY + (entityliving.posY - entityliving.prevPosY) * (double)f + 1.62D))/* - (double)entityliving.yOffset*/ + yOffset + randY;
		        double d2 = entityliving.prevPosZ + (entityliving.posZ - entityliving.prevPosZ) * (double)f;
		        Vec3 vec3d = new Vec3(d, d1, d2);
		        float f4 = MathHelper.cos(-f3 * 0.01745329F - 3.141593F);
		        float f5 = MathHelper.sin(-f3 * 0.01745329F - 3.141593F);
		        float f6 = -MathHelper.cos(-f1 * 0.01745329F - 0.7853982F);
		        float f7 = MathHelper.sin(-f1 * 0.01745329F - 0.7853982F);
		        float f8 = f5 * f6;
		        float f9 = f7;
		        float f10 = f4 * f6;
		        //entityliving.info = f3;
		        double d3 = 2.0D;
		        Vec3 vec3d1 = vec3d.addVector((double)f8 * d3, (double)f9 * d3, (double)f10 * d3);              // \/ water collide check
		        int lightLevel = ent.worldObj.getLightFromNeighbors(new BlockPos(MathHelper.floor_double(vec3d1.xCoord), MathHelper.floor_double(vec3d1.yCoord), MathHelper.floor_double(vec3d1.zCoord)));
		        if (lightLevel > 4) {
		        	//System.out.println("test light check: " + lightLevel + " - phase: " + tryPhase + " - dist check: " + ent.getDistance(vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord));
		        	MovingObjectPosition movingobjectposition = entityliving.worldObj.rayTraceBlocks(new Vec3(ent.posX, ent.posY+1, ent.posZ), vec3d1, true);
		        	
		        	//if (movingobjectposition == null || (movingobjectposition.blockX == (int)vec3d1.xCoord && movingobjectposition.blockY == (int)vec3d1.yCoord && movingobjectposition.blockZ == (int)vec3d1.zCoord)) {
			        	//System.out.println("test 2 light check: " + lightLevel + " - " + tryPhase);
			        	
			        	foundLit = true;
			        	foundVec = vec3d1;
		        	//}
		        }
		        tryPhase++;
	    	}
	    	
	        if (foundLit) {
	        	return foundVec;
	        }
	        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
		return null;
    }
    
    public static MovingObjectPosition getAimBlock(EntityLivingBase ent, int yOffset, float dist, boolean noYaw) {
    	
    	//if (true) return null;
    	try {
	    	EntityLivingBase entityliving = ent;
	    	float f = dist;
	        float f1 = entityliving.prevRotationPitch + (entityliving.rotationPitch - entityliving.prevRotationPitch) * f;
	    	float f3 = entityliving.prevRotationYaw + (entityliving.rotationYaw - entityliving.prevRotationYaw) * f;
	    	if (noYaw) f3 = 0.00001F;
	        //int i = (int)Math.floor((double)(f3 / 90F) + 0.5D);
	        //f3 = (float)i * 90F;
	        double d = entityliving.prevPosX + (entityliving.posX - entityliving.prevPosX) * (double)f;
	        double d1 = ((entityliving.prevPosY + (entityliving.posY - entityliving.prevPosY) * (double)f + 1.6200000000000001D))/* - (double)entityliving.yOffset*/ + yOffset;
	        double d2 = entityliving.prevPosZ + (entityliving.posZ - entityliving.prevPosZ) * (double)f;
	        Vec3 vec3d = new Vec3(d, d1, d2);
	        float f4 = MathHelper.cos(-f3 * 0.01745329F - 3.141593F);
	        float f5 = MathHelper.sin(-f3 * 0.01745329F - 3.141593F);
	        float f6 = -MathHelper.cos(-f1 * 0.01745329F - 0.7853982F);
	        float f7 = MathHelper.sin(-f1 * 0.01745329F - 0.7853982F);
	        float f8 = f5 * f6;
	        float f9 = f7;
	        float f10 = f4 * f6;
	        //entityliving.info = f3;
	        double d3 = 2.0D;
	        Vec3 vec3d1 = vec3d.addVector((double)f8 * d3, (double)f9 * d3, (double)f10 * d3);              // \/ water collide check
	        int lightLevel = ent.worldObj.getLightFromNeighbors(new BlockPos(MathHelper.floor_double(vec3d1.xCoord), MathHelper.floor_double(vec3d1.yCoord), MathHelper.floor_double(vec3d1.zCoord)));
	        if (lightLevel > 4) {
	        	//System.out.println("test light check: " + lightLevel);
	        }
	        MovingObjectPosition movingobjectposition = entityliving.worldObj.rayTraceBlocks(vec3d, vec3d1, true);
	
	        int id = -1;
	        
	        if(movingobjectposition == null) {
	            return null;
	        }
	        
	        return movingobjectposition;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
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

    public static boolean canSpawnScentHere(World parWorld, Vec3 parPos) {

    	if (!ZAConfigFeatures.awareness_Sound) return false;
    	
    	if (ZAConfig.extraScentCutoffRange == -1) return true;
    	
    	AxisAlignedBB aabb = new AxisAlignedBB(parPos.xCoord, parPos.yCoord, parPos.zCoord, parPos.xCoord + 1, parPos.yCoord + 1, parPos.zCoord + 1);
    	aabb = aabb.expand(ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange, ZAConfig.extraScentCutoffRange);
    	
    	List list = parWorld.getEntitiesWithinAABB(EntityScent.class, aabb);
    	
    	if (list.size() > 0) {
    		//System.out.println("returning false");
    		return false;
    	}
    	
        /*for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
        }*/
    	
    	return true;
    }
}
