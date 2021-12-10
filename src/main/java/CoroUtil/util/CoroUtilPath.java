package CoroUtil.util;

import java.util.Random;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.forge.CULog;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CoroUtilPath {

	public static Path getSingleNodePath(BlockCoord coords) {
		PathPoint points[] = new PathPoint[1];
        points[0] = new PathPoint(coords.posX, coords.posY, coords.posZ);
		Path pe = new Path(points);
		return pe;
	}
	
	public static boolean tryMoveToEntityLivingLongDist(EntityLiving entSource, Entity entityTo, double moveSpeedAmp) {
		return tryMoveToXYZLongDist(entSource, entityTo.posX, entityTo.getEntityBoundingBox().minY, entityTo.posZ, moveSpeedAmp);
	}
	
	/**
	 * If close enough, paths to coords, if too far based on attribute, tries to find best spot towards target to pathfind to
	 * 
	 * @param ent
	 * @param x
	 * @param y
	 * @param z
	 * @param moveSpeedAmp
	 * @return
	 */
	public static boolean tryMoveToXYZLongDist(EntityLiving ent, double x, double y, double z, double moveSpeedAmp) {
		
		World world = ent.world;
		
		boolean success = false;

		try {
			if (ent.getNavigator().noPath()) {

				double distToPlayer = ent.getDistance(x, y, z);//ent.getDistanceToEntity(player);

				double followDist = ent.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();

				if (distToPlayer <= followDist) {
					//boolean success = ent.getNavigator().tryMoveToEntityLiving(player, moveSpeedAmp);
					//success = ent.getNavigator().tryMoveToXYZ(x, y, z, moveSpeedAmp);
					success = CoroUtilCompatibility.tryPathToXYZModCompat(ent, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z), moveSpeedAmp);
					//System.out.println("success? " + success + "- move to player: " + ent + " -> " + player);
				} else {
					/*int x = MathHelper.floor(player.posX);
					int y = MathHelper.floor(player.posY);
					int z = MathHelper.floor(player.posZ);*/

					double d = x + 0.5F - ent.posX;
					double d2 = z + 0.5F - ent.posZ;
					double d1;
					d1 = y + 0.5F - (ent.posY + (double) ent.getEyeHeight());

					double d3 = MathHelper.sqrt(d * d + d2 * d2);
					float f2 = (float) ((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
					float f3 = (float) (-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
					float rotationPitch = -f3;//-ent.updateRotation(rotationPitch, f3, 180D);
					float rotationYaw = f2;//updateRotation(rotationYaw, f2, 180D);

					EntityLiving center = ent;

					Random rand = world.rand;

					float randLook = rand.nextInt(90) - 45;
					//int height = 10;
					double dist = (followDist * 0.75D) + rand.nextInt((int) followDist / 2);//rand.nextInt(26)+(queue.get(0).retryState * 6);
					int gatherX = (int) Math.floor(center.posX + ((double) (-Math.sin((rotationYaw + randLook) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));
					int gatherY = (int) center.posY;//Math.floor(center.posY-0.5 + (double)(-MathHelper.sin(center.rotationPitch / 180.0F * 3.1415927F) * dist) - 0D); //center.posY - 0D;
					int gatherZ = (int) Math.floor(center.posZ + ((double) (Math.cos((rotationYaw + randLook) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));

					BlockPos pos = new BlockPos(gatherX, gatherY, gatherZ);

					if (!world.isBlockLoaded(pos)) return false;

					IBlockState state = world.getBlockState(pos);
					//Block block = state.getBlock();
					int tries = 0;
					if (!CoroUtilBlock.isAir(state.getBlock())) {
						int offset = -5;

						while (tries < 30) {
							if (CoroUtilBlock.isAir(state.getBlock()) || !state.isSideSolid(world, pos, EnumFacing.UP)) {
								break;
							}
							gatherY += offset++;
							pos = new BlockPos(gatherX, gatherY, gatherZ);
							state = world.getBlockState(pos);
							tries++;
						}
					} else {
						//int offset = 0;
						while (tries < 30) {
							if (!CoroUtilBlock.isAir(state.getBlock()) && state.isSideSolid(world, pos, EnumFacing.UP)) {
								break;
							}
							gatherY -= 1;//offset++;
							pos = new BlockPos(gatherX, gatherY, gatherZ);
							state = world.getBlockState(pos);
							tries++;
						}
					}

					if (tries < 30) {
						//success = ent.getNavigator().tryMoveToXYZ(gatherX, gatherY, gatherZ, moveSpeedAmp);
						success = CoroUtilCompatibility.tryPathToXYZModCompat(ent, gatherX, gatherY, gatherZ, moveSpeedAmp);
						//System.out.println("pp success? " + success + "- move to player: " + ent + " -> " + player);
					} else {
						//fallback for extreme y differences, just path to topmost block, hopefully wont break much for inside structures etc

						pos = world.getHeight(pos).down();

						if (!world.isBlockLoaded(pos)) return false;

						state = world.getBlockState(pos);
						if (state.isSideSolid(world, pos, EnumFacing.UP)) {
							success = CoroUtilCompatibility.tryPathToXYZModCompat(ent, pos.getX(), pos.getY(), pos.getZ(), moveSpeedAmp);
						}
					}
				}
			}
		} catch (Exception ex) {
			//theres is 1 case where this happens: https://github.com/Corosauce/ZombieAwareness/issues/27
			//tracked it down to possibly a null AABB on a block
			CULog.err("Exception trying to pathfind");
			if (ConfigCoroUtil.useLoggingError) {
				ex.printStackTrace();
			}
		}
		
		return success;
	}
	
}
