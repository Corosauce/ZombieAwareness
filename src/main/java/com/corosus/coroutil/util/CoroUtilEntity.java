package com.corosus.coroutil.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class CoroUtilEntity {

    public static String getName(Entity ent) {
        return ent != null ? ent.getName().getString() : "nullObject";
    }

    public static boolean canSee(Entity p_70685_1_, BlockPos pos) {
        Vector3d vector3d = new Vector3d(p_70685_1_.getX(), p_70685_1_.getEyeY(), p_70685_1_.getZ());
        Vector3d vector3d1 = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        if (p_70685_1_.level != p_70685_1_.level || vector3d1.distanceToSqr(vector3d) > 128.0D * 128.0D) return false; //Forge Backport MC-209819
        return p_70685_1_.level.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, p_70685_1_)).getType() == RayTraceResult.Type.MISS;
    }
}
