package ZombieAwareness;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;

/**
 * 
 * Forge sound event doesnt pass along position, so we are using mc events to get that
 * 
 * @author Corosus
 *
 */
public class WorldEventListener implements IWorldEventListener {

	public int dimID = -1;
	
	public WorldEventListener(int dimID) {
		this.dimID = dimID;
	}
	
	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos,
                                  BlockState oldState, BlockState newState, int flags) {

	}

	@Override
	public void notifyLightSet(BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2,
			int y2, int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable PlayerEntity player,
			SoundEvent soundIn, SoundCategory category, double x, double y,
			double z, float volume, float pitch) {

		//there is an edge case where world can be null during world load, just cancel the call
		World world = DimensionManager.getWorld(dimID);
		if (world == null) return;

		ZAUtil.hookSoundEvent(soundIn, world, x, y, z, volume, pitch);

	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange,
			double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void onEntityAdded(Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(Entity entityIn) {

	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {

	}

	@Override
	public void playEvent(PlayerEntity player, int type, BlockPos blockPosIn,
                          int data) {

		//there is an edge case where world can be null during world load, just cancel the call
		World world = DimensionManager.getWorld(dimID);
		if (world == null) return;

		ZAUtil.hookPlayEvent(world, type, blockPosIn, data);
		
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}

	@Override
	public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_,
			double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_,
			int... p_190570_16_) {
		
	}

}
