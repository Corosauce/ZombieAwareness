package com.corosus.coroutil.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OldUtil {

    static Field field_modifiers = null;

    public static Object getPrivateValue(Class var0, Object var1, String var2)
    {
        try
        {
            Field var3 = var0.getDeclaredField(var2);
            var3.setAccessible(true);
            return var3.get(var1);
        }
        catch (Exception var4)
        {
            return null;
        }
    }

    public static void setPrivateValue(Class var0, Object var1, String var2, Object var3) throws IllegalArgumentException, SecurityException, NoSuchFieldException
    {
        try
        {
            if (field_modifiers == null) {
                field_modifiers = Field.class.getDeclaredField("modifiers");
                field_modifiers.setAccessible(true);
            }

            Field var4 = var0.getDeclaredField(var2);
            int var5 = field_modifiers.getInt(var4);

            if ((var5 & 16) != 0)
            {
                field_modifiers.setInt(var4, var5 & -17);
            }

            var4.setAccessible(true);
            //added in 1.6.4
            field_modifiers.setInt(var4, var4.getModifiers() & ~Modifier.FINAL);
            var4.set(var1, var3);
        }
        catch (IllegalAccessException var6)
        {
            //logger.throwing("ModLoader", "setPrivateValue", var6);
            //throwException("An impossible error has occured!", var6);
        }
    }

}
