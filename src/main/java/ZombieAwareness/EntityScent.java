package ZombieAwareness;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import ZombieAwareness.config.ZAConfig;
import ZombieAwareness.config.ZAConfigClient;

public class EntityScent extends Entity implements IEntityAdditionalSpawnData {

	/**
	 * 
	 * age has a static max that counts down
	 * 
	 * peak strength is for scaling actual current strength based on age scale
	 * 
	 */
	
	//0 == blood node, 1 == sound node, 2 == wander node
    public int type = 0;
    public boolean isUsed = false;
    
    private static final DataParameter<Integer> STRENGTH_PEAK = EntityDataManager.<Integer>createKey(EntityScent.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityScent.class, DataSerializers.VARINT);
    
    public long lastBuffTime = 0;
    public float lastMultiply = 1F;
    
    public static int MAX_AGE = 30*20;

    public EntityScent(World var1) {
        super(var1);
        this.isImmuneToFire = true;
        this.setSize(0.0F, 0.0F);
    }

    @Override
    protected void entityInit() {
    	this.getDataManager().register(STRENGTH_PEAK, Integer.valueOf(0));
    	this.getDataManager().register(AGE, Integer.valueOf(0));
    }
    
    @Override
    public boolean canBeCollidedWith() {
    	return false;
    }

    @Override
    public void setDead() {
        super.setDead();
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double var1) {
        return true;
    }
    
    @Override
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_,
    		double p_145770_5_) {
    	return true;
    }

    public float getRange() {
    	float range = (float)getStrengthScaled() / 100.0F * (float)ZAConfig.maxPFRangeSense;

    	return range;
    }

    public void setStrengthPeak(int strength) {
    	int strTry = strength;
        if (strTry > ZAConfig.senseMaxStrength) {
        	strTry = ZAConfig.senseMaxStrength;
        }
        this.dataManager.set(STRENGTH_PEAK, strTry);
        this.resetAge();
    }
    
    public void resetAge() {
    	this.dataManager.set(AGE, MAX_AGE);
    }
    
    public int getStrengthPeak() {
    	return this.dataManager.get(STRENGTH_PEAK);
    }
    
    public int getStrengthScaled() {
    	return (int)((double)this.dataManager.get(this.STRENGTH_PEAK) * getAgeScale());
    }
    
    public double getAgeScale() {
    	return (double)this.dataManager.get(this.AGE) / (double)MAX_AGE;
    }

    @Override
    public void onUpdate() {
    	
    	//TODO: if raining, age smell sense much faster
    	
    	int age = this.dataManager.get(AGE);
    	this.dataManager.set(AGE, --age);
        
        if(!world.isRemote && age <= 0) {
        	this.setDead();
        }
        
        boolean scentDebug = ZAConfigClient.client_debugSensesVisual;
        if (scentDebug) {
	        if (world.isRemote) {
	        	if (world.getTotalWorldTime()/*+this.getEntityId()*/ % 5 == 0) {
	        		for (int i = 0; i < getStrengthScaled() / 10; i++) {
	        			double range = 1D;
	        			double x = posX - world.rand.nextDouble() / 2 + world.rand.nextDouble();
	        			double y = posY - world.rand.nextDouble() / 2 + world.rand.nextDouble();
	        			double z = posZ - world.rand.nextDouble() / 2 + world.rand.nextDouble();
	        			if (type == 0) {
	        				world.spawnParticle(ParticleTypes.HEART, true, x, y, z, 0, 0, 0);
                        } else if (type == 1) {
                            world.spawnParticle(ParticleTypes.NOTE, true, x, y, z, 0, 0, 0);
                        } else if (type == 2) {
                            world.spawnParticle(ParticleTypes.REDSTONE, true, x, y, z, 0, 0, 0);
                        }
	        			
	        		}
	        	}
	        }
        }
    }

    @Override
    public void writeEntityToNBT(CompoundNBT var1) {
        var1.setInteger("age", this.dataManager.get(AGE));
        var1.setInteger("strengthpeak", this.dataManager.get(STRENGTH_PEAK));
        var1.setInteger("type", type);
    }

    @Override
    public void readEntityFromNBT(CompoundNBT var1) {
    	this.dataManager.set(AGE, var1.getInteger("age"));
    	this.dataManager.set(STRENGTH_PEAK, var1.getInteger("strengthpeak"));
        type = var1.getInteger("type");
    }

	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(this.type);	
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		type = data.readInt();
	}
}
