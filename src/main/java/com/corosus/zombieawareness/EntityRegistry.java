package com.corosus.zombieawareness;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRegistry {

    @ObjectHolder(ZombieAwareness.MODID + ":scent")
    public static EntityType<EntityScent> SCENT;

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityType<?>> e) {
        IForgeRegistry<EntityType<?>> r = e.getRegistry();
        r.register(
                EntityType.Builder.of(EntityScent::new, MobCategory.MISC)
                        .setShouldReceiveVelocityUpdates(false)
                        .setUpdateInterval(20)
                        .setTrackingRange(128)
                        .sized(0f, 0f)
                        .build("scent")
                        .setRegistryName("scent"));
    }

}
