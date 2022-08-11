package com.corosus.zombieawareness;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRegistry {

    // Entity Register
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ZombieAwareness.MODID);

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
    }

    public static final RegistryObject<EntityType<EntityScent>> SCENT =
            ENTITY_TYPES.register("scent", () -> EntityType.Builder.<EntityScent>
                    of(EntityScent::new, MobCategory.MISC)
                    .setShouldReceiveVelocityUpdates(false)
                    .setUpdateInterval(20)
                    .setTrackingRange(128)
                    .sized(0f, 0f)
                    .build(new ResourceLocation(ZombieAwareness.MODID, "scent").toString()));

}
