package ZombieAwareness;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import ZombieAwareness.config.ZAConfig;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityScent extends Entity implements IEntityAdditionalSpawnData {

    public int type = 0;
    public boolean isUsed = false;
    public int strength;
    public int age;


    public EntityScent(World var1) {
        super(var1);
        this.isImmuneToFire = true;
        this.setSize(0.1F, 0.1F);
        this.strength = 100;
        this.age = 0;
        if (!var1.isRemote) {
        	ZAUtil.traceCount++;
        }
        //System.out.println("new trace: " + ZAUtil.traceCount);
    }
    
    @Override
    public boolean canBeCollidedWith() {
    	return false;
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!worldObj.isRemote) ZAUtil.traceCount--;
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
    		return (float)this.strength / 100.0F * (float)ZAConfig.maxPFRangeSense / 2.0F;
    	} else {
    		return (float)this.strength / 100.0F * (float)ZAConfig.maxPFRangeSense;
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
        if (type == 0) {
        	//System.out.println(this.strength + " - " + worldObj.isRemote);
        }
        
        if(!worldObj.isRemote && (this.strength <= 0 || age > 1200)) {
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

	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {
		data.writeInt(this.type);
		data.writeInt(this.age);		
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data) {
		
		//for easy cleaning purposes
		if (!worldObj.isRemote) this.setDead();
		
		type = data.readInt();
		age = data.readInt();
		//if (type == 0) System.out.println("synced age: " + age);
	}
}
