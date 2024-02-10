package com.corosus.zombieawareness.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class MobListsConfig {

    public static List<String> enhancedMobsDefaults = new ArrayList<>();
    public static List<String> enhanceableMobsList = new ArrayList<>();
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
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> enhanceableMobs;

        private CategoryGeneral() {

            BUILDER.comment("General mod settings").push("general");

            enhancedMobs = BUILDER.comment("Mobs enhanced by zombie awareness").defineList("enhancedMobs", enhancedMobsDefaults,
                    it -> it instanceof String);

            enhanceableMobs = BUILDER.comment("This is a list to use as a reference for your modpack for mobs you might want to enhance, if these mobs also walk on the ground, they will probably work if you add them to enhancedMobs").defineList("enhanceableMobs", enhanceableMobsList,
                    it -> it instanceof String);

            BUILDER.pop();
        }
    }
    public static final ForgeConfigSpec CONFIG = BUILDER.build();
}
