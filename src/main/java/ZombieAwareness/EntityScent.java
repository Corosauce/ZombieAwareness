package ZombieAwareness;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import ZombieAwareness.config.ZAConfig;

public class EntityScent extends Entity implements IEntityAdditionalSpawnData {

	//0 == blood node, 1 == sound node, 2 == wander node
    public int type = 0;
    public boolean isUsed = false;
    
    //TODO: set to data attribute for better client syncing
    private int strength = 0;
    public int age;
    
    public long lastBuffTime = 0;
    public float lastMultiply = 1F;
    
    public static int AGE_MULTIPLIER = 10;

    public EntityScent(World var1) {
        super(var1);
        this.isImmuneToFire = true;
        this.setSize(0.0F, 0.0F);
        this.strength = 100;
        this.age = this.strength * AGE_MULTIPLIER;
    }
    
    @Override
    public boolean canBeCollidedWith() {
    	return false;
    }

    @Override
    public void setDead() {
        super.setDead();
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean isInRangeToRenderDist(double var1) {
        return true;
    }
    
    @Override
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_,
    		double p_145770_5_) {
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

    public void setStrength(int strength) {
        //this.age = (100 - var1) * 10;
        /*if (age < 0) {
          age = 0;
        }*/
        this.strength = strength;
        if (this.strength > ZAConfig.senseMaxStrength) {
        	this.strength = ZAConfig.senseMaxStrength;
        }
    	this.age = strength * AGE_MULTIPLIER;
        //System.out.println("age: " + age);
    }
    
    public int getStrength() {
    	return this.strength;
    }

    @Override
    public void onUpdate() {
    	
    	//TODO: if raining, age smell sense much faster
    	
        this.age--;
        
        this.strength = /*100 - */this.age / AGE_MULTIPLIER;
        if (this.strength > ZAConfig.senseMaxStrength) {
        	this.strength = ZAConfig.senseMaxStrength;
        }
        //this.setDead();
        if (type == 0) {
        	//System.out.println(this.strength + " - " + worldObj.isRemote);
        }
        
        /*if(!worldObj.isRemote && (this.strength <= 0 || age > 1200)) {
        	this.setDead();
        }*/
        
        if(!worldObj.isRemote && age <= 0) {
        	this.setDead();
        }
        
        boolean scentDebug = ZAConfig.client_debugSensesVisual;
        if (scentDebug) {
	        if (worldObj.isRemote) {
	        	if (worldObj.getTotalWorldTime()/*+this.getEntityId()*/ % 5 == 0) {
	        		for (int i = 0; i < strength / 10; i++) {
	        			double range = 1D;
	        			double x = posX - worldObj.rand.nextDouble() / 2 + worldObj.rand.nextDouble();
	        			double y = posY - worldObj.rand.nextDouble() / 2 + worldObj.rand.nextDouble();
	        			double z = posZ - worldObj.rand.nextDouble() / 2 + worldObj.rand.nextDouble();
	        			if (type == 0) {
	        				worldObj.spawnParticle(EnumParticleTypes.HEART, true, x, y, z, 0, 0, 0);
	        			} else if (type == 1) {
	        				worldObj.spawnParticle(EnumParticleTypes.NOTE, true, x, y, z, 0, 0, 0);
	        			}
	        			
	        		}
	        	}
	        }
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
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(this.type);
		data.writeInt(this.age);		
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		
		//for easy cleaning purposes
		if (!worldObj.isRemote) this.setDead();
		
		type = data.readInt();
		age = data.readInt();
		//if (type == 0) System.out.println("synced age: " + age);
	}
}
