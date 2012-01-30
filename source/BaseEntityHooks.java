// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode

package net.minecraft.src;

import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;

// Referenced classes of package net.minecraft.src:
//            World, ItemStack, KeyBinding, GuiScreen,
//            RenderBlocks, Block, IBlockAccess, EntityPlayer

public abstract interface BaseEntityHooks {

    /*public BaseEntityHooks()
    {
    }*/

    void EntHook_Init(EntityCreature ent, World world);
    void EntHook_Loaded(EntityCreature ent, NBTTagCompound data);
    void EntHook_Saved(EntityCreature ent, NBTTagCompound data);
    void EntHook_Killed(EntityCreature ent, Entity from);

    boolean EntHook_AttackEntityFrom(EntityCreature ent, Entity entFrom, int damage);
    boolean EntHook_TryAttackEntity(Entity entFrom, Entity entTo, float dist);

    void EntHook_onUpdate_pre(EntityCreature ent);

    void EntHook_onUpdate_post(EntityCreature ent);
    void EntHook_AITick(EntityCreature ent);

}
