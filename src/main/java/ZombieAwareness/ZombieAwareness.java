package ZombieAwareness;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import modconfig.ConfigMod;
import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.pathfinding.IPFCallback;
import CoroUtil.pathfinding.PFCallbackItem;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import ZombieAwareness.config.ZAConfig;
import ZombieAwareness.config.ZAConfigFeatures;
import ZombieAwareness.config.ZAConfigPlayerLists;
import ZombieAwareness.config.ZAConfigSpawning;

@Mod(modid = "ZAMod", name="Zombie Awareness", version="v1.9.5")
public class ZombieAwareness implements IPFCallback {
	
	@Mod.Instance( value = "ZAMod" )
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
    public static long lastSpawnTime;
    public static long lastSpawnSysTime;
    
    
    
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
    	
    	//sync to forge values
    	configMain.hookUpdatedValues();
    	/*preInitConfig = new Configuration(event.getSuggestedConfigurationFile());

        try
        {
            preInitConfig.load();
            
            ZAConfig.maxPFRange = preInitConfig.get(Configuration.CATEGORY_GENERAL, "maxPFRange", 64).getInt();
            ZAConfig.awareness_Sound = preInitConfig.get(Configuration.CATEGORY_GENERAL, "awareness_Sound", true).getBoolean(true);
            ZAConfig.awareness_Scent = preInitConfig.get(Configuration.CATEGORY_GENERAL, "awareness_Scent", true).getBoolean(true);
            ZAConfig.awareness_Light = preInitConfig.get(Configuration.CATEGORY_GENERAL, "awareness_Light", true).getBoolean(true);
            ZAConfig.sightRange = preInitConfig.get(Configuration.CATEGORY_GENERAL, "sightRange", 16).getInt();
            ZAConfig.omnipotent = preInitConfig.get(Configuration.CATEGORY_GENERAL, "omnipotent", false).getBoolean(false);
            ZAConfig.seeThroughWalls = preInitConfig.get(Configuration.CATEGORY_GENERAL, "seeThroughWalls", false).getBoolean(false);
            ZAConfig.scentStrength = preInitConfig.get(Configuration.CATEGORY_GENERAL, "scentStrength", 60).getInt();
            ZAConfig.soundStrength = preInitConfig.get(Configuration.CATEGORY_GENERAL, "soundStrength", 60).getInt();
            ZAConfig.frequentSoundThreshold = preInitConfig.get(Configuration.CATEGORY_GENERAL, "frequentSoundThreshold", 1000).getInt();
            ZAConfig.wanderingHordes = preInitConfig.get(Configuration.CATEGORY_GENERAL, "wanderingHordes", true).getBoolean(true);
            ZAConfig.noisyZombies = preInitConfig.get(Configuration.CATEGORY_GENERAL, "noisyZombies", true).getBoolean(true);
            ZAConfig.noisyPistons = preInitConfig.get(Configuration.CATEGORY_GENERAL, "noisyPistons", true).getBoolean(true);
            ZAConfig.maxZombiesNight = preInitConfig.get(Configuration.CATEGORY_GENERAL, "maxZombiesNight", 50).getInt();
            ZAConfig.zombieSpawnTickDelay = preInitConfig.get(Configuration.CATEGORY_GENERAL, "zombieSpawnTickDelay", 5).getInt();
            ZAConfig.zombieRandSpeedBoost = preInitConfig.get(Configuration.CATEGORY_GENERAL, "zombieRandSpeedBoost", 5).getInt();
            //tickRateMainLoop = preInitConfig.get(Configuration.CATEGORY_GENERAL, "mainLoopTickRate", 1).getInt();
            ZAConfig.tickRateAILoop = preInitConfig.get(Configuration.CATEGORY_GENERAL, "tickRateAILoop", 30).getInt();
            ZAConfig.tickRatePlayerLoop = preInitConfig.get(Configuration.CATEGORY_GENERAL, "tickRatePlayerLoop", 1).getInt();
            
            
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, e, "Zombie Awareness has a problem loading it's configuration");
        }
        finally
        {
            preInitConfig.save();
        }*/
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	
    	MinecraftForge.EVENT_BUS.register(new ZAEventHandler());
    	FMLCommonHandler.instance().bus().register(new ZAEventHandlerFML());
    	
    	
    	proxy.init(this);
    	
    	//restartApplication();
    	
        //ModLoader.setInGUIHook(this, true, false);
        //ModLoader.setInGameHook(this, true, false);
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandZA());
    }
    
    public ZombieAwareness() {
    	int hm = 0;
    	//TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    }
    
    public void onTick(MinecraftServer var1) {
    	onTickInGame(var1);
    }

    public void onTickInGame(MinecraftServer var1) {
    	
    	//wanderingHordes = true;
    	//sightRange = 16;
        
    	mc = var1;
    	
    	World worldTemp = this.worldRef;
    	
        if (worldTemp != null) {
        	//System.out.println(worldRef.getWorldTime());
	        //if (lastWorldTime != worldRef.getWorldTime()) {
	        	//lastWorldTime = worldRef.getWorldTime();
	        	
        	if (false && tryTropicraft) {
        		worldTemp = mc.worldServerForDimension(-127);
                if (worldTemp != null) {
                	worldTick(worldTemp);
                } else {
                	tryTropicraft = false;
                }
        	}
            
            worldTemp = mc.worldServerForDimension(0);
            if (worldTemp != null) {
            	worldTick(worldTemp);
            }
	        	
	        //}
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
    		
    		int lastCount = 0;
    		
    		boolean spawned = false;
    		
    		Random rand = new Random();
    		
    		//time reset fix
    		if (lastSpawnTime - 1000 > world.getWorldTime()) {
    			lastSpawnTime = 0;
    		}
    		
        	//AI Processing
    		if (world.getWorldTime() % ZAConfig.tickRateAILoop == 0) {
	        	List ents = world.loadedEntityList;
	        	for (int i = 0; i < world.loadedEntityList.size(); i++) {
	        		Entity ent = (Entity)world.loadedEntityList.get(i);
	        		
	        		if (ent instanceof EntityMob && !(ent instanceof ICoroAI) && (
	        				(ZAConfigPlayerLists.blacklistUsedAITick && (EntityList.getEntityString(ent) == null || ((!ZAConfigPlayerLists.forceListUsedAITickAsWhitelist && !ZAConfigPlayerLists.blacklistAITick.toLowerCase().contains(EntityList.getEntityString(ent).toLowerCase())) || (ZAConfigPlayerLists.forceListUsedAITickAsWhitelist && ZAConfigPlayerLists.blacklistAITick.toLowerCase().contains(EntityList.getEntityString(ent).toLowerCase())))
	        						)) || 
	        						(!ZAConfigPlayerLists.blacklistUsedAITick && (!(ent instanceof EntityEnderman) && !(ent instanceof EntityWolf) && !(ent instanceof EntityCreeper) && !(ent instanceof EntityPigZombie)))
	        						)) {
	        			
	        			//if (EntityList.getEntityString(ent) != null) System.out.println(EntityList.getEntityString(ent).toLowerCase());
	        			
	        			ZAUtil.aiTick((EntityLiving)ent);
	        			
	        			if (!((EntityLiving)ent).getNavigator().noPath() && ent.onGround && ent.isCollidedHorizontally) {
	        				//ent.motionY = 0.41999998688697815D;
	        			}
	        			
	        			if ((world.provider.getDimensionId() == 0) && ent instanceof EntityZombie) {
	        				lastCount++;
	        				
	        				if (lastSpawnTime < ent.worldObj.getWorldTime() && !spawned && ent.worldObj.getClosestPlayerToEntity(ent, 32) == null && rand.nextInt(ZAConfigSpawning.maxZombiesNightBaseRarity + (lastZombieCount * 4 / (Math.max(1, ZAConfig.tickRateAILoop)))) == 0) {
	        					if (!ent.worldObj.isDaytime() && lastZombieCount < ZAConfigSpawning.maxZombiesNight && ent.worldObj.canBlockSeeSky(new BlockPos(MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posY), MathHelper.floor_double(ent.posZ))) && ent.worldObj.getLight(new BlockPos(MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posY), MathHelper.floor_double(ent.posZ))) < 5) {
		    						
	        						EntityZombie entZ = new EntityZombie(ent.worldObj);
		    						entZ.setPosition(ent.posX, ent.posY, ent.posZ);
		    						ent.worldObj.spawnEntityInWorld(entZ);
		    						//lastZombieCount = ++lastCount;
		    						
		    						spawned = true;
		    						lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.worldObj.getWorldTime();
		    						lastSpawnSysTime = System.currentTimeMillis();
		    						
		    						if (ZAConfig.debugConsoleSpawns) dbg("Spawned new surface zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
	        					} else if (ZAConfigFeatures.extraSpawningCave && lastZombieCount < ZAConfigSpawning.extraSpawningMaxCount) {
	        						EntityPlayer closestPlayer = CoroUtilEntity.getClosestVulnerablePlayerToEntity(world, ent, ZAConfigSpawning.extraSpawningDistMax);
		        					if (closestPlayer != null && (!ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(closestPlayer))) 
		        							&& closestPlayer.getDistanceSqToEntity(ent) > ZAConfigSpawning.extraSpawningDistMin
		        							&& !ent.worldObj.canBlockSeeSky(new BlockPos(MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posY), MathHelper.floor_double(ent.posZ))) && ent.worldObj.getLight(new BlockPos(MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posY), MathHelper.floor_double(ent.posZ))) < 5) {
		        						
		        						Block id = ent.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(ent.posX), MathHelper.floor_double((ent.getEntityBoundingBox().minY - 0.5D)), MathHelper.floor_double(ent.posZ))).getBlock();
		        						
		        						if (!CoroUtilBlock.isAir(id) && id != Blocks.grass) {
			        						EntityZombie entZ = new EntityZombie(ent.worldObj);
				    						entZ.setPosition(ent.posX, ent.posY, ent.posZ);
				    						ent.worldObj.spawnEntityInWorld(entZ);
				    						
				    						if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(closestPlayer);
				    						
				    						//lastZombieCount = ++lastCount;
				    						
				    						spawned = true;
				    						lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.worldObj.getWorldTime();
				    						lastSpawnSysTime = System.currentTimeMillis();
			        						
			        						if (ZAConfig.debugConsoleSpawns) dbg("Spawned new cave zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
		        						}
		        					}
		        				}
	        				}
	        				
	        				
	        			}
	        		}
	        	}
	        	
	        	
	        	
	        	//if (lastZombieCount != lastCount) System.out.println("lastZombieCount: " + lastZombieCount);
	        	
	        	if (world.provider.getDimensionId() == 0) lastZombieCount = lastCount;
    		}
    	}
    	
    	
    	
    	//Player Processing - for blood visual
    	if (world.getWorldTime() % ZAConfig.tickRatePlayerLoop == 0) {
			for(int i = 0; i < world.playerEntities.size(); i++) {
				EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
				if (player != null) { 
					ZAUtil.playerTick(player);
				}
			}
    	}
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
				
				if (!item.ent.isDead && OldUtil.chunkExists(item.ent.worldObj, (int)item.ent.posX / 16, (int)item.ent.posZ / 16)) item.ent.getNavigator().setPath(item.pe, item.speed);
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
	
	public static void dbg(Object obj) {
		if (ZAConfig.debugConsole) {
			System.out.println(obj);
		}
	}

}
