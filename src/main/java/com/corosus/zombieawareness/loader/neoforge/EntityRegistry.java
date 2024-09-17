package com.corosus.zombieawareness.loader.neoforge;

import com.corosus.zombieawareness.EntityScent;
import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ZombieAwareness.MODID)
public class EntityRegistry {

    // Entity Register
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ZombieAwareness.MODID);

    public static void init(ModContainer container) {
        //IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(container.getEventBus());
    }

    /*public static final RegistryObject<EntityType<EntityScent>> SCENT =
            ENTITY_TYPES.register("scent", () -> ZombieAwareness.SENSE);*/

    public static final DeferredHolder<EntityType<?>, EntityType<EntityScent>> SCENT =
            ENTITY_TYPES.register("scent", () -> {
                ZombieAwareness.SENSE = EntityType.Builder.<EntityScent>
                                of(EntityScent::new, MobCategory.MISC)
                        .setShouldReceiveVelocityUpdates(false)
                        .setUpdateInterval(20)
                        .setTrackingRange(128)
                        .sized(0f, 0f)
                        .build(ResourceLocation.fromNamespaceAndPath(ZombieAwareness.MODID, "scent").toString());
                return ZombieAwareness.SENSE;
            });

}
