package com.corosus.zombieawareness.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import com.corosus.zombieawareness.ZombieAwareness;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class MobListsConfig {

    public static List<String> enhancedMobsDefaults = new ArrayList<>();
    //public static List<String> listEnhancedMobsParsedConfig = new ArrayList<>();

    static {
        String mc = "minecraft:";
        enhancedMobsDefaults.add(mc + "zombie");
        enhancedMobsDefaults.add(mc + "husk");
        enhancedMobsDefaults.add(mc + "creeper");
        enhancedMobsDefaults.add(mc + "skeleton");
        enhancedMobsDefaults.add(mc + "stray");
        enhancedMobsDefaults.add(mc + "witch");
        enhancedMobsDefaults.add(mc + "zombie_villager");
    }

    private static final Builder BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final class CategoryGeneral {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enhancedMobs;

        private CategoryGeneral() {

            BUILDER.comment("General mod settings").push("general");

            enhancedMobs = BUILDER.comment("Mobs enhanced by zombie awareness").defineList("enhancedMobs", enhancedMobsDefaults,
                    it -> it instanceof String);

            BUILDER.pop();
        }
    }
    public static final ForgeConfigSpec CONFIG = BUILDER.build();
}
