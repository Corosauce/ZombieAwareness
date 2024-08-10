package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZombieAwareness;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MixinEntitySerialization {

    @Inject(method = "addAdditionalSaveData",
            at = @At(value = "HEAD"))
    public void hook(CompoundTag data, CallbackInfo ci) {
        Mob ent = ((Mob)(Object)this);
        data.put("za_data", ZombieAwareness.instance().getPersistentData(ent));
    }

    @Inject(method = "readAdditionalSaveData",
            at = @At(value = "HEAD"))
    public void hook2(CompoundTag data, CallbackInfo ci) {
        Mob ent = ((Mob)(Object)this);
        ZombieAwareness.instance().setPersistentData(ent, data.getCompound("za_data"));
    }
}
