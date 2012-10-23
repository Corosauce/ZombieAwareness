package ZombieAwareness;

import net.minecraft.src.*;

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
        ZAUtil.traceCount++;
    }

    @Override
    public void setDead() {
        super.setDead();
        ZAUtil.traceCount--;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean isInRangeToRenderDist(double var1) {
        return true;
    }

    public void entityInit() {}

    public float getRange() {
        //return (float)this.strength / 100.0F * mod_ZombieAwareness.maxPFRange;
        //return (float)this.strength / 100.0F * (float)mod_PathingActivated.MaxPFRange.get().intValue();
    	
    	if (this.type == 2) {
    		return (float)this.strength / 100.0F * 128;
    	} else if (this.type == 1) {
    		return (float)this.strength / 100.0F * (float)mod_ZombieAwareness.maxPFRange / 2.0F;
    	} else {
    		return (float)this.strength / 100.0F * (float)mod_ZombieAwareness.maxPFRange;
    	}
        
    }

    public void setStrength(int var1) {
        this.age = (100 - var1) * 10;
        /*if (age < 0) {
          age = 0;
        }*/
        this.strength = 100 - this.age / 10;
        //System.out.println("age: " + age);
    }

    @Override
    public void onUpdate() {
        ++this.age;
        this.strength = 100 - this.age / 10;
        //this.setDead();
        //System.out.println(this.getRange());
        if(this.strength <= 0) {
        	this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound var1) {
        var1.setShort("age", (short)age);
        var1.setShort("type", (short)type);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound var1) {
        age = var1.getShort("age");
        type = var1.getShort("type");
    }
}
