package net.minecraft.src;

import java.util.List;
import java.util.Random;
import net.minecraft.src.PathfinderEnh;

// Referenced classes of package net.minecraft.src:
//            Entity, Vec3D, StepSound, AxisAlignedBB,
//            World, MathHelper, Material, NBTTagCompound,
//            Block, MovingObjectPosition, ItemStack

public abstract class EntityHooks extends EntityLiving {

    private static boolean sdkGrapplingHookNotFound = false;
    private static int sdkBlockRopeId = -1;

    public int uID;
    public int mobtype;
    public int team;
    public int orders;
    public int guardX;
    public int guardY;
    public int guardZ;
    public int enhanced;
    public int guardEntID;
    public int mountEntID;
    public int kills;


    public Entity guardEnt;
    public Entity attackingEnt;
    public int state;
    public int maxhealth;
    public int AIDelay;
    public int mineDelay;
    public int PFDelay;
    public float curBlockDmg;
    public int curBlockX;
    public int curBlockZ;
    public boolean mining;
    public boolean forcejump;
    public int noMoveTicks;
    public float info;
    public String info2;
    public int nearbyMinerCount;
    public boolean canSwitchTarget;
    public boolean noTick;

    public float pathFollowDist = 2.0F;

    public int noSeeTicks = 0;
    public int pathfindDelay = 0;
    public float plDist;

    public boolean swingArm = false;
    public int swingTick = 0;

    public int lastNodeTimer;
    public int lastNode;

    public PathfinderEnh pf;

    public EntityHooks(World world) {
        super(world);
        //
        uID = -1;
        mobtype = 0;
        team = 0;
        orders = 0;
        guardX = -1;
        guardY = -1;
        guardZ = -1;
        enhanced = 0;
        guardEntID = -1;
        mountEntID = -1;
        guardEnt = null;
        attackingEnt = null;
        maxhealth = 0;
        noTick = false;
        pathFollowDist = 1.2F;
        pf = new PathfinderEnh(world);
        //
        EntAPI.RunHooks_Init((EntityCreature)this,world);
    }




    //Main Hooks

    public void onUpdate() {
        EntAPI.RunHooks_onUpdate_pre((EntityCreature)this);
        super.onUpdate();
        EntAPI.RunHooks_onUpdate_post((EntityCreature)this);
    }

    public boolean canClimb() {
        if (/*this instanceof EntityCreeper || *//*this instanceof EntityCreeper*/ false) {
            return true;
        }

        return false;
    }

    public boolean isOnLadder() {
        /*if (canClimb() && this.isCollidedHorizontally) {
        	return true;
        }*/
        return super.isOnLadder();
    }

    public boolean attackEntityFrom(Entity entity, int i) {
        if (EntAPI.RunHooks_AttackEntityFrom((EntityCreature)this, entity, i)) {
            return super.attackEntityFrom(DamageSource.causeThrownDamage(entity, entity), i);
        }

        return false;
    }

    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("uID", (short)this.uID);
        nbttagcompound.setShort("MobType", (short)this.mobtype);
        nbttagcompound.setShort("Team", (short)this.team);
        nbttagcompound.setShort("Orders", (short)this.orders);
        nbttagcompound.setShort("guardX", (short)this.guardX);
        nbttagcompound.setShort("guardY", (short)this.guardY);
        nbttagcompound.setShort("guardZ", (short)this.guardZ);
        nbttagcompound.setShort("enhanced", (short)this.enhanced);
        nbttagcompound.setShort("guardEntID", (short)this.guardEntID);
        nbttagcompound.setShort("mineDelay", (short)this.mineDelay);
        nbttagcompound.setShort("kills", (short)this.kills);
        super.writeEntityToNBT(nbttagcompound);
        EntAPI.RunHooks_Saved((EntityCreature)this, nbttagcompound);
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        this.uID = nbttagcompound.getShort("uID");
        this.mobtype = nbttagcompound.getShort("MobType");
        this.team = nbttagcompound.getShort("Team");
        this.orders = nbttagcompound.getShort("Orders");
        this.guardX = nbttagcompound.getShort("guardX");
        this.guardY = nbttagcompound.getShort("guardY");
        this.guardZ = nbttagcompound.getShort("guardZ");
        this.enhanced = nbttagcompound.getShort("enhanced");
        this.guardEntID = nbttagcompound.getShort("guardEntID");
        this.mountEntID = nbttagcompound.getShort("mountEntID");
        this.mineDelay = nbttagcompound.getShort("mineDelay");
        this.kills = nbttagcompound.getShort("kills");

        if(this instanceof EntityMob) {
            this.maxhealth = 20;
            /*} else if(this instanceof EntityPlayer) {
                this.maxhealth = 20;
                firstTick = true;
                this.team = playerTeamInit;*/
        } else if(this instanceof EntityAnimal) {
            this.maxhealth = 10;
        }

        if(this.health > this.maxhealth) {
            this.maxhealth = this.health;
        }

        if(mod_AITools.isGuardian(this)) {
            this.noTick = true;
        }

        super.readEntityFromNBT(nbttagcompound);
        EntAPI.RunHooks_Loaded((EntityCreature)this, nbttagcompound);
    }

    public void setEntityDead() {
        //System.out.println("hmmm!");
        if (deathTime < 1 && team == 1) {
            return;
        }

        super.setEntityDead();
    }

    public void onDeath(DamageSource var1) {
        EntAPI.RunHooks_Killed((EntityCreature)this, var1.getEntity());

        if(this instanceof EntityAnimal) {
            mod_PathingActivated.spawnScent(this);
        }

        super.onDeath(var1);
    }

    protected void tryAttackEntity(Entity var1, float var2) {
        if (EntAPI.RunHooks_TryAttackEntity(this, var1, var2)) {
            this.attackEntity(var1, var2);
        }
    }

    protected Entity tryFindPlayer() {
        Entity ent = findPlayerToAttack();

        if (mod_PathingActivated.sameTeam(this, ent)) {
            return null;
        } else {
            return ent;
        }
    }

    //this is currently the undefined mcp function
    protected Entity findEntityToAttack() {
        return null;
    }

    protected Entity findPlayerToAttack() {
        return null;
    }
    
    protected void attackEntity(Entity var1, float var2) {}

    public int getTalkInterval() {
        if (team == 1) {
            return 400;
        }

        return 80;
    }

    //Entity creature stuff

    protected void updateEntityActionState() {
        super.updateEntityActionState();
        if (this.isInsideOfMaterial(Material.water)) this.isJumping = true;
    }

    public Entity getEntityToAttack() {
        return null;
    }

    public boolean shouldFaceTarget() {
        //if (mod_PathingActivated.hasPetMod) {
        //System.out.println(state);
        //(new StringBuilder()).append("state - ").append(state).toString();

    	if(this.isCollidedHorizontally/* && !this.swingArm*/) {
            this.isJumping = true;
        } else {
        	this.isJumping = false;
        }
    	
        if(state != 1) {
            if((state == 3 || state == 4) && getEntityToAttack() != null) {
                if(state == 3) {
                    if (this.guardEnt != null && this.getDistanceToEntity(this.guardEnt) > 32.0F) {
                        if (teleportToTarget(this.guardEnt)) {
                            setPathToEntity(null);
                        }
                    }

                    if (this.guardEnt != null && isSolidPath(this.guardEnt)) {
                        setPathToEntity(null);
                        this.moveForward = this.moveSpeed;
                        faceEntity(this.guardEnt, 30.0F, 30.0F);
                        //faceEntity(getTarget(), 30.0F, 30.0F);
                        //System.out.println(getTarget());
                        return true;
                    }

                    //faceEntity(this.guardEnt, 30.0F, 30.0F);
                    return false;
                    /*d1 = guardEnt.posX - posX;
                    d2 = guardEnt.posZ - posZ;
                    double d5 = guardEnt.posY - (double)i;*/
                } else if(state == 4) {
                    if (getEntityToAttack() != null && (isSolidPath(getEntityToAttack()) || this.getPath() == null)) {
                        this.moveForward = this.moveSpeed;
                        faceEntity(getEntityToAttack(), 30.0F, 30.0F);
                        setPathToEntity(null);
                        return true;
                    }

                    return false;
                    /*d1 = playerToAttack.posX - posX;
                    d2 = playerToAttack.posZ - posZ;
                    double d6 = playerToAttack.posY - (double)i;*/
                }
            } else if(state == 2) {
                return false;
                //this code is depreciated, pet mod needs to try to pathfind to this spot
                //d1 = (double)guardX - posX;
                //d2 = (double)guardZ - posZ;
                //double d7 = (double)guardY - (double)i;
            }
        }

        //} else {

        

        /*if (((EntityCreature)this).getTarget() != null && (isSolidPath(((EntityCreature)this).getTarget()) || this.getPath() == null)) {
        	return true;
        }*/
        //}
        return false;
    }



    public boolean shouldTarget(Entity var1) {
        if (mod_PathingActivated.sameTeam(this, var1)) {
            /*if (getDistanceToEntity(var1) < 5.0F) {
               return false;
            }*/
            return false;
        }

        if(mod_PathingActivated.useEnt(this) && !mobException()) {
            if(mod_PathingActivated.OmnipotentHostiles.get().booleanValue()) {
                return true;
            } else {
                float var2 = (float)mod_PathingActivated.AwarenessRange.get().intValue();
                this.plDist = this.getDistanceToEntity(var1);
                return this.plDist <= var2 && (this.canEntityBeSeen(var1) || mod_PathingActivated.XRayVision.get());
            }
        } else {
            return false;
        }
    }
    
    public boolean mobException() {
    	if (this instanceof EntityWolf || this instanceof EntityEnderman) {
    		return true;
    	}
    	return false;
    }

    public boolean isSolidPath(Entity var1) {
        if (this.team != 1 && !mod_PathingActivated.useEnt(this)) {
            return true;
        }

        if (this.getDistanceToEntity(var1) > mod_PathingActivated.MaxPFRange.get()) {
            return true;
        }

        return this.canEntityBeSeen(var1) && (this.getDistanceToEntity(var1) < 5.0F) && Math.abs(this.posY - (double)this.yOffset - (var1.posY - (double)var1.yOffset)) <= 3.5D;
    }

    public boolean shouldPath() {
        return mod_PathingActivated.useEnt(this);
    }

    public boolean tryPath(Entity var1, float var2) {
        return tryPath(var1, var2, false);
    }

    public boolean tryPath(Entity var1, float var2, boolean pet) {
        if (pathfindDelay > 0 && !((EntityCreature)this).pf.tryAgain) {
            return false;
        }

        if (mod_PathingActivated.hasPetMod) {
            if (mod_PathingActivated.sameTeam(this, var1) && !pet) {
                return false;
            }
        }

        if((this.shouldPath() || pet) && var1 != null) {
            float var3 = this.getDistanceToEntity(var1);

            if(var3 > var2) {
                return false;
            } else {
                //mod_MovePlus.displayMessage((new StringBuilder()).append("PFCount: ").append(mod_PathingActivated.PFCount++).toString());
            	
                
                pathfindDelay = (int)var2*2 + rand.nextInt(100);
                //setPathToEntity(worldObj.getPathToEntity(this, var1, var2));
                setPathToEntity(pf.getPathToEntity(this, var1, var2, canClimb()));

                //System.out.println(this);
                //setPathToEntity(this.worldObj.getPathToEntity(this, var1, var2));
                
                //this code is probably useless now that pf is threaded
                if(this.getPath() == null) {
                    pathfindDelay = (int)var2*2 + rand.nextInt(200);
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public boolean tryPathXYZ(int x, int y, int z, float var2) {
        if (pathfindDelay > 0) {
            return false;
        }

        float var3 = (float)this.getDistance((double)x, (double)y, (double)z);

        if(var3 > var2) {
            return false;
        } else {
            //mod_MovePlus.displayMessage((new StringBuilder()).append("PFCount: ").append(mod_PathingActivated.PFCount++).toString());
            //System.out.println("path XYZ");
            pathfindDelay = (int)var2*2 + rand.nextInt(100);
            setPathToEntity(this.worldObj.getEntityPathToXYZ(this, x, y, z, var2));

            if(this.getPath() == null) {
                pathfindDelay = (int)var2*2 + rand.nextInt(200);
            }

            return true;
        }

        //return false;
    }

    public PathEntity getPath() {
        return null;
    }

    public void setPathToEntity(PathEntity var1) {
    }

    public float getPathDist() {
        float var1 = this.plDist + 32.0F;

        if(var1 > (float)mod_PathingActivated.MaxPFRange.get()) {
            var1 = (float)mod_PathingActivated.MaxPFRange.get();
        }

        if((float)mod_PathingActivated.MaxPFRange.get().intValue() > 0.0F) {
            var1 = (float)mod_PathingActivated.MaxPFRange.get().intValue();
        }

        return var1;
    }

    public static boolean notMoving(EntityLiving var0, float var1) {
        double var2 = var0.prevPosX - var0.posX;
        double var4 = var0.prevPosZ - var0.posZ;
        float var6 = (float)Math.sqrt(var2 * var2 + var4 * var4);
        return var6 < var1;
    }

    public float getXZDistanceToEntity(Entity var1) {
        float var2 = (float)(this.posX - var1.posX);
        float var3 = (float)(this.posZ - var1.posZ);
        return (float)Math.sqrt((double)(var2 * var2 + var3 * var3));
    }



    public void onLivingUpdate() {
        if (mod_PathingActivated.hasNMMode) {
            if (mod_NMMode.entFireImmune(this)) {
            	this.func_40045_B();
            }
        }

        if (mod_PathingActivated.hasPetMod) {
            if (this.team == 1) {
                this.func_40045_B();
            }
        }

        super.onLivingUpdate();

        if(swingArm) {
            if (swingTick > 3 && swingTick < 9) {
                swingTick+=2;
            } else {
                swingTick++;
            }

            if(swingTick >= 24) {
                swingTick = 0;
                swingArm = false;
            }
        } else {
            swingTick = 0;
        }

        if (swingTick <= 16) {
            swingProgress = (float)swingTick / 8F;
        } else {
            swingProgress = 0F;
        }
    }


    //Feature extra overrides

    public void moveEntity(double d, double d1, double d2) {
        //AI Added
        if(orders != 2) {
            if(enhanced == 1) {
                if(!mod_PathingActivated.freezePets || team != 1) {
                    super.moveEntity(motionX * (double)mod_PathingActivated.enhPetSpeedMultiplier, motionY, motionZ * (double)mod_PathingActivated.enhPetSpeedMultiplier);
                }
            } else if(!mod_PathingActivated.freezePets || team != 1) {
                super.moveEntity(motionX, motionY, motionZ);
            }
        } else {
            super.moveEntity(0.0D, motionY, 0.0D);
        }
    }

    protected boolean canDespawn() {
        //AI Modded
        if (team != 1 && orders == 0) {
            return true;
        }

        return false;
    }

    public boolean isInRangeToRenderVec3D(Vec3D vec3d) {
        if(mod_PathingActivated.unlimitedEntityRenderRange) {
            return true;
        } else {
            double d = posX - vec3d.xCoord;
            double d1 = posY - vec3d.yCoord;
            double d2 = posZ - vec3d.zCoord;
            double d3 = d * d + d1 * d1 + d2 * d2;
            return isInRangeToRenderDist(d3);
        }
    }

    public void applyEntityCollision(Entity entity) {
        if(entity.riddenByEntity == this || entity.ridingEntity == this) {
            return;
        }

        double d = entity.posX - posX;
        double d1 = entity.posY - posY;
        double d2 = entity.posZ - posZ;
        double d3 = MathHelper.abs_max(d, d2);
        d3 = MathHelper.abs_max(d, d1);

        if(d3 >= 0.0099999997764825821D) {
            d3 = MathHelper.sqrt_double(d3);
            d /= d3;
            d1 /= d3;
            d2 /= d3;
            double d4 = 1.0D / d3;

            if(d4 > 1.0D) {
                d4 = 1.0D;
            }

            d *= d4;
            d1 *= d4;
            d2 *= d4;
            d *= 0.05000000074505806D;
            d1 *= 0.05000000074505806D;
            d2 *= 0.05000000074505806D;
            d *= 1.0F - entityCollisionReduction;
            d2 *= 1.0F - entityCollisionReduction;

            if (!mod_PathingActivated.VerticalCollision) {
                d1 = 0;
            }

            if(mod_PathingActivated.NothingPushesPlayer) {
                /*if(!(this instanceof EntityPlayer)) {
                    addVelocity(-d, -d1, -d2);
                }*/
                addVelocity(-d, -d1, -d2);

                if(!(entity instanceof EntityPlayer)) {
                    entity.addVelocity(d, d1, d2);
                }
            } else {
                addVelocity(-d, -d1, -d2);
                entity.addVelocity(d, d1, d2);
            }
        }
    }

    public boolean isEntityInsideOpaqueBlock() {
        if (this.ridingEntity != null) {
            return false;
        }

        for(int i = 0; i < 8; i++) {
            float f = ((float)((i >> 0) % 2) - 0.5F) * width * 0.9F;
            float f1 = ((float)((i >> 1) % 2) - 0.5F) * 0.1F;
            float f2 = ((float)((i >> 2) % 2) - 0.5F) * width * 0.9F;
            int j = MathHelper.floor_double(posX + (double)f);
            int k = MathHelper.floor_double(posY + (double)getEyeHeight() + (double)f1);
            int l = MathHelper.floor_double(posZ + (double)f2);

            if(worldObj.isBlockNormalCube(j, k, l)) {
                return true;
            }
        }

        return false;
    }

    public boolean teleportToTarget(Entity var1) {
        int var4 = MathHelper.floor_double(var1.posX) - 2;
        int var5 = MathHelper.floor_double(var1.posZ) - 2;
        int var6 = MathHelper.floor_double(var1.boundingBox.minY);

        for(int var7 = 0; var7 <= 4; ++var7) {
            for(int var8 = 0; var8 <= 4; ++var8) {
                if((var7 < 1 || var8 < 1 || var7 > 3 || var8 > 3) &&
                        this.worldObj.isBlockNormalCube(var4 + var7, var6 - 1, var5 + var8) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7, var6, var5 + var8) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7+1, var6, var5 + var8) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7, var6, var5 + var8+1) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7+1, var6, var5 + var8+1) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7, var6 + 1, var5 + var8) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7+1, var6 + 1, var5 + var8) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7, var6 + 1, var5 + var8+1) &&
                        !this.worldObj.isBlockNormalCube(var4 + var7+1, var6 + 1, var5 + var8+1)) {
                    this.setLocationAndAngles((double)((float)(var4 + var7) + 0.5F), (double)var6, (double)((float)(var5 + var8) + 0.5F), this.rotationYaw, this.rotationPitch);
                    return true;
                }
            }
        }

        return false;
    }
}