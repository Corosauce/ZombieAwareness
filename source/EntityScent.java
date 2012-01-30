package net.minecraft.src;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.mod_PathingActivated;

public class EntityScent extends Entity {

    public int type = 0;
    public boolean isUsed = false;
    public int strength;
    public int age;


    public EntityScent(World var1) {
        super(var1);
        this.isImmuneToFire = true;
        this.setSize(1.1F, 1.1F);
        this.strength = 100;
        this.age = 0;
        mod_PathingActivated.traceCount++;
    }

    public void setEntityDead() {
        super.setEntityDead();
        mod_PathingActivated.traceCount--;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean isInRangeToRenderDist(double var1) {
        return true;
    }

    public void entityInit() {}

    public float getRange() {
        return (float)this.strength / 100.0F * 64.0F;
        //return (float)this.strength / 100.0F * (float)mod_PathingActivated.MaxPFRange.get().intValue();
        //return this.type == 1?(float)this.strength / 100.0F * (float)mod_PathingActivated.MaxPFRange.get().intValue() / 2.0F:(float)this.strength / 100.0F * (float)mod_PathingActivated.MaxPFRange.get().intValue();
    }

    public void setStrength(int var1) {
        this.age = (100 - var1) * 10;
        /*if (age < 0) {
          age = 0;
        }*/
        this.strength = 100 - this.age / 10;
        //System.out.println("age: " + age);
    }

    public void onUpdate() {
        ++this.age;
        this.strength = 100 - this.age / 10;

        //System.out.println(this.getRange());
        if(this.strength <= 0) {
            this.setEntityDead();
        }
    }

    public void writeEntityToNBT(NBTTagCompound var1) {
        var1.setShort("age", (short)age);
        var1.setShort("type", (short)type);
    }

    public void readEntityFromNBT(NBTTagCompound var1) {
        age = var1.getShort("age");
        type = var1.getShort("type");
    }
}
