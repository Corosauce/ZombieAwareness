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
    public int strength;
    public int age;

    public EntityScent(World var1) {
        super(var1);
        this.isImmuneToFire = true;
        this.setSize(0.0F, 0.0F);
        this.strength = 100;
        this.age = 0;
        //System.out.println("new trace: " + ZAUtil.traceCount);
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
        
        boolean scentDebug = ZAConfig.client_debugSensesVisual;
        if (scentDebug) {
	        if (worldObj.isRemote) {
	        	if (worldObj.getTotalWorldTime() % 5 == 0) {
	        		for (int i = 0; i < strength / 10; i++) {
	        			double range = 1D;
	        			double x = posX - worldObj.rand.nextDouble() / 2 + worldObj.rand.nextDouble();
	        			double y = posY - worldObj.rand.nextDouble() / 2 + worldObj.rand.nextDouble();
	        			double z = posZ - worldObj.rand.nextDouble() / 2 + worldObj.rand.nextDouble();
	        			if (type == 0) {
	        				worldObj.spawnParticle(EnumParticleTypes.HEART, x, y, z, 0, 0, 0);
	        			} else if (type == 1) {
	        				worldObj.spawnParticle(EnumParticleTypes.NOTE, x, y, z, 0, 0, 0);
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
