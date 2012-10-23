package ZombieAwareness;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import CoroAI.PFQueue;
import CoroAI.entity.*;
import net.minecraft.src.*;

public class ZAUtil {
	
	public static long traceCount = 0;
    public static int lastHealth;
    public static long lastBleedTime;
    public static long lastSoundTime;
    public static float lastMultiply = 1.0F;
    
    public static Random rand = new Random();
	
	public static void playerTick(EntityPlayer player) {
    	
		if (mod_ZombieAwareness.wanderingHordes) {
			if (rand.nextInt(100) == 0) {
				spawnWaypoint(player);
			}
		}
		
        if(player.getHealth() != lastHealth) {
            if(player.getHealth() < lastHealth) {
                spawnScent(player);
            }

            lastHealth = player.getHealth();
        }

        if(player.getHealth() < 12 && lastBleedTime < System.currentTimeMillis()) {
            lastBleedTime = System.currentTimeMillis() + 30000L;
            spawnScent(player);
        }
    }
    
    public static void moveHelper(EntityLiving ent) {
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
    
    public static void huntTarget(EntityLiving ent, EntityLiving targ, int pri) {
		PFQueue.getPath(ent, targ, mod_ZombieAwareness.maxPFRange, pri);
		//System.out.println("huntTarget call: " + ent + " -> " + targ);
		ent.setAttackTarget(targ);
		//setState(EnumActState.FIGHTING);
	}
	
	public static void huntTarget(EntityLiving ent, EntityLiving targ) {
		huntTarget(ent, targ, 0);
	}
    
    public static boolean isEnemy(Entity ent, Entity targ) {
    	if (targ instanceof EntityLiving) {
			if (targ instanceof EntityPlayer) {
				if (!((EntityPlayer) targ).capabilities.isCreativeMode) {
					return true;
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
    	
    	if (ent instanceof c_EnhAI) return;
    	
    	//if (ent.rand.nextInt(10) == 0) {
    		if (!(ent instanceof EntityCreeper)) ai_FindTarget(ent);
    	
    	
	    	if (ent.getAttackTarget() == null && (ent.getNavigator().getPath() == null || ent.getNavigator().getPath().isFinished())) {
	    		ai_FindSense(ent);
	    	}
    	//}
	    	
	    customMobTick(ent);
    	
    	moveHelper(ent);
    }
    
    public static void customMobTick(EntityLiving ent) {
    	if (ent instanceof EntitySpider) {
    		if (ent.riddenByEntity != null && ent.riddenByEntity instanceof EntitySkeleton) {
    			//setAge(ent, 0);
    			//setAge((EntityLiving)ent.riddenByEntity, 0);
    			
    			if (ent.worldObj.rand.nextInt(100) == 0) {
    				spawnWaypoint(ent);
    			}
    		}
    	}
    }
    
    public static void setAge(EntityLiving ent, int age) {
    	try {
    		setPrivateValue(EntityLiving.class, ent, "bq", age);
    	} catch (Exception ex) {
    		try {
    			setPrivateValue(EntityLiving.class, ent, "entityAge", age);
    		} catch (Exception ex2) {
    			ex2.printStackTrace();
    		}
    	}
    }
    
    public static void ai_FindSense(EntityLiving ent) {
    	
        Entity var3 = getScent(ent);

        if(var3 != null) {
        	
        	int curAge = ent.getAge();
        	
        	setAge(ent, curAge/2);
        	//ent.entityAge = 0;
            PFQueue.getPath(ent, var3, 64F/*ent.getDistanceToEntity(var3) + 32F*/);
            //PFQueue.getPath(ent, mc.thePlayer, maxPFRange);
            //System.out.println("ai_FindSense call: " + ent + " -> " + ((EntityScent)var3).type);
        }
        
    }
    
    public static void ai_FindTarget(EntityLiving ent) {
    	long huntRange = mod_ZombieAwareness.sightRange;
    	//maxPFRange = 128;
    	
    	if (/*ent.getHealth() > ent.getMaxHealth() * 0.90F && */(ent.getAttackTarget() == null || ent.worldObj.rand.nextInt(100) == 0)) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(isEnemy(ent, entity1))
	            {
	            	if (mod_ZombieAwareness.seeThroughWalls || ((EntityLiving) entity1).canEntityBeSeen(ent)) {
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
	        	huntTarget(ent, (EntityLiving)clEnt);
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
    }
    
    public static Entity getScent(Entity var0) {
        List var1 = var0.worldObj.getEntitiesWithinAABBExcludingEntity(var0, var0.boundingBox.expand((double)mod_ZombieAwareness.maxPFRange, (double)mod_ZombieAwareness.maxPFRange, (double)mod_ZombieAwareness.maxPFRange));
        Entity var2 = null;
        Entity var3 = null;
        Object var4 = null;
        float var5 = 90000.0F;
        float var6 = 90000.0F;
        boolean var7 = false;

        for(int var8 = 0; var8 < var1.size(); ++var8) {
            var2 = (Entity)var1.get(var8);

            if (var2 instanceof EntityScent) {
            	//System.out.println("Ent: " + var0 + " | " + var0.posX + " | " + var0.getDistanceToEntity(var2) + " | " + ((EntityScent)var2).getRange());
	            if(var0.getDistanceToEntity(var2) < ((EntityScent)var2).getRange() && var0.getDistanceToEntity(var2) > 5.0F && var0.worldObj.rand.nextInt(20) == 0) {
	                var3 = var2;
	                
	            }
            }
        }

        return var3;
    }

    public static void soundHook(String var0, World world, float var1, float var2, float var3, float var4, float var5) {
        

    	//System.out.println("Derb: " + var0 + " - TC: " + traceCount);
    	
        //TEEEEEMMMMMMMMPPPPPPPPP
        if (!mod_ZombieAwareness.awareness_Scent || traceCount >= 75) {
            return;
        }

        if (!canSpawnTrace(world, (int)var1, (int)var2, (int)var3)) {
            return;
        }

        
        
        EntityPlayer var6 = world.getClosestPlayer(var1, var2, var3, 3D);//ModLoader.getMinecraftInstance().thePlayer;
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

            if(var7 < 25) {
                var9.setStrength(mod_ZombieAwareness.soundStrength);
            }

            var7 = var9.strength;

            if(var0.substring(7).equals("drr")) {
                var7 += 10;
            }

            if(lastSoundTime + (long)mod_ZombieAwareness.frequentSoundThreshold > System.currentTimeMillis()) {
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
        	EntityPlayer farPlayer = world.getClosestPlayer(var1, var2, var3, 128D);
            
            if (farPlayer != null) {
            	if(var0.substring(7).equals("bow") || var0.substring(7).equals("pop") || var0.substring(7).equals("wood")) {
                    return;
                }
            	
            	if (var0.contains("zombie")/*var7 > 15*/) {
            		if (rand.nextInt(20) == 0) {
            			EntityScent es = spawnSoundSense(world, var1, var2, var3, 80);
            			//if (es != null) System.out.println("Derbbb: " + var0 + " - TC: " + traceCount + " - " + (es).getRange());
            		}
            	} else {
            		if(var0.contains("piston")) {
            			if (rand.nextInt(40) == 0) {
	            			if (traceCount < 25) {
		            			EntityScent es = spawnSoundSense(world, var1, var2, var3, 100);
		            			//if (es != null) System.out.println("Derbbb: " + var0 + " - TC: " + traceCount + " - " + (es).getRange());
	            			}
            			}
                    }
            	}
            }
        }
    }
    
    public static EntityScent spawnSoundSense(World world, float x, float y, float z, int strength) {
    	if (traceCount < 50) { //safety so it doesnt hog
			EntityScent var9 = new EntityScent(world);
			var9.setStrength(strength);
			var9.type = 1;
			
			int size = 10;
			int randX = var9.worldObj.rand.nextInt(size);
			int randZ = var9.worldObj.rand.nextInt(size);
			
            var9.setPosition((double)x + (-(size/2) + randX), (double)y, (double)z + (-(size/2) + randZ));
            world.spawnEntityInWorld(var9);
            return var9;
		}
    	return null;
    }

    public static void spawnScent(Entity var0) {
        if (!mod_ZombieAwareness.awareness_Scent || traceCount > 75) {
            return;
        }

        if (!canSpawnTrace(var0.worldObj, (int)var0.posX, (int)var0.posY, (int)var0.posZ)) {
            return;
        }

        double height = var0.posY - (double)var0.yOffset + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        EntityScent var1 = new EntityScent(var0.worldObj);
        var1.setPosition(var0.posX, height, var0.posZ);
        var0.worldObj.spawnEntityInWorld(var1);
        var1.setStrength(mod_ZombieAwareness.scentStrength);
        var1.type = 0;
        //System.out.println("scent: " + var0 + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
    }
    
    public static void spawnWaypoint(Entity var0) {
        if (!mod_ZombieAwareness.awareness_Scent || traceCount > 75) {
            return;
        }
        
        int range = 256;
        
        int tryX = (int)var0.posX - (range/2) + (rand.nextInt(range));
        int tryZ = (int)var0.posZ - (range/2) + (rand.nextInt(range));
        int tryY = var0.worldObj.getHeightValue(tryX, tryZ);

        if (!canSpawnTrace(var0.worldObj, tryX, tryY, tryZ)) {
            return;
        }

        double height = var0.posY - (double)var0.yOffset + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        EntityScent var1 = new EntityScent(var0.worldObj);
        var1.setPosition(tryX, tryY, tryZ);
        var0.worldObj.spawnEntityInWorld(var1);
        var1.setStrength(60);
        var1.type = 2;
        //System.out.println("WP: " + var0 + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
    }

    public static boolean canSpawnTrace(World world, int x, int y, int z) {
        int id = Block.pressurePlatePlanks.blockID;

        if (world.getBlockId(x-1,y,z) == id) {
            return false;
        }
        return true;
    }
    
    public static void setPrivateValueBoth(Class var0, Object var1, String obf, String mcp, Object var3)
    {
        try
        {
            try
            {
                ModLoader.setPrivateValue(var0, var1, obf, var3);
            }
            catch (Exception ex)
            {
                ModLoader.setPrivateValue(var0, var1, mcp, var3);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static Object getPrivateValueBoth(Class var0, Object var1, String obf, String mcp)
    {
        try
        {
            try
            {
                return getPrivateValue(var0, var1, obf);
            }
            catch (Exception ex)
            {
                return getPrivateValue(var0, var1, mcp);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getPrivateValue(Class var0, Object var1, String var2) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field var3 = var0.getDeclaredField(var2);
            var3.setAccessible(true);
            return var3.get(var1);
        }
        catch (IllegalAccessException var4)
        {
            ModLoader.throwException("An impossible error has occured!", var4);
            return null;
        }
    }

    static Field field_modifiers = null;

    public static void setPrivateValue(Class var0, Object var1, int var2, Object var3) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            Field var4 = var0.getDeclaredFields()[var2];
            var4.setAccessible(true);
            int var5 = field_modifiers.getInt(var4);

            if ((var5 & 16) != 0)
            {
                field_modifiers.setInt(var4, var5 & -17);
            }

            var4.set(var1, var3);
        }
        catch (IllegalAccessException var6)
        {
            //logger.throwing("ModLoader", "setPrivateValue", var6);
            //throwException("An impossible error has occured!", var6);
        }
    }

    public static void setPrivateValue(Class var0, Object var1, String var2, Object var3) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            if (field_modifiers == null)
            {
                field_modifiers = Field.class.getDeclaredField("modifiers");
                field_modifiers.setAccessible(true);
            }

            Field var4 = var0.getDeclaredField(var2);
            int var5 = field_modifiers.getInt(var4);

            if ((var5 & 16) != 0)
            {
                field_modifiers.setInt(var4, var5 & -17);
            }

            var4.setAccessible(true);
            var4.set(var1, var3);
        }
        catch (IllegalAccessException var6)
        {
            //logger.throwing("ModLoader", "setPrivateValue", var6);
            //throwException("An impossible error has occured!", var6);
        }
    }
}
