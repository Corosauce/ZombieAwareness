package ZombieAwareness;

import ZombieAwareness.config.ZAConfigClient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderScent extends Render {

    public static ResourceLocation TEXTURE64 = new ResourceLocation(ZombieAwareness.modID + ":textures/entities/bloodx64.png");
    //public static ResourceLocation TEXTURE32 = new ResourceLocation(ZombieAwareness.modID + ":textures/entities/bloodx32.png");

    protected RenderScent(RenderManager renderManager) {
		super(renderManager);
		//shadowSize = 0F;
	}

    public void doRenderNode(Entity var1, double var2, double var4, double var6, float var8, float var9) {
        //System.out.println("1");

    	boolean TEMP = false;

        //System.out.println("2");
        if (TEMP || ((EntityScent)var1).type == 0) {
        	//renderImage(var1, var2, var4, var6, var8, var9);
            //renderImage(var1, var2, var4, var6, var8, var9, "/misc/shadow.png");
        	float str = (float)((EntityScent)var1).getAgeScale();
        	renderBlood(var1, var2, var4, var6, str, var9);
        } else {

        }

        //}
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TEXTURE64;
	}

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9) {
    	//call texture set
    	bindEntityTexture(var1);
    	//if (!ZAConfig.client_renderBlood) return;

        //if (((EntityScent)var1).type == 0) {

        //System.out.println("!!!!!!!!!!!!!! - " + (double)(((double)((EntityScent)var1).strength)/100.0D));
        //}
    	shadowSize = 1.0F;
        GL11.glPushMatrix();
        if (ZAConfigClient.client_renderBlood) {
        	this.doRenderNode(var1, var2, var4, var6, var8, var9);
        }
        shadowSize = 0.0F;
        //
        GL11.glPopMatrix();
    }

    private World getWorldFromRenderManager() {
        return this.renderManager.world;
    }

    private void renderBlood(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks)
    {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(TEXTURE64);
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 0.7D + (shadowAlpha * 0.3D);

        if (entityIn instanceof EntityLiving)
        {
            EntityLiving entityliving = (EntityLiving)entityIn;
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
            IBlockState iblockstate = world.getBlockState(blockpos.down());

            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && world.getLightFromNeighbors(blockpos) > 3)
            {
            	//for (int ii = 0; ii < 100; ii++) {
            		//double xx = entityIn.world.rand.nextDouble() * 10D;
            		//double zz = entityIn.world.rand.nextDouble() * 10D;
            		this.renderBloodSingle(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
            	//}
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderBloodSingle(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_)
    {
        if (state.isFullCube())
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            double d0 = p_188299_9_;//((double)p_188299_9_ - (p_188299_4_ - ((double)p_188299_8_.getY() + p_188299_13_)) / 2.0D) * 1D/*0.5D*/ * (double)this.getWorldFromRenderManager().getLightBrightness(p_188299_8_);

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
    }

}
