package ZombieAwareness;

import java.io.File;
import java.util.*;

import ZombieAwareness.config.*;
import modconfig.ConfigMod;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod(modid = ZombieAwareness.modID, name="Zombie Awareness", version=ZombieAwareness.version, dependencies="required-after:coroutil")
public class ZombieAwareness implements IPFCallback {
	
	@Mod.Instance( value = ZombieAwareness.modID )
	public static ZombieAwareness instance;
	public static final String modID = "zombieawareness";
	public static final String version = "${version}";

	//Usefull references
    public static MinecraftServer mc;
    public static World worldRef;
    public static boolean ingui;
    
    public static int lastZombieCount;
    public static int lastMobsCountSurface;
    public static int lastMobsCountCaves;
    public static long lastSpawnTime;
    public static long lastSpawnSysTime;
    
    //public static HashMap<Class, Boolean> lookupClassToEntityTick = new HashMap<Class, Boolean>();
    
    @SidedProxy(clientSide = "ZombieAwareness.ClientProxy", serverSide = "ZombieAwareness.CommonProxy")
    public static CommonProxy proxy;

	public static Configuration config;
	public static String configCategory = "tickable_entities";

	//used mainly for filename placeholder for dynamic config
	public static ZAConfigMobLists mobLists = new ZAConfigMobLists();

	public static HashMap<Class, String> lookupClassToOldName = new HashMap<>();
	public static HashMap<Class, String> lookupClassToRegisteredName = new HashMap<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ZAConfigFeatures configMain = new ZAConfigFeatures();
    	ConfigMod.addConfigFile(event, new ZAConfig());
    	ConfigMod.addConfigFile(event, configMain);
    	ConfigMod.addConfigFile(event, new ZAConfigPlayerLists());
    	ConfigMod.addConfigFile(event, new ZAConfigSpawning());
    	ConfigMod.addConfigFile(event, new ZAConfigClient());

    	
    	//sync to forge values
    	configMain.hookUpdatedValues();

		config = new Configuration(new File("config" + File.separator + mobLists.getConfigFileName() + ".cfg"));
		String comment = "Entities to be enhanced, will show everything ZombieAwareness should be able to enhance, " +
				"only zombie based mobs and some others are default on, if a mob uses ground pathfinding and is able to be hostile you can try enabling more from other mods, " +
				"but how well the enhancing works depends on how the mod implemented their mob";
		config.setCategoryComment(configCategory, comment);
		config.save();
    	
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
		try {
			generateEntityTickList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

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
            
            worldTemp = mc.getWorld(0);
            if (worldTemp != null) {
            	worldTick(worldTemp);
            }
            
        } else {
        	worldRef = mc.getWorld(0);
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
	        		
	        		if (canProcessEntity(ent) && ent instanceof EntityLiving) {
	        			
	        			//if (EntityList.getEntityString(ent) != null) System.out.println(EntityList.getEntityString(ent).toLowerCase());
	        			
	        			ZAUtil.tickAI((EntityLiving)ent);
	        			
	        			if (!((EntityLiving)ent).getNavigator().noPath() && ent.onGround && ent.isCollidedHorizontally) {
	        				//ent.motionY = 0.41999998688697815D;
	        			}
	        			
	        			if ((world.provider.getDimension() == 0) && ent instanceof IMob) {
	        				
	        				int x = MathHelper.floor(ent.posX);
	        				int y = MathHelper.floor(ent.posY);
	        				int z = MathHelper.floor(ent.posZ);
	        				
	        				//not subtracting 0.5 incase of slab
	        				if (ZAUtil.isInDarkCave(world, x, (int)(ent.getEntityBoundingBox().minY - 0.3D), z, false)) {
	        					lastCountMobsCaves++;
	        				} else {
	        					lastCountMobsSurface++;
	        				}
	        				
	        				if (ent instanceof EntityZombie) {
	        					lastCountZombies++;
		        				if (lastSpawnTime < ent.world.getTotalWorldTime() && !spawned && ent.world.getClosestPlayerToEntity(ent, 32) == null && rand.nextInt(ZAConfigSpawning.maxZombiesNightBaseRarity + (lastZombieCount * 4 / (Math.max(1, ZAConfig.tickRateAILoop)))) == 0) {
		        					if (!ent.world.isDaytime() && lastZombieCount < ZAConfigSpawning.maxZombiesNight && ent.world.canSeeSky(new BlockPos(x, y, z)) && ent.world.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {
			    						
		        						EntityZombie entZ = new EntityZombie(ent.world);
			    						entZ.setPosition(ent.posX, ent.posY, ent.posZ);
			    						entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (IEntityLivingData)null);
			    						ZAUtil.giveRandomSpeedBoost(entZ);
			    						ent.world.spawnEntity(entZ);
			    						//lastZombieCount = ++lastCount;
			    						
			    						spawned = true;
			    						lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.world.getTotalWorldTime();
			    						lastSpawnSysTime = System.currentTimeMillis();
			    						
			    						if (ZAConfig.debugConsoleSpawns) dbg("Spawned new surface zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
		        					} else if (ZAConfigFeatures.extraSpawningCave && lastZombieCount < ZAConfigSpawning.maxZombiesNight/*lastZombieCountCaves < ZAConfigSpawning.extraSpawningCavesMaxCount*/) {
		        						EntityPlayer closestPlayer = ent.world.getNearestAttackablePlayer(ent, ZAConfigSpawning.extraSpawningDistMax, ZAConfigSpawning.extraSpawningDistMax);
			        					if (closestPlayer != null && (!ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(closestPlayer))) 
			        							&& closestPlayer.getDistanceSqToEntity(ent) > ZAConfigSpawning.extraSpawningDistMin
			        							&& !ent.world.canSeeSky(new BlockPos(x, y, z)) && ent.world.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {
			        						
			        						//Block id = 
			        						IBlockState state = ent.world.getBlockState(new BlockPos(x, (int)(ent.getEntityBoundingBox().minY - 0.5D), z));
			        						
			        						if (!CoroUtilBlock.isAir(state.getBlock()) && (state.getBlock() != Blocks.GRASS || state.getMaterial() == Material.GRASS)) {
				        						EntityZombie entZ = new EntityZombie(ent.world);
					    						entZ.setPosition(ent.posX, ent.posY, ent.posZ);
					    						entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (IEntityLivingData)null);
					    						ZAUtil.giveRandomSpeedBoost(entZ);
					    						ent.world.spawnEntity(entZ);
					    						
					    						if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(closestPlayer);
					    						
					    						//lastZombieCount = ++lastCount;
					    						
					    						spawned = true;
					    						lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.world.getTotalWorldTime();
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
				
				if (!item.ent.isDead && OldUtil.chunkExists(item.ent.world, MathHelper.floor(item.ent.posX / 16), MathHelper.floor(item.ent.posZ / 16))) {
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

	public static boolean canProcessEntity(Entity ent) {
		if (!canEntityBeProcessedOverride(ent)) {
			return false;
		}
		return canProcessEntity(ent.getClass(), false);
	}

	/**
	 * Looks up and generates config info if entry missing
	 *
	 * @param ent
	 * @param pregen
	 * @return
	 */
	public static boolean canProcessEntity(Class ent, boolean pregen) {

		String entName = getEntityRegisteredName(ent);
		if (ZAUtil.lookupTickableEntities.containsKey(ent))
		{
			return ZAUtil.lookupTickableEntities.get(ent);
		}

		boolean result = false;
		if (canConfigEntity(ent)) {
			if (!pregen) config.load();
			boolean canProcess = getDefaultForEntity(ent);
			result = config.get(configCategory, entName, canProcess).getBoolean(canProcess);
			if (!pregen) config.save();
			ZAUtil.lookupTickableEntities.put(ent, result);
		}

		return result;
	}
	
	/**
	 * Handles special cases for specific instances like if owned and has an owner. But I want that to never be processed anyways
	 * Placeholder for now until more dynamic needs are found
	 * 
	 * @param entity
	 * @return false if you want to cancel processing, true lets it continue with other rules
	 */
	public static boolean canEntityBeProcessedOverride(Entity entity) {
		return true;
		/*boolean result = false;
		if (entity instanceof IEntityOwnable) {
			if (entity.
		}*/
	}

	/**
	 * Used to avoid adding entries to config that cant be used even if set to true
	 *
	 * @param ent
	 * @return
	 */
	public static boolean canConfigEntity(Class ent) {
		return EntityMob.class.isAssignableFrom(ent) || IMob.class.isAssignableFrom(ent);
	}
	
	public static boolean getDefaultForEntity(Class ent) {

		boolean result = false;
		if (canConfigEntity(ent)) {
			if (ent.isAssignableFrom(EntityZombie.class) || ent.isAssignableFrom(EntitySkeleton.class) || ent.isAssignableFrom(EntityWitch.class) || ent.isAssignableFrom(EntitySpider.class)) {
				result = true;
			} else {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Generates list of entities we can process, these are written to config they can modify every entities config after first run
	 *
	 */
	public static void generateEntityTickList() {
		config.load();

		for (Map.Entry<ResourceLocation, EntityEntry> entry : ForgeRegistries.ENTITIES.getEntries()) {

			lookupClassToOldName.put(entry.getValue().getEntityClass(), entry.getValue().getName());
			lookupClassToRegisteredName.put(entry.getValue().getEntityClass(), entry.getKey().toString());

			boolean tickEnt = canProcessEntity(entry.getValue().getEntityClass(), true);
		}

		//pre 1.11.2 way
		/*for (Map.Entry<Class<? extends Entity >, String> entry : EntityList.CLASS_TO_NAME.entrySet()) {
			boolean tickEnt = canProcessEntity(entry.getKey(), true);
    	}*/
		config.save();
	}
	
	public static void dbg(Object obj) {
		if (ZAConfig.debugConsole) {
			System.out.println(obj);
		}
	}

	/**
	 * 1.10.2: only name available
	 * 1.11.2: 'name' field in EntityEntry, new registered name is the snake case key for ForgeRegistries.ENTITIES.getEntries() as a resource location
	 *
	 * @param ent
	 * @return
	 */
	public static String getEntityRegisteredName(Class ent) {
		try {

			return getClassToOldName(ent);
			//return EntityList.getEntityStringFromClass(ent);
		} catch (Exception ex) {
			if (ZAConfig.debugConsole) {
				ex.printStackTrace();
			}
			return ent.getClass().getSimpleName();

		}
	}

	/**
	 * Patch method from 1.11.2 entity registry rework, using new cached lookup
	 *
	 * @param ent
	 * @return
	 */
	public static String getClassToOldName(Class ent) {

		return lookupClassToOldName.get(ent);

	}
}
