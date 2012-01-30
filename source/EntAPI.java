package net.minecraft.src;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public final class EntAPI {

    public static final Map hook_init = new HashMap();
    public static final Map hook_loaded = new HashMap();
    public static final Map hook_saved = new HashMap();
    public static final Map hook_killed = new HashMap();
    public static final Map hook_AttackEntityFrom = new HashMap();
    public static final Map hook_TryAttackEntity = new HashMap();

    public static final Map hook_onUpdate_pre = new HashMap();
    public static final Map hook_onUpdate_post = new HashMap();
    public static final Map hook_AITick = new HashMap();

    public EntAPI() {
        /*try {

            Method m = ModLoader.class.getDeclaredMethod("SetInGameHook", new Class[] { BaseMod.class, Boolean.TYPE, Boolean.TYPE });
            m.invoke(null, new Object[] { this, Boolean.valueOf(true), Boolean.valueOf(false) });

        } catch (Throwable e) { e.printStackTrace(); }*/
    }

    public static void SetHook_Init(BaseEntityHooks basemod) {
        hook_init.put(basemod, true);
    }
    public static void RunHooks_Init(EntityCreature ent, World world) {
        for(Iterator iterator = hook_init.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_Init(ent, world);
        }
    }

    public static void SetHook_Loaded(BaseEntityHooks basemod) {
        hook_loaded.put(basemod, true);
    }
    public static void RunHooks_Loaded(EntityCreature ent, NBTTagCompound data) {
        for(Iterator iterator = hook_loaded.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_Loaded(ent, data);
        }
    }

    public static void SetHook_Saved(BaseEntityHooks basemod) {
        hook_saved.put(basemod, true);
    }
    public static void RunHooks_Saved(EntityCreature ent, NBTTagCompound data) {
        for(Iterator iterator = hook_saved.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_Saved(ent, data);
        }
    }

    public static void SetHook_Killed(BaseEntityHooks basemod) {
        hook_killed.put(basemod, true);
    }
    public static void RunHooks_Killed(EntityCreature ent, Entity from) {
        for(Iterator iterator = hook_killed.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_Killed(ent, from);
        }
    }

    public static void SetHook_AttackEntityFrom(BaseEntityHooks basemod) {
        hook_AttackEntityFrom.put(basemod, true);
    }
    public static boolean RunHooks_AttackEntityFrom(EntityCreature ent, Entity entFrom, int damage) {
        boolean doDamage = true;

        for(Iterator iterator = hook_AttackEntityFrom.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();

            if (!((BaseEntityHooks)entry.getKey()).EntHook_AttackEntityFrom(ent, entFrom, damage)) {
                doDamage = false;
            }
        }

        return doDamage;
    }

    public static void SetHook_TryAttackEntity(BaseEntityHooks basemod) {
        hook_TryAttackEntity.put(basemod, true);
    }
    public static boolean RunHooks_TryAttackEntity(Entity entFrom, Entity entTo, float dist) {
        boolean doAttack = true;

        for(Iterator iterator = hook_TryAttackEntity.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();

            if (!((BaseEntityHooks)entry.getKey()).EntHook_TryAttackEntity(entFrom, entTo, dist)) {
                doAttack = false;
            }
        }

        return doAttack;
    }



    public static void SetHook_onUpdate_pre(BaseEntityHooks basemod) {
        hook_onUpdate_pre.put(basemod, true);
    }
    public static void RunHooks_onUpdate_pre(EntityCreature ent) {
        for(Iterator iterator = hook_onUpdate_pre.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_onUpdate_pre(ent);
        }
    }

    public static void SetHook_onUpdate_post(BaseEntityHooks basemod) {
        hook_onUpdate_post.put(basemod, true);
    }
    public static void RunHooks_onUpdate_post(EntityCreature ent) {
        for(Iterator iterator = hook_onUpdate_post.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_onUpdate_post(ent);
        }
    }

    public static void SetHook_AITick(BaseEntityHooks basemod) {
        hook_AITick.put(basemod, true);
    }
    public static void RunHooks_AITick(EntityCreature ent) {
        for(Iterator iterator = hook_AITick.entrySet().iterator(); iterator.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            ((BaseEntityHooks)entry.getKey()).EntHook_AITick(ent);
        }
    }

}