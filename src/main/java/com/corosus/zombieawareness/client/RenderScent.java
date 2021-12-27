package com.corosus.zombieawareness.client;

import com.corosus.zombieawareness.ZombieAwareness;
import com.corosus.zombieawareness.EntityScent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;

import com.corosus.zombieawareness.config.ZAConfigClient;

public class RenderScent extends EntityRenderer {
    
    public static ResourceLocation TEXTURE64 = new ResourceLocation(ZombieAwareness.MODID + ":textures/entities/bloodx64.png");
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(TEXTURE64);

    protected RenderScent(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
    }

    /*protected RenderScent(EntityRendererManager renderManager) {
		super(renderManager);
	}

    public void doRenderNode(Entity var1, double var2, double var4, double var6, float var8, float var9) {

        if (((EntityScent)var1).type == 0) {
        	float str = (float)((EntityScent)var1).getAgeScale();
        	renderBlood(var1, var2, var4, var6, str, var9);
        }
    }*/

    @Override
    public ResourceLocation getTextureLocation(Entity p_110775_1_) {
        return TEXTURE64;
    }

    @Override
    public void render(Entity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        //bindEntityTexture(var1);
        float shadowSize = 1.0F;
        float shadowStrength = 1.0F;
        this.shadowStrength = 1;
        this.shadowRadius = 1;
        //GL11.glPushMatrix();
        if (ZAConfigClient.client_renderBlood && (((EntityScent)pEntity).type == 0)) {
            //this.doRenderNode(var1, var2, var4, var6, var8, var9);
            renderBlood(pMatrixStack, pBuffer, pEntity, shadowStrength, pPartialTicks, pEntity.level, shadowSize);
        }
        this.shadowStrength = 0;
        this.shadowRadius = 0;
        //shadowSize = 0.0F;
        //GL11.glPopMatrix();
    }

    private static void renderBlood(MatrixStack p_229096_0_, IRenderTypeBuffer p_229096_1_, Entity p_229096_2_, float p_229096_3_, float p_229096_4_, IWorldReader p_229096_5_, float p_229096_6_) {
        float f = p_229096_6_;
        if (p_229096_2_ instanceof MobEntity) {
            MobEntity mobentity = (MobEntity)p_229096_2_;
            if (mobentity.isBaby()) {
                f = p_229096_6_ * 0.5F;
            }
        }

        double d2 = MathHelper.lerp((double)p_229096_4_, p_229096_2_.xOld, p_229096_2_.getX());
        double d0 = MathHelper.lerp((double)p_229096_4_, p_229096_2_.yOld, p_229096_2_.getY());
        double d1 = MathHelper.lerp((double)p_229096_4_, p_229096_2_.zOld, p_229096_2_.getZ());
        int i = MathHelper.floor(d2 - (double)f);
        int j = MathHelper.floor(d2 + (double)f);
        int k = MathHelper.floor(d0 - (double)f);
        int l = MathHelper.floor(d0);
        int i1 = MathHelper.floor(d1 - (double)f);
        int j1 = MathHelper.floor(d1 + (double)f);
        MatrixStack.Entry matrixstack$entry = p_229096_0_.last();
        IVertexBuilder ivertexbuilder = p_229096_1_.getBuffer(SHADOW_RENDER_TYPE);

        //TODO: either integrate cleanly with entity renderer (no nested rendering), or make it particles

        Tessellator tessellator = Tessellator.getInstance();
        //BufferBuilder bufferbuilder = tessellator.getBuilder();
        //((BufferBuilder) ivertexbuilder).begin(GL11.GL_QUADS, DefaultVertexFormats.NEW_ENTITY);

        for(BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
            renderBlockShadow(matrixstack$entry, ivertexbuilder, p_229096_5_, blockpos, d2, d0, d1, f, p_229096_3_);
        }

        ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //SHADOW_RENDER_TYPE.end((BufferBuilder) ivertexbuilder, 0, 0, 0);
        //SHADOW_RENDER_TYPE.end((BufferBuilder) ivertexbuilder, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);

    }

    private static void renderBlockShadow(MatrixStack.Entry p_229092_0_, IVertexBuilder p_229092_1_, IWorldReader p_229092_2_, BlockPos p_229092_3_, double p_229092_4_, double p_229092_6_, double p_229092_8_, float p_229092_10_, float p_229092_11_) {
        BlockPos blockpos = p_229092_3_.below();
        BlockState blockstate = p_229092_2_.getBlockState(blockpos);
        //if (blockstate.getRenderShape() != BlockRenderType.INVISIBLE && p_229092_2_.getMaxLocalRawBrightness(p_229092_3_) > 3) {
            if (blockstate.isCollisionShapeFullBlock(p_229092_2_, blockpos)) {
                VoxelShape voxelshape = blockstate.getShape(p_229092_2_, p_229092_3_.below());
                if (!voxelshape.isEmpty()) {
                    float f = (float)(((double)p_229092_11_ - (p_229092_6_ - (double)p_229092_3_.getY()) / 2.0D) * 0.5D * (double)p_229092_2_.getBrightness(p_229092_3_));
                    if (f >= 0.0F) {
                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        AxisAlignedBB axisalignedbb = voxelshape.bounds();
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
                        shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f4, f6, f8);
                        shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f5, f6, f9);
                        shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f5, f7, f9);
                        shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f4, f7, f8);
                    }

                }
            }
        //}
    }

    private static void shadowVertex(MatrixStack.Entry p_229091_0_, IVertexBuilder p_229091_1_, float p_229091_2_, float p_229091_3_, float p_229091_4_, float p_229091_5_, float p_229091_6_, float p_229091_7_) {
        p_229091_1_.vertex(p_229091_0_.pose(), p_229091_3_, p_229091_4_, p_229091_5_).color(1.0F, 1.0F, 1.0F, p_229091_2_).uv(p_229091_6_, p_229091_7_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(p_229091_0_.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    /*private World getWorldFromRenderManager() {
        return this.renderManager.world;
    }*/
    
    /*private void renderBlood(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks)
    {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(TEXTURE64);
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 0.7D + (shadowAlpha * 0.3D);

        if (entityIn instanceof MobEntity)
        {
            MobEntity entityliving = (MobEntity)entityIn;
            f *= entityliving.getRenderSizeModifier();

            if (entityliving.isChild())
            {
                f *= 0.5F;
            }
        }

        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        int i = MathHelper.floor(d5 - (double)f);
        int j = MathHelper.floor(d5 + (double)f);
        int k = MathHelper.floor(d0 - (double)f);
        int l = MathHelper.floor(d0);
        int i1 = MathHelper.floor(d1 - (double)f);
        int j1 = MathHelper.floor(d1 + (double)f);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)))
        {
            BlockState iblockstate = world.getBlockState(blockpos.down());

            if (iblockstate.getRenderType() != BlockRenderType.INVISIBLE && world.getLightFromNeighbors(blockpos) > 3)
            {
                this.renderBloodSingle(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderBloodSingle(BlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_)
    {
        if (state.isFullCube())
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            double d0 = p_188299_9_;

            if (d0 >= 0.0D)
            {
                if (d0 > 1.0D)
                {
                    d0 = 1.0D;
                }

                AxisAlignedBB axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                double d1 = (double)p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                double d2 = (double)p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                double d3 = (double)p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_ + 0.015625D;
                double d4 = (double)p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                double d5 = (double)p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                float f = (float)((p_188299_2_ - d1) / 2.0D / (double)p_188299_10_ + 0.5D);
                float f1 = (float)((p_188299_2_ - d2) / 2.0D / (double)p_188299_10_ + 0.5D);
                float f2 = (float)((p_188299_6_ - d4) / 2.0D / (double)p_188299_10_ + 0.5D);
                float f3 = (float)((p_188299_6_ - d5) / 2.0D / (double)p_188299_10_ + 0.5D);
                vertexbuffer.pos(d1, d3, d4).tex((double)f, (double)f2).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
                vertexbuffer.pos(d1, d3, d5).tex((double)f, (double)f3).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
                vertexbuffer.pos(d2, d3, d5).tex((double)f1, (double)f3).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
                vertexbuffer.pos(d2, d3, d4).tex((double)f1, (double)f2).color(1.0F, 1.0F, 1.0F, (float)d0).endVertex();
            }
        }
    }*/

}
