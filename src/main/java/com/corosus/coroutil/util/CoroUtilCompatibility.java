package com.corosus.coroutil.util;

import net.minecraft.entity.MobEntity;

public class CoroUtilCompatibility {

    /**
     * Used to contain compat with other mods, still used incase i add that back in
     * @param ent
     * @param x
     * @param y
     * @param z
     * @param speed
     * @return
     */
    public static boolean tryPathToXYZModCompat(MobEntity ent, int x, int y, int z, double speed) {
        return tryPathToXYZVanilla(ent, x, y, z, speed);
    }

    public static boolean tryPathToXYZVanilla(MobEntity ent, int x, int y, int z, double speed) {
        return ent.getNavigation().moveTo(x, y, z, speed);
    }

}
