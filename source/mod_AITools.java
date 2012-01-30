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

public class mod_AITools extends BaseMod {

    public ModSettings settings;
    public ModSettingScreen modscreen;


    static Class class$fz;
    static Class class$fp;
    static Class class$ur;
    static Class class$cl;
    static Class class$xr;
    static Class class$MLProp2;
    static Class class$java$lang$String;

    public static long lastTickRun = 0;
    public static boolean inMenu = false;

    public static int hostileTeamInit = 2;
    public static int animalTeamInit = 0;
    public static int playerTeamInit = 1;
    public static int guardianTeamInit = 4;

    public static boolean noHumansPlus = false;

    public static boolean tryNMMode = true;


    public mod_AITools() {
    }
    
    public void load() {
    	
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

    public String getVersion() {
        return "Version 1.0 for MC "+ModLoader.VERSION.substring(ModLoader.VERSION.indexOf(" ")+1);
    }

    public static void setupProperties(Class var0) throws IllegalArgumentException, IllegalAccessException, IOException, SecurityException, NoSuchFieldException {
        Properties var1 = new Properties();
        File var2 = new File(Minecraft.getMinecraftDir(), "/config/");
        File var3 = new File(var2, var0.getName() + ".cfg");

        if(var3.exists() && var3.canRead()) {
            var1.load(new FileInputStream(var3));
        }

        StringBuilder var4 = new StringBuilder();
        Field[] var5;
        int var6 = (var5 = var0.getFields()).length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Field var8 = var5[var7];

            if((var8.getModifiers() & 8) != 0 && var8.isAnnotationPresent(class$MLProp2 == null?(class$MLProp2 = class$("net.minecraft.src.MLProp2")):class$MLProp2)) {
                Field var9 = var8.get((Object)null).getClass().getField("defvalue");
                Class var10 = var9.getType();
                MLProp2 var11 = (MLProp2)var8.getAnnotation(class$MLProp2 == null?(class$MLProp2 = class$("net.minecraft.src.MLProp2")):class$MLProp2);
                String var12 = var11.name().length() != 0?var11.name():var8.getName();
                Object var13 = var8.get((Object)null);
                Object var14;

                if(var13 instanceof STBoolean) {
                    var14 = ((STBoolean)var13).get();
                } else if(var13 instanceof STInt) {
                    var14 = ((STInt)var13).get();
                } else if(var13 instanceof STFloat) {
                    var14 = ((STFloat)var13).get();
                } else if(var13 instanceof STKey) {
                    var14 = ((STKey)var13).get();
                } else {
                    var14 = var8.get((Object)null);
                }

                StringBuilder var15 = new StringBuilder();

                if(var11.min() != Double.NEGATIVE_INFINITY) {
                    var15.append(String.format(",>=%.1f", new Object[] {Double.valueOf(var11.min())}));
                }

                if(var11.max() != Double.POSITIVE_INFINITY) {
                    var15.append(String.format(",<=%.1f", new Object[] {Double.valueOf(var11.max())}));
                }

                StringBuilder var16 = new StringBuilder();

                if(var11.info().length() > 0) {
                    var16.append(" -- ");
                    var16.append(var11.info());
                }

                var4.append(String.format("%s (%s:%s%s)%s\n", new Object[] {var12, var10.getName(), var14, var15, var16}));

                if(var1.containsKey(var12)) {
                    String var17 = var1.getProperty(var12);
                    Object var18 = null;

                    if(var10.isAssignableFrom(class$java$lang$String == null?(class$java$lang$String = class$("java.lang.String")):class$java$lang$String)) {
                        var18 = var17;
                    } else if(var10.isAssignableFrom(Integer.TYPE)) {
                        var18 = Integer.valueOf(Integer.parseInt(var17));
                    } else if(var10.isAssignableFrom(Short.TYPE)) {
                        var18 = Short.valueOf(Short.parseShort(var17));
                    } else if(var10.isAssignableFrom(Byte.TYPE)) {
                        var18 = Byte.valueOf(Byte.parseByte(var17));
                    } else if(var10.isAssignableFrom(Boolean.TYPE)) {
                        var18 = Boolean.valueOf(Boolean.parseBoolean(var17));
                    } else if(var10.isAssignableFrom(Float.TYPE)) {
                        var18 = Float.valueOf(Float.parseFloat(var17));
                    } else if(var10.isAssignableFrom(Double.TYPE)) {
                        var18 = Double.valueOf(Double.parseDouble(var17));
                    } else {
                        var18 = Boolean.valueOf(Boolean.parseBoolean(var17));
                    }

                    if(var18 != null) {
                        if(var18 instanceof Number) {
                            double var19 = ((Number)var18).doubleValue();

                            if(var11.min() != Double.NEGATIVE_INFINITY && var19 < var11.min() || var11.max() != Double.POSITIVE_INFINITY && var19 > var11.max()) {
                                continue;
                            }
                        }

                        if(!var18.equals(var14)) {
                            if(var13 instanceof STBoolean) {
                                ((STBoolean)var13).set((Boolean)var18, "");
                            } else if(var13 instanceof STInt) {
                                ((STInt)var13).set((Integer)var18, "");
                            } else if(var13 instanceof STFloat) {
                                ((STFloat)var13).set((Float)var18, "");
                            } else if(var13 instanceof STKey) {
                                ((STKey)var13).set((Integer)var18, "");
                            }
                        }
                    }
                } else {
                    var1.setProperty(var12, var14.toString());
                }
            }
        }

        if(!var1.isEmpty() && (var3.exists() || var3.createNewFile()) && var3.canWrite()) {
            var1.store(new FileOutputStream(var3), var4.toString());
        }
    }

    static Class class$(String var0) {
        try {
            return Class.forName(var0);
        } catch (ClassNotFoundException var2) {
            throw new NoClassDefFoundError(var2.getMessage());
        }
    }



    public static boolean isOrigMob(EntityLiving entityliving) {
        try {
            return (entityliving instanceof EntityCreeper) ||
                   (entityliving instanceof EntityGhast) ||
                   entityliving instanceof EntityPigZombie ||
                   (entityliving instanceof EntitySkeleton) ||
                   (entityliving instanceof EntitySlime) ||
                   (entityliving instanceof EntitySpider) ||
                   (entityliving instanceof EntityZombie) || isNMMob(entityliving);
        } catch(Exception exception) {
            return false;
        }
    }



    public static boolean isNMMob(EntityLiving entityliving) {
        if (!tryNMMode || !mod_PathingActivated.hasNMMode) {
            return false;
        }

        try {
            return Class.forName("EntityZombieMiner").isInstance(entityliving) ||
                   Class.forName("EntityZombieNazi").isInstance(entityliving) ||
                   Class.forName("EntityZombieKnight").isInstance(entityliving) ||
                   Class.forName("EntityPlayerProxy").isInstance(entityliving);
        } catch(Exception exception) {
            tryNMMode = false;
            return false;
        }
    }

    public static boolean isGuardian(EntityLiving entityliving) {
        try {
            return Class.forName("SdkEntityGuardians").isInstance(entityliving);
        } catch(Exception exception) {
            return false;
        }
    }

    public static boolean isNobleHuman(EntityLiving entityliving) {
        if (noHumansPlus) {
            return false;
        }

        try {
            if(Class.forName("HumanBehaviourAI").isInstance(entityliving)) {
                return true;
            }
        } catch(Exception exception) {
            noHumansPlus = true;
            return false;
        }

        return false;
    }

    public static boolean isPet(EntityCreature entityliving) {
        try {
            Field field = (EntityCreature.class).getDeclaredField("team");
            return field.getInt(entityliving) == 1;
        } catch(Exception exception) {
            return false;
        }
    }

    public static boolean isNonDefaultAnimal(EntityLiving entityliving) {
        try {
            if(entityliving instanceof EntityAnimal) {
                return !(entityliving instanceof EntityChicken) && !(entityliving instanceof EntityCow) && !(entityliving instanceof EntityPig) && !(entityliving instanceof EntitySheep);
            } else {
                return false;
            }
        } catch(Exception exception) {
            return false;
        }
    }

    public static boolean notMoving(EntityCreature entityliving, float f) {
        double d = entityliving.prevPosX - entityliving.posX;
        double d1 = entityliving.prevPosZ - entityliving.posZ;
        float f1 = MathHelper.sqrt_double(d * d + d1 * d1);
        return f1 < f;
    }

    public static Field getTargetField(Entity entity) {
        try {
            if(entity instanceof EntityGhast) {
                return (EntityGhast.class).getField("g");
            }

            if(entity instanceof EntityCreature) {
                return (EntityCreature.class).getField("h");
            } else {
                return null;
            }
        } catch(NoSuchFieldException nosuchfieldexception) {
            try {
                if(entity instanceof EntityGhast) {
                    return (EntityGhast.class).getField("targetedEntity");
                }

                if(entity instanceof EntityCreature) {
                    return (EntityCreature.class).getField("entityToAttack");
                } else {
                    return null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public static int getuID(Entity entity) {
        if (entity instanceof EntityCreature) {
            return ((EntityCreature)entity).uID;
        } else if (entity instanceof EntityPlayer) {
            return -1;
        }

        return -2;
    }

    public static boolean isThreat(EntityCreature entityliving, EntityLiving entityliving1) {
        if(!(entityliving1 instanceof EntityCreature) || !(entityliving instanceof EntityCreature)) {
            return false;
        }

        if(((EntityCreature)entityliving1).entityToAttack == null || !(((EntityCreature)entityliving1).entityToAttack instanceof EntityLiving)) {
            return false;
        }

        //enemy targetting friend check
        if(getTeam(((EntityCreature)entityliving1).entityToAttack) == playerTeamInit) {
            return true;
        }

        return ((EntityCreature)entityliving1).entityToAttack == ModLoader.getMinecraftInstance().thePlayer || ((EntityCreature)entityliving1).entityToAttack == entityliving.guardEnt || ((EntityCreature)entityliving1).entityToAttack == entityliving;
    }

}
