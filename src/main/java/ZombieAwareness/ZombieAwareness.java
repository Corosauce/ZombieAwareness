package ZombieAwareness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import modconfig.ConfigMod;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import CoroUtil.OldUtil;
import CoroUtil.pathfinding.IPFCallback;
import CoroUtil.pathfinding.PFCallbackItem;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import ZombieAwareness.config.ZAConfig;
import ZombieAwareness.config.ZAConfigClient;
import ZombieAwareness.config.ZAConfigFeatures;
import ZombieAwareness.config.ZAConfigPlayerLists;
import ZombieAwareness.config.ZAConfigSpawning;

@Mod(modid = "zombieawareness", name="Zombie Awareness", version="v1.20", dependencies="required-after:coroutil")
public class ZombieAwareness implements IPFCallback {
	
	@Mod.Instance( value = "zombieawareness" )
	public static ZombieAwareness instance;
	public static String modID = "zombieawareness";

	//Usefull references
    public static MinecraftServer mc;
    public static World worldRef;
    public static EntityPlayer player;
    //public static World lastWorld;
    public static boolean ingui;
    
    public static boolean openChat = false;
    
    public static long lastWorldTime;
    
    public static boolean tryTropicraft = true;
    
    public static int lastZombieCount;
    public static int lastMobsCountSurface;
    public static int lastMobsCountCaves;
    public static long lastSpawnTime;
    public static long lastSpawnSysTime;
    
    public static HashMap<Class, Boolean> lookupClassToEntityTick = new HashMap<Class, Boolean>();
    
    /*@Override
    public boolean hasClientSide() {
    	return false;
    }*/
    
    /** For use in preInit ONLY */
    public Configuration preInitConfig;
    
    @SidedProxy(clientSide = "ZombieAwareness.ClientProxy", serverSide = "ZombieAwareness.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ZAConfigFeatures configMain = new ZAConfigFeatures();
    	ConfigMod.addConfigFile(event, "zaconfig", new ZAConfig());
    	ConfigMod.addConfigFile(event, "zaconfigfeatures", configMain);
    	ConfigMod.addConfigFile(event, "zaconfigplayerlists", new ZAConfigPlayerLists());
    	ConfigMod.addConfigFile(event, "zaconfigspawning", new ZAConfigSpawning());
    	ConfigMod.addConfigFile(event, "zaconfigclient", new ZAConfigClient());
    	
    	//sync to forge values
    	configMain.hookUpdatedValues();
    	
    	SoundRegistry.init();
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(new ZAEventHandler());
    	
    	proxy.init(this);
    }
    
    @Mod.EventHandler
    public void loadPost(FMLPostInitializationEvent event) {
    	//TODO: move??
    	generateEntityTickList();
    }
    
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandZA());
    }
    
    public ZombieAwareness() {
    	
    }
    
    public void onTick(MinecraftServer var1) {
    	onTickInGame(var1);
    }

    public void onTickInGame(MinecraftServer var1) {
        
    	mc = var1;
    	
    	World worldTemp = this.worldRef;
    	
        if (worldTemp != null) {
            
            worldTemp = mc.worldServerForDimension(0);
            if (worldTemp != null) {
            	worldTick(worldTemp);
            }
            
        } else {
        	worldRef = mc.worldServerForDimension(0);
        	//worldRef.addWorldAccess(new ZAWorldAccess());
        }

        ingui = false;
    }
    
    public void worldTick(World world) {
    	//Only run on master
    	if (!world.isRemote) {
    		
    		manageCallbackQueue();
    		
    		int lastCountZombies = 0;
    		int lastCountMobsSurface = 0;
    		int lastCountMobsCaves = 0;
    		
    		boolean spawned = false;
    		
    		Random rand = new Random();
    		
    		//time reset fix
    		if (lastSpawnTime - 1000 > world.getTotalWorldTime()) {
    			lastSpawnTime = 0;
    		}
    		
        	//AI Processing
    		if (world.getTotalWorldTime() % ZAConfig.tickRateAILoop == 0) {
	        	List ents = world.loadedEntityList;
	        	for (int i = 0; i < world.loadedEntityList.size(); i++) {
	        		Entity ent = world.loadedEntityList.get(i);
	        		
	        		if (shouldTickEntity(ent)) {
	        			
	        			//if (EntityList.getEntityString(ent) != null) System.out.println(EntityList.getEntityString(ent).toLowerCase());
	        			
	        			ZAUtil.tickAI((EntityLiving)ent);
	        			
	        			if (!((EntityLiving)ent).getNavigator().noPath() && ent.onGround && ent.isCollidedHorizontally) {
	        				//ent.motionY = 0.41999998688697815D;
	        			}
	        			
	        			if ((world.provider.getDimension() == 0) && ent instanceof IMob) {
	        				
	        				int x = MathHelper.floor_double(ent.posX);
	        				int y = MathHelper.floor_double(ent.posY);
	        				int z = MathHelper.floor_double(ent.posZ);
	        				
	        				//not subtracting 0.5 incase of slab
	        				if (ZAUtil.isInDarkCave(world, x, (int)(ent.getEntityBoundingBox().minY - 0.3D), z, false)) {
	        					lastCountMobsCaves++;
	        				} else {
	        					lastCountMobsSurface++;
	        				}
	        				
	        				if (ent instanceof EntityZombie) {
	        					lastCountZombies++;
		        				if (lastSpawnTime < ent.worldObj.getTotalWorldTime() && !spawned && ent.worldObj.getClosestPlayerToEntity(ent, 32) == null && rand.nextInt(ZAConfigSpawning.maxZombiesNightBaseRarity + (lastZombieCount * 4 / (Math.max(1, ZAConfig.tickRateAILoop)))) == 0) {
		        					if (!ent.worldObj.isDaytime() && lastZombieCount < ZAConfigSpawning.maxZombiesNight && ent.worldObj.canSeeSky(new BlockPos(x, y, z)) && ent.worldObj.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {
			    						
		        						EntityZombie entZ = new EntityZombie(ent.worldObj);
			    						entZ.setPosition(ent.posX, ent.posY, ent.posZ);
			    						entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (IEntityLivingData)null);
			    						ZAUtil.giveRandomSpeedBoost(entZ);
			    						ent.worldObj.spawnEntityInWorld(entZ);
			    						//lastZombieCount = ++lastCount;
			    						
			    						spawned = true;
			    						lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.worldObj.getTotalWorldTime();
			    						lastSpawnSysTime = System.currentTimeMillis();
			    						
			    						if (ZAConfig.debugConsoleSpawns) dbg("Spawned new surface zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
		        					} else if (ZAConfigFeatures.extraSpawningCave && lastZombieCount < ZAConfigSpawning.maxZombiesNight/*lastZombieCountCaves < ZAConfigSpawning.extraSpawningCavesMaxCount*/) {
		        						EntityPlayer closestPlayer = ent.worldObj.getNearestAttackablePlayer(ent, ZAConfigSpawning.extraSpawningDistMax, ZAConfigSpawning.extraSpawningDistMax);
			        					if (closestPlayer != null && (!ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(closestPlayer))) 
			        							&& closestPlayer.getDistanceSqToEntity(ent) > ZAConfigSpawning.extraSpawningDistMin
			        							&& !ent.worldObj.canSeeSky(new BlockPos(x, y, z)) && ent.worldObj.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {
			        						
			        						//Block id = 
			        						IBlockState state = ent.worldObj.getBlockState(new BlockPos(x, (int)(ent.getEntityBoundingBox().minY - 0.5D), z));
			        						
			        						if (!CoroUtilBlock.isAir(state.getBlock()) && (state.getBlock() != Blocks.GRASS || state.getMaterial() == Material.GRASS)) {
				        						EntityZombie entZ = new EntityZombie(ent.worldObj);
					    						entZ.setPosition(ent.posX, ent.posY, ent.posZ);
					    						entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (IEntityLivingData)null);
					    						ZAUtil.giveRandomSpeedBoost(entZ);
					    						ent.worldObj.spawnEntityInWorld(entZ);
					    						
					    						if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(closestPlayer);
					    						
					    						//lastZombieCount = ++lastCount;
					    						
					    						spawned = true;
					    						lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.worldObj.getTotalWorldTime();
					    						lastSpawnSysTime = System.currentTimeMillis();
				        						
				        						if (ZAConfig.debugConsoleSpawns) dbg("Spawned new cave zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
			        						}
			        					}
			        				}
		        				}
	        				}
	        				
	        			}
	        		}
	        	}
	        	
	        	
	        	
	        	//if (lastZombieCount != lastCount) System.out.println("lastZombieCount: " + lastZombieCount);
	        	
	        	if (world.provider.getDimension() == 0) {
	        		lastZombieCount = lastCountZombies;
	        		lastMobsCountSurface = lastCountMobsSurface;
	        		lastMobsCountCaves = lastCountMobsCaves;
	        	}
    		}
    	}
    	
    	
    	
    	//Player Processing - for blood visual
    	if (world.getTotalWorldTime() % ZAConfig.tickRatePlayerLoop == 0) {
			for(int i = 0; i < world.playerEntities.size(); i++) {
				EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
				if (player != null) { 
					ZAUtil.tickPlayer(player);
				}
			}
    	}
    	
    	ZAUtil.tickWorld(world);
    }
    
    
    public static int timeout;
    public static String msg;
    public static int color;
    public static int defaultColor = 16777215;
    
    public static void displayMessage(String var0, int var1) {
        msg = var0;
        timeout = 85;
        color = var1;
    }

    public static void displayMessage(String var0) {
        displayMessage(var0, defaultColor);
    }

    public static boolean keyDownLastTick = false;
    public static boolean heldItemLastTick = false;

    public static boolean toggle = false;

    public ArrayList<PFCallbackItem> callbackList = new ArrayList<PFCallbackItem>();
    
	@Override
	public synchronized void pfComplete(PFCallbackItem ci) {
		//System.out.println("callback: " + ci.ent);
		callbackList.add(ci);
	}

	@Override
	public void manageCallbackQueue() {
		ArrayList<PFCallbackItem> list = getQueue();
		
		try {
			for (int i = 0; i < list.size(); i++) {
				PFCallbackItem item = list.get(i);
				
				//lazy fix
				/*if (item.speed == 0.23F) {
					item.speed = 1F;
				}*/
				
				if (!item.ent.isDead && OldUtil.chunkExists(item.ent.worldObj, MathHelper.floor_double(item.ent.posX / 16), MathHelper.floor_double(item.ent.posZ / 16))) {
					item.ent.getNavigator().setPath(item.pe, item.speed);
				}
			}
		} catch (Exception ex) {
			System.out.println("Crash in ZA Callback Item manager");
			ex.printStackTrace();
		}
		
		//if (list.size() > 0) System.out.println("cur list size: "  + list.size());
		
		list.clear();
	}

	@Override
	public ArrayList<PFCallbackItem> getQueue() {
		return callbackList;
	}
	
	public static boolean shouldTickEntity(Entity ent) {
		
		if (!canEntityBeProcessedOverride(ent)) {
			return false;
		}
		
		/**
		 * list is default on, used for enderman, etc
		 * 
		 * if using list override
		 * - if entry exists
		 * -- return value
		 * - if entry doesnt exist
		 * -- generate default value based on non list rules
		 * 
		 * for generating or if not using list:
		 * - usual pile of if statements we used
		 * 
		 */
		
		return ent instanceof EntityMob && (
		(ZAConfigPlayerLists.blacklistUsedAITick && 
		(EntityList.getEntityString(ent) == null || 
		((!ZAConfigPlayerLists.forceListUsedAITickAsWhitelist && !ZAConfigPlayerLists.blacklistAITick.toLowerCase().contains(EntityList.getEntityString(ent).toLowerCase())) || 
		(ZAConfigPlayerLists.forceListUsedAITickAsWhitelist && ZAConfigPlayerLists.blacklistAITick.toLowerCase().contains(EntityList.getEntityString(ent).toLowerCase()))))) || 
		(!ZAConfigPlayerLists.blacklistUsedAITick && (!(ent instanceof EntityEnderman) && !(ent instanceof EntityWolf) && !(ent instanceof EntityCreeper) && !(ent instanceof EntityPigZombie)))
		);
	}
	
	/**
	 * Handles special cases for specific instances like if owned and has an owner. But I want that to never be processed anyways
	 * Placeholder for now until more dynamic needs are found
	 * 
	 * @param ent
	 * @return false if you want to cancel processing, true lets it continue with other rules
	 */
	public static boolean canEntityBeProcessedOverride(Entity entity) {
		return true;
		/*boolean result = false;
		if (entity instanceof IEntityOwnable) {
			if (entity.
		}*/
	}
	
	public static boolean getDefaultForEntity(Class ent) {
		boolean result = false;
		if (EntityMob.class.isAssignableFrom(ent)) {
			if (EntityZombie.class.isAssignableFrom(ent) || EntitySkeleton.class.isAssignableFrom(ent) || EntityWitch.class.isAssignableFrom(ent) || EntitySpider.class.isAssignableFrom(ent)) {
				result = true;
			} else {
				result = false;
			}
			/*if (ent instanceof EntityEnderman || ent instanceof EntityWolf || ent instanceof EntityCreeper || ent instanceof EntityPigZombie || ent instanceof EntityWither) {
				result = false;
			} else {
				result = true;
			}*/
		}
		return result;
	}
	
	public static void generateEntityTickList() {
		for (Map.Entry<Class<? extends Entity >, String> entry : EntityList.CLASS_TO_NAME.entrySet()) {
			boolean tickEnt = getDefaultForEntity(entry.getKey());
			lookupClassToEntityTick.put(entry.getKey(), tickEnt);
    		//System.out.println(entry.getKey() + " - " + tickEnt);
    	}
	}
	
	public static void dbg(Object obj) {
		if (ZAConfig.debugConsole) {
			System.out.println(obj);
		}
	}

}
