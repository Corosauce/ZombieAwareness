package ZombieAwareness;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import ZombieAwareness.config.ZAConfig;
import ZombieAwareness.config.ZAConfigClient;

import java.util.logging.Level;

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
    
    private static final DataParameter<Integer> STRENGTH_PEAK = EntityDataManager.defineId(EntityScent.class, DataSerializers.INT);
    private static final DataParameter<Integer> AGE = EntityDataManager.defineId(EntityScent.class, DataSerializers.INT);
    
    public long lastBuffTime = 0;
    public float lastMultiply = 1F;
    
    public static int MAX_AGE = 30*20;

    public EntityScent(EntityType<EntityScent> entityScentEntityType, World var1) {
        super(entityScentEntityType, var1);
    }

    @Override
    protected void defineSynchedData() {
    	this.getEntityData().define(STRENGTH_PEAK, Integer.valueOf(0));
    	this.getEntityData().define(AGE, Integer.valueOf(0));
    }
    
    @Override
    public boolean canBeCollidedWith() {
    	return false;
    }

    @Override
    public boolean isSteppingCarefully() {
        return true;
    }

    @Override
    public boolean shouldRender(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_70112_1_) {
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
        this.getEntityData().set(STRENGTH_PEAK, strTry);
        this.resetAge();
    }
    
    public void resetAge() {
    	this.getEntityData().set(AGE, MAX_AGE);
    }
    
    public int getStrengthPeak() {
    	return this.getEntityData().get(STRENGTH_PEAK);
    }
    
    public int getStrengthScaled() {
    	return (int)((double)this.getEntityData().get(this.STRENGTH_PEAK) * getAgeScale());
    }
    
    public double getAgeScale() {
    	return (double)this.getEntityData().get(this.AGE) / (double)MAX_AGE;
    }

    @Override
    public void tick() {
    	
    	//TODO: if raining, age smell sense much faster
    	
    	int age = this.getEntityData().get(AGE);
    	this.getEntityData().set(AGE, --age);
        
        if(!level.isClientSide() && age <= 0) {
        	this.kill();
        }
        
        boolean scentDebug = ZAConfigClient.client_debugSensesVisual;
        if (scentDebug) {
	        if (level.isClientSide()) {
	        	if (level.getGameTime()/*+this.getEntityId()*/ % 5 == 0) {
	        		for (int i = 0; i < getStrengthScaled() / 10; i++) {
	        			double range = 1D;
	        			double x = getX() - level.random.nextDouble() / 2 + level.random.nextDouble();
	        			double y = getY() - level.random.nextDouble() / 2 + level.random.nextDouble();
	        			double z = getZ() - level.random.nextDouble() / 2 + level.random.nextDouble();
	        			if (type == 0) {
	        				level.addParticle(ParticleTypes.HEART, true, x, y, z, 0, 0, 0);
                        } else if (type == 1) {
                            level.addParticle(ParticleTypes.NOTE, true, x, y, z, 0, 0, 0);
                        } else if (type == 2) {
                            level.addParticle(ParticleTypes.BUBBLE, true, x, y, z, 0, 0, 0);
                        }
	        			
	        		}
	        	}
	        }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT var1) {
        var1.putInt("age", this.getEntityData().get(AGE));
        var1.putInt("strengthpeak", this.getEntityData().get(STRENGTH_PEAK));
        var1.putInt("type", type);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT var1) {
    	this.getEntityData().set(AGE, var1.getInt("age"));
    	this.getEntityData().set(STRENGTH_PEAK, var1.getInt("strengthpeak"));
        type = var1.getInt("type");
    }

	@Override
	public void writeSpawnData(PacketBuffer data) {
		data.writeInt(this.type);	
	}

	@Override
	public void readSpawnData(PacketBuffer data) {
		type = data.readInt();
	}

    /*@Override
    protected void defineSynchedData() {

    }*/

    @Override
    public IPacket<?> getAddEntityPacket() {
        return null;
    }
}
