package com.corosus.zombieawareness;

import com.corosus.coroutil.util.CU;
import com.corosus.zombieawareness.config.ZAConfigClient;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityScent extends Entity/* implements IEntityAdditionalSpawnData*/ {

	/**
	 * 
	 * age has a static max that counts down
	 * 
	 * peak strength is for scaling actual current strength based on age scale
	 * 
	 */
	
	//0 == blood node, 1 == sound node, 2 == wander node
    //public int type = 0;
    public boolean isUsed = false;
    
    private static final EntityDataAccessor<Integer> STRENGTH_PEAK = SynchedEntityData.defineId(EntityScent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(EntityScent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(EntityScent.class, EntityDataSerializers.BYTE);

    public long lastBuffTime = 0;
    public float lastMultiply = 1F;
    
    public static int MAX_AGE = 30*20;

    public EntityScent(EntityType<EntityScent> entityScentEntityType, Level var1) {
        super(entityScentEntityType, var1);
    }

    @Override
    protected void defineSynchedData() {
    	this.getEntityData().define(STRENGTH_PEAK, Integer.valueOf(0));
    	this.getEntityData().define(AGE, Integer.valueOf(0));
    	this.getEntityData().define(TYPE, Byte.valueOf((byte) 0));
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
    	float range = (float)getStrengthScaled() / 100.0F * (float) ZAConfigGeneral.maxPFRangeSense;

    	return range;
    }

    public void setStrengthPeak(int strength) {
    	int strTry = strength;
        if (strTry > ZAConfigGeneral.senseMaxStrength) {
        	strTry = ZAConfigGeneral.senseMaxStrength;
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
        int decayRate = level().isRaining() && level().canSeeSky(blockPosition()) ? 3 : 1;
        age -= decayRate;
        if (age < 0) age = 0;

    	this.getEntityData().set(AGE, age);
        
        if(!level().isClientSide() && age <= 0) {
        	this.kill();
        }

        boolean scentDebug = ZAConfigClient.client_debugSensesVisual;
        if (scentDebug) {
	        if (level().isClientSide()) {
	        	if (level().getGameTime()/*+this.getEntityId()*/ % 5 == 0) {
	        		for (int i = 0; i < getStrengthScaled() / 10; i++) {
	        			double range = 1D;
	        			double x = getX() - CU.rand().nextDouble() / 2 + CU.rand().nextDouble();
	        			double y = getY() - CU.rand().nextDouble() / 2 + CU.rand().nextDouble();
	        			double z = getZ() - CU.rand().nextDouble() / 2 + CU.rand().nextDouble();
	        			if (getSenseType() == 0) {
	        				level().addParticle(ParticleTypes.HEART, true, x, y, z, 0, 0, 0);
                        } else if (getSenseType() == 1) {
                            level().addParticle(ParticleTypes.NOTE, true, x, y, z, 0, 0, 0);
                        } else if (getSenseType() == 2) {
                            level().addParticle(ParticleTypes.ANGRY_VILLAGER, true, x, y, z, 0, 0, 0);
                        }
	        			
	        		}
	        	}
	        }
        }
    }

    public int getSenseType() {
        return this.getEntityData().get(TYPE);
    }

    public void setSenseType(int type) {
        this.getEntityData().set(TYPE, (byte)type);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag var1) {
        var1.putInt("age", this.getEntityData().get(AGE));
        var1.putInt("strengthpeak", this.getEntityData().get(STRENGTH_PEAK));
        var1.putInt("type", getSenseType());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag var1) {
    	this.getEntityData().set(AGE, var1.getInt("age"));
    	this.getEntityData().set(STRENGTH_PEAK, var1.getInt("strengthpeak"));
    	this.getEntityData().set(TYPE, var1.getByte("type"));
        //type = var1.getInt("type");
    }

	/*@Override
	public void writeSpawnData(FriendlyByteBuf data) {
		data.writeInt(this.getSenseType());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data) {
		type = data.readInt();
	}*/

    /*@Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }*/
}
