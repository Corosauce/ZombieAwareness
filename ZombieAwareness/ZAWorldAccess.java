package ZombieAwareness;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IWorldAccess;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class ZAWorldAccess implements IWorldAccess {

	World worldObj;
	
	ZAWorldAccess(World world) {
		worldObj = world;
	}
	
	public void markBlockNeedsUpdate(int var1, int var2, int var3) {};

    /**
     * As of mc 1.2.3 this method has exactly the same signature and does exactly the same as markBlockNeedsUpdate
     */
	public void markBlockNeedsUpdate2(int var1, int var2, int var3) {};

    /**
     * Called across all registered IWorldAccess instances when a block range is invalidated. Args: minX, minY, minZ,
     * maxX, maxY, maxZ
     */
	public void markBlockRangeNeedsUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {};

    /**
     * Plays the specified sound. Arg: x, y, z, soundName, unknown1, unknown2
     */
	public void playSound(String var1, double var2, double var4, double var6, float var8, float var9) {
		//System.out.println("bwah!" + var1);
		try {
			ZAUtil.soundHook(var1, worldObj, (float)var2, (float)var4, (float)var6, var8, var9);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	};

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
	public void spawnParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {};

    /**
     * Start the skin for this entity downloading, if necessary, and increment its reference counter
     */
	public void obtainEntitySkin(Entity var1) {};

    /**
     * Decrement the reference counter for this entity's skin image data
     */
	public void releaseEntitySkin(Entity var1) {};

    /**
     * Plays the specified record. Arg: recordName, x, y, z
     */
	public void playRecord(String var1, int var2, int var3, int var4) {};

    /**
     * In all implementations, this method does nothing.
     */
	public void doNothingWithTileEntity(int var1, int var2, int var3, TileEntity var4) {};

    /**
     * Plays a pre-canned sound effect along with potentially auxiliary data-driven one-shot behaviour (particles, etc).
     */
	public void playAuxSFX(EntityPlayer var1, int var2, int var3, int var4, int var5, int var6) {}

	@Override
	public void destroyBlockPartially(int var1, int var2, int var3, int var4,
			int var5) {
		// TODO Auto-generated method stub
		
	};
}
