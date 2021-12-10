package CoroUtil.util;

import net.minecraft.entity.Entity;

public class CoroUtilEntity {

    public static String getName(Entity ent) {
        return ent != null ? ent.getName().getString() : "nullObject";
    }
}
