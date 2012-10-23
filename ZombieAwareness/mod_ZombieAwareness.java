package ZombieAwareness;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/*import zombiecraft.GameLogic.WaveManager;
import zombiecraft.GameLogic.ZCGame;*/

import java.util.Random;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import java.util.Map;


import CoroAI.PFQueue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import ZombieAwareness.ZAWorldAccess;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityEnderman;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.MLProp;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;

@Mod(modid = "ZAMod", name="Zombie Awareness", version="v1.1 for MC v1.3.2")
public class mod_ZombieAwareness
    implements Runnable {

	//Usefull references
    public static MinecraftServer mc;
    public static World worldRef;
    public static EntityPlayer player;
    public static World lastWorld;
    public static boolean ingui;
    
    public static boolean openChat = false;
    
    public static long lastWorldTime;
    
    public static long maxPFRange = 64; //also max awareness range
    
    public static boolean awareness_Sound = true;
    public static boolean awareness_Scent = true;
    
    //Sights
    public static long sightRange = 16;
    public static boolean omnipotent = false;
    public static boolean seeThroughWalls = false;
    public static int scentStrength = 40;
    public static int soundStrength = 40;
    public static int frequentSoundThreshold = 1000;
    
    public static boolean wanderingHordes = true;
    
    /*@Override
    public boolean hasClientSide() {
    	return false;
    }*/
    
    /** For use in preInit ONLY */
    public Configuration preInitConfig;

    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
    	preInitConfig = new Configuration(event.getSuggestedConfigurationFile());

        try
        {
            preInitConfig.load();
            
            maxPFRange = preInitConfig.getOrCreateIntProperty("maxPFRange", Configuration.CATEGORY_GENERAL, 64).getInt();
            awareness_Sound = preInitConfig.getOrCreateBooleanProperty("awareness_Sound", Configuration.CATEGORY_GENERAL, true).getBoolean(true);
            awareness_Scent = preInitConfig.getOrCreateBooleanProperty("awareness_Scent", Configuration.CATEGORY_GENERAL, true).getBoolean(true);
            sightRange = preInitConfig.getOrCreateIntProperty("sightRange", Configuration.CATEGORY_GENERAL, 16).getInt();
            omnipotent = preInitConfig.getOrCreateBooleanProperty("omnipotent", Configuration.CATEGORY_GENERAL, false).getBoolean(false);
            seeThroughWalls = preInitConfig.getOrCreateBooleanProperty("seeThroughWalls", Configuration.CATEGORY_GENERAL, false).getBoolean(false);
            scentStrength = preInitConfig.getOrCreateIntProperty("scentStrength", Configuration.CATEGORY_GENERAL, 60).getInt();
            soundStrength = preInitConfig.getOrCreateIntProperty("soundStrength", Configuration.CATEGORY_GENERAL, 60).getInt();
            frequentSoundThreshold = preInitConfig.getOrCreateIntProperty("frequentSoundThreshold", Configuration.CATEGORY_GENERAL, 1000).getInt();
            wanderingHordes = preInitConfig.getOrCreateBooleanProperty("wanderingHordes", Configuration.CATEGORY_GENERAL, false).getBoolean(false);
            
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, e, "Zombie Awareness has a problem loading it's configuration");
        }
        finally
        {
            preInitConfig.save();
        }
    }
    
    @Init
    public void load(FMLInitializationEvent event)
    {
    	
    	mc = ModLoader.getMinecraftServerInstance();
    	
    	(new Thread(this)).start();
    	
    	//ModLoader.registerEntityID(EntityScent.class, "EntityScent", ModLoader.getUniqueEntityId());
    	
        //ModLoader.setInGUIHook(this, true, false);
        //ModLoader.setInGameHook(this, true, false);
    }
    
    //public HashMap<World, Boolean> worldsAddedTo;

    public void run() {
        try {
            while(true) {
                if(mc == null) {
                    mc = ModLoader.getMinecraftServerInstance();
                }

                if(mc == null) {
                    Thread.sleep(1000L);
                } else {
                    if(mc.worldServerForDimension(0) == null) {
                        Thread.sleep(1000L);
                    } else {
                        if (lastWorld != worldRef) {
                            //worldSaver = null;
                            lastWorld = worldRef;
                            /*worldRef = mc.worldServerForDimension(127);
                            if (worldRef != null) {
                            	worldRef.addWorldAccess(new ZAWorldAccess(worldRef));
                            }*/
                            worldRef = mc.worldServerForDimension(0);
                            worldRef.addWorldAccess(new ZAWorldAccess(worldRef));
                            ZAUtil.traceCount = 0;
                            System.out.println("add world access");
                            //iMan = null; //auto resets in zcgamesp when ready
                            //getFXLayers();
                        }

                        worldRef = mc.worldServerForDimension(0);
                        
                        //player = mc.thePlayer;
                        Thread.sleep(1000L);
                    }
                }
            }
        } catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    //public static mod_ZombieAwareness i = new mod_ZombieAwareness();
    
    public mod_ZombieAwareness() {
    	int hm = 0;
    	TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
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
	        	
        		/*worldTemp = mc.worldServerForDimension(127);
                if (worldTemp != null) {
                	worldTick(worldTemp);
                }*/
                
                worldTemp = mc.worldServerForDimension(0);
                if (worldTemp != null) {
                	worldTick(worldTemp);
                }
	        	
	        //}
        } else {
        	worldTemp = mc.worldServerForDimension(0);
        	//worldRef.addWorldAccess(new ZAWorldAccess());
        }

        ingui = false;
    }
    
    public void worldTick(World world) {
    	//Only run on master
    	if (!world.isRemote) {
        	//AI Processing
        	List ents = world.loadedEntityList;
        	for (int i = 0; i < world.loadedEntityList.size(); i++) {
        		Entity ent = (Entity)world.loadedEntityList.get(i);
        		
        		if (ent instanceof EntityMob && !(ent instanceof EntityEnderman) && !(ent instanceof EntityWolf)/* && !(ent instanceof EntityCreeper)*/ && !(ent instanceof EntityPigZombie)) {
        			ZAUtil.aiTick((EntityLiving)ent);
        		}
        	}
    	}
    	
    	//Player Processing - run on ssp and smp, for blood visual
    	List<EntityPlayer> players = new LinkedList();
		for(int i = 0; i < world.playerEntities.size(); i++) {
			EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
			if (player != null) { 
				ZAUtil.playerTick(player);
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

}
