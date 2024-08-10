package com.corosus.zombieawareness.loader.forge;

import com.corosus.zombieawareness.EntityScent;
import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRegistry {

    // Entity Register
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ZombieAwareness.MODID);

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
    }

    /*public static final RegistryObject<EntityType<EntityScent>> SCENT =
            ENTITY_TYPES.register("scent", () -> ZombieAwareness.SENSE);*/

    public static final RegistryObject<EntityType<EntityScent>> SCENT =
            ENTITY_TYPES.register("scent", () -> {
                ZombieAwareness.SENSE = EntityType.Builder.<EntityScent>
                                of(EntityScent::new, MobCategory.MISC)
                        .setShouldReceiveVelocityUpdates(false)
                        .setUpdateInterval(20)
                        .setTrackingRange(128)
                        .sized(0f, 0f)
                        .build(new ResourceLocation(ZombieAwareness.MODID, "scent").toString());
                return ZombieAwareness.SENSE;
            });

}
