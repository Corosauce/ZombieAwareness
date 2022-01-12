package com.corosus.zombieawareness.client;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.EntityScent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;

import com.corosus.zombieawareness.config.ZAConfigClient;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;

import java.util.Random;

public class RenderScent extends EntityRenderer {
    
    public static ResourceLocation TEXTURE64 = new ResourceLocation(ZombieAwareness.MODID + ":textures/entities/bloodx64.png");
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(TEXTURE64);

    protected RenderScent(EntityRendererProvider.Context p_i46179_1_) {
        super(p_i46179_1_);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity p_110775_1_) {
        return TEXTURE64;
    }

    @Override
    public void render(Entity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        //bindEntityTexture(var1);
        float shadowSize = 1.0F;
        float shadowStrength = 1.0F;
        this.shadowStrength = 1;
        this.shadowRadius = 1;
        //GL11.glPushMatrix();
        if (ZAConfigClient.client_renderBlood && (((EntityScent)pEntity).type == 0)) {
            //this.doRenderNode(var1, var2, var4, var6, var8, var9);
            float scale = 0.7F + ((float)((EntityScent)pEntity).getAgeScale() * 0.3F);
            float alpha = (float)((EntityScent)pEntity).getAgeScale();
            renderBlood(pMatrixStack, pBuffer, pEntity, shadowStrength, pPartialTicks, pEntity.level, scale, alpha);
        }
        this.shadowStrength = 0;
        this.shadowRadius = 0;
        //shadowSize = 0.0F;
        //GL11.glPopMatrix();
    }

    private static void renderBlood(PoseStack p_229096_0_, MultiBufferSource p_229096_1_, Entity p_229096_2_, float p_229096_3_, float p_229096_4_, LevelReader p_229096_5_, float p_229096_6_, float alpha) {
        float f = p_229096_6_;
        if (p_229096_2_ instanceof Mob) {
            Mob mobentity = (Mob)p_229096_2_;
            if (mobentity.isBaby()) {
                f = p_229096_6_ * 0.5F;
            }
        }

        double d2 = Mth.lerp((double)p_229096_4_, p_229096_2_.xOld, p_229096_2_.getX());
        double d0 = Mth.lerp((double)p_229096_4_, p_229096_2_.yOld, p_229096_2_.getY());
        double d1 = Mth.lerp((double)p_229096_4_, p_229096_2_.zOld, p_229096_2_.getZ());
        int i = Mth.floor(d2 - (double)f);
        int j = Mth.floor(d2 + (double)f);
        int k = Mth.floor(d0 - (double)f);
        int l = Mth.floor(d0);
        int i1 = Mth.floor(d1 - (double)f);
        int j1 = Mth.floor(d1 + (double)f);
        PoseStack.Pose matrixstack$entry = p_229096_0_.last();
        VertexConsumer ivertexbuilder = p_229096_1_.getBuffer(SHADOW_RENDER_TYPE);

        //TODO: either integrate cleanly with entity renderer (no nested rendering), or make it particles

        Tesselator tessellator = Tesselator.getInstance();
        //BufferBuilder bufferbuilder = tessellator.getBuilder();
        //((BufferBuilder) ivertexbuilder).begin(GL11.GL_QUADS, DefaultVertexFormats.NEW_ENTITY);

        for(BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
            renderBlockShadow(matrixstack$entry, ivertexbuilder, p_229096_5_, blockpos, d2, d0, d1, f, p_229096_3_, alpha);
        }

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //SHADOW_RENDER_TYPE.end((BufferBuilder) ivertexbuilder, 0, 0, 0);
        //SHADOW_RENDER_TYPE.end((BufferBuilder) ivertexbuilder, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);

    }

    private static void renderBlockShadow(PoseStack.Pose p_229092_0_, VertexConsumer p_229092_1_, LevelReader p_229092_2_, BlockPos p_229092_3_, double p_229092_4_, double p_229092_6_, double p_229092_8_, float p_229092_10_, float p_229092_11_, float alpha) {
        BlockPos blockpos = p_229092_3_.below();
        BlockState blockstate = p_229092_2_.getBlockState(blockpos);

        /*float light = (float)p_229092_2_.getMaxLocalRawBrightness(p_229092_3_) / 15F;
        light = (float)p_229092_2_.getBrightness(p_229092_3_);*/
        //TODO: until i fix proper, just have it less blaringly bright
        float light = 0.5F;
        //light = (float)LevelRenderer.getLightColor(p_229092_2_, blockpos) / (float)15728880;
        //System.out.println(light);
        //light = 0.2F;
        //if (blockstate.getRenderShape() != BlockRenderType.INVISIBLE && p_229092_2_.getMaxLocalRawBrightness(p_229092_3_) > 3) {
            if (blockstate.isCollisionShapeFullBlock(p_229092_2_, blockpos)) {
                VoxelShape voxelshape = blockstate.getShape(p_229092_2_, p_229092_3_.below());
                if (!voxelshape.isEmpty()) {
                    float f = (float)(((double)p_229092_11_ - (p_229092_6_ - (double)p_229092_3_.getY()) / 2.0D) * 0.5D * (double)p_229092_2_.getBrightness(p_229092_3_));
                    f = alpha;
                    if (f >= 0.0F) {
                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        AABB axisalignedbb = voxelshape.bounds();
                        double d0 = (double)p_229092_3_.getX() + axisalignedbb.minX;
                        double d1 = (double)p_229092_3_.getX() + axisalignedbb.maxX;
                        double d2 = (double)p_229092_3_.getY() + axisalignedbb.minY;
                        double d3 = (double)p_229092_3_.getZ() + axisalignedbb.minZ;
                        double d4 = (double)p_229092_3_.getZ() + axisalignedbb.maxZ;
                        float f1 = (float)(d0 - p_229092_4_);
                        float f2 = (float)(d1 - p_229092_4_);
                        float f3 = (float)(d2 - p_229092_6_);
                        float f4 = (float)(d3 - p_229092_8_);
                        float f5 = (float)(d4 - p_229092_8_);
                        float f6 = -f1 / 2.0F / p_229092_10_ + 0.5F;
                        float f7 = -f2 / 2.0F / p_229092_10_ + 0.5F;
                        float f8 = -f4 / 2.0F / p_229092_10_ + 0.5F;
                        float f9 = -f5 / 2.0F / p_229092_10_ + 0.5F;
                        shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f4, f6, f8, light);
                        shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f5, f6, f9, light);
                        shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f5, f7, f9, light);
                        shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f4, f7, f8, light);
                    }

                }
            }
        //}
    }

    private static void shadowVertex(PoseStack.Pose p_229091_0_, VertexConsumer p_229091_1_, float p_229091_2_, float p_229091_3_, float p_229091_4_, float p_229091_5_, float p_229091_6_, float p_229091_7_, float light) {
        p_229091_1_.vertex(p_229091_0_.pose(), p_229091_3_, p_229091_4_, p_229091_5_).color(1.0F * light, 1.0F * light, 1.0F * light, p_229091_2_).uv(p_229091_6_, p_229091_7_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(p_229091_0_.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

}
