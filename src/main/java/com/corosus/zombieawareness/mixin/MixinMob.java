package com.corosus.zombieawareness.mixin;

import com.corosus.zombieawareness.ZAUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MixinMob {

    @Inject(method = "finalizeSpawn",
            at = @At(value = "HEAD"), cancellable = true)
    public void hook(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        LivingEntity ent = ((LivingEntity)(Object)this);
        if (ent.level().isClientSide) return;
        ZAUtil.processMobSpawn(ent);
    }
}
