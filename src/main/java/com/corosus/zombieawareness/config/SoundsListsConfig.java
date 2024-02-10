package com.corosus.zombieawareness.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class SoundsListsConfig {

    public static List<String> allSoundsInGameList = new ArrayList<>();

    private static final Builder BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final class CategoryGeneral {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> allSoundsInGame;
        public final ForgeConfigSpec.ConfigValue<Boolean> outputPlayedSoundsToConsole;

        private CategoryGeneral() {

            BUILDER.comment("General mod settings").push("general");

            allSoundsInGame = BUILDER.comment("This is a list to use as a reference for your modpack for sounds you might want mobs to hear").defineList("allSoundsInGame", allSoundsInGameList,
                    it -> it instanceof String);

            outputPlayedSoundsToConsole = BUILDER.comment("Output nearby played sounds to console, helps figure out what is playing what so you can decide if you want to add it to the list of sounds zombies will hear").define("outputPlayedSoundsToConsole", false);

            BUILDER.pop();
        }
    }
    public static final ForgeConfigSpec CONFIG = BUILDER.build();
}
