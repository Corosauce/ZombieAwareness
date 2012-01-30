package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityScent;
import net.minecraft.src.MLProp;
import net.minecraft.src.MLProp2;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderScent;
import net.minecraft.src.STBoolean;
import net.minecraft.src.STFloat;
import net.minecraft.src.STInt;
import net.minecraft.src.STKey;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleButtonModel;

public class mod_PathingActivated extends BaseMod implements BaseEntityHooks {

    @MLProp2
    public static STBoolean OmnipotentHostiles;

    @MLProp2
    public static STBoolean ScentAwareness;
    @MLProp2
    public static STBoolean SoundAwareness;
    @MLProp2
    public static STInt ScentStrength;
    @MLProp2
    public static STInt SoundStrength;
    @MLProp2
    public static STBoolean XRayVision;
    @MLProp2
    public static STBoolean TargetLocking;
    /*@MLProp2
    public static STInt PFRange;*/
    @MLProp2
    public static STInt AwarenessRange;
    @MLProp2
    public static STInt MaxPFRange;
    @MLProp2
    public static STText MobBlacklist;
    @MLProp2
    public static int frequentSoundThreshold = 1000;

    public ModSettings settings;
    public ModSettingScreen modscreen;

    public static Map mobsToUse = new HashMap();
    public static Map hostilesToNames = new HashMap();
    public static boolean inUse = false;

    public static Map classToUseMapping = new HashMap();


    public static int lastHealth;
    public static long lastBleedTime;
    public static long lastSoundTime;
    public static float lastMultiply = 1.0F;

    public static EntityLiving thePlayer;

    public static long PFCount = 0;

    public static boolean hasPetMod = false;
    public static boolean hasNMMode = false;
    public static boolean hasMinerZombie = false;

    public static boolean freezePets = false;
    public static double enhPetSpeedMultiplier = 0.0D;
    public static boolean unlimitedEntityRenderRange = false;
    public static boolean VerticalCollision = false;
    public static boolean NothingPushesPlayer = false;
    public static boolean hostilesUseLadders = false;

    public static long lastTickRun = 0;
    public static boolean inMenu = false;

    public static long traceCount = 0;
    
    public static IBlockAccess worldCache;
    public static long lastWorldCacheTime;
    public static long worldCacheDelay;
    
    public static long lastPathfindTime;
    
    public static boolean redMoonActive = false;


    public mod_PathingActivated() {
        OmnipotentHostiles = new STBoolean("OmnipotentHostiles", Boolean.valueOf(false));
        TargetLocking = new STBoolean("TargetLocking", Boolean.valueOf(false));
        XRayVision = new STBoolean("XRayVision", Boolean.valueOf(false));
        //PFRange = new STInt("PFRange", 64, 0, 8, 512);
        MaxPFRange = new STInt("PFRange", 64, 0, 8, 512);
        AwarenessRange = new STInt("AwarenessRange", 16, 0, 8, 512);
        MobBlacklist = new STText("MobBlacklist", "Skeleton");
        ScentAwareness = new STBoolean("ScentAwareness", Boolean.valueOf(true));
        SoundAwareness = new STBoolean("SoundAwareness", Boolean.valueOf(true));
        ScentStrength = new STInt("ScentStrength", 75, 0, 5, 100);
        SoundStrength = new STInt("SoundStrength", 30, 0, 5, 100);

        try {
            //setupProperties(this.getClass());
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        ModLoader.RegisterEntityID(EntityScent.class, "EntityScent", ModLoader.getUniqueEntityId());
        ModLoader.SetInGameHook(this, true, false);
        ModLoader.SetInGUIHook(this, true, false);
        EntAPI.SetHook_AITick(this);
        EntAPI.SetHook_onUpdate_pre(this);
        EntAPI.SetHook_TryAttackEntity(this);

        try {
            //readURL hmm = new readURL("http://dl.dropbox.com/u/24987042/splashes.txt");
            //hmm.readTextFromURL();
        } catch (Exception ex) {
        }

        settings = new ModSettings("mod_ZombieAwareness");
        modscreen = new ModSettingScreen("Zombie Awareness");
        modscreen.append(new WidgetBoolean(ScentAwareness, "Scent Awareness", "Yes", "No"));
        settings.append(ScentAwareness);
        modscreen.append(new WidgetBoolean(SoundAwareness, "Sound Awareness", "Yes", "No"));
        settings.append(SoundAwareness);
        modscreen.append(new WidgetInt(ScentStrength, "Scent Strength"));
        settings.append(ScentStrength);
        modscreen.append(new WidgetInt(SoundStrength, "Sound Strength"));
        settings.append(SoundStrength);
        modscreen.append(new WidgetInt(AwarenessRange, "Sight Range"));
        settings.append(AwarenessRange);
        modscreen.append(new WidgetBoolean(XRayVision, "X-Ray Sight"));
        settings.append(XRayVision);
        modscreen.append(new WidgetInt(MaxPFRange, "Pathfind Range"));
        settings.append(MaxPFRange);
        modscreen.append(new WidgetText(MobBlacklist, "Mob Blacklist"));
        settings.append(MobBlacklist);
        modscreen.append(new WidgetBoolean(TargetLocking, "Lose target", "never", "dist. + LOS"));
        settings.append(TargetLocking);
        modscreen.append(new WidgetBoolean(OmnipotentHostiles, "Auto target player", "Yes", "No"));
        settings.append(OmnipotentHostiles);
        /*hostilesToNames.put(net.minecraft.src.EntityCreeper.class, "Creeper");
        hostilesToNames.put(net.minecraft.src.EntitySkeleton.class, "Skeleton");
        hostilesToNames.put(net.minecraft.src.EntityZombie.class, "Zombie");
        hostilesToNames.put(net.minecraft.src.EntitySpider.class, "Spider");
        hostilesToNames.put(net.minecraft.src.EntityPigZombie.class, "PigZombie");
        Subscreen subscreen = new Subscreen("button", "Choose Monsters", new WidgetSinglecolumn(new Widget[0]));
        subscreen.setText("Choose Monsters");
        for(int i = 0; i < mobs.length; i++)
        {
            String s = (String)hostilesToNames.get(mobs[i]);
            SettingBoolean settingboolean = new SettingBoolean((new StringBuilder()).append("pf_").append(s).toString(), Boolean.valueOf(true));
            settings.append(settingboolean);
            subscreen.add(new WidgetBoolean(settingboolean, s));
            mobsToUse.put(s, settingboolean);
        }

        modscreen.append(subscreen);*/
        SimpleButtonModel simplebuttonmodel = new SimpleButtonModel();
        simplebuttonmodel.addActionCallback(new ModAction(settings, "resetAll", new Class[0]));
        Button button = new Button(simplebuttonmodel);
        button.setText("Reset all to defaults");
        modscreen.append(button);
        settings.load();
        //doMobList();
    }
    
    public void load() {
    	
    }

    public void ModsLoaded() {
        try {
            //mod_PathingActivated.hasPetMod = true;
        } catch (Exception ex) {
        }

        doMobList();
    }

    public static void doMobList() {
        classToUseMapping.clear();
        //System.out.println("Blacklist: ");
        String[] splEnts = MobBlacklist.get().split(",");

        for (int i = 0; i < splEnts.length; i++) {
            splEnts[i] = splEnts[i].trim();
            //System.out.println(splEnts[i]);
        }

        HashMap hashmap = null;

        /*try
        {
          hashmap = (HashMap)ModLoader.getPrivateValue(EntityList.class, null, "a");
        } catch (Throwable throwable) {
          //ModLoader.getLogger().throwing(getClass().getSimpleName(), "setupConfig", throwable);
          return;
        }*/
        try {
            try {
                hashmap = (HashMap)ModLoader.getPrivateValue(EntityList.class, null, "a");
            } catch (NoSuchFieldException ex) {
                hashmap = (HashMap)ModLoader.getPrivateValue(EntityList.class, null, "stringToClassMapping");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //WidgetClassicTwocolumn widgetclassictwocolumn = new WidgetClassicTwocolumn(new Widget[0]);
        //widgetclassictwocolumn.add(new WidgetMulti(mode, "Mode"));
        Iterator iterator = hashmap.entrySet().iterator();
        //System.out.print("Zombie Awareness BlackList: ");
        StringBuilder blackList = (new StringBuilder()).append("Zombie Awareness BlackList: ");
        StringBuilder entList = (new StringBuilder()).append("List Options: ");

        for (Iterator i$ = hashmap.keySet().iterator(); i$.hasNext(); ) {
            Object o = i$.next();
            String s = (String)o;
            Class class1 = (Class)hashmap.get(o);

            try {
                class1.getDeclaredConstructor(new Class[] { EntityList.class });
            } catch (Throwable throwable1) {
                classToUseMapping.put(class1, false);//continue;
            }

            if ((!Modifier.isAbstract(class1.getModifiers()))) {
                //SettingBoolean settingboolean = new SettingBoolean("mobarrow_" + s, Boolean.valueOf(true));
                //mod_Arrows303.Settings.append(settingboolean);
                //widgetclassictwocolumn.add(new WidgetBoolean(settingboolean, s));
                //mobSettings.put(s, settingboolean);
                if ((IMob.class.isAssignableFrom(class1))) {
                    boolean foundEnt = false;

                    for (int i = 0; i < splEnts.length; i++) {
                        if (s.compareToIgnoreCase(splEnts[i]) == 0) {
                            foundEnt = true;
                            blackList.append(s + " ");
                            //System.out.println("adding to blacklist: " + splEnts[i]);
                            break;
                        }
                    }

                    entList.append(s + " ");
                    classToUseMapping.put(class1, !foundEnt);
                } else {
                    //non mobs
                    classToUseMapping.put(class1, false);
                }

                //System.out.println("hmmmm? " + s);
            }
        }

        System.out.println(entList.toString());
        System.out.println(blackList.toString());
    }

    /*public static boolean shouldEnableEnt(Entity var0) {
       return ((Boolean)classToUseMapping.get(var0.getClass())).booleanValue();
    }*/

    public void EntHook_Init(EntityCreature ent, World world) {
        //entInit(world, ent);
    }
    public void EntHook_Loaded(EntityCreature ent, NBTTagCompound data) {
        //AI Added
        //mod_AIManager.entLoaded(ent, data);
    }
    public void EntHook_Saved(EntityCreature ent, NBTTagCompound data) {
        //AI Added
        //mod_AIManager.entSaved(ent, data);
    }
    public void EntHook_Killed(EntityCreature ent, Entity from) {
    }
    //_303 suggests setting field_9346_af to 0 before attackEntityFrom is called, would prevent having to overload this?
    public boolean EntHook_AttackEntityFrom(EntityCreature ent, Entity entFrom, int damage) {
        return true;
    }

    public boolean EntHook_TryAttackEntity(Entity entFrom, Entity entTo, float dist) {
        if (sameTeam(entFrom, entTo)) {
            return false;
        }

        if((double)dist < 2.0D && entTo.boundingBox.maxY > entFrom.boundingBox.minY && entTo.boundingBox.minY < entFrom.boundingBox.maxY && ((EntityLiving)entFrom).attackTime == 0) {
            if (entFrom instanceof EntityZombie) {
                ((EntityCreature)entFrom).swingArm = true;
                ((EntityCreature)entFrom).swingTick = 0;
            }
        }

        return true;
    }

    public static boolean sameTeam(Entity ent1, Entity ent2) {
        //if both 0, more of a neutral setup than teams
        if (getTeam(ent1) == 0 && getTeam(ent2) == 0) {
            return false;
        }

        if (getTeam(ent1) == getTeam(ent2)) {
            return true;
        }

        return false;
    }

    public static int getTeam(Entity entity) {
        if (entity instanceof EntityCreature) {
            return ((EntityCreature)entity).team;
        } else if (entity instanceof EntityPlayer) {
            return 1;
        }

        return 0;
    }

    public void EntHook_onUpdate_pre(EntityCreature ent) {
        //if (true) return;
        //displayMessage((new StringBuilder()).append("yay!").append("").toString());

        //ent.info2 = (new StringBuilder()).append("playerToAttack - ").append(ent.playerToAttack).toString();
        if(ent.pathfindDelay > 0) {
            ent.pathfindDelay--;
        }

        if(ent.entityToAttack != null) {
            if(!ent.canEntityBeSeen(ent.entityToAttack)) {
                ++ent.noSeeTicks;
            } else {
                ent.noSeeTicks = 0;
            }

            //uhhhhhh, what if an entity is pathfinding towards you from somewhere? wouldnt this break the pursuit eventually if they get out of sight?
            
            //ADD A NEW BOOLEAN, HAS SEEN, only run a noseeticks check if they had already seen the target
            
            //solution?: only reset if has no path
            if(ent.noSeeTicks > 150) {
                ent.noSeeTicks = 0;
                //System.out.println("no see trigger!");
                ent.entityToAttack = null;
                ent.setPathToEntity(null);
            }
        }

        if(ent.notMoving(ent, 0.15F)) {
            ++ent.noMoveTicks;

            if(ent.noMoveTicks > 50) {
                if(ent.rand.nextInt(10) == 0) {
                    //System.out.println("idle trigger!");
                    ent.entityToAttack = null;
                    ent.setPathToEntity(null);
                } else {
                    ent.noMoveTicks = 0;
                }
            }
        } else {
            ent.noMoveTicks = 0;
        }

        //put a 'cant navigate' detector here using pathentity size changing, and timer
        if (ent.hasPath() && ent.getPath() != null) {
            if (ent.lastNode != ent.getPath().pathLength) {
                ent.lastNode = ent.getPath().pathLength;
                ent.lastNodeTimer = 0;
                //System.out.println("lastNodeTimer = 0");
            } else {
                ent.lastNodeTimer++;
            }

            if (ent.lastNodeTimer > 200) {
                //System.out.println("path reset");
                ent.setPathToEntity(null);
            }
        }
    }

    public void EntHook_onUpdate_post(EntityCreature ent) {
    }

    public void EntHook_AITick(EntityCreature ent) {
        //ent.isJumping = false;
        thePlayer = ModLoader.getMinecraftInstance().thePlayer;

        if(ent.entityToAttack == null && ent.shouldTarget(thePlayer)) {
            //System.out.println("targetted player");
            if(thePlayer != null) {
            	ent.entityToAttack = thePlayer;
                ent.state = 4;
            	PFQueue.getPath(ent, ent.entityToAttack, ent.getPathDist());
            }
        } else if(!ent.hasPath() && getTeam(ent) != 1) {
            Entity var3 = mod_PathingActivated.getScent(ent);

            if(var3 != null) {
                ent.state = 1;

                //if (ent.tryPath(var3, ent.getPathDist())) {
                    //System.out.println("chance!");
                //}
                
                //new pathfind system code
                PFQueue.getPath(ent, var3, ent.getPathDist());
            }
        }

        //state stuff
        if (ent.entityToAttack != null && getTeam(ent) != 1) {
            if (!TargetLocking.get() && ent.getDistanceToEntity(ent.entityToAttack) > AwarenessRange.get()) {
                ent.entityToAttack = null;
                ent.state = 1;
            } else {
                ent.state = 4;
            }
        }

        /*if(ent.isCollidedHorizontally) {
            ent.isJumping = true;
        }*/

        if (ent instanceof EntityCreeper && ent.team != 1) {
            if (ent.entityToAttack != null && !(ent.entityToAttack instanceof EntityPlayer)) {
                ent.entityToAttack = thePlayer;
            }
        }

        int pSize = 0;

        if(ent instanceof EntityCreature) {
            PathEntity pEnt = ((EntityCreature)ent).getPath();

            if(pEnt != null) {
                pSize = pEnt.pathLength;
            }
        }

        //ent.info2 = (new StringBuilder()).append("pet state: "+ent.state+" - targ: "+ent.playerToAttack+" - pathsize: "+pSize).toString();
    }

    public boolean OnTickInGUI(float f, Minecraft game, GuiScreen gui) {
        if (ModLoader.getMinecraftInstance().thePlayer != null) {
            //long ticksRan = System.currentTimeMillis();
            if (!(gui instanceof GuiContainer) && !(gui instanceof GuiChat) && gui != null) {
                inMenu = true;
                lastTickRun = 0;
            }

            //System.out.println(gui);
            //playerTick(mc.thePlayer);
        }

        return true;
    }

    public boolean OnTickInGame(float f, Minecraft var1) {
        if (inMenu) {
            //System.out.println(lastTickRun);
            if (lastTickRun > 10) {
                doMobList();
                inMenu = false;
            }

            lastTickRun++;
        }

        if (!inMenu) {
            EntityPlayerSP var2 = var1.thePlayer;

            if(var2.health != lastHealth) {
                if(var2.health < lastHealth) {
                    spawnScent(var2);
                }

                lastHealth = var2.health;
            }

            if(var2.health < 12 && lastBleedTime < System.currentTimeMillis()) {
                lastBleedTime = System.currentTimeMillis() + 30000L;
                spawnScent(var2);
            }
        }
        
        //var1.fontRenderer.drawStringWithShadow(new StringBuilder().append("PFCount: ").append(PFCount).toString(), 3, 175, 0xffffff);

        return true;
    }

    public static Entity getScent(Entity var0) {
        List var1 = var0.worldObj.getEntitiesWithinAABBExcludingEntity(var0, var0.boundingBox.expand((double)MaxPFRange.get().intValue(), (double)MaxPFRange.get().intValue(), (double)MaxPFRange.get().intValue()));
        Entity var2 = null;
        Entity var3 = null;
        Object var4 = null;
        float var5 = 90000.0F;
        float var6 = 90000.0F;
        boolean var7 = false;

        for(int var8 = 0; var8 < var1.size(); ++var8) {
            var2 = (Entity)var1.get(var8);

            if(var2 instanceof EntityScent && var0.getDistanceToEntity(var2) < ((EntityScent)var2).getRange() && var0.getDistanceToEntity(var2) > 5.0F && var0.rand.nextInt(1000) == 0) {
                var3 = var2;
            }
        }

        return var3;
    }

    public static void spawnSoundTrace(String var0, float var1, float var2, float var3, float var4, float var5) {
        //System.out.println("sound: " + var0);

        //TEEEEEMMMMMMMMPPPPPPPPP
        if (!ScentAwareness.get() || traceCount >= 75) {
            return;
        }

        if (!canSpawnTrace((int)var1, (int)var2, (int)var3)) {
            return;
        }

        EntityPlayerSP var6 = ModLoader.getMinecraftInstance().thePlayer;
        int var7 = (int)(20.0F * var4);
        boolean var8 = false;
        /*if(var0.substring(7).equals("drr")) {
           var8 = true;
        }*/

        if(var0.substring(7).equals("bow") || var0.substring(7).equals("pop") || var0.substring(7).equals("wood")) {
            return;
        }

        if((var8 || Math.abs(var6.posX - (double)var1) < 3.0D && Math.abs(var6.posY - (double)var6.getEyeHeight() - (double)var2) < 3.0D && Math.abs(var6.posZ - (double)var3) < 3.0D) && var7 > 15) {
            EntityScent var9 = new EntityScent(var6.worldObj);

            if(var7 < 25) {
                var9.setStrength(SoundStrength.get());
            }

            var7 = var9.strength;

            if(var0.substring(7).equals("drr")) {
                var7 += 10;
            }

            if(lastSoundTime + (long)frequentSoundThreshold > System.currentTimeMillis()) {
                lastMultiply += 0.1F;
                var7 = (int)((float)var7 * lastMultiply);
            } else {
                lastMultiply = 1.0F;
            }

            lastSoundTime = System.currentTimeMillis();
            var9.setStrength(var7);
            var9.type = 1;
            var9.setPosition((double)var1, (double)var2, (double)var3);
            var6.worldObj.entityJoinedWorld(var9);
            
            //System.out.println("sound: " + var0 + " - range: " + var9.getRange());
            //System.out.println(var9.getRange());
        }
    }

    public static void spawnScent(Entity var0) {
        if (!SoundAwareness.get() || traceCount > 75) {
            return;
        }

        if (!canSpawnTrace((int)var0.posX, (int)var0.posY, (int)var0.posZ)) {
            return;
        }

        double height = var0.posY - (double)var0.yOffset + 0.0D;
        /*if (var0 instanceof EntityPlayer) {
          height -= 1.0D;
        }*/
        //System.out.println(height);
        EntityScent var1 = new EntityScent(var0.worldObj);
        var1.setPosition(var0.posX, height, var0.posZ);
        var0.worldObj.entityJoinedWorld(var1);
        var1.setStrength(ScentStrength.get());
        var1.type = 0;
        //System.out.println("scent: " + var0 + " - range: " + var1.getRange());
        //System.out.println("?!?!?! - " + var1.type);
        //System.out.println(var1.getRange());
    }

    public static boolean canSpawnTrace(int x, int y, int z) {
        int id = Block.pressurePlatePlanks.blockID;

        if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x-1,y,z) == id) {
            return false;
        }

        /*if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x,y,z) == id) {
           return false;
        }
        if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x+1,y,z) == id) {
           return false;
        }
        if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x,y,z-1) == id) {
           return false;
        }
        if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x,y,z+1) == id) {
           return false;
        }

        if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x,y-1,z) == id) {
           return false;
        }
        if (ModLoader.getMinecraftInstance().theWorld.getBlockId(x,y+1,z) == id) {
           return false;
        }*/
        return true;
    }

    public void AddRenderer(Map var1) {
        var1.put(EntityScent.class, new RenderScent());
    }

    public static boolean useEnt(Entity var0) {
        try {
            return ((Boolean)classToUseMapping.get(var0.getClass())).booleanValue();
        } catch (Exception ex) {
            return false;
        }
    }

    public String getVersion() {
        return "Version 1.2 for MC "+ModLoader.VERSION.substring(ModLoader.VERSION.indexOf(" ")+1);
    }
    
    
    
}
