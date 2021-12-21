package ZombieAwareness;

import CoroUtil.OldUtil;
import CoroUtil.forge.CULog;
import CoroUtil.pathfinding.IPFCallback;
import CoroUtil.pathfinding.PFCallbackItem;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import ZombieAwareness.config.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod(modid = ZombieAwarenessOld.modID, name="Zombie Awareness", version= ZombieAwarenessOld.version, dependencies="required-after:coroutil")
public class ZombieAwarenessOld implements IPFCallback {
	
	@Mod.Instance( value = ZombieAwarenessOld.modID )
	public static ZombieAwarenessOld instance;
	public static final String modID = "zombieawareness";
	public static final String version = "${version}";
    
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
    	/*ZAConfigFeatures configMain = new ZAConfigFeatures();
    	ConfigMod.addConfigFile(event, new ZAConfig());
    	ConfigMod.addConfigFile(event, configMain);
    	ConfigMod.addConfigFile(event, new ZAConfigPlayerLists());
    	ConfigMod.addConfigFile(event, new ZAConfigSpawning());
    	ConfigMod.addConfigFile(event, new ZAConfigClient());*/

    	
    	//sync to forge values
    	//configMain.hookUpdatedValues();

		/*config = new Configuration(new File("config" + File.separator + mobLists.getConfigFileName() + ".cfg"));
		String comment = "Entities to be enhanced, will show everything ZombieAwareness should be able to enhance, " +
				"only zombie based mobs and some others are default on, if a mob uses ground pathfinding and is able to be hostile you can try enabling more from other mods, " +
				"but how well the enhancing works depends on how the mod implemented their mob";
		config.setCategoryComment(configCategory, comment);
		config.save();*/
    	
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
    
    public ZombieAwarenessOld() {
    	
    }
    
    public void onTick() {
    	onTickInGame();
    }

    public void onTickInGame() {
    	
    	World world = DimensionManager.getWorld(0);
    	
        if (world != null) {
			tickOverworld(world);
        }
    }
    
    public void tickOverworld(World world) {
    	if (world.isRemote) return;

    	manageCallbackQueue();

		ZAUtil.trackProfile();
    }

    public static void tickEntity(LivingEntity ent) {

		boolean spawned = false;

		Random rand = new Random();

		World world = ent.world;
		int dimID = world.provider.getDimension();

		//stagger ticking by entity id
		if ((world.getTotalWorldTime() + ent.getEntityId()) % ZAConfig.tickRateAILoop == 0) {
			if (canProcessEntity(ent) && ent instanceof MobEntity) {

				ZAUtil.tickAI((MobEntity)ent);

				if ((dimID == 0) && ent instanceof IMob) {

					int x = MathHelper.floor(ent.posX);
					int y = MathHelper.floor(ent.posY);
					int z = MathHelper.floor(ent.posZ);

					if (ent instanceof ZombieEntity) {
						if (ZAUtil.getWorldData(dimID).lastSpawnTime < ent.world.getTotalWorldTime() && !spawned && ent.world.getClosestPlayerToEntity(ent, 32) == null && rand.nextInt(ZAConfigSpawning.maxZombiesNightBaseRarity + (ZAUtil.getWorldData(dimID).lastZombieCount * 4 / (Math.max(1, ZAConfig.tickRateAILoop)))) == 0) {
							if (!ent.world.isDaytime() && ZAUtil.getWorldData(dimID).lastZombieCount < ZAConfigSpawning.maxZombiesNight && ent.world.canSeeSky(new BlockPos(x, y, z)) && ent.world.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {

								CULog.dbg("spawning extra zombie clone, dim " + dimID + ", last count: " + ZAUtil.getWorldData(dimID).lastZombieCount);

								ZombieEntity entZ = new ZombieEntity(ent.world);
								entZ.setPosition(ent.posX, ent.posY, ent.posZ);
								entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (ILivingEntityData)null);
								ZAUtil.giveRandomSpeedBoost(entZ);
								ent.world.spawnEntity(entZ);
								//lastZombieCount = ++lastCount;

								spawned = true;
								ZAUtil.getWorldData(dimID).lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.world.getTotalWorldTime();
								ZAUtil.getWorldData(dimID).lastSpawnSysTime = System.currentTimeMillis();

								if (ZAConfig.debugConsoleSpawns) dbg("Spawned new surface zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
							} else if (ZAConfigFeatures.extraSpawningCave && ZAUtil.getWorldData(dimID).lastZombieCount < ZAConfigSpawning.maxZombiesNight/*lastZombieCountCaves < ZAConfigSpawning.extraSpawningCavesMaxCount*/) {
								PlayerEntity closestPlayer = ent.world.getNearestAttackablePlayer(ent, ZAConfigSpawning.extraSpawningDistMax, ZAConfigSpawning.extraSpawningDistMax);
								if (closestPlayer != null && (!ZAConfigPlayerLists.whiteListUsedExtraSpawning || ZAConfigPlayerLists.whitelistExtraSpawning.contains(CoroUtilEntity.getName(closestPlayer)))
										&& closestPlayer.getDistanceSqToEntity(ent) > ZAConfigSpawning.extraSpawningDistMin
										&& !ent.world.canSeeSky(new BlockPos(x, y, z)) && ent.world.getLightFromNeighbors(new BlockPos(x, y, z)) < 5) {

									BlockState state = ent.world.getBlockState(new BlockPos(x, (int)(ent.getEntityBoundingBox().minY - 0.5D), z));

									if (!CoroUtilBlock.isAir(state.getBlock()) && (state.getBlock() != Blocks.GRASS || state.getMaterial() == Material.GRASS)) {
										ZombieEntity entZ = new ZombieEntity(ent.world);
										entZ.setPosition(ent.posX, ent.posY, ent.posZ);
										entZ.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entZ)), (ILivingEntityData)null);
										ZAUtil.giveRandomSpeedBoost(entZ);
										ent.world.spawnEntity(entZ);

										if (ZAConfigSpawning.extraSpawningAutoTarget) entZ.setAttackTarget(closestPlayer);

										//lastZombieCount = ++lastCount;

										spawned = true;
										ZAUtil.getWorldData(dimID).lastSpawnTime = ZAConfigSpawning.zombieSpawnTickDelay + ent.world.getTotalWorldTime();
										ZAUtil.getWorldData(dimID).lastSpawnSysTime = System.currentTimeMillis();

										if (ZAConfig.debugConsoleSpawns) dbg("Spawned new cave zombie at: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
									}
								}
							}
						}
					}

				}
			}
		}
	}

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




}
