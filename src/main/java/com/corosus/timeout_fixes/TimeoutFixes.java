package com.corosus.timeout_fixes;

import com.corosus.timeout_fixes.config.TimeoutFixesConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TimeoutFixes.MODID)
public class TimeoutFixes
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "timeout_fixes";

    public TimeoutFixes() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TimeoutFixesConfig.CONFIG);
    }
}
